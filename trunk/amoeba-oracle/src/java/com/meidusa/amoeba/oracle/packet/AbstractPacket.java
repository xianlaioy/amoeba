package com.meidusa.amoeba.oracle.packet;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.meidusa.amoeba.oracle.io.OraclePacketConstant;

/**
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
 * @author struct
 */
public class AbstractPacket implements Packet,OraclePacketConstant {

    protected int  length;
    protected byte type;
    protected byte flags;
    protected int  dataLen;
    protected int  dataOff;
    protected int  packetCheckSum;
    protected int  headerCheckSum;

    public void init(byte[] buffer) {
        init(new AnoPacketBuffer(buffer));
    }
    
    protected void init(AnoPacketBuffer buffer){
    	length = buffer.readUB2();
    	packetCheckSum = buffer.readUB2();
    	type = (byte)(buffer.readUB1()& 0xff);
    	flags = (byte)(buffer.readUB1()& 0xff);
    	headerCheckSum = buffer.readUB2();
    }

    /**
	 * �����ݰ�ת����ByteBuffer
	 * @return
	 */
	public ByteBuffer toByteBuffer(){
		try {
			return toBuffer().toByteBuffer();
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}

    /**
	 * д��֮��һ����Ҫ�������������buffer��ָ��λ��ָ��ĩβ����һ��λ�ã����ܳ���λ�ã���
	 * @param buffer
	 */
	protected void afterPacketWritten(AnoPacketBuffer buffer){
		int position = buffer.getPosition();
		int packetLength = position;
		buffer.setPosition(0);
		buffer.writeByte((byte)(packetLength/ 256));
		buffer.writeByte((byte)(packetLength % 256));
		buffer.setPosition(4);
		buffer.writeByte(type);
		buffer.writeByte(flags);
		buffer.setPosition(position);
	}
	
	/**
	 * @param buffer ������������Ļ���
	 * @throws UnsupportedEncodingException ��String to bytes�������벻֧�ֵ�ʱ��
	 */
	protected void write2Buffer(AnoPacketBuffer buffer) throws UnsupportedEncodingException {
		buffer.writeUB2(length);
		buffer.writeUB2(packetCheckSum);
		buffer.writeUB1(type);
		buffer.writeUB1(flags);
		buffer.writeUB2(headerCheckSum);
	}

	/**
	 * �÷���������{@link #write2Buffer(PacketBuffer)} д�뵽ָ����buffer�����ҵ�����{@link #afterPacketWritten(PacketBuffer)}
	 */
	public AnoPacketBuffer toBuffer(AnoPacketBuffer buffer) throws UnsupportedEncodingException {
		write2Buffer(buffer);
		afterPacketWritten(buffer);
		return buffer;
	}
	
	public AnoPacketBuffer toBuffer() throws UnsupportedEncodingException{
		int bufferSize = calculatePacketSize();
		bufferSize = (bufferSize<5?5:bufferSize);
		AnoPacketBuffer buffer = new AnoPacketBuffer(bufferSize);
		return toBuffer(buffer);
	}
	
	/**
	 * ����packet�Ĵ�С�������̫���˷��ڴ棬�����̫С��Ӱ������
	 * @return
	 */
	protected int calculatePacketSize(){
		return DATA_OFFSET + 1;
	}
	
	public String toString(){
		return ToStringBuilder.reflectionToString(this);
	}
	
	
	public  Object clone(){
		try {
			return (AbstractPacket)super.clone();
		} catch (CloneNotSupportedException e) {
			//�߼����治�ᷢ����֧�����
			return null;
		}
	}
}
