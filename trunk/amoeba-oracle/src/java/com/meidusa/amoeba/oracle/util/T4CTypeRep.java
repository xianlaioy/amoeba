package com.meidusa.amoeba.oracle.util;

import java.sql.SQLException;

class T4CTypeRep {

    short             oVersion;
    static final byte PRO       = 1;
    static final byte DTY       = 2;
    static final byte RXH       = 3;
    static final byte UDS       = 4;
    static final byte OAC       = 5;
    static final byte DSC       = 1;
    static final byte DTYDTY    = 20;
    static final byte DTYRXH8   = 21;
    static final byte DTYUDS8   = 22;
    static final byte DTYOAC8   = 23;
    static final byte NATIVE    = 0;
    static final byte UNIVERSAL = 1;
    static final byte LSB       = 2;
    static final byte MAXREP    = 3;
    static final byte B1        = 0;
    static final byte B2        = 1;
    static final byte B4        = 2;
    static final byte B8        = 3;
    static final byte PTR       = 4;
    static final byte MAXTYPE   = 4;
    byte              rep[];
    final byte        NUMREPS   = 5;
    byte              conversionFlags;
    boolean           serverConversion;

    T4CTypeRep(){
        conversionFlags = 0;
        serverConversion = false;
        rep = new byte[NUMREPS];
        rep[0] = 0;
        rep[1] = 1;
        rep[2] = 1;
        rep[3] = 1;
        rep[4] = 1;
    }

    void setRep(byte byte0, byte byte1) throws SQLException {
        if (byte0 < 0 || byte0 > 4 || byte1 > 3) {
            throw new RuntimeException("��Ч�����ͱ�ʾ");
        }
        rep[byte0] = byte1;
    }

    byte getRep(byte byte0) throws SQLException {
        if (byte0 < 0 || byte0 > 4) {
            throw new RuntimeException("��Ч�����ͱ�ʾ");
        }
        return rep[byte0];
    }

    void setFlags(byte byte0) {
        conversionFlags = byte0;
    }

    byte getFlags() {
        return conversionFlags;
    }

    boolean isConvNeeded() {
        boolean flag = (conversionFlags & 2) > 0;
        return flag;
    }

    void setServerConversion(boolean flag) {
        serverConversion = flag;
    }

    boolean isServerConversion() {
        return serverConversion;
    }

    void setVersion(short word0) {
        oVersion = word0;
    }

    short getVersion() {
        return oVersion;
    }

}
