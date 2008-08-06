package com.meidusa.amoeba.oracle.net;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import com.meidusa.amoeba.net.DatabaseConnection;
import com.meidusa.amoeba.net.io.PacketInputStream;
import com.meidusa.amoeba.net.io.PacketOutputStream;
import com.meidusa.amoeba.oracle.io.OraclePacketInputStream;
import com.meidusa.amoeba.oracle.io.OraclePacketOutputStream;

public abstract class OracleConnection extends DatabaseConnection {

	public OracleConnection(SocketChannel channel, long createStamp) {
		super(channel, createStamp);
		/**
		 * TODO
		 * ������
		 */
		this.setAuthenticated(true);
	}

	@Override
	protected PacketInputStream createPacketInputStream() {
		return new OraclePacketInputStream();
	}

	@Override
	protected PacketOutputStream createPakcetOutputStream() {
		return new OraclePacketOutputStream();
	}

	/**
	 * Ϊ���������ܣ�����Oracle���ݰ�д��Ŀ�ĵص�ʱ���Ѿ������˰�ͷ������Ҫ����PacketOutputStream����
	 */
	public void postMessage(byte[] msg)
    {
        ByteBuffer out= ByteBuffer.allocate(msg.length);
        out.put(msg);
        out.flip();
        _outQueue.append(out);
        _cmgr.invokeConnectionWriteMessage(this);
    }
}
