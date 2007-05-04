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

import java.sql.*;
import java.util.*;
import com.sun.grid.reporting.dbwriter.db.*;
import com.sun.grid.reporting.dbwriter.file.*;


public class ReportingJobManager extends ReportingStoredObjectManager {
   static String primaryKeyFields[] = {
      "j_job_number",
      "j_task_number",
      "j_pe_taskid"
   };
   
   protected Map accountingMap;
   protected Map new_jobMap;
   protected Map job_logMap;
   protected ReportingJobUsageManager usageManager;
   protected ReportingJobRequestManager requestManager;
   protected ReportingJobLogManager     joblogManager;
   
   /** Creates a new instance of ReportingJobManager */
   public ReportingJobManager(Database p_database, ReportingObjectManager p_jobLogManager)
      throws ReportingException {
      super(p_database, "sge_job", "j_", false, primaryKeyFields,
      new ReportingJob(null),
      "j_open = 1");
      
      accountingMap = new HashMap();
      accountingMap.put("j_job_number", "a_job_number");
      accountingMap.put("j_task_number", "a_task_number");
      accountingMap.put("j_pe_taskid", "a_pe_taskid");
      accountingMap.put("j_job_name", "a_job_name");
      accountingMap.put("j_group", "a_group");
      accountingMap.put("j_owner", "a_owner");
      accountingMap.put("j_account", "a_account");
      accountingMap.put("j_priority", "a_priority");
      accountingMap.put("j_submission_time", "a_submission_time");
      accountingMap.put("j_project", "a_project");
      accountingMap.put("j_department", "a_department");
      
      new_jobMap = new HashMap();
      new_jobMap.put("j_job_number", "nj_job_number");
      new_jobMap.put("j_task_number", "nj_task_number");
      new_jobMap.put("j_pe_taskid", "nj_pe_taskid");
      new_jobMap.put("j_job_name", "nj_job_name");
      new_jobMap.put("j_owner", "nj_owner");
      new_jobMap.put("j_group", "nj_group");
      new_jobMap.put("j_project", "nj_project");
      new_jobMap.put("j_department", "nj_department");
      new_jobMap.put("j_account", "nj_account");
      new_jobMap.put("j_priority", "nj_priority");
      new_jobMap.put("j_submission_time", "nj_submission_time");

      job_logMap = new HashMap();
      job_logMap.put("j_job_number", "jl_job_number");
      job_logMap.put("j_task_number", "jl_task_number");
      job_logMap.put("j_pe_taskid", "jl_pe_taskid");
      job_logMap.put("j_job_name", "jl_job_name");
      job_logMap.put("j_owner", "jl_owner");
      job_logMap.put("j_group", "jl_group");
      job_logMap.put("j_project", "jl_project");
      job_logMap.put("j_department", "jl_department");
      job_logMap.put("j_account", "jl_account");
      job_logMap.put("j_priority", "jl_priority");
      job_logMap.put("j_submission_time", "jl_submission_time");
      
      usageManager = new ReportingJobUsageManager(p_database);
      requestManager = new ReportingJobRequestManager(p_database);
      joblogManager = (ReportingJobLogManager) p_jobLogManager;
      
      // CR 6359492: missing entry in table sge_job if job number is reused
      // 
      // The primary key of the ReportingJobManager is not really a primary key
      // If the gridengines has an job number overflow it is possible that the table
      // sge_job has more then one row with the same "primary key" information.
      // We specify a sort criteria so that the row with the youngest submission
      // time is taken.
      
      SortCriteria [] sort = new SortCriteria [] {
         new SortCriteria("j_submission_time", SortCriteria.DESCENDING )
      };
      getDatabaseObjectManager().setSortCriteria(sort);
   }
   
