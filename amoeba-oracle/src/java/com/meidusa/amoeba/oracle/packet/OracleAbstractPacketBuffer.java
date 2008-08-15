package com.meidusa.amoeba.oracle.packet;

import com.meidusa.amoeba.packet.AbstractPacketBuffer;

public class OracleAbstractPacketBuffer extends AbstractPacketBuffer {
	public static short versionNumber;//���ڴ��ȫ�ַ������˵İ汾��amoeba ������Ķ�� oracle server �ڰ汾�������Ҫһ��
	
	public OracleAbstractPacketBuffer(byte[] buf) {
		super(buf);
	}

	public OracleAbstractPacketBuffer(int size) {
		super(size);
	}
	
	public short readUB1() {
        return (short) (buffer[position++] & 0xff);
    }

    public void writeUB1(short s) {
        writeByte((byte) (s & 0xff));
    }

    public int readUB2() {
        int i = 0;
        i |= (buffer[position++] & 0xff) << 8;
        i |= (buffer[position++] & 0xff);
        i &= 0xfffff;
        return i;
    }

    public void writeUB2(int i) {
        ensureCapacity(2);
        int j = 0xffff & i;
        buffer[position++] = (byte) ((j >>> 8) & 0xff);
        buffer[position++] = (byte) (j & 0xff);
    }

    public long readUB4() {
        long l = 0L;
        l |= ((buffer[position++] & 0xff) << 24);
        l |= ((buffer[position++] & 0xff) << 16);
        l |= ((buffer[position++] & 0xff) << 8);
        l |= (buffer[position++] & 0xff);
        l &= -1L;
        return l;
    }

    public void writeUB4(long l) {
        ensureCapacity(4);
        long m = l & -1L;
        buffer[position++] = (byte) ((m >>> 24) & 0xff);
        buffer[position++] = (byte) ((m >>> 16) & 0xff);
        buffer[position++] = (byte) ((m >>> 8) & 0xff);
        buffer[position++] = (byte) (m & 0xff);
    }

}
