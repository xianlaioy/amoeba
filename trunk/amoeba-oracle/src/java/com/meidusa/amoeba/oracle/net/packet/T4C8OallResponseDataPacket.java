package com.meidusa.amoeba.oracle.net.packet;

import com.meidusa.amoeba.net.packet.AbstractPacketBuffer;
import com.meidusa.amoeba.oracle.accessor.Accessor;
import com.meidusa.amoeba.oracle.handler.OracleQueryMessageHandler;
import com.meidusa.amoeba.oracle.net.packet.assist.T4C8TTIrxh;
import com.meidusa.amoeba.oracle.net.packet.assist.T4CTTIdcb;
import com.meidusa.amoeba.oracle.net.packet.assist.T4CTTIoer;
import com.meidusa.amoeba.oracle.net.packet.assist.T4CTTIrxd;

/**
 * @author hexianmao
 * @version 2008-9-12 ����10:54:51
 */
public class T4C8OallResponseDataPacket extends DataPacket {

    int                               cursor;
    long                              rowsProcessed;
    int                               receiveState;

    T4CTTIoer                         oer = new T4CTTIoer();
    T4C8TTIrxh                        rxh = new T4C8TTIrxh();
    T4CTTIrxd                         rxd = new T4CTTIrxd();
    T4CTTIdcb                         dcb = new T4CTTIdcb();

    Accessor[]                        definesAccessors;

    private OracleQueryMessageHandler handler;

    public T4C8OallResponseDataPacket(OracleQueryMessageHandler handler){
        this.handler = handler;
    }

    public T4C8OallResponseDataPacket(){
    }

    @Override
    protected void init(AbstractPacketBuffer buffer) {
        super.init(buffer);
        T4CPacketBuffer meg = (T4CPacketBuffer) buffer;
        boolean flag = false;
        while (true) {
            byte byte0 = meg.unmarshalSB1();
            switch (byte0) {
                case 4:// ��ʾ���ݽ������ؽ������
                    oer.init();
                    oer.unmarshal(meg);
                    cursor = oer.currCursorID;
                    rowsProcessed = oer.curRowNumber;
                    if (oer.retCode != 1403) {
                        try {
                            // TODO
                            // oer.processError(oracleStatement);
                        } catch (Exception e) {
                            receiveState = 0;
                        }
                    }

                    // if (receiveState != 1) {
                    // throw new RuntimeException("OALL8 ���ڲ�һ��״̬");
                    // }
                    receiveState = 0;
                    return;
                case 6:
                    rxh.init();
                    rxh.unmarshalV10(rxd, meg);
                    if (rxh.uacBufLength > 0) {
                        throw new RuntimeException("��Ч��������");
                    }
                    break;
                case 7:
                    // if (receiveState != 1) {
                    // throw new RuntimeException("OALL8 ���ڲ�һ��״̬");
                    // }
                    receiveState = 2;
                    for (int k = 0; k < rxh.numRqsts; k++) {
                        meg.unmarshalCLRforREFS();
                    }
                    break;
                case 8:// _L5
                    if (flag) {
                        throw new RuntimeException("protocol error");
                    }
                    int j = meg.unmarshalUB2();
                    int ai[] = new int[j];
                    for (int l = 0; l < j; l++) {
                        ai[l] = (int) meg.unmarshalUB4();
                    }
                    cursor = ai[2];
                    meg.unmarshalUB2();
                    int i1 = meg.unmarshalUB2();
                    if (i1 > 0) {
                        for (int k1 = 0; k1 < i1; k1++) {
                            meg.unmarshalUB4();
                            meg.unmarshalDALC();
                            meg.unmarshalUB2();
                        }
                    }
                    flag = true;
                    break;
                case 11:// _L6
                    // T4CTTIiov t4cttiiov = new T4CTTIiov(meg, rxh, rxd);
                    // t4cttiiov.init();
                    // t4cttiiov.unmarshalV10();
                    // if (oracleStatement.returnParamAccessors == null && !t4cttiiov.isIOVectorEmpty()) {
                    // byte abyte0[] = t4cttiiov.getIOVector();
                    // outBindAccessors = t4cttiiov.processRXD(outBindAccessors, numberOfBindPositions, bindBytes,
                    // bindChars, bindIndicators, bindIndicatorSubRange, conversion, tmpBindsByteArray, abyte0,
                    // parameterStream, parameterDatum, parameterOtype, oracleStatement, null, null, null);
                    // }
                    // flag3 = true;
                    break;
                case 16:// ��ʾ��ѯ�ֶε�����
                    dcb.init(0);
                    definesAccessors = dcb.receive(definesAccessors, meg);
                    // numberOfDefinePositions = dcb.numuds;
                    // definesLength = numberOfDefinePositions;
                    // rxd.setNumberOfColumns(numberOfDefinePositions);
                    break;
                case 19:// _L8
                    // fob.marshal();
                    break;
                case 21:// _L9
                    // int i = meg.unmarshalUB2();
                    // rxd.unmarshalBVC(i);
                    break;
                default:
                    // System.err.println("protocol error");
                    throw new RuntimeException("protocol error");
            }

            // meg.sentCancel = false;
            // meg.pipeState = -1;

        }
    }

    @Override
    protected Class<? extends AbstractPacketBuffer> getPacketBufferClass() {
        return T4CPacketBuffer.class;
    }

    // ///////////////////////////////////////////////////////////////////////////////////

    public static boolean isParseable(byte[] message) {
        return true;
    }

    public static boolean isPacketEOF(byte[] buffer) {
        int i = 0;
        switch (i) {
            case 4:
                // ...
        }
        return true;
    }

}
