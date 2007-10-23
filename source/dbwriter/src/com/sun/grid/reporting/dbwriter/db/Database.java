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

package com.sun.grid.reporting.dbwriter.db;

import java.sql.*;
import java.util.*;
import com.sun.grid.logging.SGELog;
import com.sun.grid.reporting.dbwriter.ReportingException;
import com.sun.grid.reporting.dbwriter.event.CommitEvent;
import com.sun.grid.reporting.dbwriter.event.CommitListener;
import com.sun.grid.reporting.dbwriter.event.DatabaseListener;

public class Database {
   
   public final static int TYPE_POSTGRES = 1;
   public final static int TYPE_ORACLE = 2;
   public final static int TYPE_MYSQL = 3;
   
   public final static int DEFAULT_MAX_CONNECTIONS = 4;
   
   public final static int UNKNOWN_ERROR = 0;
   public final static int NO_CONNECTION_ERROR = 1;
   public final static int SYNTAX_ERROR = 2;
   
   public final static int DELETE_LIMIT = 500;
   
   // non static data and functions
   protected String driver;
   protected String url;
   protected String userName;
   protected String userPW;
   protected ErrorHandler errorHandler;
   protected static int type;
   
   /** list of registered <code>DatabaseListener</code> */
   private List databaseListeners = new ArrayList();
   
   /** list of registered <code>CommitListener</code> */
   private List commitListeners = new ArrayList();
   
   /** list of used connections */
   private ArrayList usedConnections = new ArrayList();
   
   /** list of unused connections */
   private ArrayList unusedConnections = new ArrayList();
   
   /** max number of connections ( used + unused ) */
   private int maxConnections = DEFAULT_MAX_CONNECTIONS;
   
   private long sqlThreshold;
   
   public Database() {
   }
   
   
   /**
    * Initialize the database
    * @param p_driver      class name of the jdbc driver
    * @param p_url         jdbc connection url
    * @param p_userName    name of the database user
    * @param p_userPW      password of the database user
    * @param sqlThreshold  sql threshold in milli seconds
    *
    * @throws com.sun.grid.reporting.dbwriter.ReportingException
    */
   public void init(String p_driver, String p_url, String p_userName, String p_userPW, long sqlThreshold) throws ReportingException {
      driver = p_driver;
      url    = p_url;
      userName = p_userName;
      userPW = p_userPW;
      this.sqlThreshold = sqlThreshold;
      type = getDatabaseTypeFromURL( p_url );
      
      
      
      // try to open the JDBC driver - if this fails, we can't do anything and
      // better exit. Better: don't catch exception.
      try {
         SGELog.fine( "register jdbc driver ''{0}''", driver );
         Class.forName(driver);
      } catch (ClassNotFoundException e) {
         throw new ReportingException( "Database.driverNotFound", driver );
      }
      
      if( driver.equals( "org.postgresql.Driver" ) ) {
         errorHandler = new PostgresErrorHandler();
      }
   }
   
   /**
    *  register a database listener
    *  @param lis   the database listener
    */
   public void addDatabaseListener(DatabaseListener lis) {
      synchronized(databaseListeners) {
         databaseListeners.add(lis);
      }
   }
   
   /**
    *  register a CommitListener
    *  @param lis the CommitListener
    */
   public void addCommitListener(CommitListener lis) {
      synchronized(commitListeners) {
         commitListeners.add(lis);
      }
   }
   
   /**
    *   remove a registered database listener
    *   @param lis  the database listener which should be removed
    */
   public void removeDatabaseListener(DatabaseListener lis) {
      synchronized(databaseListeners) {
         databaseListeners.remove(lis);
   /**
    * Creates a new instance of ValueRecordManager
    */
      }
   }
   
   /**
    *   remove a registered CommitListener
    *   @param lis the CommitListener which should be removed
    */
   public void removeCommitListener(CommitListener lis) {
      synchronized(commitListeners) {
         commitListeners.remove(lis);
      }
   }
   
