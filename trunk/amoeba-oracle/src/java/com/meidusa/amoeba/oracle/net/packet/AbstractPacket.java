package com.meidusa.amoeba.oracle.net.packet;

import java.io.UnsupportedEncodingException;

import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.packet.AbstractPacketBuffer;
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
public abstract class AbstractPacket extends com.meidusa.amoeba.net.packet.AbstractPacket implements SQLnetDef, OraclePacketConstant {

    protected int   length;
    protected short type;
    protected short flags;
    protected int   dataLen;
    protected int   dataOffset;
    protected int   packetCheckSum;
    protected int   headerCheckSum;
    private String  data;
    private byte[]  buffer;

    public AbstractPacket(short type){
        this.type = type;
    }

    public void init(byte[] buffer, Connection conn) {
        this.buffer = buffer;
        super.init(buffer, conn);
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
        buffer.setPosition(0);
        OracleAbstractPacketBuffer oracleBuffer = (OracleAbstractPacketBuffer) buffer;
        oracleBuffer.writeUB2(position);
        oracleBuffer.writeUB2(packetCheckSum);
        oracleBuffer.writeUB1(type);
        oracleBuffer.writeUB1(flags);
        oracleBuffer.writeUB2(headerCheckSum);
        buffer.setPosition(position);
    }

    /**
     * ����packet�Ĵ�С�������̫���˷��ڴ棬�����̫С��Ӱ������
     */
    protected int calculatePacketSize() {
        return DATA_OFFSET + 1;
    }

    protected String extractData() {
        if (dataLen <= 0) {
            data = new String();
        } else if (length > dataOffset) {
            data = new String(buffer, dataOffset, dataLen);
        } else {
            byte abyte0[] = new byte[dataLen];
            data = new String(abyte0);
        }
        return data;
    }

    protected Class<? extends AbstractPacketBuffer> getPacketBufferClass() {
        return OracleAbstractPacketBuffer.class;
    }

    public String getData() {
        return data;
    }

}
