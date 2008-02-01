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
import com.sun.grid.reporting.dbwriter.event.RecordDataEvent;
import java.sql.*;
import com.sun.grid.reporting.dbwriter.db.*;
import com.sun.grid.reporting.dbwriter.file.*;
import java.util.List;

public class ShareLogManager extends RecordManager implements DeleteManager {   
  
   /** Creates a new instance of ReportingShareLogManager */
   public ShareLogManager(Database p_database, Controller controller) throws ReportingException {
      super(p_database, "sge_share_log", "sl_", false, controller);
   }
         
   public void initRecordFromEvent(Record sharelog, RecordDataEvent e) {
      if (e.reportingSource == ReportingSource.SHARELOG) {
         sharelog.initFromStringArray(e.data);
      }
   }   

   public String[] getDeleteRuleSQL(Timestamp time, List subScope) {
      int dbType = database.getType();
      StringBuffer sql = new StringBuffer();
      
      String delete = "DELETE FROM sge_share_log WHERE sl_id IN (";
      String select = "SELECT sl_id from sge_share_log WHERE sl_curr_time < ";
      
      if (dbType == Database.TYPE_MYSQL) {
         sql.append(select);
      } else {
         sql.append(delete);
         sql.append(select);
      }
      
      sql.append(DateField.getValueString(time));      
      
      if (subScope != null && !subScope.isEmpty() ) {
         sql.append(" AND sl_node IN (");
         for (int i = 0; i < subScope.size(); i++) {
            if (i > 0) {
               sql.append(", ");
            }
            sql.append("'");
            sql.append(subScope.get(i));
            sql.append("'");
         }
         sql.append(")");
      }
      
      if (dbType != Database.TYPE_MYSQL) {
         sql.append(super.getDeleteLimit());
         sql.append(")");
      }
      
      String result[] = new String[1];
      result[0] = sql.toString();
      return result;
   }

   public Record newDBRecord() {
      return new ShareLog(this);
   }
}
