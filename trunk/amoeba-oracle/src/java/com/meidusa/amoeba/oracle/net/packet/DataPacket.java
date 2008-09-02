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
     * true,��ʾ�����������������������ر����ӡ�
     */
    public boolean isDataEOF() {
        if ((dataFlags & 0x40) == 0x40) {
            return true;
        }
        return false;
    }

    /**
     * true����ʾһ�����ݰ�����ɡ�
     */
    public boolean isPacketEOF() {
        if (dataFlags == 0 || isDataEOF()) {
            return true;
        }
        return false;
    }

    /**
     * true,��ʾһ�����ݰ���δ��ɣ��ȴ���ȡ��һ���������
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

}
