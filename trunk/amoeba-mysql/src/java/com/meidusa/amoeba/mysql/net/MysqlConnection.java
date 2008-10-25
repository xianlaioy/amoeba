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

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

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
        _outQueue.append(ByteBuffer.wrap(msg));
        _cmgr.invokeConnectionWriteMessage(this);
    }
	
	public String toString(){
		try{
			return this.getClass().getName()+"@"+(this.getChannel()!= null?this.getChannel().socket().toString():"not connected")+", hashcode="+this.hashCode();
		}catch(Exception e){
			return super.toString();
		}
	}

}
