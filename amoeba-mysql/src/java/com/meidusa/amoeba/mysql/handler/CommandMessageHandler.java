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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.meidusa.amoeba.mysql.net.CommandInfo;
import com.meidusa.amoeba.mysql.net.CommandListener;
import com.meidusa.amoeba.mysql.net.MysqlClientConnection;
import com.meidusa.amoeba.mysql.net.MysqlConnection;
import com.meidusa.amoeba.mysql.net.MysqlServerConnection;
import com.meidusa.amoeba.mysql.net.packet.EOFPacket;
import com.meidusa.amoeba.mysql.net.packet.ErrorPacket;
import com.meidusa.amoeba.mysql.net.packet.MysqlPacketBuffer;
import com.meidusa.amoeba.mysql.net.packet.OkPacket;
import com.meidusa.amoeba.mysql.net.packet.QueryCommandPacket;
import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.MessageHandler;
import com.meidusa.amoeba.net.Sessionable;
import com.meidusa.amoeba.net.packet.Packet;
import com.meidusa.amoeba.net.poolable.ObjectPool;
import com.meidusa.amoeba.net.poolable.PoolableObject;
import com.meidusa.amoeba.util.Reporter;
import com.meidusa.amoeba.util.StringUtil;

/**
 * 
 * @author <a href=mailto:piratebase@sina.com>Struct chen</a>
 *
 */
public abstract class CommandMessageHandler implements MessageHandler,Sessionable,Reporter.SubReporter {
	private static Logger logger = Logger.getLogger(CommandMessageHandler.class); 
	
	/**
	 * ��ʾ���������ص����ݰ�����ʾ��ǰ�Ự״̬
	 * @author <a href=mailto:piratebase@sina.com>Struct chen</a>
	 *
	 */
	static class SessionStatus{
		public static final int QUERY = 1;
		public static final int RESULT_HEAD  = 2;
		public static final int EOF_FIELDS  = 4;
		public static final int EOF_ROWS  = 8;
		public static final int OK  = 16;
		public static final int ERROR  = 32;
		public static final int COMPLETED  = 64;
	}
	
	static enum CommandStatus{
		ConnectionNotComplete,ConnectionCompleted,AllCompleted
	}
	
	/**
	 * ������������ӵ�״̬��������ǰ�����״̬,��ǰ���ӵ����ݰ�
	 * @author <a href=mailto:piratebase@sina.com>Struct chen</a>
	 *
	 */
	static abstract class ConnectionStatuts{
		protected Connection conn;
		public ConnectionStatuts(Connection conn){
			this.conn = conn;
		}
		int statusCode;
		int packetIndex;
		List<byte[]> buffers;
		protected  byte commandType;
		
		public void clearBuffer(){
			if(buffers != null){
				buffers.clear();
			}
		}
		
		public void setCommandType(byte commandType){
			this.commandType = commandType;
			statusCode = 0;
			packetIndex = 0; 
		}
		/**
		 * �жϴӷ������˷��ص����ݰ��Ƿ��ʾ��ǰ����Ľ�����
		 * @param buffer
		 * @return
		 */
		public boolean isCompleted(byte[] buffer) {
			if(this.commandType == QueryCommandPacket.COM_INIT_DB){
				boolean isCompleted = false; 
				if(MysqlPacketBuffer.isErrorPacket(buffer)){
					statusCode |= SessionStatus.ERROR;
					statusCode |= SessionStatus.COMPLETED;
					isCompleted = true;
				}else if(MysqlPacketBuffer.isOkPacket(buffer)){
					statusCode |= SessionStatus.OK;
					statusCode |= SessionStatus.COMPLETED;
					isCompleted = true;
				}
				return isCompleted;
			}else{
				return false;
			}
		}
	}
	
	protected static class CommandQueue{
		protected List<CommandInfo> sessionInitQueryQueue; //���еĴӿͻ��˷��͹����� command ����
		protected CommandInfo currentCommand;//��ǰ��query
		private final Lock lock = new ReentrantLock(false);
		protected Map<MysqlServerConnection,ConnectionStatuts> connStatusMap = new HashMap<MysqlServerConnection,ConnectionStatuts>();
		private boolean mainCommandExecuted;
		private MysqlClientConnection source;
		public CommandQueue(MysqlClientConnection source){
			this.source = source;
		}
		public boolean isMultiple(){
			return connStatusMap.size()>1;
		}
		
