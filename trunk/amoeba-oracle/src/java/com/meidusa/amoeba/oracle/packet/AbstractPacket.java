package com.meidusa.amoeba.oracle.packet;

import java.nio.ByteBuffer;


/**
 * 
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
 * 
 * @author struct
 *
 */
public class AbstractPacket implements Packet, SQLnetDef {
	
	protected byte[] header;
	protected byte buffer[];
	protected int length;
	protected byte type;
	protected byte flags;
	protected int dataLen;
    protected int dataOff;
    protected int packetCheckSum;
    protected int headerCheckSum;
	public void init(byte[] buffer) {
		this.buffer =  buffer;
		header = new byte[8];
		length = header[0] & 0xff;
        length <<= 8;
        length |= header[1] & 0xff;
        type = header[4];
        flags = header[5];
	}

	public ByteBuffer toByteBuffer() {
		if(buffer == null){
			
		}
		return null;
	}

}
