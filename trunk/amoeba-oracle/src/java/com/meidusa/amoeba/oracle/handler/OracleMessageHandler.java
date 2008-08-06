package com.meidusa.amoeba.oracle.handler;

import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.MessageHandler;
import com.meidusa.amoeba.net.Sessionable;

/**
 * �ǳ��򵥵����ݰ�ת������
 * @author struct
 *
 */
public class OracleMessageHandler implements MessageHandler,Sessionable {

	private Connection clientConn;
	private Connection serverConn;
	private boolean isEnded = false;
	public OracleMessageHandler(Connection clientConn,Connection serverConn){
		this.clientConn = clientConn;
		this.serverConn = serverConn;
	}
	public void handleMessage(Connection conn, byte[] message) {
		if(conn == clientConn){
			serverConn.postMessage(message);
		}else{
			clientConn.postMessage(message);
		}
	}
	public boolean checkIdle(long now) {
		return false;
	}
	public void endSession() {
		clientConn.postClose(null);
		serverConn.postClose(null);
		isEnded = true;
	}
	
	public boolean isEnded() {
		return isEnded;
	}
	public void startSession() throws Exception {
		
	}

}
