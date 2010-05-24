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
package com.meidusa.amoeba.route;

import com.meidusa.amoeba.net.DatabaseConnection;
import com.meidusa.amoeba.net.poolable.ObjectPool;
import com.meidusa.amoeba.parser.ParseException;
import com.meidusa.amoeba.parser.statement.Statement;
import com.meidusa.amoeba.util.Tuple;

/**
 * @author struct
 */
public interface QueryRouter {

    public Tuple<Statement,ObjectPool[]> doRoute(DatabaseConnection connection, String sql, boolean ispreparedStatment,
                                Object[] parameters) throws ParseException;

    public String getDefaultPool();

    public Statement parseSql(DatabaseConnection connection, String sql) throws ParseException;

    public int parseParameterCount(DatabaseConnection connection, String sql) throws ParseException;

    public ObjectPool getObjectPool(Object key);
}
