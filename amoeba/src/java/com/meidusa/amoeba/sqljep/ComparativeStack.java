package com.meidusa.amoeba.sqljep;

import java.util.Stack;

import com.meidusa.amoeba.sqljep.function.Comparative;

/**
 * ����ĿǰֻΪ�ڲ�ϵͳ��� Comparative
 * �����ڵ�һ�ο�ʼpop��ʱ���¼ÿ��pop�����Ķ������ͣ��������pop�Ķ����а����� Comparative����
 * ����һ��push��ʱ�򣬾ͱ�����Comparative��ȥ��Ȼ��������һ��pop�ĺۼ�
 * pop .... pop ... push��Ϊһ��ѭ��
 * @author struct
 *
 * @param <E>
 */
public class ComparativeStack extends Stack<Comparable> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Comparative lastComparative;
	private boolean autoBox = true;
	public boolean isAutoBox() {
		return autoBox;
	}

	public void setAutoBox(boolean autoBox) {
		this.autoBox = autoBox;
	}

	public Comparable<?> push(Comparable<?> item) {
		try{
			if(autoBox && lastComparative != null && !(item instanceof Comparative) ){
				lastComparative.setValue(item);
				return super.push(lastComparative);
			}else{
				return super.push(item);
			}
		}finally{
			if(autoBox){
				lastComparative = null;
			}
		}
		
	}

	public synchronized Comparable<?> pop() {
		Comparable<?> obj =super.pop();
		if(obj instanceof Comparative){
			if(autoBox){
				lastComparative = (Comparative)obj;
				obj = lastComparative.getValue();
			}
		}
		
		return obj;
	}
}
