/**
 * <pre>
 * 	This program is free software; you can redistribute it and/or modify it under the terms of 
 * the GNU General Public License as published by the Free Software Foundation; either version 3 of the License, 
 * or (at your option) any later version. 
 * 
 * 	This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * See the GNU General Public License for more details. 
 * 	You should have received a copy of the GNU General Public License along with this program; 
 * if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * </pre>
 */
package com.meidusa.amoeba.mysql.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.meidusa.amoeba.context.ProxyRuntimeContext;
import com.meidusa.amoeba.mysql.jdbc.MysqlDefs;
import com.meidusa.amoeba.mysql.net.MysqlClientConnection;
import com.meidusa.amoeba.mysql.net.packet.BindValue;
import com.meidusa.amoeba.mysql.net.packet.ErrorPacket;
import com.meidusa.amoeba.mysql.net.packet.ExecutePacket;
import com.meidusa.amoeba.mysql.net.packet.FieldPacket;
import com.meidusa.amoeba.mysql.net.packet.LongDataPacket;
import com.meidusa.amoeba.mysql.net.packet.MysqlPacketBuffer;
import com.meidusa.amoeba.mysql.net.packet.OkPacket;
import com.meidusa.amoeba.mysql.net.packet.QueryCommandPacket;
import com.meidusa.amoeba.mysql.net.packet.ResultSetHeaderPacket;
import com.meidusa.amoeba.mysql.net.packet.RowDataPacket;
import com.meidusa.amoeba.mysql.net.packet.result.MysqlResultSetPacket;
import com.meidusa.amoeba.net.Connection;
import com.meidusa.amoeba.net.MessageHandler;
import com.meidusa.amoeba.net.Sessionable;
import com.meidusa.amoeba.net.poolable.ObjectPool;
import com.meidusa.amoeba.parser.dbobject.Column;
import com.meidusa.amoeba.parser.statement.InsertStatement;
import com.meidusa.amoeba.parser.statement.SelectStatement;
import com.meidusa.amoeba.parser.statement.Statement;
import com.meidusa.amoeba.route.QueryRouter;
import com.meidusa.amoeba.util.Tuple;

/**
 * handler
 * 
 * @author <a href=mailto:piratebase@sina.com>Struct chen</a>
 */
public class MySqlCommandDispatcher implements MessageHandler {

    protected static Logger logger  = Logger.getLogger(MySqlCommandDispatcher.class);
    private static Logger lastInsertID = Logger.getLogger("lastInsertId");
    private long timeout = ProxyRuntimeContext.getInstance().getConfig().getQueryTimeout() * 1000;

    private static byte[]   STATIC_OK_BUFFER;
    static {
        OkPacket ok = new OkPacket();
        ok.affectedRows = 0;
        ok.insertId = 0;
        ok.packetId = 1;
        ok.serverStatus = 2;
        STATIC_OK_BUFFER = ok.toByteBuffer(null).array();
    }

