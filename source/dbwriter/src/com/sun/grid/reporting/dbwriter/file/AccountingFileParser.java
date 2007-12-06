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
import com.sun.grid.reporting.dbwriter.db.*;

public class AccountingFileParser extends FileParser {
   
   /**
    * Creates a new instance of AccountingFileParser
    */
   public AccountingFileParser(String p_fileName, String p_delimiter, Controller p_controller) {
      super(p_fileName, p_delimiter, ReportingSource.ACCOUNTING, p_controller);
      
      Field myfields[] = {
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
      
      super.setInfo(myfields, null, ReportingSource.ACCOUNTING);
   }
}