		public void clearAllBuffer(){
			Collection<ConnectionStatuts> collection = connStatusMap.values();
			for(ConnectionStatuts status : collection){
				status.clearBuffer();
			}
		}
		
		/**
		 * ������һ������������false����ʾ������û�������ˡ�
		 * 
		 * @return
		 */
		private boolean tryNextCommandTuple(){
			if(sessionInitQueryQueue == null){
				return false;
			}else{
				if(sessionInitQueryQueue.size()>0){
					currentCommand = sessionInitQueryQueue.get(0);
					if(logger.isDebugEnabled()){
						QueryCommandPacket command = new QueryCommandPacket();
						command.init(currentCommand.getBuffer(),source);
						logger.debug(command);
					}
					return true;
				}
				return false;
			}
		}
		
		/**
		 * �жϷ��ص������Ƿ��ǵ�ǰ����Ľ�������
		 * ��ǰȫ�����Ӷ�ȫ�������Ժ����ʾ��ǰ������ȫ������
		 * @param conn
		 * @param buffer
		 * @return
		 */
		protected  CommandStatus checkResponseCompleted(Connection conn,byte[] buffer){
			boolean isCompleted = false;
			ConnectionStatuts connStatus = connStatusMap.get(conn);
			if(connStatus == null){
				logger.error("connection Status not Found, byffer="+StringUtil.dumpAsHex(buffer, buffer.length));
			}
			isCompleted = connStatus.isCompleted(buffer);
			connStatus.packetIndex ++;
			/**
			 * ����Ƕ�����ӵģ���Ҫ�����ݻ����������ȴ�����ȫ������Ժ󣬽����ݽ�����װ��Ȼ���͵��ͻ���
			 * {@link #CommandMessageHandler.mergeMessageToClient}
			 */
			if(connStatus.buffers == null){
				connStatus.buffers = new ArrayList<byte[]>();
			}
			connStatus.buffers.add(buffer);
			if(isCompleted){
				lock.lock();
				try{
					if(currentCommand.getCompletedCount().incrementAndGet() == connStatusMap.size()){
						if(logger.isDebugEnabled()){
							Packet packet = null;
							if(MysqlPacketBuffer.isErrorPacket(buffer)){
								packet = new ErrorPacket();
							}else if(MysqlPacketBuffer.isEofPacket(buffer)){
								packet = new EOFPacket();
							}else if(MysqlPacketBuffer.isOkPacket(buffer)){
								packet = new OkPacket();
							}
							packet.init(buffer,conn);
							logger.debug("returned Packet:"+packet);
						}
						return CommandStatus.AllCompleted;
						
					}else{
						return CommandStatus.ConnectionCompleted;
					}
				}finally{
					lock.unlock();
				}
			}else{
				return CommandStatus.ConnectionNotComplete;
			}
		}
		
		/**
		 * �Ƿ�append �ɹ�������ɹ����ʾ��ǰ�����ѻ�������Ҫ��������֤���������ѭ��
		 * ���������û�жѻ�������򷵻�false.
		 * ���򷵻�true�� ���ʾ��ֱ�ӷ�������
		 * @param commandInfo
		 * @param force ǿ��append �������Ϊtrue
		 * @return
		 */
		public synchronized  boolean appendCommand(CommandInfo commandInfo,boolean force){
			if(force){
				if(sessionInitQueryQueue == null){
					sessionInitQueryQueue = Collections.synchronizedList(new ArrayList<CommandInfo>());
				}
				if(!sessionInitQueryQueue.contains(commandInfo)){
					sessionInitQueryQueue.add(commandInfo);
				}
				return true;
			}else{
				if(sessionInitQueryQueue == null){
					return false;
				}else{
					if(sessionInitQueryQueue.size() ==0){
						return false;
					}
					if(!sessionInitQueryQueue.contains(commandInfo)){
						sessionInitQueryQueue.add(commandInfo);
					}
					return true;
				}
			}
		}
	}
	
