package com.meidusa.amoeba.aladdin.io;

import com.meidusa.amoeba.net.Connection;

public interface ResultPacket {
	
	public void setError(int errorCode,String errorMessage);
	/**
	 * ��ResultSet��Щ���ϲ��Ժ�д��Connection
	 * head--> fields --> eof -->rows --> eof
	 * @param conn
	 */
	public abstract void wirteToConnection(Connection conn);

}