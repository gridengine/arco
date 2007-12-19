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
 *   Copyright: 2007 by Sun Microsystems, Inc.
 *
 *   All Rights Reserved.
 *
 ************************************************************************/
/*___INFO__MARK_END__*/

package com.sun.grid.reporting.dbwriter;

import com.sun.grid.logging.SGELog;
import com.sun.grid.reporting.dbwriter.db.BackupStatement;
import com.sun.grid.reporting.dbwriter.db.Database;
import com.sun.grid.reporting.dbwriter.db.Database.ConnectionProxy;
import com.sun.grid.reporting.dbwriter.db.Record;
import junit.framework.*;
import java.util.*;
import java.sql.*;
import java.util.logging.Level;

/**
 * It test that even if there was an error in one of the statements in the batch
 * all the correct statements are inserted in the database
 */
public class TestBatchInserts extends AbstractDBWriterTestCase {
   
   private String debugLevel;
   
   /** Creates a new instance of TestBatchInserts */
   public TestBatchInserts(String name) {
      super(name);
   }
   
   public static Test suite() {
      TestSuite suite = new TestSuite(TestBatchInserts.class);
      return suite;
   }
   
   public void setUp() throws Exception {
      
      super.setUp();
      
      debugLevel = DBWriterTestConfig.getTestDebugLevel();
      if( debugLevel == null ) {
         debugLevel = Level.INFO.toString();
      }
   }
   
   /**
    * Tests if the batch continues execution even, if one of the
    * Statements in the batch produces error
    */
   public void testBatchExecution() throws Exception {
      Iterator iter = getDBList().iterator();
      
      while(iter.hasNext()) {
         TestDB db = (TestDB)iter.next();
         String orgDebugLevel = db.getDebugLevel();
         try {
            db.setDebugLevel(debugLevel);
            batchExecution(db);
         } finally {
            db.setDebugLevel(orgDebugLevel);
         }
      }
   }
   
