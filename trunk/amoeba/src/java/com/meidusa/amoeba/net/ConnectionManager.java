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

import java.io.IOException;
import java.net.SocketException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.meidusa.amoeba.context.ProxyRuntimeContext;
import com.meidusa.amoeba.data.ConMgrStats;
import com.meidusa.amoeba.util.Initialisable;
import com.meidusa.amoeba.util.InitialisationException;
import com.meidusa.amoeba.util.LoopingThread;
import com.meidusa.amoeba.util.Queue;
import com.meidusa.amoeba.util.Reporter;
import com.meidusa.amoeba.util.Tuple;

/**
 * @author <a href=mailto:piratebase@sina.com>Struct chen</a>
 */
public class ConnectionManager extends LoopingThread implements Reporter, Initialisable {

    protected static Logger                          logger                          = Logger.getLogger(ConnectionManager.class);
    protected static final int                       SELECT_LOOP_TIME                = 100;

    // codes for notifyObservers()
    protected static final int                       CONNECTION_ESTABLISHED          = 0;

    protected static final int                       CONNECTION_FAILED               = 1;
    protected static final int                       CONNECTION_CLOSED               = 2;
    protected static final int                       CONNECTION_AUTHENTICATE_SUCCESS = 3;
    protected static final int                       CONNECTION_AUTHENTICATE_FAILD   = 4;

    protected Selector                               _selector;

    private List<NetEventHandler>                    _handlers                       = new ArrayList<NetEventHandler>();
    protected ArrayList<ConnectionObserver>          _observers                      = new ArrayList<ConnectionObserver>();

    /** Our current runtime stats. */
    protected ConMgrStats                            _stats;

    /** �����Ѿ�ʧЧ��������Ͽ��Ķ��� */
    protected Queue<Tuple<Connection, Exception>>    _deathq                         = new Queue<Tuple<Connection, Exception>>();

    protected Queue<Tuple<NetEventHandler, Integer>> _registerQueue                  = new Queue<Tuple<NetEventHandler, Integer>>();

    /** Counts consecutive runtime errors in select() */
    protected int                                    _runtimeExceptionCount;

    /** Connection idle check per 5 second */
    private long                                     idleCheckTime                   = 5000;

    private long                                     lastIdleCheckTime               = 0;

    public void setIdleCheckTime(long idleCheckTime) {
        this.idleCheckTime = idleCheckTime;
    }

    public long getIdleCheckTime() {
		return idleCheckTime;
	}

	public void appendReport(StringBuilder report, long now, long sinceLast, boolean reset, Level level) {
        report.append("* ").append(this.getName()).append("\n");
        report.append("- Registed Connection size: ").append(_selector.keys().size()).append("\n");
        report.append("- created Connection size: ").append(_stats.connects.get()).append("\n");
        report.append("- disconnect Connection size: ").append(_stats.disconnects.get()).append("\n");
        if (reset) {
            _stats = new ConMgrStats();
        }
    }

    public ConnectionManager() throws IOException{
        _selector = SelectorProvider.provider().openSelector();
        // create our stats record
        _stats = new ConMgrStats();
    }

    public ConnectionManager(String managerName) throws IOException{
        super(managerName);
        _selector = SelectorProvider.provider().openSelector();

        // create our stats record
        _stats = new ConMgrStats();
        this.setDaemon(true);
    }

