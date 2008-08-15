package com.meidusa.amoeba.oracle.packet;

import java.nio.ByteBuffer;

import org.apache.log4j.Logger;

import com.meidusa.amoeba.packet.AbstractPacketBuffer;

/**
 * �������˷��ص�Ҫ��ͻ����ط������ݰ�
 * 
 * @author hexianmao
 * @version 2008-8-6 ����05:32:52
 */
public class ResendPacket extends AbstractPacket {

    private static Logger       logger = Logger.getLogger(ResendPacket.class);
    private final static byte[] b      = { 0x00, 0x08, 0x00, 0x00, 0x0b, 0x00, 0x00, 0x00 };

    public void init(byte[] buffer) {
        super.init(buffer);

        if (logger.isDebugEnabled()) {
            logger.debug(this.toString());
        }
    }

    public ByteBuffer toByteBuffer() {
        return ByteBuffer.wrap(b);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("ResendPacket info ==============================\n");
        return sb.toString();
    }
    
    @Override
	protected Class<? extends AbstractPacketBuffer> getBufferClass() {
		return AbstractPacketBuffer.class;
	}
}