   private void batchExecution(TestDB db) throws Exception {
      db.cleanDB();
      
      ReportingDBWriter dbw = createDBWriter(debugLevel, db);
      
      TestFileWriter writer = new TestFileWriter();
      
      dbw.setReportingFile(writer.getReportingFile().getAbsolutePath());
           
      
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
      
      //jobLog specific
      long ts = submission;
      Timestamp time =  new Timestamp(ts);
      String event = "restart";
      String state = "r";
      String host  = "foo";
      String user  = "execution daemon";
      String message = "job scheduled for execution";
      int stateTime = 50;
      
      String jobPstmStr = "INSERT INTO sge_job (j_id, j_job_number, j_task_number, j_pe_taskid, j_job_name, j_group, " +
            "j_owner, j_account, j_priority, j_submission_time, j_project, j_department) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
      String jobLogPstmStr = "INSERT INTO sge_job_log (jl_id, jl_parent, jl_time, jl_event, jl_state, jl_user, " +
            "jl_host, jl_state_time, jl_message) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";
      
      writer.writeHostLine(submission);
      
      assertEquals( "Renaming failed", writer.rename(), true );
      
      dbw.initialize();
      
      try {
         dbw.start();
         
         writer.waitUntilFileIsDeleted();
         
         assertEquals( "Error on dbwriter startup, dbwriter thread is not alive", true, dbw.isAlive() );
         
         RecordManager logManager = dbw.jobLogManager;
         RecordManager jobManager = dbw.jobManager;
         Controller controller = dbw.controller;
         
         Record record = logManager.newDBRecord();
         Connection connection = dbw.getDatabase().getConnection();
         
         PreparedStatement jobPstm = ((ConnectionProxy) connection).prepareStatement(jobPstmStr, jobManager);
         
         //we insert one record in jobManager's batch
         StringBuffer jobStmt = new StringBuffer();
         jobStmt.append("INSERT INTO sge_job (j_id, j_job_number, j_task_number, j_pe_taskid, j_job_name, j_group, " +
               "j_owner, j_account, j_priority, j_submission_time, j_project, j_department) VALUES (");
         
         jobPstm.setInt(1, 1);
         jobStmt.append(1+",");
         jobPstm.setInt(2, jobNumber);
         jobStmt.append(jobNumber+",");
         jobPstm.setInt(3, taskNumber);
         jobStmt.append(taskNumber+",");
         jobPstm.setString(4, peTaskId);
         jobStmt.append("'" +peTaskId+"',");
         jobPstm.setString(5, jobName);
         jobStmt.append("'" +jobName+"',");
         jobPstm.setString(6, group);
         jobStmt.append("'" +group+"',");
         jobPstm.setString(7, owner);
         jobStmt.append("'" +owner+"',");
         jobPstm.setString(8, account);
         jobStmt.append("'" +account+"' ,");
         jobPstm.setInt(9, priority);
         jobStmt.append(priority+",");
         jobPstm.setTimestamp(10, time);
         jobStmt.append("{ts '" +time+ "'} ,");
         jobPstm.setString(11, project);
         jobStmt.append("'" +project+"',");
         jobPstm.setString(12, department);
         jobStmt.append("'" +department+"' )");
         jobPstm.addBatch();
         BackupStatement jobBackup = new BackupStatement(new Integer(1), jobStmt.toString());
         ((ConnectionProxy) connection).insertBackup(jobManager, jobBackup);
         
         //  We insert 3 statements in the batch, the second one has an invalid foreign key and will
         //  cause BatchUpdateException.
         
         PreparedStatement pstm = ((ConnectionProxy) connection).prepareStatement(jobLogPstmStr, logManager);
         
         for (int i = 0; i < 3; i++) {
            StringBuffer logStmt = new StringBuffer();
            logStmt.append("INSERT INTO sge_job_log (jl_id, jl_parent, jl_time, jl_event, jl_state, jl_user, " +
                  "jl_host, jl_state_time, jl_message) VALUES (");
            //bind pstm
            pstm.setInt(1, i + 1);
            logStmt.append(i+1+",");
            if (i == 1) {
               pstm.setInt(2, 100); //wrong foreign key
               logStmt.append(100+",");
            } else {
               pstm.setInt(2, 1);
               logStmt.append(1+",");
            }
            pstm.setTimestamp(3, time);
            logStmt.append("{ts '" +time+ "'} ,");
            pstm.setString(4, event);
            logStmt.append("'" +event+"',");
            pstm.setString(5, state);
            logStmt.append("'" +state+"',");
            pstm.setString(6, user);
            logStmt.append("'" +user+"' ,");
            pstm.setString(7, host);
            logStmt.append("'" +host+"',");
            pstm.setInt(8, stateTime);
            logStmt.append(stateTime+",");
            pstm.setString(9, message);
            logStmt.append("'" +message+"')");
            pstm.addBatch();
            BackupStatement backup = new BackupStatement(new Integer(i+2), logStmt.toString());
            ((ConnectionProxy) connection).insertBackup(logManager, backup);
         }
         
         Integer lineNum = null;
         try {
            SGELog.info("before flushing");
            //we also test here that the batches from parentManager get executed first
            controller.flushBatchesAtEnd(connection);
            SGELog.info("after flushing");
         } catch (ReportingBatchException rbe) {
            SGELog.info("In ReportingBatchException");
            lineNum = (Integer) rbe.getLineNumber();
            SGELog.info("LineNubmer is:" +lineNum);
         } catch (Exception e) {
            SGELog.info("In Exception" + e);
         }
         
         SGELog.info("before checkin num lines flushing");
         assertEquals("Not a correct error line", new Integer(3), lineNum);
         //if database is Oracle the rollback is already performed in the handleBatchBackup
         //so we cannot test this here
         if (dbw.getDatabase().getType() != dbw.getDatabase().TYPE_ORACLE) {
            //before rollback the BackupList should contain backup statements for the manager
            List list = ((ConnectionProxy) connection).getBackupList(jobManager);
            assertEquals("Not a correct number of backup statements", 1, list.size());
            list = ((ConnectionProxy) connection).getBackupList(logManager);
            assertEquals("Not a correct number of backup statements", 3, list.size());
            
            // before rollback there should be PreparedStatement for the Manager
            PreparedStatement pstmCheck = ((ConnectionProxy) connection).getPreparedStatement(jobManager);
            assertNotNull("PreparedStatement shoudl not be null", pstmCheck);
            pstmCheck = ((ConnectionProxy) connection).getPreparedStatement(logManager);
            assertNotNull("PreparedStatement shoudl not be null", pstmCheck);
            
            if(!((ConnectionProxy) connection).getIsClosedFlag()) {
               dbw.getDatabase().rollback(connection);
            }
         }
         //after rollback the BackupList should be null for the manager
         List list = ((ConnectionProxy) connection).getBackupList(jobManager);
         assertNull("BackupList should be null", list);
         list = ((ConnectionProxy) connection).getBackupList(logManager);
         assertNull("BackupList should be null", list);
         
         // after rollback the PreparedStatement for the Manager should be null
         PreparedStatement pstmCheck = ((ConnectionProxy) connection).getPreparedStatement(jobManager);
         assertNull("PreparedStatement shoudl not be null", pstmCheck);
         pstmCheck = ((ConnectionProxy) connection).getPreparedStatement(logManager);
         assertNull("PreparedStatement shoudl not be null", pstmCheck);
         
         SGELog.info("Before releasing connection");
         dbw.getDatabase().release(connection);
         
      } finally {
         
         shutdownDBWriter(dbw);
      }
      
   }
   
   /**
    * query the job log entries from the database.
    * @param db          database of the dbwriter
    * @throws Exception  can throw any exception
    * @return number of job log entries
    */
   private int queryJobLog(Database db) throws Exception {
      Connection conn = db.getConnection();
      try {
         String sql = DBWriterTestConfig.getTestJobLogBatchSQL();
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
