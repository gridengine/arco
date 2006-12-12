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

public class ReportingUserManager extends ReportingStoredObjectManager {
   static String primaryKeyFields[] = {
      "u_user"
   };
   
   protected Map accountingMap;
   protected Map sharelogMap;
   
   /** Creates a new instance of ReportingUserManager */
   public ReportingUserManager(Database p_database, ReportingValueManager p_valueManager)
      throws ReportingException {
      super(p_database, "sge_user", "u_", false, primaryKeyFields,
            new ReportingUser(null), null);
      
      accountingMap = new HashMap();
      accountingMap.put("u_user", "a_owner");
      
      sharelogMap = new HashMap();
      sharelogMap.put("u_user", "sl_user");
      
      valueManager = p_valueManager;
   }

   public void initObjectFromEvent(DatabaseObject queue, ReportingEventObject e) {
      if (e.reportingSource == ReportingSource.ACCOUNTING) {
         initObjectFromEventData(queue, e.data, accountingMap);
      } else if (e.reportingSource == ReportingSource.SHARELOG) {
         initObjectFromEventData(queue, e.data, sharelogMap);
      }
   }
   
   public DatabaseObject findObject(ReportingEventObject e, java.sql.Connection connection ) throws ReportingException {
      DatabaseObject obj = null;
      
      if (e.reportingSource == ReportingSource.ACCOUNTING) {
         obj = findObjectFromEventData(e.data, accountingMap, connection);
      } else if (e.reportingSource == ReportingSource.SHARELOG) {
         obj = findObjectFromEventData(e.data, sharelogMap, connection);
      }
      
      return obj;
   }
}
