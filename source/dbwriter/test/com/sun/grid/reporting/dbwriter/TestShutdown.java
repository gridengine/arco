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
import com.sun.grid.reporting.dbwriter.db.Database;
import java.util.Iterator;
import java.util.logging.Level;
import junit.framework.Test;
import junit.framework.TestSuite;

public class TestShutdown extends AbstractDBWriterTestCase {
   
   String debugLevel;
   long   timeout;
   
   /** Creates a new instance of TestParsing */
   public TestShutdown(String name) {
      super(name);
   }
   
   public void setUp() throws Exception {
      
      super.setUp();
      
      debugLevel = DBWriterTestConfig.getTestDebugLevel();
      if( debugLevel == null ) {
         debugLevel = Level.INFO.toString();         
      }
      timeout = DBWriterTestConfig.getTestTimeout() * 1000;
   }
   
   
   public static Test suite() {
      TestSuite suite = new TestSuite(TestShutdown.class);
      return suite;
   }
   
   public void testShutdown() throws Exception {
      Iterator iter = getDBList().iterator();

      while(iter.hasNext()) {
         TestDB db = (TestDB)iter.next();
         String orgDebugLevel = db.getDebugLevel();
         try {
            db.setDebugLevel(debugLevel);
            doShutdown(db);
         } finally {
            db.setDebugLevel(orgDebugLevel);
         }
      }      
   }
   
   
   /**
    * Tests the shutdown of the dbwriter
    * All threads have to die, and the 
    * pid file have to be deleted
    * This test finds the Bug 6320688
    *
    * @throws java.lang.Exception 
    */
   private void doShutdown(TestDB db) throws Exception {

      
      ReportingDBWriter dbw = createDBWriter(debugLevel, db);
      
      TestFileWriter writer = new TestFileWriter();

      dbw.setReportingFile(writer.getReportingFile().getAbsolutePath());
      dbw.setVacuumSchedule(ReportingDBWriter.DEFAULT_VACUUM_SCHEDULE);
      dbw.initialize();
      dbw.start();
      
      Thread.sleep(2000);
      
      String [] threadNames = null;
      
      switch(dbw.getDatabase().getType()) {
         case Database.TYPE_POSTGRES:
            threadNames = new String [] { ReportingDBWriter.REPORTING_THREAD_NAME, 
                            ReportingDBWriter.DERIVED_THREAD_NAME, 
                            ReportingDBWriter.VACUUM_THREAD_NAME };
            break;
         case Database.TYPE_ORACLE:
            /* vacuum thread will not run on oracle database */
            threadNames = new String[] { ReportingDBWriter.REPORTING_THREAD_NAME, 
                            ReportingDBWriter.DERIVED_THREAD_NAME };
            break;
         default:
            throw new IllegalStateException("Unknown database type " + dbw.getDatabase().getType());
      }
      
      ThreadGroup threadGroup = dbw.getDbWriterThreadGroup();
      // Wait for the startup of all threads
      
      int threadCount = 0;
      Thread [] threads = null;

      long startTime = System.currentTimeMillis();
      long endTime   = startTime + timeout;
      
      for(int i = 0; i < threadNames.length; i++ ) {
         
         boolean found = false;
         do {
            threads = new Thread[threadNames.length];

            threadCount = threadGroup.enumerate(threads, true);
            for(int ii=0; ii < threadCount; ii++) {

               if( threads[ii].getName().equals(threadNames[i])) {
                  SGELog.fine("thread {0} has been started", threadNames[i] );
                  found = true;
                  break;
               }
            }
            
            assertTrue("Timeout while waiting on startup of thread " + threadNames[i], System.currentTimeMillis() < endTime );
            
            if( !found ) {
               Thread.sleep(1000);
            }
            
         } while (!found);
      }
      
      // Test wether the pid file exits      
      assertTrue("PID file " + dbw.getPidFile() + " does not exists" , dbw.getPidFile().exists() );
      
      // Now stop the dbwriter and ensure that all threads has died
      
      dbw.stopProcessing();
      
      startTime = System.currentTimeMillis();
      endTime = startTime + timeout;
      
      while(true) {
         threadCount = threadGroup.activeCount();
         if( threadCount == 0 ) {
            break;
         }
         assertTrue("Timeout while waiting on the end of all dbwriter threads ", System.currentTimeMillis() < endTime );         
         Thread.sleep(1000);
         
      }
      
      // Test wether the pid file has been deleted
      assertFalse("PID file " + dbw.getPidFile() + " has not been deleted", dbw.getPidFile().exists() );
      
   }
   
}