	protected MysqlClientConnection source;
	private boolean completed;
	private long createTime;
	private long timeout;
	private long endTime;
	private boolean ended = false;
	protected CommandQueue commandQueue;
	private ObjectPool[] pools;
	private CommandInfo info = new CommandInfo();
	protected byte commandType;
	protected Map<Connection,MessageHandler> handlerMap = new HashMap<Connection,MessageHandler>();
	private final Lock lock = new ReentrantLock(false);
	
	public CommandMessageHandler(final MysqlClientConnection source,byte[] query,ObjectPool[] pools,long timeout){
		handlerMap.put(source, source.getMessageHandler());
		source.setMessageHandler(this);
		commandQueue = new CommandQueue(source);
		QueryCommandPacket command = new QueryCommandPacket();
		command.init(query,source);
		this.pools = pools;
		info.setBuffer(query);
		info.setMain(true);
		
		this.source = source;
		this.createTime = System.currentTimeMillis();
		this.timeout = timeout;
	}
	
	/**
	 * �жϱ�handled��Connection ��Ϣ�����Ƿ����
	 * @return
	 */
	public boolean isCompleted(){
		return completed;
	}
	
	/**
	 * ��Ҫ��Ϊ�˷�������� �� �ͻ������ӵĻ���һ�£����磬��ǰ��schema ��charset�ȣ�
	 * 
	 * �ڷ���������֮ǰ��Ԥ����Ҫ����һЩ������������sourceConnection��destConnection ��ǰ��database��һ�£���Ҫ����init_db Command
	 * Ϊ�˼��ٸ��Ӷȣ�ֻҪһ��Connection��Ҫ���������ô�������Ӷ����뷢��һ����ͬ�����
	 * 
	 * @param sourceMysql
	 * @param destMysqlConn
	 */
	//TODO ��Ҫ�����Ż�
	protected void appendPreMainCommand(){
		Set<MysqlServerConnection> connSet = commandQueue.connStatusMap.keySet();
		final MysqlConnection sourceMysql =(MysqlConnection) source;
		for(Connection destConn : connSet){
			MysqlConnection destMysqlConn = (MysqlConnection)destConn;
			if(!StringUtil.equalsIgnoreCase(sourceMysql.getSchema(), destMysqlConn.getSchema())){
				if(sourceMysql.getSchema() != null){
					QueryCommandPacket selectDBCommand = new QueryCommandPacket();
					selectDBCommand.arg = sourceMysql.getSchema();
					selectDBCommand.command = QueryCommandPacket.COM_INIT_DB;
					
					byte[] buffer = selectDBCommand.toByteBuffer(destMysqlConn).array();
					CommandInfo info = new CommandInfo();
					info.setBuffer(buffer);
					info.setMain(false);
					info.setRunnable(new Runnable(){
						public void run() {
							Set<MysqlServerConnection> connSet = commandQueue.connStatusMap.keySet();
							for(Connection conn : connSet){
								((MysqlConnection)conn).setSchema(sourceMysql.getSchema());
							}
						}
					});
					commandQueue.appendCommand(info,true);
				}
			}
			
			if(sourceMysql.getCharset()!= null &&
					!StringUtil.equalsIgnoreCase(sourceMysql.getCharset(),destMysqlConn.getCharset())){
				QueryCommandPacket charsetCommand = new QueryCommandPacket();
				charsetCommand.arg = "set names " + sourceMysql.getCharset();
				charsetCommand.command = QueryCommandPacket.COM_QUERY;
				
				byte[] buffer = charsetCommand.toByteBuffer(sourceMysql).array();
				CommandInfo info = new CommandInfo();
				info.setBuffer(buffer);
				info.setMain(false);
				info.setRunnable(new Runnable(){
					public void run() {
						Set<MysqlServerConnection> connSet = commandQueue.connStatusMap.keySet();
						for(Connection conn : connSet){
							((MysqlConnection)conn).setCharset(sourceMysql.getCharset());
						}
					}
				});
				commandQueue.appendCommand(info,true);
			}
			
			if(sourceMysql.isAutoCommit() != destMysqlConn.isAutoCommit()){
				QueryCommandPacket charsetCommand = new QueryCommandPacket();
				charsetCommand.arg = "set autocommit = " + (sourceMysql.isAutoCommit()?1:0);
				charsetCommand.command = QueryCommandPacket.COM_QUERY;
				
				byte[] buffer = charsetCommand.toByteBuffer(sourceMysql).array();
				CommandInfo info = new CommandInfo();
				info.setBuffer(buffer);
				info.setMain(false);
				info.setRunnable(new Runnable(){
					public void run() {
						Set<MysqlServerConnection> connSet = commandQueue.connStatusMap.keySet();
						for(Connection conn : connSet){
							((MysqlConnection)conn).setAutoCommit(sourceMysql.isAutoCommit());
						}
					}
				});
				commandQueue.appendCommand(info,true);
			}
			
		}
	}
	protected void appendAfterMainCommand(){
		
	}

