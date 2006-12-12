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
import com.sun.grid.reporting.dbwriter.db.*;
import com.sun.grid.reporting.dbwriter.file.*;

public class ReportingShareLogManager extends ReportingObjectManager {   
   /** Creates a new instance of ReportingShareLogManager */
   public ReportingShareLogManager(Database p_database)
      throws ReportingException {
      super(p_database, "sge_share_log", "sl_", false,
            new ReportingShareLog(null));
   }
         
   public void initObjectFromEvent(DatabaseObject sharelog, ReportingEventObject e) {
      if (e.reportingSource == ReportingSource.SHARELOG) {
         sharelog.initFromStringArray(e.data);
      }
   }   

   public String[] getDeleteRuleSQL(long timestamp, String time_range, int time_amount, java.util.List values) {
      Timestamp time = getDeleteTimeEnd(timestamp, time_range, time_amount);
      StringBuffer sql = new StringBuffer("DELETE FROM sge_share_log WHERE sl_curr_time < ");
      sql.append(DateField.getValueString(time));
      
      if (values != null && !values.isEmpty() ) {
         sql.append(" AND sl_node IN (");
         for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
               sql.append(", ");
            }
            sql.append("'");
            sql.append(values.get(i));
            sql.append("'");
         }
         sql.append(")");
      }
      
      String result[] = new String[1];
      result[0] = sql.toString();
      return result;
   }
}
