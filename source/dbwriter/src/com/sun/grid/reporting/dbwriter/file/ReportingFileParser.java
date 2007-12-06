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
package com.sun.grid.reporting.dbwriter.file;

import com.sun.grid.reporting.dbwriter.Controller;
import java.util.*;
import com.sun.grid.reporting.dbwriter.db.*;
import com.sun.grid.reporting.dbwriter.ReportingParseException;


public class ReportingFileParser extends FileParser {
   Field accountingFields[];
   Map           accountingMap;
   
   Field hostFields[];
   Map     hostMap;
   
   Field hostConsumableFields[];
   Map     hostConsumableMap;
   
   Field queueFields[];
   Map     queueMap;
   
   Field queueConsumableFields[];
   Map     queueConsumableMap;
   
   Field sharelogFields[];
   Map     sharelogMap;
   
   Field newjobFields[];
   Map     newjobMap;
   
   Field joblogFields[];
   Map     joblogMap;
   
   Field newArFields[];
   Map      newArMap;
   
   Field  arAttributeFields[];
   Map      arAttributeMap;
   
   Field arLogFields[];
   Map      arLogMap;
   
   Field arAccountingFields[];
   Map      arAccountingMap;
   
   /** Creates a new instance of AccountingFileReader */
   public ReportingFileParser(String p_fileName, String p_delimiter, Controller p_controller) {
      super(p_fileName, p_delimiter, ReportingSource.REPORTING, p_controller);
      
      accountingFields = new Field[] {
         new DateField("time"),
         new StringField("type"),
         new StringField("a_qname"),
         new StringField("a_hostname"),
         new StringField("a_group"),
         new StringField("a_owner"),
         new StringField("a_job_name"),
         new IntegerField("a_job_number"),
         new StringField("a_account"),
         new IntegerField("a_priority"),
         new DateField("a_submission_time"),
         new DateField("a_start_time"),
         new DateField("a_end_time"),
         new IntegerField("a_failed"),
         new IntegerField("a_exit_status"),
         new IntegerField("a_ru_wallclock"),
         new DoubleField("a_ru_utime"),
         new DoubleField("a_ru_stime"),
         new IntegerField("a_ru_maxrss").parseDouble(),
         new IntegerField("a_ru_ixrss").parseDouble(),
         new IntegerField("a_ru_issmrss").parseDouble(),
         new IntegerField("a_ru_idrss").parseDouble(),
         new IntegerField("a_ru_isrss").parseDouble(),
         new IntegerField("a_ru_minflt").parseDouble(),
         new IntegerField("a_ru_majflt").parseDouble(),
         new IntegerField("a_ru_nswap").parseDouble(),
         new IntegerField("a_ru_inblock").parseDouble(),
         new IntegerField("a_ru_outblock").parseDouble(),
         new IntegerField("a_ru_msgsnd").parseDouble(),
         new IntegerField("a_ru_msgrcv").parseDouble(),
         new IntegerField("a_ru_nsignals").parseDouble(),
         new IntegerField("a_ru_nvcsw").parseDouble(),
         new IntegerField("a_ru_nivcsw").parseDouble(),
         new StringField("a_project"),
         new StringField("a_department"),
         new StringField("a_granted_pe"),
         new IntegerField("a_slots"),
         new IntegerField("a_task_number"),
         new DoubleField("a_cpu"),
         new DoubleField("a_mem"),
         new DoubleField("a_io"),
         new StringField("a_category"),
         new DoubleField("a_iow"),
         new StringField("a_pe_taskid"),
         new DoubleField("a_maxvmem")
      };
      accountingMap = createMap(accountingFields);
      
      hostFields = new Field[] {
         new DateField("time"),
         new StringField("type"),
         new StringField("h_name"),
         new DateField("h_time"),
         new StringField("h_state"),
         new StringField("h_load")
      };
      hostMap = createMap(hostFields);
      
      hostConsumableFields = new Field[] {
         new DateField("time"),
         new StringField("type"),
         new StringField("hc_name"),
         new DateField("hc_time"),
         new StringField("hc_state"),
         new StringField("hc_consumables")
      };
      hostConsumableMap = createMap(hostConsumableFields);
      
      queueFields = new Field[] {
         new DateField("time"),
         new StringField("type"),
         new StringField("q_qname"),
         new StringField("q_qhostname"),
         new DateField("q_time"),
         new StringField("q_state"),
      };
      queueMap = createMap(queueFields);
      
      queueConsumableFields = new Field[] {
         new DateField("time"),
         new StringField("type"),
         new StringField("qc_qname"),
         new StringField("qc_qhostname"),
         new DateField("qc_time"),
         new StringField("qc_state"),
         new StringField("qc_consumables")
      };
      queueConsumableMap = createMap(queueConsumableFields);
      
      sharelogFields = new Field[] {
         new DateField("time"),
         new StringField("type"),
         new DateField("sl_curr_time"),
         new DateField("sl_usage_time"),
         new StringField("sl_node"),
         new StringField("sl_user"),
         new StringField("sl_project"),
         new IntegerField("sl_shares"),
         new IntegerField("sl_job_count"),
         new DoubleField("sl_level"),
         new DoubleField("sl_total"),
         new DoubleField("sl_long_target_share"),
         new DoubleField("sl_short_target_share"),
         new DoubleField("sl_actual_share"),
         new DoubleField("sl_usage"),
         new DoubleField("sl_cpu"),
         new DoubleField("sl_mem"),
         new DoubleField("sl_io"),
         new DoubleField("sl_ltcpu"),
         new DoubleField("sl_ltmem"),
         new DoubleField("sl_ltio"),
         new StringField("dummy")
      };
      sharelogMap = createMap(sharelogFields);
      
      newjobFields = new Field[] {
         new DateField("time"),
         new StringField("type"),
         new DateField("nj_submission_time"),
         new IntegerField("nj_job_number"),
         new IntegerField("nj_task_number"),
         new StringField("nj_pe_taskid"),
         new StringField("nj_job_name"),
         new StringField("nj_owner"),
         new StringField("nj_group"),
         new StringField("nj_project"),
         new StringField("nj_department"),
         new StringField("nj_account"),
         new IntegerField("nj_priority")
      };
      newjobMap = createMap(newjobFields);
         
      joblogFields = new Field[] {
         new DateField("time"),
         new StringField("type"),
         new DateField("jl_time"),
         new StringField("jl_event"),
         new IntegerField("jl_job_number"),
         new IntegerField("jl_task_number"),
         new StringField("jl_pe_taskid"),
         new StringField("jl_state"),
         new StringField("jl_user"),
         new StringField("jl_host"),
         new IntegerField("jl_state_time"),
         new IntegerField("jl_priority"),
         new DateField("jl_submission_time"),
         new StringField("jl_job_name"),
         new StringField("jl_owner"),
         new StringField("jl_group"),
         new StringField("jl_project"),
         new StringField("jl_department"),
         new StringField("jl_account"),
         new StringField("jl_message")
      };
      joblogMap = createMap(joblogFields);        
   }
   
