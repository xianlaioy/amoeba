package com.meidusa.amoeba.net;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;


/**
 * ��������ӹ���,�������� {@link #setConnectionManager(ConnectionManager)},
 * ���ConnectoinManager�������������������������������.
 * 
 * @author struct
 *
 */
public abstract class AbstractConnectionFactory implements ConnectionFactory {

	protected ConnectionManager connectionManager;
	
	public ConnectionManager getConnectionManager() {
		return connectionManager;
	}

	public void setConnectionManager(ConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
	}

	/**
	 * ����һ������,��ʼ������,ע�ᵽ���ӹ�����,
	 * 
	 * @return Connection ���ظ�����ʵ��
	 */
	public Connection createConnection(SocketChannel channel, long createStamp) throws IOException {
		Connection connection = (Connection) newConnectionInstance(channel,System.currentTimeMillis());
		initConnection(connection);
		connectionManager.postRegisterNetEventHandler(connection, SelectionKey.OP_READ);
		return connection;
	}
	
	/**
	 * �����Ժ�,����������´�����������һЩ��ʼ��
	 * @param connection
	 */
	protected void initConnection(Connection connection){
		
	}
	
	/**
	 * ��������ʵ��
	 * @param channel
	 * @param createStamp
	 * @return
	 */
	protected abstract Connection newConnectionInstance(SocketChannel channel, long createStamp);

}