	/**
	 * �����ӿ�ʼһ�������ʱ��
	 * @param conn
	 */
	protected void startConnectionCommand(Connection conn,CommandInfo currentCommand){
		if(conn instanceof CommandListener){
			CommandListener listener = (CommandListener)conn;
			listener.startCommand(commandQueue.currentCommand);
		}
	}
	
	/**
	 * ���������һ�������ʱ��ִ�У�ֻ����������Լ����������������ӡ�
	 * @param conn
	 */
	protected void finishedConnectionCommand(Connection conn,CommandInfo currentCommand){
		if(conn instanceof CommandListener){
			CommandListener listener = (CommandListener) conn;
			listener.finishedCommand(this.commandQueue.currentCommand);
		}
	}
	
	public void handleMessage(Connection fromConn, byte[] message) {
		/*if(ended){
			logger.error("ended session handler handle message:\n"+StringUtil.dumpAsHex(message, message.length));
			return;
		}*/

		if(fromConn == source){
			CommandInfo info = new CommandInfo();
			info.setBuffer(message);
			info.setMain(true);
			
			if(!commandQueue.appendCommand(info,false)){
				dispatchMessageFrom(source,message);
			}
		}else{
			
			if(logger.isDebugEnabled()){
				if(MysqlPacketBuffer.isErrorPacket(message)){
					logger.error("connection="+fromConn.hashCode()+",error packet:\n"+StringUtil.dumpAsHex(message, message.length));
				}
			}
			//�ж������Ƿ������
			CommandStatus commStatus = commandQueue.checkResponseCompleted(fromConn, message);
			
			if(CommandStatus.AllCompleted == commStatus || CommandStatus.ConnectionCompleted == commStatus){
				finishedConnectionCommand(fromConn,commandQueue.currentCommand);
				lock.lock();
				try{
					if(this.ended){
						releaseConnection(fromConn);
					}
				}finally{
					lock.unlock();
				}
			}
			
			if(CommandStatus.AllCompleted == commStatus){
				try{
					if(commandQueue.currentCommand.isMain()){
						commandQueue.mainCommandExecuted = true;
						releaseConnection(source);
					}

					/**
					 * ����ǿͻ��������������:
					 * 1�������Ƕ�̨server�ģ���Ҫ���кϲ�����
					 * 2����̨serverֱ��д�����ͻ���
					 */
					
					if(commandQueue.currentCommand.isMain()){
						if(commandQueue.isMultiple()){
							List<byte[]> list = this.mergeMessages();
							if(list != null){
								for(byte[] buffer : list){
									dispatchMessageFrom(fromConn,buffer);
								}
							}
						}else{
							dispatchMessageFrom(fromConn,message);
						}
					}else{
						//����������Ժ󷵻س�����Ϣ���������ǰ��session
						Collection<ConnectionStatuts> connectionStatutsSet = commandQueue.connStatusMap.values();
						for(ConnectionStatuts connStatus : connectionStatutsSet){
							//���Ƿ�ÿ�����������ص����ݰ���û���쳣��Ϣ��
							if((connStatus.statusCode & SessionStatus.ERROR) >0){
								this.commandQueue.currentCommand.setStatusCode(connStatus.statusCode);
								byte[] errorBuffer = connStatus.buffers.get(connStatus.buffers.size()-1);
								if(!commandQueue.mainCommandExecuted){
									dispatchMessageFrom(connStatus.conn,errorBuffer);
									if(source.isAutoCommit()){
										this.endSession();
									}
								}else{
									if(logger.isDebugEnabled()){
										byte[] commandBuffer = commandQueue.currentCommand.getBuffer();
										StringBuffer buffer = new StringBuffer();
										buffer.append("Current Command Execute Error:\n");
										buffer.append(StringUtil.dumpAsHex(commandBuffer,commandBuffer.length));
										buffer.append("\n error Packet:\n");
										buffer.append(StringUtil.dumpAsHex(errorBuffer,errorBuffer.length));
										logger.debug(buffer.toString());
									}
								}
								return;
							}
						}
					}
				}finally{
					afterCommandCompleted(commandQueue.currentCommand);
				}
			}else{
				if(commandQueue.currentCommand.isMain()){
					if(!commandQueue.isMultiple()){
						dispatchMessageFrom(fromConn,message);
					}
				}
			}
		}
	}
	
