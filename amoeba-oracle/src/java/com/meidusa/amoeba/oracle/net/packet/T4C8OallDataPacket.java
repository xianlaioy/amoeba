package com.meidusa.amoeba.oracle.net.packet;

import com.meidusa.amoeba.net.packet.AbstractPacketBuffer;
import com.meidusa.amoeba.oracle.accessor.Accessor;
import com.meidusa.amoeba.oracle.accessor.T4CCharAccessor;
import com.meidusa.amoeba.oracle.accessor.T4CDateAccessor;
import com.meidusa.amoeba.oracle.accessor.T4CVarcharAccessor;
import com.meidusa.amoeba.oracle.accessor.T4CVarnumAccessor;

/**
 * @author hexianmao
 * @version 2008-8-20 ����03:02:04
 */
public class T4C8OallDataPacket extends T4CTTIfunPacket {

    long                   options;
    int                    cursor;
    public int             sqlStmtLength;
    public int             numberOfBindPositions;
    public byte[][]        bindParams;
    int                    defCols;

    public byte[]          sqlStmt;
    final long[]           al8i4 = new long[13];
    public T4CTTIoac[]     oacdefBindsSent;
    T4CTTIoac[]            oacdefDefines;
    Accessor[]             definesAccessors;

    int                    receiveState;
    boolean                plsql;

    T4CTTIrxdDataPacket    rxd;
    T4C8TTIrxhDataPacket   rxh;
    T4CTTIoac              oac;
    T4CTTIdcbDataPacket    dcb;
    T4CTTIofetchDataPacket ofetch;
    T4CTTIoexecDataPacket  oexec;
    T4CTTIfobDataPacket    fob;

    static byte[][]        desc  = { { (byte) 0x06, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x16, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x00 }, { (byte) 0x01, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x05, (byte) 0x00, (byte) 0x01, (byte) 0x10, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x00 }, { (byte) 0x06, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x16, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x00 }, { (byte) 0x01, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x06, (byte) 0x00, (byte) 0x01, (byte) 0x10, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x00 }, { (byte) 0x01, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x0b, (byte) 0x00, (byte) 0x01, (byte) 0x10, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x00 }, { (byte) 0x01, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x0c, (byte) 0x00, (byte) 0x01, (byte) 0x10, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x00 }, { (byte) 0x01, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x08, (byte) 0x00, (byte) 0x01, (byte) 0x10, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x00 }, { (byte) 0x01, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x11, (byte) 0x00, (byte) 0x01, (byte) 0x10, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x00 }, { (byte) 0x01, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x04, (byte) 0x00, (byte) 0x01, (byte) 0x10, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x00 }, { (byte) 0x01, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x07, (byte) 0x00, (byte) 0x01, (byte) 0x10, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x00 }, { (byte) 0x01, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x00, (byte) 0x01, (byte) 0x10, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x00 }, { (byte) 0x01, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x00, (byte) 0x01, (byte) 0x10, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x00 }, { (byte) 0x01, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x05, (byte) 0x00, (byte) 0x01, (byte) 0x10, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x00 }, { (byte) 0x0c, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x07, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x00 }, { (byte) 0x01, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x04, (byte) 0x00, (byte) 0x01, (byte) 0x10, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x00 }, { (byte) 0x01, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x09, (byte) 0x00, (byte) 0x01, (byte) 0x10, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x00 }, { (byte) 0x0c, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x07, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x01, (byte) 0x01, (byte) 0x00 } };

    static byte[][]        data  = { { (byte) 0x06, (byte) 0xc5, (byte) 0x02, (byte) 0x17, (byte) 0x2b, (byte) 0x62, (byte) 0x28 }, { (byte) 0x05, (byte) 0x63, (byte) 0x68, (byte) 0x69, (byte) 0x6e, (byte) 0x61 }, { (byte) 0x05, (byte) 0xc4, (byte) 0x02, (byte) 0x04, (byte) 0x29, (byte) 0x39 }, { (byte) 0x06, (byte) 0x65, (byte) 0x78, (byte) 0x70, (byte) 0x69, (byte) 0x72, (byte) 0x65 }, { (byte) 0x0b, (byte) 0x61, (byte) 0x62, (byte) 0x63, (byte) 0x5f, (byte) 0x73, (byte) 0x75, (byte) 0x62, (byte) 0x6a, (byte) 0x65, (byte) 0x63, (byte) 0x74 }, { (byte) 0x0c, (byte) 0x31, (byte) 0x32, (byte) 0x33, (byte) 0x34, (byte) 0x35, (byte) 0x36, (byte) 0x37, (byte) 0x38, (byte) 0x39, (byte) 0x2b, (byte) 0x2b, (byte) 0x70 }, { (byte) 0x08, (byte) 0x68, (byte) 0x7a, (byte) 0x5f, (byte) 0x63, (byte) 0x68, (byte) 0x69, (byte) 0x6e, (byte) 0x61 }, { (byte) 0x11, (byte) 0x68, (byte) 0x65, (byte) 0x78, (byte) 0x69, (byte) 0x61, (byte) 0x6e, (byte) 0x6d, (byte) 0x61, (byte) 0x6f, (byte) 0x40, (byte) 0x31, (byte) 0x36, (byte) 0x33, (byte) 0x2e, (byte) 0x63, (byte) 0x6f, (byte) 0x6d }, { (byte) 0x04, (byte) 0x33, (byte) 0x35, (byte) 0x31, (byte) 0x34 }, { (byte) 0x07, (byte) 0x61, (byte) 0x6c, (byte) 0x69, (byte) 0x62, (byte) 0x61, (byte) 0x62, (byte) 0x61 }, { (byte) 0x02, (byte) 0x43, (byte) 0x4e }, { (byte) 0x02, (byte) 0x68, (byte) 0x65 }, { (byte) 0x05, (byte) 0x67, (byte) 0x6f, (byte) 0x6f, (byte) 0x64, (byte) 0x21 }, { (byte) 0x07, (byte) 0x78, (byte) 0x6c, (byte) 0x07, (byte) 0x1e, (byte) 0x01, (byte) 0x01, (byte) 0x01 }, { (byte) 0x04, (byte) 0x53, (byte) 0x41, (byte) 0x4c, (byte) 0x45 }, { (byte) 0x09, (byte) 0x70, (byte) 0x75, (byte) 0x62, (byte) 0x6c, (byte) 0x69, (byte) 0x73, (byte) 0x68, (byte) 0x65, (byte) 0x64 }, { (byte) 0x07, (byte) 0x78, (byte) 0x6c, (byte) 0x07, (byte) 0x1e, (byte) 0x01, (byte) 0x01, (byte) 0x01 } };

