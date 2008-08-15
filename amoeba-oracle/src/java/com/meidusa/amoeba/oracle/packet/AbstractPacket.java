package com.meidusa.amoeba.oracle.packet;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import org.apache.commons.lang.builder.ToStringBuilder;

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
public class AbstractPacket implements Packet, OraclePacketConstant {

    private byte[]  buffer;
    protected int   length;
    protected short type;
    protected short flags;
    protected int   dataLen;
    protected int   dataOffset;
    protected int   packetCheckSum;
    protected int   headerCheckSum;

    public void init(byte[] buffer) {
        this.buffer = buffer;
        init(new AnoPacketBuffer(buffer));
    }

    protected void init(AnoPacketBuffer buffer) {
        length = buffer.readUB2();
        packetCheckSum = buffer.readUB2();
        type = buffer.readUB1();
        flags = buffer.readUB1();
        headerCheckSum = buffer.readUB2();
    }

    /**
     * �����ݰ�ת����ByteBuffer
     */
    public ByteBuffer toByteBuffer() {
        try {
            return toBuffer().toByteBuffer();
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    /**
     * д��֮��һ����Ҫ�������������buffer��ָ��λ��ָ��ĩβ����һ��λ�ã����ܳ���λ�ã���
     */
    protected void afterPacketWritten(AnoPacketBuffer buffer) {
        int position = buffer.getPosition();
        int packetLength = position;
        buffer.setPosition(0);
        buffer.writeUB2(packetLength);
        buffer.writeUB2(packetCheckSum);
        buffer.writeUB1(type);
        buffer.writeUB1(flags);
        buffer.writeUB2(headerCheckSum);
        buffer.setPosition(position);
        buffer.setPacketLength(packetLength);
    }

    /**
     * @param buffer ������������Ļ���
     * @throws UnsupportedEncodingException ��String to bytes�������벻֧�ֵ�ʱ��
     */
    protected void write2Buffer(AnoPacketBuffer buffer) throws UnsupportedEncodingException {
        buffer.setPosition(HEADER_SIZE);
    }

    /**
     * �÷���������{@link #write2Buffer(PacketBuffer)} д�뵽ָ����buffer�����ҵ�����{@link #afterPacketWritten(PacketBuffer)}
     */
    protected AnoPacketBuffer toBuffer(AnoPacketBuffer buffer) throws UnsupportedEncodingException {
        write2Buffer(buffer);
        afterPacketWritten(buffer);
        return buffer;
    }

    protected AnoPacketBuffer toBuffer() throws UnsupportedEncodingException {
        int bufferSize = calculatePacketSize();
        bufferSize = (bufferSize < (DATA_OFFSET + 1) ? (DATA_OFFSET + 1) : bufferSize);
        AnoPacketBuffer buffer = new AnoPacketBuffer(bufferSize);
        return toBuffer(buffer);
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

}