	/**
	 * ��һ�����������ʱ������������ݰ������ҳ��Է�����һ��command
	 * ���������û������������ǰ�ػ�
	 * @param oldCommand ��ǰ��command
	 */
	protected void afterCommandCompleted(CommandInfo oldCommand){
		if(oldCommand.getRunnable()!= null){
			oldCommand.getRunnable().run();
		}
		commandQueue.clearAllBuffer();

		//��һ����������һ�����ݰ�����򽫵�ǰ������Ӷ�����ɾ����
		commandQueue.sessionInitQueryQueue.remove(0);
		if(!ended){
			startNextCommand();
		}
	}
	
	//�ж��Ƿ���Ҫ����������һ���ͻ�������
	//������һ������
	protected synchronized void startNextCommand(){
		if(commandQueue.currentCommand != null && (commandQueue.currentCommand.getStatusCode() & SessionStatus.ERROR) >0){
			if(source.isAutoCommit()){
				this.endSession();
			}
			return;
		}
		
		if(!this.ended && commandQueue.tryNextCommandTuple()){
			commandType = commandQueue.currentCommand.getBuffer()[4];
			Collection<ConnectionStatuts> connSet = commandQueue.connStatusMap.values();
			
			boolean commandCompleted = commandQueue.currentCommand.getCompletedCount().get() == commandQueue.connStatusMap.size();
			
			if(!commandCompleted){
				for(ConnectionStatuts status : connSet){
					status.setCommandType(commandType);
					startConnectionCommand(status.conn,commandQueue.currentCommand);
				}
			}
			
			dispatchMessageFrom(source,commandQueue.currentCommand.getBuffer());
			
			if(commandCompleted){
				afterCommandCompleted(commandQueue.currentCommand);
			}
		}else{
			if(source.isAutoCommit()){
				this.endSession();
			}
		}
	}
	
	/**
	 * �κ���handler������Ҫ���͵�Ŀ�����ӵ����ݰ��������ø÷������ͳ�ȥ��
	 * �ӷ������˷��͹�������Ϣ���ͻ��ˣ����ߴӿͻ��˷����������mysql server��
	 * 
	 * �����Ҫ���͵���Ϣ��2�֣�
	 * 1���ӿͻ��˷��͹�������Ϣ
	 * 2��reponse��ǰ����Ҫ����ǿͻ��˷���������������Ǹ�proxy�ڲ���������������ݰ�
	 * ����2�����ݰ�ͨ��dispatchMessage �������ͳ�ȥ�ġ�
	 * ���ڲ��������������ݰ������� afterCommandCompleted()֮�� ����ConnectionStatus.buffers�б��档
	 * commandQueue.clearAllBuffer() �Ժ�buffers �������
	 * 
	 * @param fromServer �Ƿ��Ǵ�mysql server �˷��͹�����
	 * @param message ��Ϣ����
	 */
	protected void dispatchMessageFrom(Connection fromConn,byte[] message){
		if(fromConn != source){
			dispatchMessageTo(source,message);
		}else{
			Collection<MysqlServerConnection> connSet =  commandQueue.connStatusMap.keySet();
			for(Connection conn : connSet){
				dispatchMessageTo(conn,message);
			}
		}
	}
	
