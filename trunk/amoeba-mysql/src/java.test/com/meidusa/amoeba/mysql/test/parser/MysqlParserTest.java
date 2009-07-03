package com.meidusa.amoeba.mysql.test.parser;




import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import com.meidusa.amoeba.parser.Parser;
import com.meidusa.amoeba.parser.dbobject.Column;
import com.meidusa.amoeba.parser.statment.DMLStatment;
import com.meidusa.amoeba.parser.statment.PropertyStatment;
import com.meidusa.amoeba.parser.statment.Statment;
import com.meidusa.amoeba.parser.expression.Expression;
import com.meidusa.amoeba.parser.function.*;
import com.meidusa.amoeba.mysql.parser.sql.MysqlParser;
import com.meidusa.amoeba.route.AbstractQueryRouter;
import com.meidusa.amoeba.sqljep.function.Comparative;

public class MysqlParserTest {
	
	
	static Map<Column,Comparative> columnMap = new HashMap<Column,Comparative>();
	public static void main(String[] args) throws Exception{
		Map<String,Function> funMap = AbstractQueryRouter.loadFunctionMap("./build/build-mysql/conf/functionMap.xml");
		String t = "`asdfasdfaf`";
		System.out.println(t.substring(1,t.length()-1));
		String sql1 = " SELECT * from account where time = DATE_ADD('1998-01-02', INTERVAL 31 DAY)";
		String sql2 = "select * from account where 1<2 and not (id between 12+12 and 33) and id >12+3 or id not in (11,33,'23234') or  id  in (select test.ref_Id from test dd where dd.name='test')";
		String[] sqls = {
				sql1,sql2,
				"SELECT * FROM AA WHERE ID = 'ASDF\\'ADF'",
				"SELECT '1997-12-31 23:59:59' + INTERVAL 1 MICROSECOND",
				"SELECT *,asdf from dd where id = hour('11:12:11.123451')",
				"SELECT 2 mod 9",
				"select mod(2,9)",
				"SELECT * from test where gmt_create=YEAR('1998-02-03')",
				"select now()+0",
				"SELECT Current_Date",
				"SELECT * from test where id = week(SYSDATE())",
				"SELECT * from test where name='asdfafd' || 123",
				"SELECT * from test where id = now()+1",
				"select work from account where level =1",
				"Set names utf8","set names latin1",
				"SET  SESSION  TRANSACTION ISOLATION LEVEL read COMMITTED",
				"start transaction",
				"select * from test where id =  ascii('asf')",
				"SELECT * from test where id = INSERT('Quadratic', 3, 4, 'What');",
				"select Instr('ddaass','aas')",
				"insert into  mytable(id,name) values(Instr('ddaass','aas'),INSERT('Quadratic', 3, 4, 'What'))",
				"SET OPTION SQL_SELECT_LIMIT=DEFAULT,@@global.sort_buffer_size=1000000, @@local.sort_buffer_size=1000000",
				"insert into account set id=1002, name='qwerqwer' ,create_time=(33+12)",
				"select `create-time` from account where `game-1`=1",
				"select `create-time` from `account-table` where `game-1`=UNKNOWFUNCTION()",
				"SELECT * FROM `roster-groups` WHERE `collection-owner` = 'wadd@im17.vsa.com.cn' ORDER BY `object-sequence`",
				"SELECT * , member_Blink.qq AS mqq FROM autoSiteShop    LEFT JOIN shopDetail ON autoSiteShop.id = shopDetail.shopId",
				"SELECT adsfad , member_Blink.qq AS mqq   FROM autoSiteShop   LEFT JOIN shopDetail ON autoSiteShop.id = shopDetail.shopId  LEFT JOIN member_Blink ON autoSiteShop.id = member_Blink.memberId     WHERE autowebsite = 'y'     AND id = 6388",
				"insert into test.test1 values('asdfadf',111,11123)",
				"select * from test.test1",
				"REPLACE INTO supe_spacecache9(uid, cacheid, value, updatetime) VALUES ('81828', '9', 'a:0:{}', '1219970056')",
				"REPLACE INTO supe_members (uid, groupid, username, password, secques, timeoffset, dateformat, havespace, newpm) VALUES ('219733', '9', 'KennisWai', '825e73d2764708bc30d4f401c4720f3a', '', '9999', '', '0', '0')",
				"SELECT sid, uid AS sessionuid, groupid, groupid='6' AS ipbanned, pageviews AS spageviews, styleid, lastolupdate, seccode FROM cdb_sessions WHERE sid='CgShIZ' AND CONCAT_WS('.',ip1,ip2,ip3,ip4)='210.177.156.49'",
				"SELECT t.tid, t.closed, t.dateline, t.special, t.lastpost AS lastthreadpost,  f.*, ff.*  , f.fid AS fid "+
					"FROM cdb_threads t INNER JOIN cdb_forums f ON f.fid=t.fid	LEFT JOIN cdb_forumfields ff ON ff.fid=f.fid  WHERE t.tid='1397087' AND t.displayorder>='0' LIMIT 1",
				"SELECT f.fid, f.fup, f.type, f.name, f.threads, f.posts, f.todayposts, f.lastpost, f.inheritedmod, f.forumcolumns, f.simple, ff.description, ff.moderators, ff.icon, ff.viewperm, ff.redirect FROM cdb_forums f LEFT JOIN cdb_forumfields ff USING(fid)	WHERE f.status>0 ORDER BY f.type, f.displayorder",
				"SELECT o.* FROM  (SELECT row_id   FROM  (SELECT row_id,    rownum rn     FROM    (SELECT rowid row_id       FROM offer      WHERE member_id = ?    AND status        = ?    AND gmt_expire    > sysdate    AND type = ?   ORDER BY MEMBER_ID,      STATUS         ,      GMT_EXPIRE DESC    )    WHERE rownum<=?  )  WHERE rn >= ?  ) t,  offer o  WHERE t.row_id=o.rowid ",
				"set CLIENT CHARSET gbk",
				"select * from offer where id in(12,11) limit 1,2",
				"SELECT d_tax, d_next_o_id FROM district WHERE d_w_id = 1  AND d_id = 1 FOR UPDATE",
				"select @@sql_mode",
				"select * from aaa where id = 12 AND (upper(subject) like upper(?) OR upper(keywords) like upper(?))"
				,"REPLACE INTO cdb_spacecaches (uid, variable, value, expiration) VALUES ('2980526', 'mythreads', 'a:3:{i:0;a:14:{s:3:\"tid\";s:7:\"1606800\";s:7:\"subject\";s:8:\"���Ո�M\";s:7:\"special\";s:1:\"0\";s:5:\"price\";s:1:\"0\";s:3:\"fid\";s:3:\"129\";s:5:\"views\";s:2:\"20\";s:7:\"replies\";s:1:\"0\";s:6:\"author\";s:4:\"av8d\";s:8:\"authorid\";s:7:\"2980526\";s:8:\"lastpost\";s:10:\"1236516949\";s:10:\"lastposter\";s:4:\"av8d\";s:10:\"attachment\";s:1:\"0\";s:3:\"pid\";s:6:\"154418\";s:7:\"message\";s:191:\"�h�ҵ���, �ҟo���^, ���ǿ����ܰѓp�ҵ����������ǰ�?http://bbs.macd.cn/viewthread.php?tid=1585698&amp;extra=page%3D1&amp;authorid=0&amp;page=33656F, 657��, 660F, ��Quote�˓p�ҵ���Փ. ��ԓ�h��?\";}i:1;a:14:{s:3:\"tid\";s:7:\"1594957\";s:7:\"subject\";s:25:\"Q&amp;A With Bob Prechter\";s:7:\"special\";s:1:\"0\";s:5:\"price\";s:1:\"0\";s:3:\"fid\";s:2:\"22\";s:5:\"views\";s:3:\"428\";s:7:\"replies\";s:2:\"10\";s:6:\"author\";s:4:\"av8d\";s:8:\"authorid\";s:7:\"2980526\";s:8:\"lastpost\";s:10:\"1234267824\";s:10:\"lastposter\";s:9:\"cixilarty\";s:10:\"attachment\";s:1:\"0\";s:3:\"pid\";s:4:\"8429\";s:7:\"message\";s:289:\"�Ҳ����g��, ���w�ķ��g���ܴ�Ҳ����T,߀�ǿ�ԭ�ĵİ�!�]���ن��ֵ�.:*29*:Q&amp;A With Bob PrechterThe following is a compilation of Bob Prechter\\'s best media interviews.In this Q&amp;A, Bob talks about the validity and practical applicationsof the Wave Principle and explains Socionomics,...\";}i:2;a:14:{s:3:\"tid\";s:7:\"1593908\";s:7:\"subject\";s:27:\"�ѳ����涨��� �����÷�IP\";s:7:\"special\";s:1:\"0\";s:5:\"price\";s:1:\"0\";s:3:\"fid\";s:3:\"129\";s:5:\"views\";s:2:\"44\";s:7:\"replies\";s:1:\"2\";s:6:\"author\";s:4:\"av8d\";s:8:\"authorid\";s:7:\"2980526\";s:8:\"lastpost\";s:10:\"1234077272\";s:10:\"lastposter\";s:5:\"�ꡣ0\";s:10:\"attachment\";s:1:\"0\";s:3:\"pid\";s:3:\"910\";s:7:\"message\";s:282:\"1. ���� http://bbs.macd.cn/thread-615952-1-1.html 5¥ ���������涨, Ӧ���Է�IP�η�ʽ����.ҪӪ��רҵ����г�Ľ��������������ǿ���������ԭ�еİ��������һЩϸ�ڣ�����2009��1��10��ִ�У�5.ʹ�ö������������˵ģ���ʱ��ֹ��½����¼�ڰ�����ֹ����׽�����̳����6.ʹ����׽��е��ҵ� ...\";}}', '1238258538')",
				"select * from users where user like '%rain%';",
				"SELECT magid, title FROM mag WHERE parentid = '72' ORDER BY rand() LIMIT 10",
				"EXPLAIN  select distinct(a.id),a.InfoTitle,b.corpName,a.ProPrice,a.ShowTime,a.ExpTime,d.user as user_name,b.province,b.city,a.ProIntro from blogs c, users d,provide_info a ,corp_info b ,keyword e where a.blog_id=b.blog_id and a.blog_id!= '85653' and now() - INTERVAL a.InfoExp DAY  and a.blog_id=c.id and c.owner_id=d.id and a.id=e.host_id and e.ktype=4 and e.kname='������ҵ' order by a.ShowTime desc,a.id desc",
				"EXPLAIN SELECT * FROM xx where id= 12 FORCE INDEX (xx,yyy)",
				"(select help_topic_id ,name from mysql.help_topic where help_topic_id=53 order by help_category_id desc limit 2) union all (select help_topic_id ,name from mysql.help_topic where help_topic_id=47 order by help_category_id desc limit 2) union all (select help_topic_id ,name from mysql.help_topic where help_topic_id=53 order by help_category_id desc limit 2) union all (select help_topic_id ,name from mysql.help_topic where help_topic_id=47 order by help_category_id desc limit 2)"
				//,"/* mysql-connector-java-5.1.6 ( Revision: ${svn.Revision} ) */SHOW VARIABLES WHERE Variable_name =��language�� OR Variable_name = ��net_write_timeout�� OR Variable_name = ��interactive_timeout�� OR Variable_name = ��wait_timeout�� OR Variable_name = ��character_set_client�� OR Variable_name = ��character_set_connection�� OR Variable_name = ��character_set�� OR Variable_name = ��character_set_server�� OR Variable_name = ��tx_isolation�� OR Variable_name = ��transaction_isolation�� OR Variable_name = ��character_set_results�� OR Variable_name = ��timezone�� OR Variable_name = ��time_zone�� OR Variable_name = ��system_time_zone�� OR Variable_name = ��lower_case_table_names�� OR Variable_name = ��max_allowed_packet�� OR Variable_name = ��net_buffer_length�� OR Variable_name = ��sql_mode�� OR Variable_name = ��query_cache_type�� OR Variable_name = ��query_cache_size�� OR Variable_name = ��init_connect��"
		};
		if(args.length == 0){
			for(String sql: sqls){
				parser(funMap,sql);
			}
		}else{
			BufferedReader reader = new BufferedReader(new FileReader(new File(args[0])));
			StringBuffer buffer = new StringBuffer();
			String line = null;
			while((line = reader.readLine()) != null){
				if(line.trim().startsWith("#")){
					continue;
				}else{
					buffer.append(line).append("\n");
				}
			}
			String sql = buffer.toString();
			parser(funMap,sql);
			
			
		}
	}
	
	private static void parser(Map<String,Function> funMap,String sql){
		Parser parser = new MysqlParser(new StringReader(sql));
		parser.setFunctionMap(funMap);
		try {
			Statment statment = parser.doParse();
			if(statment instanceof DMLStatment){
				DMLStatment dmlStatment = (DMLStatment)statment;
				Expression expression = dmlStatment.getExpression();
				System.out.println(sql+" =[ "+ expression+"], evaluated = {"+dmlStatment.evaluate(null)+"}");
			}else if(statment instanceof PropertyStatment ){
				PropertyStatment proStatment = (PropertyStatment)statment;
				System.out.println(proStatment.getProperties());
			}
			
		} catch (Exception e) {
			System.out.println("---------------------------------");
			System.out.println("error sql:"+ sql);
			e.printStackTrace();
			System.out.println("--------------------------");
		}
	}
	
}