   protected void parseLineType(String splitLine[]) throws ReportingParseException {
      if( splitLine == null || splitLine.length < 2 ) {
         throw new ReportingParseException("ReportingFileReader.tooLessField");
      }
      String type = splitLine[1];
      if (type.compareTo("acct") == 0) {
         setInfo(accountingFields, accountingMap, ReportingSource.ACCOUNTING);
      } else if (type.compareTo("host") == 0) {
         setInfo(hostFields, hostMap, ReportingSource.REP_HOST);
      } else if (type.compareTo("host_consumable") == 0) {
         setInfo(hostConsumableFields, hostConsumableMap, ReportingSource.REP_HOST_CONSUMABLE);
      } else if (type.compareTo("queue") == 0) {
         setInfo(queueFields, queueMap, ReportingSource.REP_QUEUE);
      } else if (type.compareTo("queue_consumable") == 0) {
         setInfo(queueConsumableFields, queueConsumableMap, ReportingSource.REP_QUEUE_CONSUMABLE);
      } else if (type.compareTo("sharelog") == 0) {
         setInfo(sharelogFields, sharelogMap, ReportingSource.SHARELOG);
      } else if (type.compareTo("new_job") == 0) {
         setInfo(newjobFields, newjobMap, ReportingSource.NEWJOB);
      } else if (type.compareTo("job_log") == 0) {
         setInfo(joblogFields, joblogMap, ReportingSource.JOBLOG);
      }
      else {
         throw new ReportingParseException("ReportingFileReader.invalidReportType", type );
      }
   }
}