   /**
    *  Get an array of all registered database listeners.
    *  @return array of registered database listeners (may be <code>null</code>)
    */
   private DatabaseListener [] getDatabaseListener() {
      DatabaseListener [] ret = null;
      if( !databaseListeners.isEmpty() ) {
         synchronized(databaseListeners) {
            if( !databaseListeners.isEmpty()) {
               ret = new DatabaseListener[databaseListeners.size()];
               databaseListeners.toArray(ret);
            }
         }
      }
      return ret;
   }
   
   /**
    *  Get an array of all registered CommitListeners.
    *  @return array of registered CommitListeners (may be <code>null</code>)
    */
   private CommitListener [] getCommitListener() {
      CommitListener [] ret = null;
      if( !commitListeners.isEmpty() ) {
         synchronized(commitListeners) {
            if( !commitListeners.isEmpty()) {
               ret = new CommitListener[commitListeners.size()];
               commitListeners.toArray(ret);
            }
         }
      }
      return ret;
   }
   
   /**
    *  notify all registered database listeners that a sql statement
    *  has been successfully execuded
    *  @param  sql  the sql statement
    */
   protected void fireSqlExecuted(String sql) {
      DatabaseListener [] lis = getDatabaseListener();
      if( lis != null && lis.length > 0 ) {
         for(int i = 0; i < lis.length; i++) {
            lis[i].sqlExecuted(sql);
         }
      }
   }
   
   /**
    *  notify all registered database listeners that a sql statement
    *  has produced an error
    *  @param  sql  the sql statement
    *  @param  error the sql error
    */
   protected void fireSqlFailed(String sql, SQLException error) {
      DatabaseListener [] lis = getDatabaseListener();
      if( lis != null && lis.length > 0) {
         for(int i = 0; i < lis.length; i++) {
            lis[i].sqlFailed(sql, error);
         }
      }
   }
   /**
    * Creates a new instance of ValueRecordManager
    */
   
   /**
    *  notify all registered CommitListeners that commit has executed
    *  succesfully
    *  @param  sql  the sql statement
    *  @param  error the sql error
    */
   public void fireCommitExecuted(String threadName, int id) {
      CommitListener [] lis = getCommitListener();
      if( lis != null && lis.length > 0) {
         for(int i = 0; i < lis.length; i++) {
            lis[i].commitExecuted(new CommitEvent(threadName, id));
         }
      }
   }
   
   /**
    *  notify all registered CommitListeners that commit
    *  has produced an error
    *  @param  sql  the sql statement
    *  @param  error the sql error
    */
   public void fireCommitFailed(String threadName, int id, SQLException error) {
      CommitListener [] lis = getCommitListener();
      if( lis != null && lis.length > 0) {
         for(int i = 0; i < lis.length; i++) {
            lis[i].commitFailed(new CommitEvent(threadName, id, error));
         }
      }
   }
   
   public static int getDatabaseTypeFromURL( String url ) throws ReportingException {
      
   /**
    * Creates a new instance of ValueRecordManager
    */
      url = url.toLowerCase();
      if( url.startsWith( "jdbc:oracle" ) ) {
         return TYPE_ORACLE;
      } else if ( url.startsWith( "jdbc:postgres" ) ) {
         return TYPE_POSTGRES;
      } else if ( url.startsWith("jdbc:mysql") ) {
         return TYPE_MYSQL;
      } else {
         throw new ReportingException( "Database.unsupportedURL", url );
      }
   }
   
   public static int getType() {
      return type;
   }
   
   public static int getDBType( String driver ) {
      if ( driver.equals("org.postgresql.Driver")) {
         return TYPE_POSTGRES;
      } else if ( driver.equals("oracle.jdbc.driver.OracleDriver")) {
         return TYPE_ORACLE;
      } else if ( driver.equals("com.mysql.jdbc.Driver")) {
         return TYPE_MYSQL;
      } else {
         return -1;
      }
   }
   