    public T4C8OallDataPacket(){
        super(OALL8);
        this.receiveState = 0;
        this.sqlStmt = new byte[0];
        this.plsql = false;
        this.defCols = 0;
    }

    @Override
    protected void unmarshal(AbstractPacketBuffer buffer) {
        super.unmarshal(buffer);
        if (msgCode == TTIFUN) {
            if (funCode == OFETCH) {
                ofetch = new T4CTTIofetchDataPacket();
                // ofetch.init(buffer);
                this.cursor = ofetch.cursor;
                this.al8i4[1] = ofetch.al8i4_1;
            } else if (funCode == OEXEC) {
                oexec = new T4CTTIoexecDataPacket();
                this.cursor = oexec.cursor;
                this.al8i4[1] = oexec.al8i4_1;
                // int[] binds = null;
                // TODO ...
                throw new RuntimeException("is not yet support");
            } else if (funCode == OALL8) {
                T4CPacketBuffer meg = (T4CPacketBuffer) buffer;

                unmarshalPisdef(meg);

                sqlStmt = meg.unmarshalCHR(sqlStmtLength);

                meg.unmarshalUB4Array(al8i4);

                unmarshalBindsTypes(meg);

                if (meg.versionNumber >= 9000 && defCols > 0) {
                    oacdefDefines = new T4CTTIoac[defCols];
                    for (int i = 0; i < defCols; i++) {
                        oacdefDefines[i] = new T4CTTIoac(meg);
                        oacdefDefines[i].unmarshal();
                    }
                }

                unmarshalBinds(meg);
            } else {
                throw new RuntimeException("Υ��Э��");
            }
        } else if (msgCode == TTIPFN) {

        }
    }

    @SuppressWarnings("unused")
    void unmarshalPisdef(T4CPacketBuffer meg) {
        options = meg.unmarshalUB4();
        cursor = meg.unmarshalSWORD();
        meg.unmarshalPTR();
        sqlStmtLength = meg.unmarshalSWORD();
        meg.unmarshalPTR();
        int al8i4Length = meg.unmarshalSWORD();
        meg.unmarshalPTR();
        meg.unmarshalPTR();
        meg.unmarshalUB4();
        meg.unmarshalUB4();
        long l = meg.unmarshalUB4();

        meg.unmarshalPTR();
        numberOfBindPositions = meg.unmarshalSWORD();

        meg.unmarshalPTR();
        meg.unmarshalPTR();
        meg.unmarshalPTR();
        meg.unmarshalPTR();
        meg.unmarshalPTR();

        if (meg.versionNumber >= 9000) {
            meg.unmarshalPTR();
            defCols = meg.unmarshalSWORD();
        }
    }

    void unmarshalBindsTypes(T4CPacketBuffer meg) {
        if (numberOfBindPositions <= 0) {
            return;
        }
        oacdefBindsSent = new T4CTTIoac[numberOfBindPositions];
        for (int i = 0; i < numberOfBindPositions; i++) {
            oacdefBindsSent[i] = new T4CTTIoac(meg);
            oacdefBindsSent[i].unmarshal();
        }
    }

    void unmarshalBinds(T4CPacketBuffer meg) {
        if (numberOfBindPositions <= 0) {
            return;
        }
        short msgCode = meg.unmarshalUB1();
        if (msgCode == TTIRXD) {
            bindParams = new byte[numberOfBindPositions][];
            for (int i = 0; i < numberOfBindPositions; i++) {
                bindParams[i] = meg.unmarshalCLRforREFS();
            }
        } else {
            throw new RuntimeException();
        }

    }

