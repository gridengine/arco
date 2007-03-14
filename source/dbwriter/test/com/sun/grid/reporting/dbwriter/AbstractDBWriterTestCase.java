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
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import junit.framework.TestCase;

public class AbstractDBWriterTestCase extends TestCase {
   
   private List dbList = new ArrayList();
   private DBWriterTestConfig dbwTestConfig;
   private DBWriterTestConfig[] database;

   private String testName;
   
   private void initConfig(String testName) {
      try {
         String configFile = System.getProperty( "com.sun.grid.reporting.dbwriter.AbstractDBWriterTestCase.configFile" );
         database = DBWriterTestConfig.getDatabases(configFile, testName);
      } catch (IOException ioe) {
         IllegalStateException ilse = new IllegalStateException("can not read database config");
         ilse.initCause(ioe);
      }
   }
   
   
   protected DBWriterTestConfig getDbwTestConfig() {
      return dbwTestConfig;
   }
   
   /** Creates a new instance of AbstractDBWriterTestCase */
   public AbstractDBWriterTestCase() {
   }
   
   public AbstractDBWriterTestCase( String name ) {
      super(name);
   }
   
   public void setUp() throws Exception {
      
      initConfig(this.getName());
      
      for (int i=0; i<database.length; i++) {
         TestDB db = new TestDB(database[i]);
         dbList.add(db);
      }
      
   }

   
   protected List getDBList() {
      return Collections.unmodifiableList(dbList);
   }

   private File tempDir;
   
   public File getTempDir() {
      if( tempDir == null ) {
         String str = System.getProperty("com.sun.grid.reporting.dbwriter.AbstractDBWriterTestCase.tempDir");
         if( str == null ) {
            throw new IllegalStateException("property com.sun.grid.reporting.dbwriter.AbstractDBWriterTestCase.tempDir is not defined");
         }
         tempDir = new File(str);
         if( !tempDir.exists() || !tempDir.isDirectory() ) {
            tempDir = null;
            throw new IllegalStateException("Invalid property com.sun.grid.reporting.dbwriter.AbstractDBWriterTestCase.tempDir");
         }
      }
      return tempDir;
   }
   static int  dbwriterCount = 0;
   protected ReportingDBWriter createDBWriter(String debugLevel, TestDB db) {

      dbwriterCount++;
      ThreadGroup tg = new ThreadGroup("dbwriter" + dbwriterCount);
      ReportingDBWriter dbw = new ReportingDBWriter(tg);
      
      dbw.setJDBCDriver( db.getJDBCDriver() );
      dbw.setJDBCUrl( db.getJDBCUrl() );
      dbw.setJDBCUser( db.getJDBCUser() );
      dbw.setJDBCPassword( db.getJDBCPassword() );
      dbw.setContinous( true );
      dbw.setVacuumSchedule( "off" );
      dbw.setInterval( 2 );
      dbw.initLogging( debugLevel );
      
      File f = new File( getTempDir(), "dbwriter"  + dbwriterCount + ".pid" );

      dbw.setPidFile(f);
      
      return dbw;
   }
   
   
   protected void shutdownDBWriter(ReportingDBWriter dbw) throws InterruptedException {
      
      ThreadGroup tg = dbw.getDbWriterThreadGroup();
      int count = tg.activeCount();
      Thread [] threads = new Thread[count];
      count = tg.enumerate(threads, true);
      
      // Shutdown the dbwriter
      dbw.stopProcessing();

      for(int i = 0; i < count; i++ ) {
         int tries = 0;
         SGELog.fine("waiting for end of thread {0}", threads[i].getName() );
         while( threads[i].isAlive() && tries < 10 ) {
            System.out.print(".");
            threads[i].join(1000);
            tries++;
         }
         assertEquals( "dbwriter thread " + threads[i].getName() + " did not shutdown in 10 seconds", tries < 10, true );
      }
   }
   
}
