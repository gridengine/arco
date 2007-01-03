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
import junit.framework.*;
import java.io.*;
import java.util.*;
import java.sql.*;

import com.sun.grid.reporting.dbwriter.db.Database;
import java.util.logging.Level;

public class TestDerivedValues extends AbstractDBWriterTestCase {
   
   private String debugLevel;
   
   public TestDerivedValues(String name) {
      super(name);
   }
   
   public static Test suite() {
      TestSuite suite = new TestSuite(TestDerivedValues.class);
      return suite;
   }
   
   public void setUp() throws Exception {
      
      super.setUp();
      
      debugLevel = DBWriterTestConfig.getTestDebugLevel();
      if( debugLevel == null ) {
         debugLevel = Level.INFO.toString();         
      }
   }
   
   
   
   public void testDelayedImport() throws Exception {
      Iterator iter = getDBList().iterator();
      
      while(iter.hasNext()) {
         TestDB db = (TestDB)iter.next();
         String orgDebugLevel = db.getDebugLevel();
         try {
            db.setDebugLevel(debugLevel);
            delayedImport(db);
         } finally {
            db.setDebugLevel(orgDebugLevel);
         }
      }      
   }

   public void testVaccumAnalyse() throws Exception {
      Iterator iter = getDBList().iterator();
      
      while(iter.hasNext()) {
         TestDB db = (TestDB)iter.next();
         String orgDebugLevel = db.getDebugLevel();
         if( db.getDBType() == Database.TYPE_POSTGRES ) {
            db.setDebugLevel(debugLevel);
            try {
               vaccumAnalyse(db);
            } finally {
               db.setDebugLevel(orgDebugLevel);
            }
         }
      }      
   }
   
   /**
    *  This test method tests the correct calculation of the
    *  derived values, even if the import of the raw data is
    *  delayed.
    *  This test method runs about an hour
    */
   private void delayedImport(TestDB db) throws Exception {
      
      db.cleanDB();
   
      ReportingDBWriter dbw = createDBWriter(debugLevel, db);
      
      
      File calcFile = File.createTempFile( "testDelayedImport", ".xml", null );
      calcFile.deleteOnExit();
      String calculationRule = DBWriterTestConfig.getTestCalculationFile();
      FileWriter fw = new FileWriter(calcFile);
      fw.write(calculationRule);
      fw.flush(); 
      fw.close();      
      dbw.setCalculationFile( calcFile.getAbsolutePath() );
      
      TestFileWriter writer = new TestFileWriter();
      
      dbw.setReportingFile( writer.getReportingFile().getAbsolutePath() );
      
      Calendar cal = Calendar.getInstance();
            
      // write a reporting file with three host value lines
      // We choose a timestamps which are two hours in the past
      // to ensure that the derived value thread don not sleep.
      cal.add( Calendar.HOUR, -2 );      
      cal.set( Calendar.MINUTE, 1 );
      cal.set( Calendar.SECOND, 0 );
      cal.set( Calendar.MILLISECOND, 0 );
      
      writer.writeHostLine( cal.getTimeInMillis() );
      cal.add( Calendar.MINUTE, 10 );
      writer.writeHostLine( cal.getTimeInMillis() );
      cal.add( Calendar.MINUTE, 10 );
      writer.writeHostLine( cal.getTimeInMillis() );
      
      assertEquals( "Renaming failed", writer.rename(), true );
      
      // start the dbwriter, it will parse the three lines and write int into
      // the database
      
      dbw.initialize();
      
      dbw.start();
      
      try {
      
         writer.waitUntilFileIsDeleted();

         assertEquals( "Error on dbwriter startup, dbwriter thread is not alive", dbw.isAlive(), true );

         // No hour values should be available in the database
         int hourValues = queryHourValues(dbw.getDatabase());
         assertEquals( "No hour values expected", hourValues, 0);

         // Now write a line in the next hour
         // The derived value thread should after that 
         // calculate the variable h_cpu_sum

         cal.add( Calendar.HOUR, 1 );

         writer.writeHostLine( cal.getTimeInMillis() );
         assertEquals( "Renaming failed", writer.rename(), true );

         // Sleep to ensure the the dbwriter has be started
         // and the derived values thread had its first cylce
         writer.waitUntilFileIsDeleted();

         // We wait here for max. 10 seconds. If the derived values
         // has not been executed in this we will run into an error
         // A better solution would be to wait for the expected
         // sql command (something like "INSERT INTO sge_host_values (*, 'h_cpu_count', *)"
         // The command can only be found with an regular
         // expression. The class SQLHistory has to be extended.
         
         int tries = 0;
         do {
            Thread.sleep(1000);
            hourValues = queryHourValues(dbw.getDatabase());      
            tries++;
         } while(hourValues <= 0 && tries < 10);
         
         assertEquals( "No hour value found", hourValues > 0 , true);
      } finally {
         // Shutdown the dbwriter
         shutdownDBWriter(dbw);
      }
    }
   