   public void handleNewObject(ReportingEventObject e, java.sql.Connection connection ) throws ReportingException {

      // CR 6359492
      // We do not allow the j_task_id 0, it is set to -1.
      IntegerField taskNumberField = null;
      
      if (e.reportingSource == ReportingSource.ACCOUNTING) {
         taskNumberField = (IntegerField)e.data.get("a_task_number");   
      } else if (e.reportingSource == ReportingSource.NEWJOB) {
         taskNumberField = (IntegerField)e.data.get("nj_task_number");   
      } else if (e.reportingSource == ReportingSource.JOBLOG) {
         taskNumberField = (IntegerField)e.data.get("jl_task_number");   
      }

      if(taskNumberField != null && taskNumberField.getValue() == 0 ) {
         taskNumberField.setValue(-1);
      }
      
      super.handleNewObject(e, connection );
   }
   
   public void initObjectFromEvent(DatabaseObject job, ReportingEventObject e) {
      if (e.reportingSource == ReportingSource.ACCOUNTING) {
         initObjectFromEventData(job, e.data, accountingMap);
      } else if (e.reportingSource == ReportingSource.NEWJOB) {
         initObjectFromEventData(job, e.data, new_jobMap);
      } else if (e.reportingSource == ReportingSource.JOBLOG) {
         initObjectFromEventData(job, e.data, job_logMap);
      } 
   }
   
   public DatabaseObject findObject(ReportingEventObject e, java.sql.Connection connection ) throws ReportingException {
      DatabaseObject obj = null;
      
      if (e.reportingSource == ReportingSource.ACCOUNTING) {
         obj = findObjectFromEventData(e.data, accountingMap, connection);
         
         if( obj != null) {
            // CR 6359492
            // If job_log is not active then only the accounting record is written into
            // the reporting file. We have to check the submission time. If the submission
            // time of the event data is younger the the submission time of the database object
            // we have to create a new entry => return null

            DateField dateField = (DateField)obj.getField("j_submission_time");
            Timestamp databaseTimestamp = dateField.getValue();

            dateField = (DateField)e.data.get("a_submission_time");
            Timestamp  eventTimestamp = dateField.getValue();

            if( databaseTimestamp.before(eventTimestamp)) {
               obj = null;
            }
         }
         return obj;         
      } else if (e.reportingSource == ReportingSource.NEWJOB) {
         // CR 6359492: on a newjob event we always create a new entry
         return null;

      } else if (e.reportingSource == ReportingSource.JOBLOG) {
         obj = findObjectFromEventData(e.data, job_logMap, connection);
      } 
      
      return obj;
   }
   
   public void initSubObjectsFromEvent(DatabaseObject obj, ReportingEventObject e, java.sql.Connection connection ) throws ReportingException {
      if (e.reportingSource == ReportingSource.ACCOUNTING) {
         // store job usage
         requestManager.handleNewSubObject(obj, e, connection );
         usageManager.handleNewSubObject(obj, e, connection );
      } else if (e.reportingSource == ReportingSource.JOBLOG) {
         joblogManager.handleNewSubObject(obj, e, connection);
      }
   }
   
   public String[] getDeleteRuleSQL(long timestamp, String time_range, int time_amount, java.util.List values) {
      String result[] = new String[4];
      Timestamp time = getDeleteTimeEnd(timestamp, time_range, time_amount);
      
      // we have to delete from sge_job_usage, sge_job_request and sge_job,
      // build some common parts
      StringBuffer subSelect = new StringBuffer("FROM sge_job WHERE j_submission_time < ");
      subSelect.append(DateField.getValueString(time));
      
      // build delete statements
      // sge_job_request
      StringBuffer sql = new StringBuffer("DELETE FROM sge_job_request WHERE jr_parent IN (SELECT j_id ");
      sql.append(subSelect.toString());
      sql.append(")");
      result[0] = sql.toString();
      
      // sge_job_usage
      sql = new StringBuffer("DELETE FROM sge_job_usage WHERE ju_parent IN (SELECT j_id ");
      sql.append(subSelect.toString());
      sql.append(")");
      result[1] = sql.toString();
      
      // sge_job_log
      sql = new StringBuffer("DELETE FROM sge_job_log WHERE jl_parent IN (SELECT j_id ");
      sql.append(subSelect.toString());
      sql.append(")");
      result[2] = sql.toString();
      
      // sge_job
      sql = new StringBuffer("DELETE ");
      sql.append(subSelect.toString());
      result[3] = sql.toString();
      
      return result;
   }
}


