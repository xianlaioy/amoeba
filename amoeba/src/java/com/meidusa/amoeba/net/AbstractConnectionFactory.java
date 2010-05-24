package com.meidusa.amoeba.net;

import java.io.IOException;
import java.nio.channels.SocketChannel;


/**
 * ��������ӹ���,�������� {@link #setConnectionManager(ConnectionManager)},
 * ���ConnectoinManager�������������������������������.
 * 
 * @author struct
 *
 */
public abstract class AbstractConnectionFactory implements ConnectionFactory {

	/**
	 * ����һ������,��ʼ������,ע�ᵽ���ӹ�����,
	 * 
	 * @return Connection ���ظ�����ʵ��
	 */
	public Connection createConnection(SocketChannel channel, long createStamp) throws IOException {
		Connection connection = (Connection) newConnectionInstance(channel,System.currentTimeMillis());
		initConnection(connection);
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
