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
package com.meidusa.amoeba.net;

import java.io.IOException;
import java.nio.channels.SelectionKey;

/**
 * net Event handler
 * @author <a href=mailto:piratebase@sina.com>Struct chen</a>
 *
 */
public interface NetEventHandler {
	
	/**
	 * ��ʱhandler��Ҫ���� when ʱ�� �� handle �������¼���
	 * 
	 * @param when
	 * @return
	 */
	public int handleEvent (long when);
	
	/**
	 * ����Ƿ��Լ�����idle���������true������Ҫ�ر�.
	 * @param now
	 * @return
	 */
	public boolean checkIdle (long now);
	
	public SelectionKey getSelectionKey();
	
	public void setSelectionKey(SelectionKey selkey);
	
	public boolean doWrite() throws IOException;
}
