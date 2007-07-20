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
import java.sql.*;
import java.util.*;

import com.sun.grid.reporting.dbwriter.db.*;
import com.sun.grid.reporting.dbwriter.file.*;


public class ReportingJobLogManager extends ReportingObjectManager
      implements DeleteManager {
   protected Map joblogMap;
   
   /** Creates a new instance of ReportingJobLogManager */
   public ReportingJobLogManager(Database p_database) throws ReportingException {
      super(p_database, "sge_job_log", "jl_", true, new ReportingJobLog(null));
      
      joblogMap = new HashMap();
      joblogMap.put("jl_time", "jl_time");
      joblogMap.put("jl_event", "jl_event");
      joblogMap.put("jl_state", "jl_state");
      joblogMap.put("jl_user", "jl_user");
      joblogMap.put("jl_host", "jl_host");
      joblogMap.put("jl_state_time", "jl_state_time");
      joblogMap.put("jl_message", "jl_message");
   }
   
   public void initObjectFromEvent(DatabaseObject jobLog, ReportingEventObject e) {
      if (e.reportingSource == ReportingSource.JOBLOG) {
         initObjectFromEventData(jobLog, e.data, joblogMap);
      }
   }
   
   //Job Log deletion rules don't have sub_scope
   public String[] getDeleteRuleSQL(Timestamp time, List subScope) {
      int dbType = Database.getType();
      StringBuffer sql = new StringBuffer();
      
      String delete = "DELETE FROM sge_job_log WHERE jl_id IN (";
      String select = "SELECT jl_id from sge_job_log WHERE jl_time < ";
      
      if (dbType == Database.TYPE_MYSQL) {
         sql.append(select);
      } else {
         sql.append(delete);
         sql.append(select);
      }
      
      sql.append(DateField.getValueString(time));
      
      sql.append(super.getDeleteLimit());
      
      if (dbType != Database.TYPE_MYSQL) {
         sql.append(")");
      }
      
      String result[] = new String[1];
      result[0] = sql.toString();
      SGELog.info("CONSTRUCTED DELETE STATEMENT: " +sql.toString());
      return result;
   }
}
