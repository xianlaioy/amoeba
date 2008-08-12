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
package com.meidusa.amoeba.mysql.handler;


import org.apache.commons.pool.ObjectPool;
import org.apache.log4j.Logger;

import com.meidusa.amoeba.mysql.net.MysqlClientConnection;
import com.meidusa.amoeba.mysql.packet.MysqlPacketBuffer;
import com.meidusa.amoeba.mysql.packet.QueryCommandPacket;
import com.meidusa.amoeba.net.Connection;

/**
 * Command Query ��������Ϣ����
 * @author <a href=mailto:piratebase@sina.com>Struct chen</a>
 *
 */
public class QueryCommandMessageHandler extends CommandMessageHandler{
	private static Logger logger = Logger.getLogger(QueryCommandMessageHandler.class); 
	
	static class QueryConnectionStatus extends CommandMessageHandler.ConnectionStatuts{
		public QueryConnectionStatus(Connection conn) {
			super(conn);
		}

		/**
		 * 
		 * Command Query:���ֽ���mysql Query������
		 * 1��select ��䣬�ڶ���EofPacket����
		 * 2������server�˷��صĴ����ʱ��
		 * 3����OK������
		 */
		@Override
		public boolean isCompleted(byte[] buffer) {
			if(this.commandType == QueryCommandPacket.COM_QUERY){
				boolean isCompleted = false; 
				if(MysqlPacketBuffer.isErrorPacket(buffer)){
					statusCode |= SessionStatus.ERROR;
					statusCode |= SessionStatus.COMPLETED;
					isCompleted = true;
				}else if(packetIndex == 0 &&  MysqlPacketBuffer.isOkPacket(buffer)){
					statusCode |= SessionStatus.OK;
					statusCode |= SessionStatus.COMPLETED;
					isCompleted = true;
				}else if(MysqlPacketBuffer.isEofPacket(buffer)){
					if((statusCode & SessionStatus.EOF_FIELDS) >0){
							statusCode |= SessionStatus.EOF_ROWS;
							statusCode |= SessionStatus.COMPLETED;
							isCompleted = true;
					}else{
						statusCode |= SessionStatus.EOF_FIELDS;
						isCompleted = false;
					}
				}else{
					if(statusCode == SessionStatus.QUERY){
						statusCode |= SessionStatus.RESULT_HEAD;
					}
				}
				return isCompleted;
			}else{
				return super.isCompleted(buffer);
			}
		}
	}
	public QueryCommandMessageHandler(MysqlClientConnection source,byte[] query,ObjectPool[] pools,long timeout){
		super(source,query,pools,timeout);
	}
	
	
	public void handleMessage(Connection conn, byte[] message) {
		if(logger.isDebugEnabled()){
			if(conn == source){
				QueryCommandPacket command = new QueryCommandPacket();
				command.init(message);
				if(command.arg != null){
					logger.debug(command.arg);
				}
			}
		}
		super.handleMessage(conn, message);
	}
	
	@Override
	protected ConnectionStatuts newConnectionStatuts(Connection conn) {
		return new QueryConnectionStatus(conn);
	}

	
}
