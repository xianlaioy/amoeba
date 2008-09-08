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
package com.meidusa.amoeba.manager.io;

import com.meidusa.amoeba.manager.ManagerConstant;
import com.meidusa.amoeba.net.io.PacketInputStream;

/**
 * 
 * <b> The Packet Header </b>
 * 
 * <pre>
 * Bytes                 Name
 *  -----                 ----
 *  3                     Packet Length
 *  1                     Packet TYPE
 * </p>
 * 
 * ���ݰ�Packet Length Ϊ �������ݰ�����(�������ݣ�header)
 * @author <a href=mailto:piratebase@sina.com>Struct chen</a>
 * 
 */
public class ManagerPacketInputStream extends PacketInputStream implements ManagerConstant{

	private boolean readPackedWithHead;
	
	public ManagerPacketInputStream(boolean readPackedWithHead){
		this.readPackedWithHead = readPackedWithHead;
	}
	protected int decodeLength() {
		
		/**
		 * �ж�һ�����ǵ�ǰ�Ѿ���ȡ�����ݰ��������Ƿ�Ȱ�ͷ��,�����:����Լ����������ĳ���,���򷵻�-1
		 */
		if (_have < getHeaderSize()) {
			return -1;
		}

		_buffer.rewind();
		
		/**
		 * manager ���ݲ��֣���ͷ=�������ݰ�����
		 */
		int length = (_buffer.get() & 0xff)
					+ ((_buffer.get() & 0xff) << 8)	
					+ ((_buffer.get() & 0xff) << 16);
					
		_buffer.position(_have);
		return length;
	}

	public int getHeaderSize() {
		return HEADER_SIZE;
	}
	
	protected boolean checkForCompletePacket ()
    {
        if (_length == -1 || _have < _length) {
            return false;
        }
        //��buffer �����������ݰ���������ͷ����
        if(readPackedWithHead){
        	_buffer.position(0);
        }else{
        	_buffer.position(this.getHeaderSize());
        }
        _buffer.limit(_length);
        return true;
    }
}