   /**
    *  this method tests the execution of the vaccum analyse
    *  sql statement even of a derived value rule or a deletion rule files
    *  Bug: 6274376
    *  This test is only executed for postgres
    */
   private void vaccumAnalyse(TestDB db) throws Exception {
      
   
      String invalidSQL="BLUBBER BLABBER";
      
      ReportingDBWriter dbw = createDBWriter(debugLevel, db);

      SGELog.info("START ----------------------------------------------------------");
      SGELog.info("START ------------- vaccumAnalyse ------------------------------");
      SGELog.info("START ----------------------------------------------------------");
      db.cleanDB();
      
      SQLHistory sqlHistory = new SQLHistory();
      
      File calcFile = File.createTempFile( "testVaccumAnalyse", ".xml", null );
      calcFile.deleteOnExit();
      
      FileWriter fw = new FileWriter(calcFile);
      PrintWriter pw = new PrintWriter(fw);
      
      pw.println("<DbWriterConfig>");
      pw.println("<derive object='host' interval='hour' variable='h_cpu'>");
      pw.print("<sql>");
      pw.print(invalidSQL);
      pw.println("</sql></derive></DbWriterConfig>");
      pw.flush();
      fw.close();      
      dbw.setCalculationFile( calcFile.getAbsolutePath() );
      
      TestFileWriter writer = new TestFileWriter();
      
      dbw.setReportingFile( writer.getReportingFile().getAbsolutePath() );
      // start the vaccuum every minute
      dbw.setVacuumSchedule("+0 +0 +1 0");
      dbw.getDatabase().addDatabaseListener(sqlHistory);
      
      // We have to write on line into the reporting file
      // since the derived values thread will not start before
      // a value is available
      writer.writeHostLine( System.currentTimeMillis() - 40 * 1000 );

      dbw.initialize(); 
      
      assertEquals( "Renaming failed", writer.rename(), true );
      
      // start the dbwriter, it will parse the  line and write int into
      // the database
      
      
      dbw.start();
      
      
      try {

         writer.waitUntilFileIsDeleted();

         assertEquals( "Error on dbwriter startup, dbwriter thread is not alive", dbw.isAlive(), true );

         String sql = invalidSQL;
         long timeout = 2 * 60000;
         SQLException [] error = new SQLException [] { null };

         boolean hasBeenExecuted = sqlHistory.waitForSqlStatementAndClear(sql, timeout, error );

         assertTrue("sql statement '" + sql + "' has not been executed", hasBeenExecuted );
         assertNotNull("sql statement '" + sql + "' did not produce an error", error[0] );

         sql = "VACUUM ANALYZE";
         error[0] = null;

         hasBeenExecuted = sqlHistory.waitForSqlStatementAndClear(sql, timeout, error );

         assertTrue("sql statement '" + sql + "' has not been executed", hasBeenExecuted );
      } finally {
         // Shutdown the dbwriter
         SGELog.info("END ----------------------------------------------------------");
         SGELog.info("END ------------- vaccumAnalyse ------------------------------");
         SGELog.info("END ----------------------------------------------------------");
         shutdownDBWriter(dbw);
      }
   }

    
   /**
    * query the hour value entries from the database.
    * @param db          database of the dbwriter
    * @throws Exception  can throw any exception
    * @return number of hour value entries
    */   
    private int queryHourValues(Database db) throws Exception {
      Connection conn = db.getConnection();      
      try {
         String sql = DBWriterTestConfig.getTestHourVariableSQL();
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
