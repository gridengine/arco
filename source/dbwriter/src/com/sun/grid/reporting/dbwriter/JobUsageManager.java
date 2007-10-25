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

import com.sun.grid.reporting.dbwriter.event.ParserEvent;
import java.sql.*;
import java.util.*;

import com.sun.grid.reporting.dbwriter.db.*;
import com.sun.grid.reporting.dbwriter.file.*;

public class JobUsageManager extends RecordManager {
   protected Map accountingMap;
   
   /** Creates a new instance of JobUsageManager */
   public JobUsageManager(Database p_database) throws ReportingException {
      super(p_database, "sge_job_usage", "ju_", true,
      new JobUsage(null));
      
      accountingMap = new HashMap();
      accountingMap.put("ju_curr_time", "a_end_time");
      accountingMap.put("ju_qname", "a_qname");
      accountingMap.put("ju_hostname", "a_hostname");
      accountingMap.put("ju_start_time", "a_start_time");
      accountingMap.put("ju_end_time", "a_end_time");
      accountingMap.put("ju_failed", "a_failed");
      accountingMap.put("ju_exit_status", "a_exit_status");
      accountingMap.put("ju_granted_pe", "a_granted_pe");
      accountingMap.put("ju_slots", "a_slots");
      accountingMap.put("ju_ru_wallclock", "a_ru_wallclock");
      accountingMap.put("ju_ru_utime", "a_ru_utime");
      accountingMap.put("ju_ru_stime", "a_ru_stime");
      accountingMap.put("ju_ru_maxrss", "a_ru_maxrss");
      accountingMap.put("ju_ru_ixrss", "a_ru_ixrss");
      accountingMap.put("ju_ru_issmrss", "a_ru_issmrss");
      accountingMap.put("ju_ru_idrss", "a_ru_idrss");
      accountingMap.put("ju_ru_isrss", "a_ru_isrss");
      accountingMap.put("ju_ru_minflt", "a_ru_minflt");
      accountingMap.put("ju_ru_majflt", "a_ru_majflt");
      accountingMap.put("ju_ru_nswap", "a_ru_nswap");
      accountingMap.put("ju_ru_inblock", "a_ru_inblock");
      accountingMap.put("ju_ru_outblock", "a_ru_outblock");
      accountingMap.put("ju_ru_msgsnd", "a_ru_msgsnd");
      accountingMap.put("ju_ru_msgrcv", "a_ru_msgrcv");
      accountingMap.put("ju_ru_nsignals", "a_ru_nsignals");
      accountingMap.put("ju_ru_nvcsw", "a_ru_nvcsw");
      accountingMap.put("ju_ru_nivcsw", "a_ru_nivcsw");
      accountingMap.put("ju_cpu", "a_cpu");
      accountingMap.put("ju_mem", "a_mem");
      accountingMap.put("ju_io", "a_io");
      accountingMap.put("ju_iow", "a_iow");
      accountingMap.put("ju_maxvmem", "a_maxvmem");
      accountingMap.put("ju_ar_number", "a_ar_number");
   }
   
   public void initRecordFromEvent(Record jobUsage, ParserEvent e) {
      if (e.reportingSource == ReportingSource.ACCOUNTING) {
         initRecordFromEventData(jobUsage, e.data, accountingMap);
      }
   }
}
