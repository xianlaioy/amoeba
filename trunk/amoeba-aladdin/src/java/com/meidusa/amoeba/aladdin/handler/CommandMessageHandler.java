package com.meidusa.amoeba.aladdin.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.meidusa.amoeba.context.ProxyRuntimeContext;
import com.meidusa.amoeba.mysql.net.MysqlClientConnection;
import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.MessageHandler;
import com.meidusa.amoeba.net.Sessionable;
import com.meidusa.amoeba.net.poolable.ObjectPool;


/**
 * 
 * @author struct
 *
 */
public class CommandMessageHandler implements MessageHandler, Sessionable {
	protected MysqlClientConnection source;
	private boolean completed;
	private long createTime;
	private long timeout;
	private long endTime;
	private boolean ended = false;
	private ObjectPool[] pools;
	private final Lock lock = new ReentrantLock(false);
	private Map<java.sql.Connection, ObjectPool> connPoolMap = new HashMap<java.sql.Connection, ObjectPool>();
	private String query;
	protected static class QueryRunnable implements Runnable{
		protected java.sql.Connection conn;
		protected Object parameter;
		QueryRunnable(java.sql.Connection conn,Object parameter){
			this.conn = conn;
			this.parameter = conn;
		}

		public void run() {
			
		}
	}
	
	public CommandMessageHandler(MysqlClientConnection source, String query,
			ObjectPool[] pools, long timeout) {
		this.query = query;
		this.pools = pools;
		this.timeout = timeout;
	}

	public void handleMessage(Connection conn, byte[] message) {
		// TODO Auto-generated method stub

	}

	public boolean checkIdle(long now) {
		if (timeout > 0) {
			return (now - createTime) > timeout;
		} else {
			if (ended) {
				/**
				 * �����session�Ѿ���������ʱ���serverConnection�˻��ڵȴ��������ݷ��ʡ����ҳ���15s,
				 * ����Ҫ�����еĻỰ
				 * �������ڸ���ԭ����ɷ�������û�з������ݻ����Ѿ������ĻỰ��ServerConnection�޷�����Pool�С�
				 */
				return (now - endTime) > 15000;
			}
			return false;
		}
	}

	public void startSession() throws Exception {
		for (ObjectPool pool : pools) {
			final java.sql.Connection conn = (java.sql.Connection) pool
					.borrowObject();
			connPoolMap.put(conn, pool);
			final CountDownLatch latch = new CountDownLatch(pools.length);
			QueryRunnable runnable = new QueryRunnable(conn,query);
			ProxyRuntimeContext.getInstance().getServerSideExecutor().execute(runnable);
		}
	}

	public void endSession() {
		lock.lock();
		try {
			if (!ended) {
				for (Map.Entry<java.sql.Connection, ObjectPool> entry : connPoolMap.entrySet()) {
					try {
						entry.getValue().returnObject(entry.getKey());
					} catch (Exception e) {
					}
				}
			}
		} finally {
			lock.unlock();
		}
	}

	public boolean isEnded() {
		lock.lock();
		try {
			return this.ended;
		} finally {
			lock.unlock();
		}
	}

}