	protected void dispatchMessageTo(Connection toConn,byte[] message){
		toConn.postMessage(message);
	}
	
	protected void releaseConnection(Connection conn){
		lock.lock();
		try{
			MessageHandler handler = handlerMap.remove(conn);
			if(handler != null){
				conn.setMessageHandler(handler);
			}
			
			if(conn instanceof MysqlServerConnection){
				PoolableObject pooledObject = (PoolableObject)conn;
				if(pooledObject.getObjectPool() != null){
					try {
						pooledObject.getObjectPool().returnObject(conn);
						if(logger.isDebugEnabled()){
							logger.debug("connection:"+conn+" return to pool");
						}
					} catch (Exception e) {
					}
				}
			}
		}finally{
			lock.unlock();	
		}
	}
	
	/**
	 * �رո�messageHandler ���һָ��������messageHandler��handle��Connection
	 */
	protected void releaseAllCompletedConnection(){
		lock.lock();
		try{
			Set<Map.Entry<Connection,MessageHandler>> handlerSet = handlerMap.entrySet();
			for(Map.Entry<Connection,MessageHandler> entry:handlerSet){
				MessageHandler handler = entry.getValue();
				Connection connection = entry.getKey();
				ConnectionStatuts status = this.commandQueue.connStatusMap.get(connection);
				if(this.commandQueue.currentCommand == null || status != null && (status.statusCode & SessionStatus.COMPLETED)>0){
					connection.setMessageHandler(handler);
					if(!connection.isClosed()){
						if(connection instanceof MysqlServerConnection){
							PoolableObject pooledObject = (PoolableObject)connection;
							if(pooledObject.getObjectPool() != null){
								try {
									pooledObject.getObjectPool().returnObject(connection);
									if(logger.isDebugEnabled()){
										logger.debug("connection:"+connection+" return to pool");
									}
								} catch (Exception e) {
								}
							}
						}
					}
				}
			}
		}finally{
			lock.unlock();
		}
	}
	
	/**
	 * �ϲ������˵���Ϣ�����͵��ͻ���
	 * ֻ���ڶ����ӵ��������Ҫ�������ݰ��ۺϣ��ۺ��Ժ���һ�����ݰ�ͨ�� {@link #dispatchMessageFrom(Connection, byte[])}�������ͳ�ȥ,
	 * һ��һ������ֱ��ͨ��{@link #dispatchMessageFrom(Connection, byte[])} ���� ֱ�ӷ��ͳ�ȥ,������Ҫmerge��
	 * @return
	 */
	protected List<byte[]> mergeMessages(){
		Collection<ConnectionStatuts> connectionStatutsSet = commandQueue.connStatusMap.values();
		boolean isSelectQuery = true;
		List<byte[]> buffers = null;
		List<byte[]> returnList = new ArrayList<byte[]>();
		for(ConnectionStatuts connStatus : connectionStatutsSet){
			//���Ƿ�ÿ�����������ص����ݰ���û���쳣��Ϣ��
			byte[] buffer = connStatus.buffers.get(connStatus.buffers.size()-1);
			buffers = connStatus.buffers;
			if((connStatus.statusCode & SessionStatus.ERROR) >0){
				return buffers;
			}
			if(isSelectQuery){
				isSelectQuery =isSelectQuery && MysqlPacketBuffer.isEofPacket(buffer);
			}
		}
		
		if(isSelectQuery){
			//��ǰ��packetId
			byte paketId = 0;
			
			//����field��Ϣ
			for(byte[] buffer : buffers){
				if(MysqlPacketBuffer.isEofPacket(buffer)){
					returnList.add(buffer);
					paketId = buffer[3];
					break;
				}else{
					returnList.add(buffer);
					paketId = buffer[3];
				}
			}
			paketId += 1;
			//����rows���ݰ�
			for(ConnectionStatuts connStatus : connectionStatutsSet){
				boolean rowStart = false;;
				for(byte[] buffer : connStatus.buffers){
					if(!rowStart){
						if(MysqlPacketBuffer.isEofPacket(buffer)){
							rowStart = true;
						}else{
							continue;
						}
					}else{
						if(!MysqlPacketBuffer.isEofPacket(buffer)){
							buffer[3] = paketId;
							paketId += 1;
							returnList.add(buffer);
						}
					}
				}
			}
			
			byte[] eofBuffer = buffers.get(buffers.size()-1);
			eofBuffer[3] = paketId;
			returnList.add(eofBuffer);
		}else{
			OkPacket ok = new OkPacket();
			StringBuffer strbuffer = new StringBuffer();
			for(ConnectionStatuts connStatus : connectionStatutsSet){
				byte[] buffer = connStatus.buffers.get(connStatus.buffers.size()-1);
				OkPacket connOK = new OkPacket();
				connOK.init(buffer,connStatus.conn);
				ok.affectedRows +=connOK.affectedRows;
				ok.insertId =connOK.insertId;
				ok.packetId = 1;
				strbuffer.append(connOK.message);
				ok.warningCount +=connOK.warningCount;
			}
			ok.message = strbuffer.toString();
			returnList.add(ok.toByteBuffer(source).array());
		}
		return returnList;
	}

