package com.meidusa.amoeba.oracle.packet;

import java.io.UnsupportedEncodingException;

import com.meidusa.amoeba.packet.AbstractPacketBuffer;

/**
 * ���ݿ�汾��Ϣ���ݰ�
 * 
 * @author hexianmao
 * @version 2008-8-14 ����07:32:33
 */
public class T4C7OversionResponseDataPacket extends DataPacket {

	public String rdbmsVersion= "Oracle9i Enterprise Edition Release 9.2.0.6.0 - Production \n With the Partitioning option \n JServer Release 9.2.0.6.0 - Production";
	public long                        retVerNum = 153093632L;
    
    @Override
    protected void init(AbstractPacketBuffer buffer) {
        super.init(buffer);
        T4CPacketBuffer meg = (T4CPacketBuffer)buffer;
        boolean flag = false;
        T4CTTIoer oer = new T4CTTIoer(meg);
        while (true) {
            byte byte0 = meg.unmarshalSB1();
            switch (byte0) {
                case 4:
                    oer.init();
                    oer.unmarshal();
                    //oer.processError();
                    break;
                case 8:
                    if (flag){
                        //DatabaseError.throwSqlException(401);
                    }
                    int retVerLen = meg.unmarshalUB2();
                    byte[] vers = meg.unmarshalCHR(retVerLen);
                    try {
						rdbmsVersion = new String(vers,"UTF8");
					} catch (UnsupportedEncodingException e) {
						rdbmsVersion = new String(vers);
					}
                    if (rdbmsVersion == null){
                        //DatabaseError.throwSqlException(438);
                    }
                    retVerNum = meg.unmarshalUB4();
                    T4CPacketBuffer.versionNumber = getVersionNumber();
                    flag = true;
                    continue;
                case 9:
                    if (getVersionNumber() >= 10000) {
                        short word0 = (short) meg.unmarshalUB2();
                        //connection.endToEndECIDSequenceNumber = word0;
                    }
                    break;
                default:{
                    //DatabaseError.throwSqlException(401);
                }
            }
            break;
        }
    }

    @Override
    protected void write2Buffer(AbstractPacketBuffer buffer) throws UnsupportedEncodingException {
        super.write2Buffer(buffer);
        T4CPacketBuffer meg = (T4CPacketBuffer)buffer;
        meg.marshalUB1((byte)8);
        meg.marshalUB2(rdbmsVersion.length());
        meg.marshalCHR(rdbmsVersion.getBytes());
        meg.marshalUB4(retVerNum);
        meg.marshalUB1((byte)9);
        
    }
    
    short getVersionNumber() {
        int i = 0;
        i = (int) ((long) i + (retVerNum >>> 24 & 255L) * 1000L);
        i = (int) ((long) i + (retVerNum >>> 20 & 15L) * 100L);
        i = (int) ((long) i + (retVerNum >>> 12 & 15L) * 10L);
        i = (int) ((long) i + (retVerNum >>> 8 & 15L));
        return (short) i;
    	//return 9260;
    }

}
