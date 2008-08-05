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
package com.meidusa.amoeba.net.poolable;

import java.nio.channels.SelectionKey;

import org.apache.commons.pool.PoolableObjectFactory;

import com.meidusa.amoeba.net.AuthingableConnection;
import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.ConnectionFactory;
import com.meidusa.amoeba.net.ConnectionManager;
import com.meidusa.amoeba.net.SocketChannelFactory;

/**
 * 
 * @author <a href=mailto:piratebase@sina.com>Struct chen</a>
 *
 */
public abstract class PoolableConnectionFactory implements PoolableObjectFactory,
		ConnectionFactory {
	protected ConnectionManager connectionManager;
	protected SocketChannelFactory socketChannelFactory;
	
	public ConnectionManager getConnectionManager() {
		return connectionManager;
	}

	public void setConnectionManager(ConnectionManager connectionManager) {
		this.connectionManager = connectionManager;
	}

	public SocketChannelFactory getSocketChannelFactory() {
		return socketChannelFactory;
	}

	public void setSocketChannelFactory(SocketChannelFactory socketChannelFactory) {
		this.socketChannelFactory = socketChannelFactory;
	}

	public void activateObject(Object arg0) throws Exception {

	}

	public void destroyObject(Object arg0) throws Exception {
		Connection connection = (Connection)arg0;
		connection.postClose(null);
	}

	/**
	 * �ڷ�������ʽ��,�����Ҫ��֤���ӵ����,�������������ķ�ʽ���ȴ���֤���.�����ָ����ʱ�����޷����������֤,���´��������ӽ�������.
	 */
	public Object makeObject() throws Exception {
		Connection connection = (Connection) createConnection(socketChannelFactory.createSokectChannel(),System.currentTimeMillis());
		connectionManager.postRegisterNetEventHandler(connection, SelectionKey.OP_READ);
		if(connection instanceof AuthingableConnection){
			AuthingableConnection authconn = (AuthingableConnection)connection;
			authconn.isAuthenticatedWithBlocked(15000);
		}
		return connection;
	}

	public void passivateObject(Object arg0) throws Exception {

	}

	public boolean validateObject(Object arg0) {
		boolean validated = true;
		if(arg0 instanceof Connection){
			Connection connection = (Connection)arg0;
			if(connection instanceof AuthingableConnection){
				AuthingableConnection authConn = (AuthingableConnection)connection;
				validated = validated && authConn.isAuthenticated();
			}
			validated = validated && !connection.isClosed();
		}else{
			validated = false;
		}
		return validated;
	}

}