    /**
     * Performs the select loop. This is the body of the conmgr thread.
     */
    protected void iterate() {
        final long iterStamp = System.currentTimeMillis();

        // �ر��Ѿ��Ͽ���������������Connection
        Tuple<Connection, Exception> deathTuple;
        while ((deathTuple = _deathq.getNonBlocking()) != null) {
        	try{
        		deathTuple.left.close(deathTuple.right);
        	}catch(Exception e){
        		logger.error("when close connection="+deathTuple.left,e);
        	}
        }

        if (idleCheckTime > 0 && iterStamp - lastIdleCheckTime >= idleCheckTime) {
            lastIdleCheckTime = iterStamp;
            // �رտ���ʱ�����������
            for (NetEventHandler handler : _handlers) {
                if (handler.checkIdle(iterStamp)) {
                	
                    // this will queue the connection for closure on our next tick
                    if (handler instanceof Connection) {
                    	Connection conn = (Connection) handler;
                    	long idlesecond = (iterStamp - conn._lastEvent)/1000;
                    	logger.warn("Disconnecting non-communicative server [manager=" + this + " conn="+conn.toString()+", socket closed!" +", idle=" + idlesecond + " s]. life="+((System.currentTimeMillis()-conn._createTime)/1000) +" s");
                    	
                        closeConnection((Connection) handler, null);
                    }
                }
            }
        }

        // ��ע������Ӽ���handler map��
        Tuple<NetEventHandler, Integer> registerHandler = null;
        while ((registerHandler = _registerQueue.getNonBlocking()) != null) {
            if (registerHandler.left instanceof Connection) {
                Connection connection = (Connection) registerHandler.left;
                this.registerConnection(connection, registerHandler.right.intValue());
                _handlers.add(connection);
            } else {
                _handlers.add(registerHandler.left);
            }
        }

        // ��������¼�
        Set<SelectionKey> ready = null;
        try {
            // check for incoming network events
            int ecount = _selector.select(SELECT_LOOP_TIME);
            // selectorLock.lock();
            // try{
            ready = _selector.selectedKeys();
            // }finally{
            // selectorLock.unlock();
            // }
            if (ecount == 0) {
                if (ready.size() == 0) {
                    return;
                } else {
                    logger.warn("select() returned no selected sockets, but there are " + ready.size() + " in the ready set.");
                }
            }

        } catch (IOException ioe) {
            logger.warn("Failure select()ing.", ioe);
            return;
        } catch (RuntimeException re) {
            // instead of looping indefinitely after things go pear-shaped, shut
            // us down in an
            // orderly fashion
            logger.warn("Failure select()ing.", re);
            if (_runtimeExceptionCount++ >= 20) {
                logger.warn("Too many errors, bailing.");
                shutdown();
            }
            return;
        }
        // clear the runtime error count
        _runtimeExceptionCount = 0;

        // �����¼������������������ȣ�
        for (SelectionKey selkey : ready) {
            NetEventHandler handler = null;
            handler = (NetEventHandler) selkey.attachment();
            if (handler == null) {
                logger.warn("Received network event but have no registered handler " + "[selkey=" + selkey + "].");
                selkey.cancel();
                continue;
            }

            if (selkey.isWritable()) {
                try {
                    boolean finished = handler.doWrite();
                    if (finished) {
                        selkey.interestOps(selkey.interestOps() & ~SelectionKey.OP_WRITE);
                    }
                } catch (Exception e) {
                    logger.warn("Error processing network data: " + handler + ".", e);
                    if (handler != null && handler instanceof Connection) {
                        closeConnection((Connection) handler, e);
                    }
                }
            }
            
            if (selkey.isReadable() || selkey.isAcceptable()) {
            	handler.handleEvent(iterStamp);
            }
        }

        ready.clear();
    }

    /**
     * �����첽��ʽ�ر�һ�����ӡ� �������رյ����ӷ���deathQueue��
     */
    void closeConnection(Connection conn, Exception exception) {
    	_deathq.append(new Tuple<Connection, Exception>(conn, exception));
    }

    public void closeAll() {
        synchronized (_selector) {
            Set<SelectionKey> keys = _selector.keys();
            for (SelectionKey key : keys) {
                Object object = key.attachment();
                if (object instanceof Connection) {
                    Connection conn = (Connection) object;
                    closeConnection(conn, null);
                }
            }
        }
    }

    /**
     * ���� ConnectionObserver������Connection ��ص������¼�
     */
    public void addConnectionObserver(ConnectionObserver observer) {
        synchronized (_observers) {
            _observers.add(observer);
        }
    }

    /**
     * �� Observer �б���ɾ��һ��Observer����
     */
    public void removeConnectionObserver(ConnectionObserver observer) {
        synchronized (_observers) {
            _observers.remove(observer);
        }
    }

