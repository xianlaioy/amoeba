/**
 * <pre>
 * 	This program is free software; you can redistribute it and/or modify it under the terms of 
 * the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, 
 * or (at your option) any later version. 
 * 
 * 	This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details. 
 * 	You should have received a copy of the GNU General Public License along with this program; 
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * </pre>
 */
package com.meidusa.amoeba.net;

import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

import com.meidusa.amoeba.net.io.PacketInputStream;
import com.meidusa.amoeba.net.io.PacketOutputStream;
import com.meidusa.amoeba.net.packet.Packet;
import com.meidusa.amoeba.net.packet.PacketFactory;
import com.meidusa.amoeba.util.Queue;

/**
 * @author <a href=mailto:piratebase@sina.com>Struct chen</a>
 */
public abstract class Connection implements NetEventHandler {

    private static Logger       logger        = Logger.getLogger(Connection.class);
    public static final long    PING_INTERVAL = 90 * 1000L;
    protected static final long LATENCY_GRACE = 30 * 1000L;

    protected ConnectionManager _cmgr;
    protected SelectionKey      _selkey;
    protected SocketChannel     _channel;
    protected long              _lastEvent;
    protected MessageHandler    _handler;
    protected final Lock        closeLock     = new ReentrantLock(false);
    protected final Lock        postCloseLock = new ReentrantLock(false);
    protected long _createTime = System.currentTimeMillis();
    protected boolean           closePosted   = false;
    private PacketInputStream   _fin;

    private PacketOutputStream  _fout;
    protected Queue<byte[]> _inQueue     = new Queue<byte[]>();
    protected Queue<ByteBuffer> _outQueue     = new Queue<ByteBuffer>();
    private boolean             socketClosed  = false;
    protected String            host;
    protected int               port;

    public PacketFactory<? extends Packet> getPacketFactory() {
        return packetFactory;
    }

    public Queue<byte[]> getInQueue(){
    	return _inQueue;
    }
    public void setPacketFactory(PacketFactory<? extends Packet> packetFactory) {
        this.packetFactory = packetFactory;
    }

    protected PacketFactory<? extends Packet> packetFactory;

    public Connection(SocketChannel channel, long createStamp){
        _channel = channel;
        try {
            host = channel.socket().getInetAddress().getHostAddress();
            port = channel.socket().getPort();
        } catch (Exception e) {
            logger.error("socket not connect", e);
        }
        _lastEvent = createStamp;
    }

    /**
     * when connection registed to ConnectionManager, {@link #init()} will invoked.
     * 
     * @see <code> {@link ConnectionManager#registerConnection(Connection, int)}</code>
     */
    protected void init() {

    }

    public void setConnectionManager(ConnectionManager cmgr) {
        this._cmgr = cmgr;
    }

    /**
     * ������ SocketChannel ��ص� SelectionKey
     */
    public void setSelectionKey(SelectionKey selkey) {
        this._selkey = selkey;
    }

    /**
     * ������SocketChannel ��ص� Selection Key
     */
    public SelectionKey getSelectionKey() {
        return _selkey;
    }

    /**
     * ����һ����������SocketChannel
     * 
     * @return
     */
    public SocketChannel getChannel() {
        return _channel;
    }

    public InetAddress getInetAddress() {
        return (_channel == null) ? null : _channel.socket().getInetAddress();
    }

    public void setMessageHandler(MessageHandler handler) {
        _handler = handler;
    }

    public MessageHandler getMessageHandler() {
        return _handler;
    }

    protected void inheritStreams(Connection other) {
        _fin = other._fin;
        _fout = other._fout;
    }

    /**
     * �ж� ���� �Ƿ�ر�
     * 
     * @return
     */
    public boolean isClosed() {
        closeLock.lock();
        try {
            return socketClosed;
        } finally {
            closeLock.unlock();
        }

    }

    /**
     * �رյ�ǰ���ӣ����Ҵ�ConnectionManager��ɾ�������ӡ�
     */
    protected void close(Exception exception) {
        closeLock.lock();
        try {
            // we shouldn't be closed twice
            if (isClosed()) {
                logger.warn("Attempted to re-close connection ["+ toString() + "]");
                Thread.dumpStack();
                return;
            }
            socketClosed = true;
        } finally {
            closeLock.unlock();
        }

        if (_handler instanceof Sessionable) {
            Sessionable session = (Sessionable) _handler;
            logger.error(this + ",closeSocket,and endSession,handler=" + session);
            session.endSession();
        }
        
        try{
	        if (_selkey != null) {
	            _selkey.attach(null);
	            Selector selector = _selkey.selector();
	            _selkey.cancel();
	            // wake up again to trigger thread death
	            selector.wakeup();
	
	            _selkey = null;
	        }
        }catch(Exception e){
        	logger.warn("Error cancel connection selectkey [conn=" + toString() + "] error=" + e + "].");
        }
        
        if(logger.isDebugEnabled()){
        	logger.debug("Closing channel " + this + ".");
        }
        try {
            _channel.close();
        } catch (IOException ioe) {
            logger.warn("Error closing connection ["+ toString() + "], error=" + ioe + "].");
        }

        if (exception != null) {
            _cmgr.connectionFailed(this, exception);
        } else {
            _cmgr.connectionClosed(this);
        }
    }

