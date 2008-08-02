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

import com.meidusa.amoeba.mysql.io.MySqlPacketConstant;

/**
 * 
 * @author <a href=mailto:piratebase@sina.com>Struct chen</a>
 *
 */
public interface Packet extends MySqlPacketConstant,com.meidusa.amoeba.packet.Packet{
	
	/**
	 * ��buffer(����ͷ) �г�ʼ�����ݰ���
	 * @param buffer buffer�Ǵ�mysql socketChannel������ȡͷ4���ֽڼ������ݰ�����
	 * 				���Ҷ�ȡ��Ӧ�ĳ������γɵ�buffer
	 */
	public void init(PacketBuffer buffer);
	
	/**
	 * ֱ��ת����buffer����
	 * @return Buffer ����
	 * @throws UnsupportedEncodingException  ��String to bytes�������벻֧�ֵ�ʱ��
	 */
	public PacketBuffer toBuffer()throws UnsupportedEncodingException;
	
	/**
	 * ����packetд�뵽buffer�� 
	 * @param buffer ������������Ļ���
	 * @throws UnsupportedEncodingException ��String to bytes�������벻֧�ֵ�ʱ��
	 */
	public PacketBuffer toBuffer(PacketBuffer buffer)throws UnsupportedEncodingException;
}