   public int getDBModelVersion() throws ReportingException {
      String sql = "select max(v_id) as version from sge_version";
      Connection conn = getConnection();
      try {
         Statement stmt = null;
         
         try {
            stmt = executeQuery(sql, conn);
         } catch (ReportingException re) {
            re.log();
            // we assume that table sge_version does not exist
            // return version 0
            return 0;
         }
         
         try {
            ResultSet rs = stmt.getResultSet();
            try {
               if(rs.next()) {
                  return rs.getInt("version");
               } else {
                  throw new ReportingException("Database.emptyVersionTable");
               }
            } finally {
               rs.close();
            }
         } catch(SQLException sqle) {
            throw createSQLError( "Database.sqlError", new Object[] { sql }, sqle, conn );
         } finally {
            try {
               stmt.close();
            } catch( java.sql.SQLException sqe ) {
            }
         }
      } finally {
         release( conn );
      }
   }
   
   public boolean test() throws ReportingException {
      Connection conn = getConnection();
      try {
         Statement stmt = executeQuery( "select count(*) from sge_host", conn );
         try {
            stmt.close();
         } catch( java.sql.SQLException sqe ) {
            return false;
         }
         return true;
      } catch( ReportingException re ) {
         return false;
      } finally {
         release( conn );
      }
   }
   
   
   
   private ReportingException createSQLError( String message, Object[] args, SQLException sqle, Connection connection ) {
      
      if( connection != null && this.getErrorType( sqle ) != SYNTAX_ERROR ) {
         try {
            connection.close();
         } catch( SQLException e ) {
            ReportingException re = new ReportingException( "Database.closeFailed" );
            re.initCause( e );
            re.log();
         }
      }
      ReportingException re = new ReportingException( message, args );
      re.initCause( sqle );
      return re;
   }
   
   
   
   
   public java.sql.Connection getConnection() throws ReportingException {
      
      java.sql.Connection ret = null;
      
      synchronized( usedConnections ) {
         
         while( unusedConnections.isEmpty() ) {
            if( getConnectionCount() >= maxConnections ) {
               try {
                  SGELog.fine( "All connections in use, thread {0} is waiting for the next free connection", Thread.currentThread().getName() );
                  usedConnections.wait();
               } catch( InterruptedException ire ) {
                  throw new ReportingException( "interrupted" );
               }
            } else {
               try {
                  SGELog.fine( "opening connection to ''{0}'' as user ''{1}''", url, userName );
                  java.sql.Connection connection = DriverManager.getConnection(url, userName, userPW);
                  ConnectionProxy proxy = new ConnectionProxy( connection, getConnectionCount() + 1 );
                  SGELog.fine( "connection {0} opened", proxy );
                  unusedConnections.add( proxy );
                  
               } catch (SQLException e) {
                  throw createSQLError( "Database.connectError", new Object[] { url, e.getMessage() }, e, null );
               }
            }
         }
         ret = (java.sql.Connection)unusedConnections.remove(0);
         usedConnections.add( ret );
      }
      SGELog.fine( "Thread {0} gots connection {1}", Thread.currentThread().getName(), ret );
      
      return ret;
   }
   
   /**
    *  get the number of open connections
    */
   public int getConnectionCount() {
      return usedConnections.size() + unusedConnections.size();
   }
   
   /**
    * This method is called from the release method. If Exception happens
    * while trying to set the autoCommit back to false. We don't want to
    * have a Connection in the pool where autoCommit is set to true
    */
   public void closeConnection(java.sql.Connection connection) {
      synchronized(usedConnections) {
         try {
            connection.close();
         } catch (SQLException sqle) {
            createSQLError( "Database.closeFailed", null, sqle, null ).log();
         }
      }
   }
   
