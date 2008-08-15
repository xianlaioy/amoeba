package com.meidusa.amoeba.oracle.packet;

import java.io.UnsupportedEncodingException;

import com.meidusa.amoeba.packet.AbstractPacketBuffer;

/**
 * Э�����ݰ�
 * 
 * @author hexianmao
 * @version 2008-8-14 ����07:29:53
 */
public class T4C8TTIproClientDataPacket extends T4CTTIMsgDataPacket {

    byte    proSvrVer        = 6;
    short   oVersion         = -1;
    byte[]  proSvrStr        = "Linuxi386/Linux-2.0.34-8.1.0".getBytes();
    short   svrCharSet       = 852;
    byte    svrFlags         = 1;
    short   svrCharSetElem   = 0;
    boolean svrInfoAvailable = false;
    short   NCHAR_CHARSET    = 0;

    byte[]  proCliVerTTC8    = new byte[7];
    byte[]  proCliStrTTC8    = new byte[15];

    protected void init(AbstractPacketBuffer absbuffer) {
        super.init(absbuffer);
        if (msgCode != TTIPRO) {
            throw new RuntimeException("Υ��Э��");
        }
        T4CPacketBuffer meg = (T4CPacketBuffer) absbuffer;
        meg.readBytes(proCliVerTTC8, 0, proCliVerTTC8.length);
        meg.readBytes(proCliStrTTC8, 0, proCliStrTTC8.length);

        // proCliVerTTC8 proSvrVer = buffer.unmarshalSB1();
        // switch (proSvrVer) {
        // case 4:
        // oVersion = MIN_OVERSION_SUPPORTED;
        // break;
        // case 5:
        // oVersion = ORACLE8_PROD_VERSION;
        // break;
        // case 6:
        // oVersion = ORACLE81_PROD_VERSION;
        // break;
        // default:
        // throw new RuntimeException("��֧�ִӷ��������յ��� TTC Э��汾");
        // }
        // buffer.unmarshalSB1();
        // proSvrStr = buffer.unmarshalTEXT(50);
        // svrCharSet = (short) buffer.unmarshalUB2();
        // svrFlags = (byte) buffer.unmarshalUB1();
        // svrCharSetElem = (short) buffer.unmarshalUB2();
        // if (svrCharSetElem > 0) {
        // buffer.unmarshalNBytes(svrCharSetElem * 5);
        // }
        // svrInfoAvailable = true;
        // if (proSvrVer < 5) {
        // return;
        // }
        //
        // byte byte0 = buffer.getRep((byte) 1);
        // buffer.setRep((byte) 1, (byte) 0);
        // int i = buffer.unmarshalUB2();
        // buffer.setRep((byte) 1, byte0);
        // byte[] abyte0 = buffer.unmarshalNBytes(i);
        // int j = 6 + (abyte0[5] & 0xff) + (abyte0[6] & 0xff);
        // NCHAR_CHARSET = (short) ((abyte0[j + 3] & 0xff) << 8);
        // NCHAR_CHARSET |= (short) (abyte0[j + 4] & 0xff);
        // if (proSvrVer < 6) {
        // return;
        // }
        //
        // short word0 = buffer.unmarshalUB1();
        // for (int k = 0; k < word0; k++) {
        // buffer.unmarshalUB1();
        // }
        // word0 = buffer.unmarshalUB1();
        // for (int l = 0; l < word0; l++) {
        // buffer.unmarshalUB1();
        // }
    }

    @Override
    protected void write2Buffer(AbstractPacketBuffer absbuffer) throws UnsupportedEncodingException {
        msgCode = TTIPRO;
        super.write2Buffer(absbuffer);
        T4CPacketBuffer meg = (T4CPacketBuffer) absbuffer;
        meg.writeByte(proSvrVer);
        meg.marshalNULLPTR();
        meg.writeBytes(proSvrStr);
        meg.marshalNULLPTR();
        meg.marshalUB2(svrCharSet);
        meg.marshalUB1(svrFlags);
        meg.marshalUB1(svrCharSetElem);

        // buffer.writeBytes(proCliVerTTC8);
        // buffer.writeBytes(proCliStrTTC8);
    }

    @Override
    protected Class<? extends AbstractPacketBuffer> getBufferClass() {
        return T4CPacketBuffer.class;
    }

}
