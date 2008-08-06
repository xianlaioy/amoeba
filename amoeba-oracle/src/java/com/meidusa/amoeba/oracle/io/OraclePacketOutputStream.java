package com.meidusa.amoeba.oracle.io;

import java.nio.ByteBuffer;

import com.meidusa.amoeba.net.io.PacketOutputStream;

public class OraclePacketOutputStream extends PacketOutputStream implements OraclePacketConstant{

	/**
	 * 
	 * @param packetwrittenWithHead д���ݵ�ʱ���Ƿ�д���ͷ��Ϣ��true--��ʾд�����ݵ�ʱ���Ѿ������˰�ͷ��Ϣ��
	 * 							   ��������Ҫ�ڵ���{@link #returnPacketBuffer()} ��ʱ����Ҫʵʱ���ɰ�ͷ��Ϣ
	 */
	public OraclePacketOutputStream(boolean packetwrittenWithHead){
		this.packetwrittenWithHead = packetwrittenWithHead;
		resetPacket();
	}
	
	protected boolean packetwrittenWithHead;
	

	public ByteBuffer returnPacketBuffer ()
    {
        // flip the buffer which will limit it to it's current position
        _buffer.flip();
        if(!packetwrittenWithHead){
	        /**
	         *  ��ͷ��Ϣ�����ȣ����ǲ�������ͷ����
	         */
	        /*int count = _buffer.limit()-HEADER_PAD.length;
	        _buffer.put((byte)(count & 0xff));
	        _buffer.put((byte) (count >>> 8));
	        _buffer.put((byte) (count >>> 16));*/
        	//TODO дһЩ��ͷ��ص���Ϣ��Ŀǰʹ��״̬����Ҫ��δ���
        }
        _buffer.rewind();
        return _buffer;
    }
	/**
	 * 
	 */
	protected void initHeader(){
		if(!packetwrittenWithHead){
			_buffer.put(HEADER_PAD);
		}
    }

}