    /**
     * Ping ��COM_STMT_SEND_LONG_DATA command remove to @MysqlClientConnection #doReceiveMessage()
     */
    public void handleMessage(Connection connection) {
    	
    	byte[] message = null;
		while((message = connection.getInQueue().getNonBlocking()) != null){
	        MysqlClientConnection conn = (MysqlClientConnection) connection;
	
	        QueryCommandPacket command = new QueryCommandPacket();
	        command.init(message, connection);
	        if (logger.isDebugEnabled()) {
	            logger.debug(command.query);
	        }
	        try {
	            if (MysqlPacketBuffer.isPacketType(message, QueryCommandPacket.COM_QUERY)) {
	                QueryRouter router = ProxyRuntimeContext.getInstance().getQueryRouter();
	                Tuple<Statement,ObjectPool[]> tuple = router.doRoute(conn, command.query, false, null);
	                Statement statment = tuple.left;
	                ObjectPool[] pools = tuple.right;
	                if (statment != null && statment instanceof SelectStatement && ((SelectStatement)tuple.left).isQueryLastInsertId()) {
	                	MysqlResultSetPacket lastPacketResult = createLastInsertIdPacket(conn,(SelectStatement)statment,false);
            			lastPacketResult.wirteToConnection(conn);
            			return;
	                }
	                
	                if(pools == null){
	                	conn.postMessage(STATIC_OK_BUFFER);
	                	return;
	                }
	                
	                MessageHandler handler = new QueryCommandMessageHandler(conn, message,statment, tuple.right, timeout);
	                if (handler instanceof Sessionable) {
	                    Sessionable session = (Sessionable) handler;
	                    try {
	                        session.startSession();
	                    } catch (Exception e) {
	                        logger.error("start Session error:", e);
	                        session.endSession(true);
	                        throw e;
	                    }
	                }
	            } else if (MysqlPacketBuffer.isPacketType(message, QueryCommandPacket.COM_STMT_PREPARE)) {
	            	
	                PreparedStatmentInfo preparedInf = conn.getPreparedStatmentInfo(command.query);
	                if(preparedInf.getByteBuffer() != null && preparedInf.getByteBuffer().length >0){
	                	conn.postMessage(preparedInf.getByteBuffer());
	                	return;
	                }
	                
	                QueryRouter router = ProxyRuntimeContext.getInstance().getQueryRouter();
	                Tuple<Statement,ObjectPool[]> tuple = router.doRoute(conn, command.query, true, null);
	                
	            	PreparedStatmentMessageHandler handler = new PreparedStatmentMessageHandler(conn,preparedInf,tuple.left, message , new ObjectPool[]{tuple.right[0]}, timeout,false);
	            	if (handler instanceof Sessionable) {
	                    Sessionable session = (Sessionable) handler;
	                    try {
	                        session.startSession();
	                    } catch (Exception e) {
	                        logger.error("start Session error:", e);
	                        session.endSession(true);
	                        throw e;
	                    }
	                }
	                return;
	            } else if (MysqlPacketBuffer.isPacketType(message, QueryCommandPacket.COM_STMT_EXECUTE)) {
	            	
	            	try{
		                long statmentId = ExecutePacket.readStatmentID(message);
		                PreparedStatmentInfo preparedInf = conn.getPreparedStatmentInfo(statmentId);
		                if (preparedInf == null) {
		                    ErrorPacket error = new ErrorPacket();
		                    error.errno = 1044;
		                    error.packetId = 1;
		                    error.sqlstate = "42000";
		                    error.serverErrorMessage = "Unknown prepared statment id=" + statmentId;
		                    conn.postMessage(error.toByteBuffer(connection).array());
		                    logger.warn("Unknown prepared statment id:" + statmentId);
		                } else {
		                	Statement statment = preparedInf.getStatment();
		                	if (statment != null && statment instanceof SelectStatement && ((SelectStatement)statment).isQueryLastInsertId()) {
		                		if(lastInsertID.isDebugEnabled()){
		                			lastInsertID.debug("SQL="+statment.getSql());
		                		}
		                		MysqlResultSetPacket lastPacketResult = createLastInsertIdPacket(conn,(SelectStatement)statment,true);
		            			lastPacketResult.wirteToConnection(conn);
		            			return;
			                }
		                	
		                	if(statment instanceof InsertStatement){
		                		if(lastInsertID.isDebugEnabled()){
		                			lastInsertID.debug("SQL="+statment.getSql());
		                		}
		                	}
		                	
		                    Map<Integer, Object> longMap = new HashMap<Integer, Object>();
		                    for (byte[] longdate : conn.getLongDataList()) {
		                        LongDataPacket packet = new LongDataPacket();
		                        packet.init(longdate, connection);
		                        longMap.put(packet.parameterIndex, packet.data);
		                    }
		
		                    ExecutePacket executePacket = new ExecutePacket(preparedInf, longMap);
		                    executePacket.init(message, connection);
		
		                    QueryRouter router = ProxyRuntimeContext.getInstance().getQueryRouter();
		                    Tuple<Statement,ObjectPool[]> tuple = router.doRoute(conn, preparedInf.getPreparedStatment(), false, executePacket.getParameters());
		
		                    PreparedStatmentExecuteMessageHandler handler = new PreparedStatmentExecuteMessageHandler(
		                                                                                                              conn,
		                                                                                                              preparedInf,tuple.left,
		                                                                                                              message,
		                                                                                                              tuple.right,
		                                                                                                              timeout);
		                    handler.setExecutePacket(executePacket);
		                    if (handler instanceof Sessionable) {
		                        Sessionable session = (Sessionable) handler;
		                        try {
		                            session.startSession();
		                        } catch (Exception e) {
		                            logger.error("start Session error:", e);
		                            session.endSession(true);
		                            throw e;
		                        }
		                    }
		                }
	            	}finally{
	            		conn.clearLongData();
	            	}
	            } else if (MysqlPacketBuffer.isPacketType(message, QueryCommandPacket.COM_INIT_DB)) {
	                conn.setSchema(command.query);
	                conn.postMessage(STATIC_OK_BUFFER);
	            } else {
	                ErrorPacket error = new ErrorPacket();
	                error.errno = 1044;
	                error.packetId = 1;
	                error.sqlstate = "42000";
	                error.serverErrorMessage = "can not use this command here!!";
	                conn.postMessage(error.toByteBuffer(connection).array());
	                logger.warn("unsupport packet:" + command);
	            }
	        } catch (Exception e) {
	            ErrorPacket error = new ErrorPacket();
	            error.errno = 1044;
	            error.packetId = 1;
	            error.sqlstate = "42000";
	            error.serverErrorMessage = e.getMessage();
	            e.printStackTrace();
	            conn.postMessage(error.toByteBuffer(connection).array());
	            logger.error("messageDispate error", e);
	        }
		}
    }
    
