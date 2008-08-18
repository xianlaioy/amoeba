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
package com.meidusa.amoeba.net.packet;

import java.nio.ByteBuffer;

import com.meidusa.amoeba.net.Connection;

/**
 * 
 * @author <a href=mailto:piratebase@sina.com>Struct chen</a>
 *
 */
public interface Packet extends Cloneable{
	
	/**
	 * ��buffer(����ͷ) �г�ʼ�����ݰ���
	 * @param buffer buffer�Ǵ�socketChannel������ȡͷn���ֽڼ������ݰ�����
	 * 				���Ҷ�ȡ��Ӧ�ĳ������γɵ�buffer
	 */
	public void init(byte[] buffer,Connection conn);
	
	
	/**
	 * �����ݰ�ת����ByteBuffer,byteBuffer�а����а�ͷ��Ϣ
	 * @return
	 */
	public ByteBuffer toByteBuffer(Connection conn);
}
