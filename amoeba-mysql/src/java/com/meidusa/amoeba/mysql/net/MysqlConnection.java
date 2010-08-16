/*
 * 	This program is free software; you can redistribute it and/or modify it under the terms of 
 * the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, 
 * or (at your option) any later version. 
 * 
 * 	This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details. 
 * 	You should have received a copy of the GNU General Public License along with this program; 
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.meidusa.amoeba.mysql.net;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.zip.Deflater;

import com.meidusa.amoeba.net.io.PacketInputStream;
import com.meidusa.amoeba.net.io.PacketOutputStream;
import com.meidusa.amoeba.mysql.io.MysqlFramedInputStream;
import com.meidusa.amoeba.mysql.io.MysqlFramingOutputStream;
import com.meidusa.amoeba.net.DatabaseConnection;

/**
 * 
 * @author <a href=mailto:piratebase@sina.com>Struct chen</a>
 *
 */
public abstract class MysqlConnection extends DatabaseConnection {
	
	protected boolean useCompression;
	private Deflater deflater = new Deflater();
	public boolean isUseCompression() {
		return useCompression;
	}

	public void setUseCompression(boolean useCompression) {
		this.useCompression = useCompression;
	}

	public MysqlConnection(SocketChannel channel, long createStamp) {
		super(channel, createStamp);
	}
	
	@Override
	protected PacketInputStream createPacketInputStream() {
		return new MysqlFramedInputStream(true);
	}
	
	@Override
	protected PacketOutputStream createPakcetOutputStream() {
		return new MysqlFramingOutputStream(true);
	}
	
	/**
	 * Ϊ���������ܣ�����mysql���ݰ�д��Ŀ�ĵص�ʱ���Ѿ������˰�ͷ������Ҫ����PacketOutputStream����
	 */
	public void postMessage(byte[] msg)
    {
        /*ByteBuffer out= ByteBuffer.allocate(msg.length);
        out.put(msg);
        out.flip();*/
		if(useCompression && authenticated){
    		byte index = msg[4];
			byte[] newBuffer = new byte[7 + msg.length];
			newBuffer[0] = (byte) (msg.length & 0xff);
			newBuffer[1] = (byte) (msg.length >>> 8);
			newBuffer[2] = (byte) (msg.length >>> 16);
			newBuffer[3] = index;
			newBuffer[4] = (byte) 0;
			newBuffer[5] = (byte) 0;
			newBuffer[6] = (byte) 0;
			System.arraycopy(msg, 0, newBuffer, 7,msg.length);
			_outQueue.append(ByteBuffer.wrap(newBuffer));
    	}else{
    		_outQueue.append(ByteBuffer.wrap(msg));
    	}
		
        writeMessage();
    }
	
    public void postMessage(ByteBuffer msg) {
    	
    	if(useCompression && authenticated){
    		byte index = msg.get(4);
			byte[] compresseData = msg.array();
			byte[] newBuffer = new byte[7 + compresseData.length];
			newBuffer[0] = (byte) (compresseData.length & 0xff);
			newBuffer[1] = (byte) (compresseData.length >>> 8);
			newBuffer[2] = (byte) (compresseData.length >>> 16);
			newBuffer[3] = index;
			newBuffer[4] = (byte) 0;
			newBuffer[5] = (byte) 0;
			newBuffer[6] = (byte) 0;
			System.arraycopy(compresseData, 0, newBuffer, 7,compresseData.length);
			msg = ByteBuffer.wrap(newBuffer);
    	}
    	
        _outQueue.append(msg);
        writeMessage();
    }
}