	protected abstract ConnectionStatuts newConnectionStatuts(Connection conn);

	public synchronized void startSession() throws Exception {
		if(logger.isInfoEnabled()){
			logger.info(this+" session start");
		}
		for(ObjectPool pool:pools){
			MysqlServerConnection conn;
			conn = (MysqlServerConnection)pool.borrowObject();
			handlerMap.put(conn, conn.getMessageHandler());
			conn.setMessageHandler(this);
			commandQueue.connStatusMap.put(conn, newConnectionStatuts(conn));
		}
		appendPreMainCommand();
		this.commandQueue.appendCommand(info, true);
		appendAfterMainCommand();
		startNextCommand();
	}
	
	public boolean checkIdle(long now) {
		if(timeout >0){
			return (now - createTime)>timeout;
		}else{
			if(ended){
				/**
				 * �����session�Ѿ���������ʱ���serverConnection�˻��ڵȴ��������ݷ��ʡ����ҳ���15s,����Ҫ�����еĻỰ
				 * �������ڸ���ԭ����ɷ�������û�з������ݻ����Ѿ������ĻỰ��ServerConnection�޷�����Pool�С�
				 */
				return (now - endTime)>15000;
			}
			return false;
		}
	}

	public void endSession() {
		lock.lock();
		try{
			if(!ended){
				endTime = System.currentTimeMillis();
				ended = true;
				this.releaseAllCompletedConnection();
				if(!this.commandQueue.mainCommandExecuted){
					ErrorPacket error = new ErrorPacket();
					error.errno = 10000;
					error.packetId = 2;
					error.serverErrorMessage = "session was killed!!";
					this.dispatchMessageTo(source, error.toByteBuffer(source).array());
					logger.warn("session was killed!!",new Exception());
					source.postClose(null);
				}else{
					if(logger.isInfoEnabled()){
						logger.info(this+" session ended.");
					}
				}
			}
		}finally{
			lock.unlock();
		}
	}
	

	public boolean isEnded() {
		lock.lock();
		try{
			return this.ended;
		}finally{
			lock.unlock();
		}
	}
	
	public void appendReport(StringBuilder buffer, long now, long sinceLast,boolean reset,Level level) {
		buffer.append("    -- MessageHandler:").append("multiple Size:").append(commandQueue.connStatusMap.size());
		if(commandQueue.currentCommand != null){
			buffer.append(",currentCommand completedCount:");
			buffer.append(commandQueue.currentCommand.getCompletedCount()).append("\n");
		}else{
			buffer.append("\n");
		}
	}

}