   /**
    *  release a connection to the database
    */
   public void release( java.sql.Connection connection ) {
      SGELog.fine( "Thread {0} releases {1}", Thread.currentThread().getName(), connection );
      synchronized( usedConnections ) {
         usedConnections.remove( connection );
         
         try {
            if(connection.getAutoCommit()) {
               //only connection with autoCommit(false) should be in the pool
               connection.setAutoCommit(false);
            }
         } catch (SQLException ex) {
            //if we did not succeed, close the connection
            closeConnection(connection);
         }
         
         if( !((ConnectionProxy)connection).getIsClosedFlag() && connection != null) {
            unusedConnections.add( connection );
         }
         usedConnections.notify();
      }
   }
   
   /**
    *  close all open connections
    */
   public void closeAll()  {
      
      synchronized( usedConnections ) {
         Iterator iter = usedConnections.iterator();
         while( iter.hasNext() ) {
            try {
               ((java.sql.Connection)iter.next()).close();
            } catch( SQLException sqle ) {
               createSQLError( "Database.closeFailed", null, sqle, null ).log();
            }
         }
         iter = unusedConnections.iterator();
         while( iter.hasNext() ) {
            try {
               ((java.sql.Connection)iter.next()).close();
            } catch( SQLException sqle ) {
               createSQLError( "Database.closeFailed", null, sqle, null ).log();
            }
         }
         usedConnections.clear();
         unusedConnections.clear();
      }
   }
   
  public int executeUpdate(String sql, java.sql.Connection connection) throws ReportingException {
      long time = System.currentTimeMillis();
      int count = 0;
      
      try {
         Statement stmt = connection.createStatement();
         try {
            SGELog.fine( "Database.sql", sql);
            count = stmt.executeUpdate(sql);
            fireSqlExecuted(sql);              
         } finally {          
            stmt.close();
            return count;
         }         
      } catch( SQLException sqle ) {
         fireSqlFailed(sql,sqle);
         throw createSQLError("Database.sqlError", new Object[] { sql }, sqle, connection);
      } finally {
         if( sqlThreshold > 0 ) {
            double diff = System.currentTimeMillis() - time;
            if( diff > sqlThreshold ) {
               SGELog.warning("Database.sqlThresholdReached", new Double(diff/1000), sql );
            }
         }
      }
   }  
   
   public void execute(String sql, java.sql.Connection connection ) throws ReportingException {
      long time = System.currentTimeMillis();
      try {
         Statement stmt = connection.createStatement();
         try {
            SGELog.fine( "Database.sql", sql );
            stmt.execute(sql);
            fireSqlExecuted(sql);
         } finally {
            stmt.close();
         }
      } catch( SQLException sqle ) {
         fireSqlFailed(sql,sqle);
         throw createSQLError( "Database.sqlError", new Object[] { sql }, sqle, connection );
      } finally {
         if( sqlThreshold > 0 ) {
            double diff = System.currentTimeMillis() - time;
            if( diff > sqlThreshold ) {
               SGELog.warning("Database.sqlThresholdReached", new Double(diff/1000), sql );
            }
         }
      }
   }
   
   /**
    * execute a sql query. The caller have to call the <code>close</code> method
    * of the returned <code>Statement</code>
    * @param  sql   the sql string
    * @param  return  the <code>Statement</code> which contains the <code>ResultSet</code>
    * @throws ReportingException if the sql statement could not be executed
    */
   public Statement executeQuery(String sql, java.sql.Connection connection) throws ReportingException {
      try {
         Statement stmt = connection.createStatement();
         
         SGELog.fine( "Database.sql", sql );
         stmt.executeQuery(sql);
         fireSqlExecuted(sql);
         return stmt;
      } catch( SQLException sqle ) {
         fireSqlFailed(sql,sqle);
         throw createSQLError( "Database.sqlError", new Object[] { sql }, sqle, connection );
      }
   }
   
   /**
    * get the jdbc connection url
    * @return the jdbc connection url
    */
   public String getUrl() {
      return url;
   }
   
