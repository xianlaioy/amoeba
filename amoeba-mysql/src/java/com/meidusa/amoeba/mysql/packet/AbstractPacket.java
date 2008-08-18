/*
 * 	This program is free software; you can redistribute it and/or modify it under the terms of 
 * the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, 
 * or (at your option) any later version. 
 * 
 * 	This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details. 
 * 	You should have received a copy of the GNU General Public License along with this program; 
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.meidusa.amoeba.mysql.packet;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.log4j.Logger;

import com.meidusa.amoeba.net.Connection;

/**
 * 
 * @author <a href=mailto:piratebase@sina.com>Struct chen</a>
 *
 */
public abstract class AbstractPacket implements Packet {
	protected static Logger logger = Logger.getLogger(AbstractPacket.class);
	/**
	 * ֻ��ʾ���ݳ��ȣ���������ͷ����
	 */
	public int packetLength;
	
	/**
	 * ��ǰ�����ݰ�������
	 */
	public byte packetId;
	
	/**
	 * ��buffer(����ͷ) �г�ʼ�����ݰ���
	 * @param buffer buffer�Ǵ�mysql socketChannel������ȡͷ4���ֽڼ������ݰ�����
	 * 				���Ҷ�ȡ��Ӧ�ĳ������γɵ�buffer
	 */
	public void init(byte[] buffer,Connection conn){
		MysqlPacketBuffer packetBuffer = new MysqlPacketBuffer(buffer);
		packetBuffer.init(conn);
		init(packetBuffer);
	}
	
	/**
	 * �����ݰ�ת����ByteBuffer
	 * @return
	 */
	public ByteBuffer toByteBuffer(Connection conn){
		try {
			return toBuffer(conn).toByteBuffer();
		} catch (UnsupportedEncodingException e) {
			return null;
		}
	}
	
	public void init(MysqlPacketBuffer buffer) {
		buffer.setPosition(0);
		packetLength = (buffer.readByte() & 0xff)	
		+ ((buffer.readByte() & 0xff) << 8)	
		+ ((buffer.readByte() & 0xff) << 16);
		packetId = buffer.readByte();
	}

	/**
	 * д��֮��һ����Ҫ�������������buffer��ָ��λ��ָ��ĩβ����һ��λ�ã����ܳ���λ�ã���
	 * @param buffer
	 */
	protected void afterPacketWritten(MysqlPacketBuffer buffer){
		int position = buffer.getPosition();
		packetLength = position-HEADER_SIZE;
		buffer.setPosition(0);
		buffer.writeByte((byte)(packetLength & 0xff));
		buffer.writeByte((byte) (packetLength >>> 8));
		buffer.writeByte((byte) (packetLength >>> 16));
		buffer.writeByte((byte) packetId);// packet id
		buffer.setPosition(position);
	}
	
	/**
	 * ����packetд�뵽buffer�У���buffer�а���4���ֽڵİ�ͷ��д���Ժ󽫼���buffer��ͷֵ
	 * @param buffer ������������Ļ���
	 * @throws UnsupportedEncodingException ��String to bytes�������벻֧�ֵ�ʱ��
	 */
	protected void write2Buffer(MysqlPacketBuffer buffer) throws UnsupportedEncodingException {
		
	}

	/**
	 * �÷���������{@link #write2Buffer(MysqlPacketBuffer)} д�뵽ָ����buffer�����ҵ�����{@link #afterPacketWritten(MysqlPacketBuffer)}
	 */
	public MysqlPacketBuffer toBuffer(MysqlPacketBuffer buffer) throws UnsupportedEncodingException {
		write2Buffer(buffer);
		afterPacketWritten(buffer);
		return buffer;
	}
	
	private MysqlPacketBuffer toBuffer(Connection conn) throws UnsupportedEncodingException{
		int bufferSize = calculatePacketSize();
		bufferSize = (bufferSize<5?5:bufferSize);
		MysqlPacketBuffer buffer = new MysqlPacketBuffer(bufferSize);
		buffer.init(conn);
		return toBuffer(buffer);
	}
	
	/**
	 * ����packet�Ĵ�С�������̫���˷��ڴ棬�����̫С��Ӱ������
	 * @return
	 */
	protected int calculatePacketSize(){
		return HEADER_SIZE + 1;
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
