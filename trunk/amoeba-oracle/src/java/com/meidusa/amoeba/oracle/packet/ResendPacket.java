package com.meidusa.amoeba.oracle.packet;

import org.apache.log4j.Logger;

/**
 * 服务器端返回的要求客户端重发的数据包
 * 
 * @author hexianmao
 * @version 2008-8-6 下午05:32:52
 */
public class ResendPacket extends AbstractPacket {

    private static Logger logger = Logger.getLogger(ResendPacket.class);

    public void init(byte[] buffer) {
        super.init(buffer);

        if (logger.isDebugEnabled()) {
            logger.debug(this.toString());
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("ResendPacket info ==============================\n");
        return sb.toString();
    }
}
