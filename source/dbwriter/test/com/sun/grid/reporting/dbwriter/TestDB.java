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
package com.sun.grid.reporting.dbwriter;

import com.sun.grid.util.SQLUtil;
import com.sun.grid.util.sqlutil.Command;
import java.util.Map;
import java.util.logging.Level;

/**
 * Database object for a test database
 */
public class TestDB {
   
   public static final int DB_TYPE_POSTGRES = 1;
   public static final int DB_TYPE_ORACLE = 2;
   
   
   private static final String [] TABLES = new String[] {
      "SGE_JOB_USAGE", "SGE_JOB_LOG", "SGE_JOB_REQUEST",
      "SGE_JOB", "SGE_QUEUE_VALUES", "SGE_QUEUE",
      "SGE_HOST_VALUES", "SGE_HOST", "SGE_DEPARTMENT_VALUES",
      "SGE_DEPARTMENT", "SGE_PROJECT_VALUES", "SGE_PROJECT",
      "SGE_USER_VALUES", "SGE_USER", "SGE_GROUP_VALUES", "SGE_GROUP",
      "SGE_SHARE_LOG", "SGE_VERSION"
   };
   
   private static final String [] VIEWS = new String [] {
      "view_job_times", "view_jobs_completed",
      "view_job_log", "view_department_values", "view_group_values",
      "view_host_values",  "view_project_values", "view_queue_values",
      "view_user_values" , "view_accounting"
   };

   public static final String DEFAULT_DEBUG_LEVEL = Level.INFO.toString();
   private SQLUtil sqlUtil = new SQLUtil();
   
   private DBWriterTestConfig config;
   private String debugLevel;
   
   /** Creates a new instance of TestDB */
   public TestDB(DBWriterTestConfig config) {
      
      this.config = config;
      
      setDebugLevel(DEFAULT_DEBUG_LEVEL);      
   }

   protected String getDBIdentifier() {
      return config.getIdentifier();      
   }
   
   
   protected String getJDBCDriver() {
      return config.getDriver();      
   }
   
   protected String getJDBCUrl() {
      return config.getUrl();      
   }
   
   protected String getJDBCUser() {
      return config.getUser();  
   }

   protected String getJDBCPassword() {
      return config.getPassword();  
   }
   
   protected String getReadOnlyUser() {
      return config.getReadOnlyUser();
   }
   
   protected String getSchema() {
      return config.getSchema();
   }
   
   int dbversion = -1;
   protected int getDBVersion() {
      if( dbversion < 0 ) {
         dbversion = config.getDbversion();
      }
      return dbversion;
   }
   
   private int dbType = -1;
           
   public int getDBType() {
      if( dbType < 0 ) {
         String driver = getJDBCDriver();
         if(driver.equals("org.postgresql.Driver")) {
            dbType = DB_TYPE_POSTGRES;
         } else if (driver.equals("oracle.jdbc.driver.OracleDriver")) {
            dbType = DB_TYPE_ORACLE;
         } else {
            throw new IllegalStateException("Can not determine dbtype for jdbc driver " + driver);
         }
      }
      return dbType;
   }
   
   
   protected String getDBDefinition() {
      return config.getDbdefinition();
   }

   /**
    * Installs the ARCO database.
    * <b>!!Attention!!</b> 
    * All existing tables and views of the database will be dropped.
    * @throws Exception
    */   
   protected int installDB() throws Exception {

      connect();
      dropDB();
      
      setEnv("READ_USER", getReadOnlyUser() );
      
      Command cmd = sqlUtil.getCommand( "install" );
      
      return cmd.run( getDBVersion() + " " + getDBDefinition() + " " + getSchema() );      
   }
   
   private void connect() {
      
      if( sqlUtil.getConnection() == null ) {      
         Command cmd = sqlUtil.getCommand( "connect" );
         int result = cmd.run( getJDBCDriver() + " " + getJDBCUrl() 
                             + " " + getJDBCUser() + " " + getJDBCPassword() );
      }
   }
   
   private void dropPostgresDB() {
      int result = 0;
      Command cmd = sqlUtil.getCommand( "drop" );
      Command debugCmd = sqlUtil.getCommand("debug");
      String orgLevel = getDebugLevel();
      try {
         setDebugLevel("OFF");
         for( int i = 0; i < VIEWS.length; i++ ) {
            result = cmd.run( " VIEW " + VIEWS[i] );
         }
         for( int i = 0; i < TABLES.length; i++ ) {
            result = cmd.run( " TABLE " + TABLES[i] );
         }      
      } finally {
         setDebugLevel(orgLevel);
      }
   }
   
   private void dropOracleDB() {
      
      String rdUser = getReadOnlyUser();
      int result = 0;
      Command cmd = sqlUtil.getCommand( "drop" );
      String orgLevel = getDebugLevel();
      setDebugLevel("OFF");
      try {
         for( int i = 0; i < VIEWS.length; i++ ) {
            result = cmd.run( " SYNONYM " + rdUser + "." + VIEWS[i] );
         }
         for( int i = 0; i < TABLES.length; i++ ) {
            result = cmd.run( " SYNONYM " + rdUser + "." + TABLES[i] );
         }         
         for( int i = 0; i < VIEWS.length; i++ ) {
            result = cmd.run( " VIEW " + VIEWS[i] + " CASCADE CONSTRAINTS" );
         }
         for( int i = 0; i < TABLES.length; i++ ) {
            result = cmd.run( " TABLE " + TABLES[i]+ " CASCADE CONSTRAINTS" );
         }      
      } finally {
         setDebugLevel(orgLevel);
      }
   }
   
   protected void dropDB() {
      
      connect();

      switch( getDBType() ) {
         case DB_TYPE_POSTGRES:
            dropPostgresDB();
            break;
         case DB_TYPE_ORACLE:
            dropOracleDB();
            break;
         default:
            throw new IllegalStateException("Unknown DB type (" + dbType +")");
      }
   }
   
   
   protected void cleanDB() {
      
      connect();
      
      Command cmd = sqlUtil.getCommand( "delete" );
      
      int result;
      for( int i = 0; i < TABLES.length; i++ ) {
         if( !TABLES[i].equalsIgnoreCase("SGE_VERSION")) {
            result = cmd.run( "from " + TABLES[i] );
         }
      }      
   }
   
   
   public String getDebugLevel() {
      return debugLevel;
   }
   
   public void setDebugLevel(String debugLevel) {
      Command cmd = sqlUtil.getCommand( "debug" );
      if( cmd.run(debugLevel) != 0 ) {
         throw new IllegalStateException("debug command failed");
      }
      this.debugLevel = debugLevel;
   }
   
   private void setEnv( String name, String value ) {
      Command cmd = sqlUtil.getCommand( "set" );
      if( cmd.run( name + " " + value ) != 0 ) {
         throw new IllegalStateException("set env command failed");
      }      
   }
   
}