   /**
    *  get the type of error of an SQLException
    *
    *  @param   sqle   the SQLException
    *  @return  the type of error
    */
   public int getErrorType( SQLException sqle ) {
      if( errorHandler != null ) {
         return errorHandler.getErrorType( sqle );
      }
      return UNKNOWN_ERROR;
   }
   
   /**
    * Commits the changes to database permanently.
    *
    * @param connection the current connection
    * @param id the id of the <code>CommitEvent</code>
    */
   public void commit(java.sql.Connection connection, int id) throws ReportingException {
      if (connection != null) {
         String name = Thread.currentThread().getName();
         try {
            SGELog.fine("Thread {0} commits {1}", name , connection);
            connection.commit();
            fireCommitExecuted(name, id);
         } catch (SQLException sqle) {
            fireCommitFailed(name, id, sqle);
            throw createSQLError("Database.commitFailed", null, sqle, connection);
         }
      }
   }
   
   public void rollback(java.sql.Connection connection) {
      if (connection != null) {
         // All caches have to be cleared to avoid non existing
         // database objects in the cache
         RecordCache.clearAllCaches();
         try {
            SGELog.fine("rollback {0}", connection);
            connection.rollback();
         } catch (SQLException sqle) {
            createSQLError("Database.rollbackFailed", null, sqle, connection).log();
         }
      }
   }
   
   
   interface ErrorHandler {
      public int getErrorType( SQLException sqle );
   }
   
   static class PostgresErrorHandler implements ErrorHandler {
      
      
      public int getErrorType(SQLException sqle) {
         String state = sqle.getSQLState();
         if( state == null ) {
            return UNKNOWN_ERROR;
         } else if( state.startsWith( "44") ) {
            return NO_CONNECTION_ERROR;
         } else if( state.startsWith( "42" ) ) {
            return SYNTAX_ERROR;
         } else {
            return UNKNOWN_ERROR;
         }
      }
      
   }
   
// AP: TODO create the mysql error handler for mysql
   static class MySQLErrorHandler implements ErrorHandler {
      
      public int getErrorType(SQLException sqle) {
         return UNKNOWN_ERROR;
      }
   }
   
   /**
    *  proxy for the jdbc connection
    */
   public class ConnectionProxy implements java.sql.Connection {
      
      private Connection realConnection;
      private int id;
      
      public ConnectionProxy( java.sql.Connection connection, int id ) throws SQLException {
         realConnection = connection;
         realConnection.setAutoCommit(false);
         this.id = id;
      }
      
      public int getId() {
         return id;
      }
      
      public int getDBType() {
         return Database.this.getType();
      }
      
      public String toString() {
         
         return "Connection " + id + " (" + userName +"@" + Database.this.url + ")";
         
      }
      
      public void clearWarnings() throws java.sql.SQLException {
         realConnection.clearWarnings();
      }
      
      public void close() throws java.sql.SQLException {
         isClosed = true;
         realConnection.close();
      }
      
      public void commit() throws java.sql.SQLException {
         realConnection.commit();
      }
      
      public java.sql.Statement createStatement() throws java.sql.SQLException {
         return realConnection.createStatement();
      }
      
      public java.sql.Statement createStatement(int param, int param1) throws java.sql.SQLException {
         return realConnection.createStatement( param, param1 );
      }
      
      public java.sql.Statement createStatement(int param, int param1, int param2) throws java.sql.SQLException {
         return realConnection.createStatement( param, param1, param2 );
      }
      
      public boolean getAutoCommit() throws java.sql.SQLException {
         return realConnection.getAutoCommit();
      }
      
      public String getCatalog() throws java.sql.SQLException {
         return realConnection.getCatalog();
      }
      
      public int getHoldability() throws java.sql.SQLException {
         return realConnection.getHoldability();
      }
      
      public java.sql.DatabaseMetaData getMetaData() throws java.sql.SQLException {
         return realConnection.getMetaData();
      }
      
