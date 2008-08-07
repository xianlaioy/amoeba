package com.meidusa.amoeba.oracle.packet;

import org.apache.log4j.Logger;

public class AnoServiceDataPacket extends DataPacket {

    private static Logger logger = Logger.getLogger(AnoServiceDataPacket.class);

    @Override
    public void init(byte[] buffer) {
        super.init(buffer);

        if (logger.isDebugEnabled()) {
            logger.debug(this.toString());
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("AnoServiceDataPacket info ==============================\n");
        return sb.toString();
    }

}
