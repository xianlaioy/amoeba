package com.meidusa.amoeba.oracle.net.packet;

import java.io.UnsupportedEncodingException;

import com.meidusa.amoeba.net.packet.AbstractPacketBuffer;

/**
 * @author hexianmao
 * @version 2008-8-11 ����04:18:34
 */
public abstract class DataPacket extends AbstractPacket {

    protected int dataFlags;

    public DataPacket(){
        super(NS_PACKT_TYPE_DATA);
    }

    /**
     * true,��ʾ�����������������������ر����ӡ����ͻ��˷��������ݰ���
     */
    public static boolean isDataEOF(byte[] buffer) {
        int dataFlags = ((buffer[8] & 0xff) << 8) | (buffer[9] & 0xff);
        if ((dataFlags & 0x40) == 0x40) {
            return true;
        }
        return false;
    }

    /**
     * true����ʾһ�����ݰ��ѽ��������ͻ��˷��������ݰ���
     */
    public static boolean isPacketEOF(byte[] buffer) {
        int dataFlags = ((buffer[8] & 0xff) << 8) | (buffer[9] & 0xff);
        if (dataFlags == 0 || isDataEOF(buffer)) {
            return true;
        }
        return false;
    }

    /**
     * �����ݰ���ǳ�δ���������ѽ�����flag:true��ʾ�ѽ����������ݰ�δ������
     */
    public static void setPacketEOF(byte[] buffer, boolean flag) {
        if (flag) {
            buffer[8] = 0x00;
            buffer[9] = 0x00;
        } else {
            buffer[8] = 0x00;
            buffer[9] = 0x20;
        }
    }

    /**
     * true,��ʾһ�����ݰ���δ��ɣ��ȴ���ȡ��һ������������ͻ��˷��������ݰ���
     */
    public static boolean hasNext(byte[] buffer) {
        int dataFlags = ((buffer[8] & 0xff) << 8) | (buffer[9] & 0xff);
        if ((dataFlags & 0x20) == 0x20) {
            return true;
        }
        return false;
    }

    /**
     * true,��ʾ�����������������������ر����ӡ����ͻ��˷��������ݰ���
     */
    public boolean isDataEOF() {
        if ((dataFlags & 0x40) == 0x40) {
            return true;
        }
        return false;
    }

    /**
     * true����ʾһ�����ݰ�����ɡ����ͻ��˷��������ݰ���
     */
    public boolean isPacketEOF() {
        if (dataFlags == 0 || isDataEOF()) {
            return true;
        }
        return false;
    }

    /**
     * true,��ʾһ�����ݰ���δ��ɣ��ȴ���ȡ��һ������������ͻ��˷��������ݰ���
     */
    public boolean hasNext() {
        if ((dataFlags & 0x20) == 0x20) {
            return true;
        }
        return false;
    }

    @Override
    protected void init(AbstractPacketBuffer buffer) {
        super.init(buffer);
        OracleAbstractPacketBuffer oasbbuffer = (OracleAbstractPacketBuffer) buffer;
        dataFlags = oasbbuffer.readUB2();
    }

    @Override
    protected void write2Buffer(AbstractPacketBuffer buffer) throws UnsupportedEncodingException {
        super.write2Buffer(buffer);
        OracleAbstractPacketBuffer oasbbuffer = (OracleAbstractPacketBuffer) buffer;
        oasbbuffer.writeUB2(dataFlags);
    }

    public static boolean isDataType(byte[] buffer) {
        if (buffer != null && buffer.length >= DATA_PACKET_HEADER_SIZE) {
            return (buffer[4] & 0xff) == NS_PACKT_TYPE_DATA;
        } else {
            return false;
        }
    }

}