    protected void notifyObservers(int code, Connection conn, Object arg1) {
        synchronized (_observers) {
            for (ConnectionObserver obs : _observers) {
                switch (code) {
                    case CONNECTION_ESTABLISHED:
                        obs.connectionEstablished(conn);
                        break;
                    case CONNECTION_FAILED:
                        obs.connectionFailed(conn, (Exception) arg1);
                        break;
                    case CONNECTION_CLOSED:
                        obs.connectionClosed(conn);
                        break;
                    default:
                        throw new RuntimeException("Invalid code supplied to notifyObservers: " + code);
                }
            }
        }
    }

    /**
     * �첽ע��һ��NetEventHandler
     */
    public void postRegisterNetEventHandler(NetEventHandler handler, int key) {
        _registerQueue.append(new Tuple<NetEventHandler, Integer>(handler, key));
        /**
         * ����ConnectionManager���ڵȴ�select���̣߳������ܹ������ٵĴ���registerQueue�����еĶ���
         */
        _selector.wakeup();
    }


    public Selector getSelector(){
    	return this._selector;
    }
    /**
     * ��ConnectionManager ����һ��SocketChannel
     */
    protected void registerConnection(Connection connection, int key) {
        SocketChannel channel = connection.getChannel();
        if (logger.isDebugEnabled()) {
            logger.debug("[" + this.getName() + "] registed Connection[" + connection.toString() + "] connected!");
        }
        SelectionKey selkey = null;
        try {
            if (!(channel instanceof SelectableChannel)) {
                try {
                    logger.warn("Provided with un-selectable socket as result of accept(), can't " + "cope [channel=" + channel + "].");
                } catch (Error err) {
                    logger.warn("Un-selectable channel also couldn't be printed.");
                }
                // stick a fork in the socket
                if (channel != null) {
                    channel.socket().close();
                }
                return;
            }

            SelectableChannel selchan = (SelectableChannel) channel;
            selchan.configureBlocking(false);
            selkey = selchan.register(_selector, key, connection);
            connection.setConnectionManager(this);
            connection.setSelectionKey(selkey);
            configConnection(connection);
            _stats.connects.incrementAndGet();
            connection.init();
            _selector.wakeup();
            return;
        } catch (IOException ioe) {
            logger.error("register connection error: " + ioe);
        }

        if (selkey != null) {
            selkey.attach(null);
            selkey.cancel();
        }

        // make sure we don't leak a socket if something went awry
        if (channel != null) {
            try {
                channel.socket().close();
            } catch (IOException ioe) {
                logger.warn("Failed closing aborted connection: " + ioe);
            }
        }
    }

    protected void configConnection(Connection connection) throws SocketException {
        connection.getChannel().socket().setSendBufferSize(ProxyRuntimeContext.getInstance().getConfig().getNetBufferSize() * 1024);
        connection.getChannel().socket().setReceiveBufferSize(ProxyRuntimeContext.getInstance().getConfig().getNetBufferSize() * 1024);
        connection.getChannel().socket().setTcpNoDelay(ProxyRuntimeContext.getInstance().getConfig().isTcpNoDelay());
    }

    /**
     * �� Connection �ر��Ժ�
     */
    protected void connectionClosed(Connection conn) {
        /**
         * ɾ���������رյ���ض���
         */
        _handlers.remove(conn);
        _stats.disconnects.incrementAndGet();
        /**
         * ֪ͨ����Observer�б������Ѿ��ر�
         */
        notifyObservers(CONNECTION_CLOSED, conn, null);
    }

    /**
     * �� Connection �����쳣�Ժ�
     */
    protected void connectionFailed(Connection conn, Exception ioe) {
        _handlers.remove(conn);
        _stats.disconnects.incrementAndGet();

        /**
         * �����������쳣ʱ��֪ͨ����Observers
         */
        notifyObservers(CONNECTION_FAILED, conn, ioe);
    }

    public void init() throws InitialisationException {
    }
    
    protected void handleIterateFailure(Exception e) {
        logger.error("iterate error:", e);
    }

}