    static void fillupAccessors() {
        for (int i = 0; i < desc.length; i++) {
            switch (desc[i][0] & 0xff) {
                case Accessor.CHAR:
                    System.out.print("Accessor.CHAR");
                    System.out.println(T4CCharAccessor.getString(data[i], desc[i][5], desc[i][5]));
                    System.out.println();
                    break;

                case Accessor.NUMBER:
                    System.out.print("Accessor.NUMBER");
                    // T4CNumberAccessor
                    System.out.println();
                    break;

                case Accessor.VARCHAR:
                    System.out.print("Accessor.VARCHAR:");
                    System.out.println(T4CVarcharAccessor.getString(data[i], desc[i][5], desc[i][5]));
                    System.out.println();
                    break;

                case Accessor.LONG:
                    System.out.print("Accessor.LONG");
                    // T4CLongAccessor
                    System.out.println();
                    break;

                case Accessor.VARNUM:
                    System.out.print("Accessor.VARNUM:");
                    System.out.println(T4CVarnumAccessor.getLong(data[i]));
                    System.out.println();
                    break;

                case Accessor.BINARY_FLOAT:
                    System.out.print("Accessor.BINARY_FLOAT");
                    // T4CBinaryFloatAccessor
                    System.out.println();
                    break;

                case Accessor.BINARY_DOUBLE:
                    System.out.print("Accessor.BINARY_DOUBLE");
                    // T4CBinaryDoubleAccessor
                    System.out.println();
                    break;

                case Accessor.RAW:
                    System.out.print("Accessor.RAW");
                    // T4CRawAccessor
                    System.out.println();
                    break;

                case Accessor.LONG_RAW:
                    System.out.print("Accessor.LONG_RAW");
                    // T4CPacketBuffer.versionNumber >= 9000 T4CRawAccessor
                    // T4CLongRawAccessor
                    System.out.println();

                    break;

                case Accessor.ROWID:
                    System.out.print("Accessor.ROWID");
                    System.out.println();

                case Accessor.UROWID:
                    System.out.print("Accessor.UROWID");
                    // T4CRowidAccessor
                    System.out.println();
                    break;

                case Accessor.RESULT_SET:
                    System.out.print("Accessor.RESULT_SET");
                    // T4CResultSetAccessor
                    System.out.println();
                    break;

                case Accessor.DATE:
                    System.out.print("Accessor.DATE:");
                    System.out.println(T4CDateAccessor.getDate(data[i]));
                    System.out.println();

                    break;

                case Accessor.BLOB:
                    System.out.print("Accessor.BLOB");
                    // l1 == -4 && T4CPacketBuffer.versionNumber >= 9000 T4CLongRawAccessor
                    // l1 == -3 && T4CPacketBuffer.versionNumber >= 9000 T4CRawAccessor
                    // T4CBlobAccessor
                    System.out.println();
                    break;

                case Accessor.CLOB:
                    System.out.print("Accessor.CLOB");
                    // l1 == -1 && T4CPacketBuffer.versionNumber >= 9000 T4CLongAccessor
                    // (l1 == 12 || l1 == 1) && T4CPacketBuffer.versionNumber >= 9000
                    // T4CVarcharAccessor
                    // T4CClobAccessor
                    System.out.println();
                    break;

                case Accessor.BFILE:
                    System.out.print("Accessor.BFILE");
                    // T4CBfileAccessor
                    System.out.println();
                    break;

                case Accessor.NAMED_TYPE:
                    System.out.print("Accessor.NAMED_TYPE");
                    // T4CNamedTypeAccessor
                    System.out.println();
                    break;

                case Accessor.REF_TYPE:
                    System.out.print("Accessor.REF_TYPE");
                    // T4CRefTypeAccessor
                    System.out.println();
                    break;

                case Accessor.TIMESTAMP:
                    System.out.print("Accessor.TIMESTAMP");
                    // T4CTimestampAccessor
                    System.out.println();
                    break;

                case Accessor.TIMESTAMPTZ:
                    System.out.print("Accessor.TIMESTAMPTZ");
                    // T4CTimestamptzAccessor
                    System.out.println();
                    break;

                case Accessor.TIMESTAMPLTZ:
                    System.out.print("Accessor.TIMESTAMPLTZ");
                    // T4CTimestampltzAccessor
                    System.out.println();
                    break;

                case Accessor.INTERVALYM:
                    System.out.print("Accessor.INTERVALYM");
                    // T4CIntervalymAccessor
                    System.out.println();
                    break;

                case Accessor.INTERVALDS:
                    System.out.print("Accessor.INTERVALDS");
                    // T4CIntervaldsAccessor
                    System.out.println();
                    break;
                default:
                    throw new RuntimeException("unknown data type!");
            }
        }
    }

    public static void main(String[] args) {

        long st = System.currentTimeMillis();
        for (int i = 0; i < 1; i++) {
            fillupAccessors();
        }
        long et = System.currentTimeMillis();

        System.out.println(desc.length + ":" + (et - st) + " ms");
    }

}
