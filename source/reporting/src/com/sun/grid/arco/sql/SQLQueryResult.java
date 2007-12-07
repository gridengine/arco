/*___INFO__MARK_BEGIN__*/
/*************************************************************************
 *
 *  The Contents of this file are made available subject to the terms of
 *  the Sun Industry Standards Source License Version 1.2
 *
 *  Sun Microsystems Inc., March, 2001
 *
 *
 *  Sun Industry Standards Source License Version 1.2
 *  =================================================
 *  The contents of this file are subject to the Sun Industry Standards
 *  Source License Version 1.2 (the "License"); You may not use this file
 *  except in compliance with the License. You may obtain a copy of the
 *  License at http://gridengine.sunsource.net/Gridengine_SISSL_license.html
 *
 *  Software provided under this License is provided on an "AS IS" basis,
 *  WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING,
 *  WITHOUT LIMITATION, WARRANTIES THAT THE SOFTWARE IS FREE OF DEFECTS,
 *  MERCHANTABLE, FIT FOR A PARTICULAR PURPOSE, OR NON-INFRINGING.
 *  See the License for the specific provisions governing your rights and
 *  obligations concerning the Software.
 *
 *   The Initial Developer of the Original Code is: Sun Microsystems, Inc.
 *
 *   Copyright: 2001 by Sun Microsystems, Inc.
 *
 *   All Rights Reserved.
 *
 ************************************************************************/
/*___INFO__MARK_END__*/
package com.sun.grid.arco.sql;

import com.iplanet.jato.RequestManager;
import com.sun.grid.arco.QueryResult;
import com.sun.grid.arco.QueryResultException;
import com.sun.grid.arco.model.Field;
import com.sun.grid.arco.model.QueryType;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import com.sun.grid.logging.SGELog;

public class SQLQueryResult extends QueryResult implements java.io.Serializable {
   
   static final int WEB_DEFAULT = -2;
   
   private transient ArcoDbConnectionPool connectionPool;
   private transient ArcoDbConnection connection;
   private transient ResultSet resultSet;
   private transient Statement  stmt;
   private transient List columnList;
   private transient boolean isActive;
   private int clusterId;
   
   private transient Class [] columnTypes;
   
   /** Creates a new instance of SQLQueryResult */
   public SQLQueryResult(QueryType query, ArcoDbConnectionPool connectionPool) {
      super(query);
      this.connectionPool = connectionPool;
      this.clusterId = WEB_DEFAULT;
   }
   
   public SQLQueryResult(QueryType query, ArcoDbConnectionPool connectionPool, int clusterId) {
      super(query);
      this.connectionPool = connectionPool;
      this.clusterId = clusterId;
   }
   
   public void activate() throws QueryResultException {
      try {
         long start = System.currentTimeMillis();
         if (clusterId == WEB_DEFAULT) {
            ArcoClusterModel acm = ArcoClusterModel.getInstance(RequestManager.getSession());
            connection = connectionPool.getConnection(acm.getCurrentCluster());
         } else {
            connection = connectionPool.getConnection(clusterId);
         }
            
         
         stmt = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY,
               ResultSet.CONCUR_READ_ONLY );
         
         
         SQLGenerator gen = connectionPool.getSQLGenerator();
         String sql = gen.generate(getQuery(), getLateBinding() );
         
         SGELog.fine("execute sql -------\n{0}\n--------", sql);
         
         
         resultSet = stmt.executeQuery(sql);
         
         if( SGELog.isLoggable( Level.CONFIG)) {
            double diff = ((double)System.currentTimeMillis() - start)/1000;
            SGELog.config("query executed in " + diff + "s");
         }
      } catch( SQLGeneratorException sqlgene) {
         passivate();
         QueryResultException qre =
               new QueryResultException(sqlgene.getMessage(),
               sqlgene.getParameter() );
         qre.initCause(sqlgene);
         throw qre;
      } catch( SQLException sqle ) {
         passivate();
         QueryResultException qre =
               new QueryResultException("sqlQueryResult.execError"
               , new Object[] { sqle.getMessage() } );
         qre.initCause(sqle);
         throw qre;
      }
      
      // Get the column types
      try {
         ResultSetMetaData rsMeta = resultSet.getMetaData();
         int colCount = rsMeta.getColumnCount();
         columnTypes = new Class[colCount];
         
         String className = null;
         for(int i = 0; i < colCount; i++ ) {
            className = rsMeta.getColumnClassName(i+1);
            try {
               columnTypes[i] = Class.forName(className);
            } catch( ClassNotFoundException cnfe ) {
               passivate();
               QueryResultException qre =
                     new QueryResultException("sqlQueryResult.unknownTypeError"
                     , new Object[] { className } );
               qre.initCause(cnfe);
               throw qre;
            }
         }
      } catch( SQLException sqle ) {
         passivate();
         QueryResultException qre =
               new QueryResultException("sqlQueryResult.getTypeError"
               , new Object[] { sqle.getMessage() } );
         qre.initCause(sqle);
         throw qre;
      }
      isActive = true;
   }
   
   public void passivate() {
      isActive = false;
      if( resultSet != null ) {
         try {
            resultSet.close();
         } catch( SQLException sqle ) {
            SGELog.warning(sqle, "Error closing resultSet: " + sqle.getMessage());
         } finally {
            resultSet = null;
         }
      }
      if( stmt != null ) {
         try {
            stmt.close();
         } catch( SQLException sqle ) {
            SGELog.warning(sqle, "Error closing statement: " + sqle.getMessage());
         } finally {
            stmt = null;
         }
      }
      if( connection != null ) {
         connectionPool.releaseConnection(connection);
         connection = null;
      }
   }
   
   
   public java.lang.Object[] createValuesForNextRow()  throws QueryResultException {
      try {
         if( resultSet.next() ) {
            List fieldList = getQuery().getField();
            Object [] ret = new Object[fieldList.size()];
            
            for( int i = 0; i < ret.length; i++) {
               try {
                  ret[i] = resultSet.getObject(i+1);
               } catch( SQLException sqle ) {
                  Field field = (Field)fieldList.get(i);
                  Object [] params = new Object[] {
                     field.getReportName(),
                     sqle.getMessage()
                  };
                  
                  QueryResultException qre =
                        new QueryResultException("sqlQueryResult.getError",
                        params );
                  qre.initCause(sqle);
                  throw qre;
               }
            }
            return ret;
         } else {
            return null;
         }
      } catch( SQLException sqle ) {
         QueryResultException qre =
               new QueryResultException("sqlQueryResult.fetchError"
               , new Object[] { sqle.getMessage() } );
         qre.initCause(sqle);
         throw qre;
      }
   }
   
   public java.util.List getColumns() {
      if( columnList == null ) {
         
         QueryType query = getQuery();
         
         com.sun.grid.arco.Util.correctFieldNames(query);
         
         List fieldList = query.getField();
         
         columnList = new ArrayList(fieldList.size());
         
         Iterator iter = fieldList.iterator();
         Field field = null;
         String fieldName = null;
         
         while(iter.hasNext()) {
            field = (Field)iter.next();
            fieldName = field.getReportName();
            columnList.add(fieldName);
         }
      }
      return columnList;
   }
   
   public Class getColumnClass(int index) {
      return columnTypes[index];
   }
   
   
   /**
    * The SQLQueryResult is editable
    * @return  true
    */
   public boolean isEditable() {
      return true;
   }
   
}
