package com.meidusa.amoeba.net;

/**
 * ��Ϊǰ�����ݿ����ӹ���
 * @author struct
 *
 */
public abstract class FrontendConnectionFactory extends AbstractConnectionFactory {
	protected String user;
	protected String password;
	
	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	protected void initConnection(Connection connection){
		super.initConnection(connection);
		if(connection instanceof AuthingableConnection){
			AuthingableConnection conn = (AuthingableConnection)connection;
			conn.setUser(user);
			conn.setPassword(password);
		}
	}
	

}
