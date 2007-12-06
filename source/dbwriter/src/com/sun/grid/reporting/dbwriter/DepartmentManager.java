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
import java.util.*;
import com.sun.grid.reporting.dbwriter.db.*;
import com.sun.grid.reporting.dbwriter.file.*;


public class DepartmentManager extends StoredRecordManager {
   static String primaryKeyFields[] = {
      "d_department"
   };
   
   protected Map accountingMap;
   
   /**
    * Creates a new instance of DepartmentManager
    */
   public DepartmentManager(Database p_database, Controller controller, ValueRecordManager p_valueManager)
      throws ReportingException {
      super(p_database, "sge_department", "d_", false, primaryKeyFields, null, controller);
      
      accountingMap = new HashMap();
      accountingMap.put("d_department", "a_department");
      
      valueManager = p_valueManager;
   }
   
   public void initRecordFromEvent(Record department, RecordDataEvent e) {
      if (e.reportingSource == ReportingSource.ACCOUNTING) {
         initRecordFromEventData(department, e.data, accountingMap);
      }
   }
   
   public Record findRecord(RecordDataEvent e, java.sql.Connection connection ) throws ReportingException {
      return findRecordFromEventData(e.data, accountingMap, connection);
   }

   public Record newDBRecord() {
      return new Department(this);
}
}