      public int getTransactionIsolation() throws java.sql.SQLException {
         return realConnection.getTransactionIsolation();
      }
      
      public java.util.Map getTypeMap() throws java.sql.SQLException {
         return realConnection.getTypeMap();
      }
      
      public java.sql.SQLWarning getWarnings() throws java.sql.SQLException {
         return realConnection.getWarnings();
      }
      
      private boolean isClosed;
      
      public boolean isClosed() throws java.sql.SQLException {
         if( !isClosed ) {
            isClosed = realConnection.isClosed();
         }
         return isClosed;
      }
      
      public boolean getIsClosedFlag() {
         return isClosed;
      }
      
      public boolean isReadOnly() throws java.sql.SQLException {
         return realConnection.isReadOnly();
      }
      
      public String nativeSQL(String str) throws java.sql.SQLException {
         return realConnection.nativeSQL( str );
      }
      
      public java.sql.CallableStatement prepareCall(String str) throws java.sql.SQLException {
         return realConnection.prepareCall( str );
      }
      
      public java.sql.CallableStatement prepareCall(String str, int param, int param2) throws java.sql.SQLException {
         return realConnection.prepareCall( str, param, param2 );
      }
      
      public java.sql.CallableStatement prepareCall(String str, int param, int param2, int param3) throws java.sql.SQLException {
         return realConnection.prepareCall( str, param, param2, param3 );
      }
      
      public java.sql.PreparedStatement prepareStatement(String str) throws java.sql.SQLException {
         return realConnection.prepareStatement( str );
      }
      
      public java.sql.PreparedStatement prepareStatement(String str, String[] str1) throws java.sql.SQLException {
         return realConnection.prepareStatement( str, str1 );
      }
      
      public java.sql.PreparedStatement prepareStatement(String str, int param) throws java.sql.SQLException {
         return realConnection.prepareStatement( str, param );
      }
      
      public java.sql.PreparedStatement prepareStatement(String str, int[] values) throws java.sql.SQLException {
         return realConnection.prepareStatement( str, values );
      }
      
      public java.sql.PreparedStatement prepareStatement(String str, int param, int param2) throws java.sql.SQLException {
         return realConnection.prepareStatement( str, param, param2 );
      }
      
      public java.sql.PreparedStatement prepareStatement(String str, int param, int param2, int param3) throws java.sql.SQLException {
         return realConnection.prepareStatement( str, param, param2, param3 );
      }
      
      public void releaseSavepoint(java.sql.Savepoint savepoint) throws java.sql.SQLException {
         realConnection.releaseSavepoint( savepoint );
      }
      
      public void rollback() throws java.sql.SQLException {
         realConnection.rollback();
      }
      
      public void rollback(java.sql.Savepoint savepoint) throws java.sql.SQLException {
         realConnection.rollback(savepoint);
      }
      
      public void setAutoCommit(boolean param) throws java.sql.SQLException {
         realConnection.setAutoCommit( param );
      }
      
      public void setCatalog(String str) throws java.sql.SQLException {
         realConnection.setCatalog( str );
      }
      
      public void setHoldability(int param) throws java.sql.SQLException {
         realConnection.setHoldability( param );
      }
      
      public void setReadOnly(boolean param) throws java.sql.SQLException {
         realConnection.setReadOnly( param );
      }
      
      public java.sql.Savepoint setSavepoint() throws java.sql.SQLException {
         return realConnection.setSavepoint();
      }
      
      public java.sql.Savepoint setSavepoint(String str) throws java.sql.SQLException {
         return realConnection.setSavepoint( str );
      }
      
      public void setTransactionIsolation(int param) throws java.sql.SQLException {
         realConnection.setTransactionIsolation( param );
      }
      
      public void setTypeMap(java.util.Map map) throws java.sql.SQLException {
         realConnection.setTypeMap( map );
      }
      
   }
}
