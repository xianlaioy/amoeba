package com.meidusa.amoeba.oracle.handler;

import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.MessageHandler;
import com.meidusa.amoeba.net.Sessionable;
import com.meidusa.amoeba.oracle.packet.AcceptPacket;
import com.meidusa.amoeba.oracle.packet.ConnectPacket;
import com.meidusa.amoeba.oracle.packet.Packet;

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
		Packet packet = null;
		if(conn == clientConn){
			serverConn.postMessage(message);
			
			/**
			 * �ӿͻ��˷��͹�������֤��Ϣ��
			 */
			if(message[4] == (byte)Packet.NS_PACKT_TYPE_CONNECT){
				packet = new ConnectPacket();
			}
			
		}else{
			
			/**
			 * �ӷ���˷��͹����Ľ������ӵ����ݰ�
			 */
			if(message[4] == (byte)Packet.NS_PACKT_TYPE_ACCEPT){
				packet = new AcceptPacket();
			}
			clientConn.postMessage(message);
		}
		
		if(packet != null){
			packet.init(message);
		}
	}
	public boolean checkIdle(long now) {
		return false;
	}
	
	public synchronized void endSession() {
		if(!isEnded()){
			isEnded = true;
			clientConn.postClose(null);
			serverConn.postClose(null);
		}
	}
	
	public boolean isEnded() {
		return isEnded;
	}
	public void startSession() throws Exception {
		
	}

}
