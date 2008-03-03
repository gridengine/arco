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
package com.sun.grid.arco;

import com.sun.grid.arco.model.NamedObject;
import com.sun.grid.arco.model.Query;
import com.sun.grid.logging.SGELog;
import java.io.File;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.TestCase;

/**
 * Test the behavour of the class com.sun.grid.arco.QueryManager and
 * com.sun.grid.arco.ResultManager
 *
 */
public class FileManagerTest extends TestCase {
   
   /** 
    *  Creates a new FileManagerTest
    *
    *  @param  name   name of the test
    */
   public FileManagerTest(String name) {
      super(name);
   }
   
   private File testdir;
   
   protected void setUp() throws java.lang.Exception {
      SGELog.init( Logger.getLogger(this.getClass().getName()) );
      File tmpDir = new File("target/tmp");
      tmpDir.mkdirs();
      
      testdir = new File(tmpDir,"testDuplicateName");      
      testdir.mkdir();
   }
   
   protected void tearDown() throws Exception {

      super.tearDown();

      deleteFiles();
      
      if(!testdir.delete()) {
         throw new IllegalStateException("Can not delete directory " + testdir);
      }
   }
   
   private void deleteFiles() {
      File [] files = testdir.listFiles();
      for(int i = 0; i < files.length; i++ ) {
         if(!files[i].delete()) {
            throw new IllegalStateException("Can not delete file " + files[i]);
         }
      }
   }
   
   
   /**
    *  This method tests, wether the QueryManager can handle
    *  queries with duplication names.
    *  see CR 6351846
    *  @throws Exception
    */
   public void testDuplicateName() throws Exception {
      
      deleteFiles();
      
      QueryManager qm = new QueryManager(testdir);
      
      
      Query query = qm.createQuery();
      
      query.setName("sample");
      
      File [] files = new File [] {
          new File(testdir,"sample1.xml"),
          new File(testdir,"sample2.xml"),
          new File(testdir,"sample3.xml")
      };
      
      for(int i = 0; i < files.length; i++ ) {
         qm.save(query,files[i]);
      }

      NamedObject[] objs = qm.getAvailableObjects();
      
      assertEquals(files.length, objs.length);

      for(int i = 0; i < objs.length; i++ ) {
         SGELog.info("Got query " + objs[i].getName() );
         assertNotNull("Object name must not be null", objs[i].getName() );
         query = qm.getQueryByName(objs[i].getName());
         assertNotNull("Query must not be null", query );
         assertEquals("Query and Object name must be equal", objs[i].getName(), query.getName());
      }
   
      deleteFiles();
   }
   
   /**
    *  This method tests the multi threaded scaning of the a directory
    *
    *  We setup three thread which scans the test directory via a query manager
    *  A ScanCounter log handler count how often the directory is really scaned.
    *  We make assertion that
    * 
    *    o the scan count is lower than the number of scans.
    *    o all scan threads have finished their tasks
    *  
    *  @throws Exception
    */
   public void testScan() throws Exception {
      
      ScanCounter scanCounter = new ScanCounter();
      
      scanCounter.setLevel(Level.FINE);
      
      QueryManager qm = new QueryManager(testdir);
      

      SGELog.getLogger().addHandler(scanCounter);
      
      Level orgLevel = SGELog.getLogger().getLevel();
      
      SGELog.getLogger().setLevel(Level.FINE);
      
      TestScanThread [] threads = new TestScanThread[3];
      int scanCountPerThread = 100;
      
      for(int i = 0; i < threads.length; i++) {
         threads[i] = new TestScanThread(qm, scanCountPerThread);
         threads[i].start();
      }
      
      int count = 3;
      Query queries [] = new Query [count];
      
      File [] files = new File [count];
      
      for(int i = 0; i < 3; i++ ) {
         queries[i] = qm.createQuery();
         queries[i].setName("sample" + i);
         qm.saveQuery(queries[i]);
      }
      
      for(int i = 0; i < threads.length; i++ ) {
         threads[i].waitForEnd(60000);
      }
      
      System.out.println("I have " + scanCounter.getScanCount() + " scan counts " );
      assertEquals("Scan count to high", true,  threads.length * scanCountPerThread  >= scanCounter.getScanCount() );
      
      SGELog.getLogger().removeHandler(scanCounter);
      SGELog.getLogger().setLevel(orgLevel);
      
      deleteFiles();
   }
   
   
   /**
    *  Helper class for the testScan method
    *  Counts how often the test directory is scaned
    */
   class ScanCounter extends Handler {
      
      private int scanCount = 0;
      
      public void close() throws SecurityException {
      }

      public void flush() {
      }
      
      public int getScanCount() {
         return scanCount;
      }
      

      public synchronized void publish(java.util.logging.LogRecord logRecord) {
         if(logRecord.getMessage().startsWith("scanning directory ")) {
            scanCount++;
            try {
               Thread.sleep(10);
            } catch(InterruptedException ire ) {}
         }
      }
   }
   
   /**
    *  Helper class for the testScan method
    *  Implements a thread which scans the test directory
    */
   class TestScanThread extends Thread {
      
      private QueryManager   qm;
      private Exception      error;
      private NamedObject [] objects;
      private boolean running = true;
      private int scanCount;
      
      public TestScanThread(QueryManager qm, int scanCount) {
         this.qm = qm;
         this.scanCount = scanCount;
      }
              
      public void run() {
         try {
            for(int i = 0; i < scanCount; i++ ) {
               qm.getAvailableObjects();
               Thread.yield();
            }
         } catch(Exception e) {
            error = e;
         } finally {
            synchronized(this) {
               running = false;
               notifyAll();
            }
         } 
      }
      
      public synchronized void waitForEnd(long timeout) throws InterruptedException {
         if(running) {
            wait(timeout);
         }
         if(running) {
            throw new IllegalStateException("Timeout while waiting for end of thread");
         }
      }
   }

   
}
