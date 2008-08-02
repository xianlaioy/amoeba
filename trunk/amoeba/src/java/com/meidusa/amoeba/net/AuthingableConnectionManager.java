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
package com.meidusa.amoeba.net;

import java.io.IOException;

/**
 * ֧��Connection �����֤���̵� ConnectionManager
 * @author <a href=mailto:piratebase@sina.com>Struct chen</a>
 *
 */
public class AuthingableConnectionManager extends ConnectionManager {
	protected Authenticator _author;
	
	public AuthingableConnectionManager()throws IOException {
	}
	
	public AuthingableConnectionManager(String managerName)
			throws IOException {
		super(managerName);
	}

	public void setAuthenticator(Authenticator author) {
		_author = author;
		_author.setConnectionManager(this);
	}

	public Authenticator getAuthenticator() {
		return _author;
	}
	
	public void registerConnection(Connection connection,int key){
		super.registerConnection(connection,key);
		beforeAuthing(connection);
	}
	
	protected void beforeAuthing(Connection authing) {
		
	}

	protected void afterAuthing(Connection conn,AuthResponseData data) {
		AuthingableConnection auconn = (AuthingableConnection)conn;
		if(AuthResponseData.SUCCESS.equalsIgnoreCase(data.code)){
			auconn.setAuthenticated(true);
			// and let our observers know about our new connection
			notifyObservers(CONNECTION_ESTABLISHED, conn, null);
			connectionAuthenticateSuccess(conn,data);
		}else{
			auconn.setAuthenticated(false);
			connectionAuthenticateFaild(conn,data);
		}
	}
	
	protected void connectionAuthenticateSuccess(Connection conn,AuthResponseData data) {
		logger.info("Connection Authenticate success [ conn=" + conn + "].");
	}
	
	protected void connectionAuthenticateFaild(Connection conn,AuthResponseData data) {
		logger.info("Connection Authenticate faild [ conn=" + conn + "].");
	}
	
}