    private MysqlResultSetPacket createLastInsertIdPacket(MysqlClientConnection conn,SelectStatement statment,boolean isPrepared){
    	Map<String,Column> selectedMap = ((SelectStatement)statment).getSelectColumnMap();
		MysqlResultSetPacket lastPacketResult = new MysqlResultSetPacket(null);
		lastPacketResult.resulthead = new ResultSetHeaderPacket();
		lastPacketResult.resulthead.columns = selectedMap.size();
		lastPacketResult.resulthead.extra = 1;
		RowDataPacket row = new RowDataPacket(isPrepared);
		row.columns = new ArrayList<Object>();
		int index =0; 
		lastPacketResult.fieldPackets = new FieldPacket[selectedMap.size()];
		for(Map.Entry<String, Column> entry : selectedMap.entrySet()){
			FieldPacket field = new FieldPacket();
			String alias = entry.getValue().getAlias();
			
			
			if("LAST_INSERT_ID".equalsIgnoreCase(entry.getValue().getName())){
				BindValue value = new BindValue();
				value.bufferType = MysqlDefs.FIELD_TYPE_LONGLONG;
				value.longBinding = conn.getLastInsertId();
				value.scale = 20;
				value.isSet = true;
				row.columns.add(value);
				field.name = (alias == null?entry.getValue().getName()+"()":alias);
				
			}else if("@@IDENTITY".equalsIgnoreCase(entry.getValue().getName())){

				BindValue value = new BindValue();
				value.bufferType = MysqlDefs.FIELD_TYPE_LONGLONG;
				value.longBinding = conn.getLastInsertId();
				value.scale = 20;
				value.isSet = true;
				row.columns.add(value);
				
				row.columns.add(value);
				field.name = (alias == null?entry.getValue().getName():alias);
				
			}else{
				BindValue value = new BindValue();
				value.bufferType = MysqlDefs.FIELD_TYPE_STRING;
				value.scale = 20;
				value.isNull = true;
				row.columns.add(value);
				field.name = (alias == null?entry.getValue().getName():alias);
			}
			
			field.type = MysqlDefs.FIELD_TYPE_LONGLONG;
			field.catalog = "def";
			field.length = 20;
			lastPacketResult.fieldPackets[index] = field; 
			index++;
		}
			
		List<RowDataPacket> list = new ArrayList<RowDataPacket>();
		list.add(row);
		lastPacketResult.setRowList(list);
		return lastPacketResult;
    }
}
