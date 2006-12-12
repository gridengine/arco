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

import java.sql.SQLException;
import java.util.Iterator;
import java.util.logging.Level;

public class TestQuotedString extends AbstractDBWriterTestCase {
   
   /** Creates a new instance of TestQuotedString */
   public TestQuotedString() {
   }
   
   public void setUp() throws Exception {
      super.setUp();
   }
   
   
   public void testQuotedString() throws Exception {
      
      
      String debugLevel = DBWriterTestConfig.getTestDebugLevel();
      if( debugLevel == null ) {
         debugLevel = Level.INFO.toString();
      } else {
         System.out.println("debugLevel comes from config file (" + debugLevel + ")" );
      }
      Iterator iter = getDBList().iterator();
      
      while(iter.hasNext()) {
         TestDB db = (TestDB)iter.next();
         db.setDebugLevel(debugLevel);
         try {
            doTestQuotedString(db, debugLevel);
         } finally {
         }
      }
   }
   
   
   private void doTestQuotedString(TestDB db, String debugLevel) throws Exception {
      
      db.cleanDB();
      
      ReportingDBWriter dbw = createDBWriter(debugLevel, db);
      
      SQLHistory sqlHistory = new SQLHistory();
      TestFileWriter writer = new TestFileWriter();
      
      dbw.setReportingFile(writer.getReportingFile().getAbsolutePath());
      dbw.getDatabase().addDatabaseListener(sqlHistory);
      dbw.initialize();
      dbw.start();
      
      try {
         
         long submission = System.currentTimeMillis() / 1000 - 10;
         int jobNumber = 8015;
         int taskNumber = -1;
         String peTaskId = null;
         String jobName = "blubber";
         String owner = "toeddel";
         String group = "nogroup";
         String project = "p1";
         String department = "default";
         String account = "default";
         int    priority = 1;
         
         writer.writeNewJob(submission, jobNumber, taskNumber, peTaskId, jobName, owner,
                 group, project, department, account, priority);
         
         long ts = submission;
         String event = "restart";
         String state = "r";
         String host  = "foo";
         String user  = "execution daemon";
         
         String [] messages = { 
              "job didn't get resources -> schedule it again",
              "job didn\"t get resources -> schedule it again",
              "'job did -> blubber blabber",
              "''ahaha ''"
         };
         
         for(int i = 0; i < messages.length; i++) {
            ts += 1;
            writer.writeJobLog(ts, event, jobNumber, taskNumber, peTaskId, state, host, user,
                    submission, jobName, owner, group, project, department, account,
                    priority, messages[i]);

            assertTrue("rename of reporting file failed", writer.rename());

            writer.waitUntilFileIsDeleted();

            SQLException[] error = new SQLException[1];
            String sql = "INSERT INTO sge_job_log (jl_id, jl_parent, jl_time, jl_event, jl_state, jl_user, jl_host, jl_state_time, jl_message)";
            boolean insertJobLogExecuted = sqlHistory.waitForSqlStatementAndClear(sql, 5000, error);

            assertTrue("INSERT INTO sge_job_log has not net been executed", insertJobLogExecuted);
            assertNull("INSERT INTO sge_job_log produced error", error[0]);
         }         
         
         
         
      } finally {
         shutdownDBWriter(dbw);
      }
   }
}
