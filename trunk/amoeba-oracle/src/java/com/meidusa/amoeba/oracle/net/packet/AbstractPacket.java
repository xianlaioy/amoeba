package com.meidusa.amoeba.oracle.net.packet;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.packet.AbstractPacketBuffer;
import com.meidusa.amoeba.net.packet.PacketBuffer;
import com.meidusa.amoeba.oracle.io.OraclePacketConstant;

/**
 * <pre>
 * 
 * |-------------------------------------------
 * |Common Packet Header  | 8   | ͨ�ð�ͷ
 * |--------------------------------------------
 * |                Data  |�ɱ� | ����
 * |--------------------------------------------
 * |
 * |
 * |ͨ�ð�ͷ��ʽ  ÿ��TNS�������ݶ�����һ��ͨ�ð�ͷ
 * |��˵���������ݵĳ��ȼ������У��ͽ�������Ϣ��
 * |------------------------------------------------
 * |              Length  | 2   | ���ĳ��ȣ�����ͨ�ð�ͷ
 * |--------------------------------------------------
 * |    Packet check sum  | 2   | ����У���
 * |------------------------------------------------
 * |                Type  | 1   | TNS ������
 * |-----------------------------------------------
 * |                Flag  | 1   | ״̬
 * |----------------------------------------------
 * |    Header check sum  | 2   | ͨ��ͷ��У���
 * |---------------------------------------------
 * 
 * 
 * </pre>
 * 
 * @author struct
 */
public abstract class AbstractPacket implements Packet, OraclePacketConstant {

    protected int   length;
    protected short type;
    protected short flags;
    protected int   dataLen;
    protected int   dataOffset;
    protected int   packetCheckSum;
    protected int   headerCheckSum;

    private byte[]  buffer;

    public AbstractPacket(short type){
        this.type = type;
    }

    public void init(byte[] buffer, Connection conn) {
        this.buffer = buffer;
        AbstractPacketBuffer packetBuffer = constractorBuffer(buffer);
        packetBuffer.init(conn);
        init(packetBuffer);
    }

    /**
     * ����ͷ����Ϣ����
     */
    protected void init(AbstractPacketBuffer buffer) {
        OracleAbstractPacketBuffer oracleBuffer = (OracleAbstractPacketBuffer) buffer;
        length = oracleBuffer.readUB2();
        packetCheckSum = oracleBuffer.readUB2();
        type = oracleBuffer.readUB1();
        flags = oracleBuffer.readUB1();
        headerCheckSum = oracleBuffer.readUB2();
    }

    public ByteBuffer toByteBuffer(Connection conn) {
        try {
            int bufferSize = calculatePacketSize();
            bufferSize = (bufferSize < (DATA_OFFSET + 1) ? (DATA_OFFSET + 1) : bufferSize);
            AbstractPacketBuffer packetBuffer = constractorBuffer(bufferSize);
            packetBuffer.init(conn);
            return toBuffer(packetBuffer).toByteBuffer();
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    /**
     * <pre>
     *  �÷���������{@link #write2Buffer(PacketBuffer)} д�뵽ָ����buffer�� 
     *  ���ҵ�����{@link #afterPacketWritten(PacketBuffer)}
     * </pre>
     */
    private AbstractPacketBuffer toBuffer(AbstractPacketBuffer buffer) throws UnsupportedEncodingException {
        write2Buffer(buffer);
        afterPacketWritten(buffer);
        return buffer;
    }

    /**
     * ����ͷ����Ϣ��װ
     */
    protected void write2Buffer(AbstractPacketBuffer buffer) throws UnsupportedEncodingException {
        buffer.setPosition(HEADER_SIZE);
    }

    /**
     * д��֮��һ����Ҫ�������������buffer��ָ��λ��ָ��ĩβ����һ��λ�ã����ܳ���λ�ã���
     */
    protected void afterPacketWritten(AbstractPacketBuffer buffer) {
        int position = buffer.getPosition();
        int packetLength = position;
        buffer.setPosition(0);
        OracleAbstractPacketBuffer oracleBuffer = (OracleAbstractPacketBuffer) buffer;
        oracleBuffer.writeUB2(packetLength);
        oracleBuffer.writeUB2(packetCheckSum);
        oracleBuffer.writeUB1(type);
        oracleBuffer.writeUB1(flags);
        oracleBuffer.writeUB2(headerCheckSum);
        buffer.setPosition(position);
        buffer.setPacketLength(packetLength);
    }

    /**
     * ����packet�Ĵ�С�������̫���˷��ڴ棬�����̫С��Ӱ������
     */
    protected int calculatePacketSize() {
        return DATA_OFFSET + 1;
    }

    protected String extractData() {
        String data;
        if (dataLen <= 0) data = new String();
        else if (length > dataOffset) {
            data = new String(buffer, dataOffset, dataLen);
        } else {
            byte abyte0[] = new byte[dataLen];
            data = new String(abyte0);
        }
        return data;
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public Object clone() {
        try {
            return (AbstractPacket) super.clone();
        } catch (CloneNotSupportedException e) {
            // �߼����治�ᷢ����֧�����
            return null;
        }
    }

    protected Class<? extends AbstractPacketBuffer> getBufferClass() {
        return OracleAbstractPacketBuffer.class;
    }

    private AbstractPacketBuffer constractorBuffer(int bufferSize) {
        AbstractPacketBuffer buffer = null;
        try {
            Constructor<? extends AbstractPacketBuffer> constractor = getBufferClass().getConstructor(int.class);
            buffer = constractor.newInstance(bufferSize);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return buffer;
    }

    private AbstractPacketBuffer constractorBuffer(byte[] buffer) {
        AbstractPacketBuffer packetbuffer = null;
        try {
            Constructor<? extends AbstractPacketBuffer> constractor = getBufferClass().getConstructor(byte[].class);
            packetbuffer = constractor.newInstance(buffer);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return packetbuffer;
    }

}