    /**
     * POST-->Queue->close->(_cmgr.connectionClosed( notify observer))-->closeSocket()
     * �����ṩ�������ã����ֻ�ǵݽ��رո����ӵ����󡣾���رս���Connection Manager����
     */
    public void postClose(Exception exception) {
        if (closePosted) {
            return;
        }
        postCloseLock.lock();
        try {
            if (closePosted) return;
            closePosted = true;
            this._cmgr.closeConnection(this, exception);
        } finally {
            postCloseLock.unlock();
        }
    }

    /**
     * �����Ӵ������ݻ������������쳣����Ҫ�ر����ӵ�����µ��ôη�����
     * 
     * @param ioe
     */
    public void handleFailure(Exception ioe) {
        // ����Ѿ��ر�
        if (isClosed()) {
            logger.warn("Failure reported on closed connection " + this + ".", ioe);
            return;
        }
        postClose(ioe);
    }

    public int handleEvent(long when) {
        int bytesInTotle = 0;
        try {
            if (_fin == null) {
                _fin = createPacketInputStream();
            }

            while (_channel != null && _channel.isOpen() && _fin.readPacket(_channel)) {
                int bytesIn = 0;
                // ��¼���һ�η���ʱ��
                _lastEvent = when;
                /**
                 * �õ�FramedInputStream �������ֽ�
                 */
                bytesIn = _fin.available();
                bytesInTotle += bytesIn;
                byte[] msg = new byte[bytesIn];
                _fin.read(msg);
                doReceiveMessage(msg);
            }
            if(_inQueue.size()>0){
            	messageProcess();
            }
        } catch (EOFException eofe) {
            // close down the socket gracefully
            handleFailure(eofe);
        } catch (IOException ioe) {
            // don't log a warning for the ever-popular "the client dropped the
            // connection" failure
            String msg = ioe.getMessage();

            if (msg == null || msg.indexOf("reset by peer") == -1) {
                logger.info("Error reading message from connection ["+ toString() + "], error=" + ioe + "].", ioe);
            }
            // deal with the failure
            handleFailure(ioe);
        } catch (Exception exception) {
        	logger.error("Error reading message from connection ["+ toString() + "], error=" + exception + "].", exception);
            handleFailure(exception);
        }

        return bytesInTotle;
    }

    
    protected void doReceiveMessage(byte[] message){
    	_inQueue.appendSilent(message);
    }
    
    protected void messageProcess() {
        _handler.handleMessage(this);
    }

    public boolean doWrite() throws IOException {
        synchronized (this.getSelectionKey()) {
            ByteBuffer buffer = null;
            int wrote = 0;
            int message = 0;
            while ((buffer = _outQueue.getNonBlocking()) != null) {
                wrote += this.getChannel().write(buffer);
                if (buffer.remaining() > 0) {
                    _outQueue.prepend(buffer);
                    return false;
                } else {
                    // buffer.clear();
                    message++;
                }
            }
            return true;

        }
    }

    public void postMessage(byte[] msg) {
        PacketOutputStream _framer = getPacketOutputStream();
        _framer.resetPacket();
        try {
            _framer.write(msg);
            ByteBuffer buffer = _framer.returnPacketBuffer();
            /*
             * ByteBuffer out= ByteBuffer.allocate(buffer.limit()); out.put(buffer); out.flip();
             */
            _outQueue.append(buffer);
            writeMessage();
        } catch (IOException e) {
            this._cmgr.connectionFailed(this, e);
        }
    }

    public void postMessage(ByteBuffer msg) {
        _outQueue.append(msg);
        writeMessage();
    }

    protected void writeMessage() {
        if (isClosed()) {
            return;
        }
        try {
            SelectionKey key = getSelectionKey();
            if (!key.isValid()) {
                handleFailure(new java.nio.channels.CancelledKeyException());
                return;
            }
            synchronized (key) {
                if (key != null && (key.interestOps() & SelectionKey.OP_WRITE) == 0) {
                    /**
                     * �������ݣ��������false�����ʾsocket send buffer �Ѿ����ˡ���Selector ��Ҫ���� Writeable event
                     */
                    boolean finished = doWrite();
                    if (!finished) {
                        key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
                    }
                }
            }
        } catch (IOException ioe) {
            handleFailure(ioe);
        }catch(CancelledKeyException ce){
        	handleFailure(ce);
        }
    }
    
    public boolean checkIdle(long now) {
        long idleMillis = now - _lastEvent;
        if (idleMillis < PING_INTERVAL + LATENCY_GRACE) {
            return false;
        }
        if (isClosed()) {
            return true;
        }
        return true;
    }

    protected abstract PacketInputStream createPacketInputStream();

    protected abstract PacketOutputStream createPakcetOutputStream();

    protected PacketOutputStream getPacketOutputStream() {
        if (_fout == null) {
            _fout = createPakcetOutputStream();
        }
        return this._fout;

    }

    protected PacketInputStream getPacketInputStream() {
        if (_fin == null) {
            _fin = createPacketInputStream();
        }
        return this._fin;

    }

    public String getSocketId(){
    	return this.host+":"+this.port;
    }
    public String toString(){
    	StringBuffer buffer = new StringBuffer();
    	buffer.append(this.getClass().getCanonicalName());
    	buffer.append("@").append(this.host).append(":").append(this.port);
    	buffer.append(",hashcode=").append(this.hashCode());
    	
    	return buffer.toString();
    	
    }
}
