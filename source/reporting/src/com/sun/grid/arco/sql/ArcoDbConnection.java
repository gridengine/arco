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


import java.util.Vector;
import java.sql.*;

import com.sun.grid.arco.ArcoConstants;

/**
 * <p><code>ArcoDbConnection</code> provides the connection to the 
 * specified Database for an Application. GenerateSql-Statements 
 * will be sent to the database and a resultset will be received.
 * </p>
 *
 */
public class ArcoDbConnection implements ArcoConstants {

  private static final int TRY_OPEN_RESET = 0;
    
  /**
   */
  private String dbType;
  private String schema;
  
  private javax.sql.ConnectionPoolDataSource datasource;
  
  /** member to cache the connection
   */
  private Connection connection;
  
  /** list of connections needed for a soft clos of connections
   */
  private static Vector instanceCache = new Vector();
  
  private int tryOpen_count = TRY_OPEN_RESET;
  
  private Integer id;
  
  public ArcoDbConnection( int id, String dbType, String schema, javax.sql.ConnectionPoolDataSource ds ) {
     this.id = new Integer(id);
     this.dbType = dbType;
     this.schema = schema;
     this.datasource = ds;
  }
  
  public Integer getId() {
     return id;
  }
  
  /** open the database connection to the specified database.
   */
  public void openConnection() throws IllegalStateException, SQLException {
      if (connection != null && !connection.isClosed()){
        throw new IllegalStateException("Database already connected");
      }
      connection = datasource.getPooledConnection().getConnection();      
  }

  /** close the opened database connection
   */
  public void closeConnection() {
    try {
       if ( connection == null || connection.isClosed()){
          return;
       }
       connection.close();
    } catch(SQLException expected) {
    } finally {
       connection = null;
    }
  }
  
  public Statement createStatement(int resultSetType, int resultSetConcurrency) 
     throws SQLException {
     
     if( connection == null ) {
        openConnection();
     }
     return connection.createStatement(resultSetType, resultSetConcurrency);
  }
   
  /** gets a resultset which contains the attributes of the specified 
   * table
   * @param table tablke to get the attributes from
   * @throws SQLException thrown by jdbcdriver
   * @return resultset which contains the attributes 
   * @see java.sql.DatabaseMetaData.getColumns(String. String, String, String)
   */  
  public ResultSet getAttributes(String table) throws SQLException {
      ResultSet retVal = null;
      try {
          if (connection == null) {
              openConnection();
          }

          DatabaseMetaData metaData = connection.getMetaData();
          String mySchema = null;
          if( this.schema != null && this.schema.length() > 0 ) {
             mySchema = this.schema;
          }
          
          
          if( dbType.equals( DB_TYPE_ORACLE ) ) {
             table = table.toUpperCase();
             if( mySchema != null ) {
               mySchema = mySchema.toUpperCase();
             }
          }
          
          retVal =  metaData.getColumns(null,mySchema, table,null);
      } catch (SQLException sql) {
          throw sql;
      }
      return retVal;
  }
  
  /**
   * @throws SQLException
   * @return
   */  
  public ResultSet getViewList() throws SQLException {
      ResultSet retVal = null;
      try {
          if (connection == null) {
              openConnection();
          }

          DatabaseMetaData meta = connection.getMetaData();
          String mySchema = null;
          if( this.schema != null && this.schema.length() > 0 ) {
             mySchema = this.schema;
          }
          if( dbType.equals( DB_TYPE_ORACLE ) ) {
             if( mySchema != null ) {
               mySchema = mySchema.toUpperCase();
             }
          }
          retVal =  meta.getTables(null,mySchema,null,new String[]{"VIEW","TABLE", "SYNONYM"});
      } catch (SQLException sql) {
          throw sql;
      }
     return retVal;
  }
  
   /**
    * Return a connection data source
    * @return
    */
   public javax.sql.ConnectionPoolDataSource getDatasource() {
      return datasource;
   }

} // end of class ArcoDbConnection
