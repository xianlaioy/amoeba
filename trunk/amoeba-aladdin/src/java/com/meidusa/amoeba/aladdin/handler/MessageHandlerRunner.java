package com.meidusa.amoeba.aladdin.handler;

import com.meidusa.amoeba.net.MessageHandler;

public interface MessageHandlerRunner extends Runnable,Cloneable {
	
	/**
	 * ��ʼ���ڲ�һЩ����
	 * @param handler
	 */
	public void init(MessageHandler handler);
	
	/**
	 * �������ʼ����handler��صĶ���
	 */
	public void reset();

}
