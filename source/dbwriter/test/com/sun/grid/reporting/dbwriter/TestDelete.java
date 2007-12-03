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
import com.sun.grid.logging.SGELog;
import com.sun.grid.reporting.dbwriter.event.CommitEvent;
import java.util.*;
import java.io.*;
import java.sql.*;
import com.sun.grid.reporting.dbwriter.db.Database;
import java.util.logging.Level;

/**
 * This Test tests the deletion rules of the dbwriter
 *
 */
public class TestDelete extends AbstractDBWriterTestCase {
   
   public TestDelete(String name ) {
      super(name);
   }
   
   /**
    * setup method
    * <li>
    *    <li> clean the database
    *    <li> read the configuration
    * </li>
    * @throws Exception
    */
   public void setUp() throws Exception {
      super.setUp();
   }
   
   public void testDelete() throws Exception {
      
      String debugLevel = DBWriterTestConfig.getDebugLevel();
      if( debugLevel == null ) {
         debugLevel = Level.INFO.toString();
      } else {
         System.out.println("debugLevel comes from config file (" + debugLevel + ")" );
      }
      
      Iterator iter = getDBList().iterator();
      
      while(iter.hasNext()) {
         TestDB db = (TestDB)iter.next();
         String orgDebugLevel = db.getDebugLevel();
         db.setDebugLevel(debugLevel);
         try {
            doDelete(db, debugLevel);
         } finally {
            db.setDebugLevel(debugLevel);
         }
      }
   }
   
   /**
    * This test performs the following steps:
    * <ul>
    *    <li> setup the dbwriter, the calculation file contains
    *         one deletion rule which deletes all sge_host_values
    *         with hv_variable = "cpu" which are older than one hour
    *    </li>
    *    <li> Write three host value lines with timestamp now - 3 hours.
    *         The dbwriter will export this lines, but the derived value
    *         thread must not delete this values.
    *    <li> Query the database table sge_host_values
    * (must contain 3 cpu values) </li>
    *    <li> Write one host value line with timestamp now - 1 hour.
    *         The dbwriter will export this line and the derived value thread
    *         has to delete the three previously import cpu values.
    *    <li> Query the database table sge_host_values
    *         (must contain 1 cpu value </li>
    *
    * </ul>
    * @param  db   database on which the test will be executed
    * @throws Exception
    */
   private void doDelete(TestDB db, String debugLevel) throws Exception {
      
      db.cleanDB();
      
      
      ReportingDBWriter dbw = createDBWriter(debugLevel,db);
      
      SQLHistory sqlHistory = new SQLHistory();
      
      
      String calculationRule = DBWriterTestConfig.getTestCalculationFile();
      
      File calcFile = File.createTempFile( "testDeleteCalc", ".xml", null );
      calcFile.deleteOnExit();
      
      FileWriter fw = new FileWriter(calcFile);
      fw.write(calculationRule);
      fw.flush();
      fw.close();
      
      TestFileWriter writer = new TestFileWriter();
      
      dbw.setReportingFile( writer.getReportingFile().getAbsolutePath() );
      
      dbw.getDatabase().addDatabaseListener(sqlHistory);
      dbw.getDatabase().addCommitListener(sqlHistory);
      
      Calendar cal = Calendar.getInstance();
      
      // write a reporting file with three host value lines
      // We choose a timestamps which are two hours in the past
      // to ensure that the derived value thread don not sleep.
      cal.add( Calendar.HOUR, -3 );
      cal.set( Calendar.MINUTE, 1 );
      cal.set( Calendar.SECOND, 0 );
      cal.set( Calendar.MILLISECOND, 0 );
      
      
      writer.writeHostLine( cal.getTimeInMillis() );
      
      cal.add( Calendar.MINUTE, 10 );
      writer.writeHostLine( cal.getTimeInMillis() );
      
      cal.add( Calendar.MINUTE, 10 );
      writer.writeHostLine( cal.getTimeInMillis() );
      
      assertTrue( "Renaming failed", writer.rename());
      
      // start the dbwriter, it will parse the three lines and write int into
      // the database
      
      dbw.initialize();
      
      dbw.start();
      
      try {
         writer.waitUntilFileIsDeleted();
         
         assertTrue("Error on dbwriter startup, dbwriter thread is not alive", dbw.isAlive());
         
         // Raw values must exists, despite they are older than two hours
         int rawValues = queryRawValues(dbw.getDatabase());
         assertEquals( "No raw values found", 3, rawValues);
         
         // Now set the calculation file
         // the deletion rules should be active
         dbw.setCalculationFile( calcFile.getAbsolutePath() );
         
         // Now write a line in the next hour
         // The derived value thread should after that
         // delete the first 3 host values
         cal.add( Calendar.HOUR, 3 );
         
         SGELog.info("Before writing lin that should trigger deletion:");
         writer.writeHostLine( cal.getTimeInMillis() );
         assertTrue( "Renaming failed", writer.rename());
         
         SGELog.info("Before deleting the file:");
         // Sleep to ensure the the dbwriter has be started
         // and the derived values thread had its first cylce
         writer.waitUntilFileIsDeleted();

         Thread.sleep(2000);
         
         SQLException[] error = new SQLException[1];
         
         SGELog.info("Before find SQL:");
         
         boolean deleteExecuted = sqlHistory.waitForSqlStatementAndClear(
               "DELETE FROM sge_host_values WHERE hv_id IN", 10000, error);
         assertTrue( "delete statement has not been executed", deleteExecuted);
         
         CommitEvent event = new CommitEvent(dbw.DERIVED_THREAD_NAME,
               CommitEvent.DELETE, new SQLException());
         boolean commitExecuted = sqlHistory.waitForCommitAndClear(event, 10000);
         assertTrue("commit of the delete statement has not been executed", commitExecuted);
         //make sure commit did not produce error
         assertNull("commit '" + event.toString() + "' produced error", event.getError());
         
         rawValues = queryRawValues(dbw.getDatabase());
         assertEquals( "Too much raw values found", 1, rawValues);    
         
         event = new CommitEvent(dbw.DERIVED_THREAD_NAME, CommitEvent.STATISTIC_INSERT,
               new SQLException());
         //we have to wait for a another delete commit, since we now limit the number of rows
         //deleted in one transaction. I needs to make another pass before to get return 0
         commitExecuted = sqlHistory.waitForCommitAndClear(event, 10000);
         assertEquals("commit of the delete statistic has not been executed",
               commitExecuted, true);
         assertNull("commit '" + event.toString() + "' produced error", event.getError());
         
      } finally {
         shutdownDBWriter(dbw);
      }
   }
   
   private int queryRawValues(Database db) throws Exception {
      Connection conn = db.getConnection();
      try {
         String sql = DBWriterTestConfig.getTestRawVariableSQL();
         Statement stmt = db.executeQuery( sql, conn );
         try {
            ResultSet rs = stmt.getResultSet();
            try {
               if( rs.next() ) {
                  return rs.getInt(1);
               } else {
                  return 0;
               }
            } finally {
               rs.close();
            }
         } finally {
            stmt.close();
         }
      } finally {
         db.release(conn);
      }
   }
   
}
