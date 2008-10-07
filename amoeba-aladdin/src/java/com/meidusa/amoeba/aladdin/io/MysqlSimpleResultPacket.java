package com.meidusa.amoeba.aladdin.io;

import java.util.concurrent.atomic.AtomicLong;

import com.meidusa.amoeba.mysql.net.packet.OkPacket;
import com.meidusa.amoeba.net.Connection;

/**
 * 
 * @author struct
 *
 */
public class MysqlSimpleResultPacket extends ErrorResultPacket {
	
	private AtomicLong resultCount = new AtomicLong();
	public void addResultCount(int count){
		resultCount.addAndGet(count);
	}
	
	public void wirteToConnection(Connection conn) {
		if(isError()){
			super.wirteToConnection(conn);
			return;
		}
		OkPacket packet = new OkPacket();
		packet.affectedRows = resultCount.get();
		packet.insertId = 1;
		packet.serverStatus = 2;
		conn.postMessage(packet.toByteBuffer(conn));
	}

}
