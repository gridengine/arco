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

import com.sun.grid.reporting.dbwriter.event.RecordDataEvent;
import java.sql.*;
import java.util.*;
import com.sun.grid.reporting.dbwriter.db.*;
import com.sun.grid.reporting.dbwriter.file.*;

public class ProjectManager extends StoredRecordManager {
   static String primaryKeyFields[] = {
      "p_project"
   };
   
   protected Map accountingMap;
   protected Map sharelogMap;
   
   /**
    * Creates a new instance of ProjectManager
    */
   public ProjectManager(Database p_database, Controller controller, ValueRecordManager p_valueManager)
      throws ReportingException {
      super(p_database, "sge_project", "p_", false, primaryKeyFields, null, controller);
      
      accountingMap = new HashMap();
      accountingMap.put("p_project", "a_project");
      
      sharelogMap = new HashMap();
      sharelogMap.put("p_project", "sl_project");
      
      valueManager = p_valueManager;
   }
   
   public void initRecordFromEvent(Record queue, RecordDataEvent e) {
      if (e.reportingSource == ReportingSource.ACCOUNTING) {
         initRecordFromEventData(queue, e.data, accountingMap);
      } else if (e.reportingSource == ReportingSource.SHARELOG) {
         initRecordFromEventData(queue, e.data, sharelogMap);
      }
   }
   
   public Record findRecord(RecordDataEvent e, java.sql.Connection connection) throws ReportingException {
      Record obj = null;
      
      if (e.reportingSource == ReportingSource.ACCOUNTING) {
         obj = findRecordFromEventData(e.data, accountingMap, connection);
      } else if (e.reportingSource == ReportingSource.SHARELOG) {
         obj = findRecordFromEventData(e.data, sharelogMap, connection);
      }
      
      return obj;
   }

   public Record newDBRecord() {
      return new Project(this);
   }
}
