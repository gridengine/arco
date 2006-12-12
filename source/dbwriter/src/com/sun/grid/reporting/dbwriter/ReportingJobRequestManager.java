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
import com.sun.grid.logging.SGELog;

public class ReportingJobRequestManager extends ReportingObjectManager {
   
   /** Creates a new instance of ReportingJobManager */
   public ReportingJobRequestManager(Database p_database) 
      throws ReportingException {
      super(p_database, "sge_job_request", "jr_", true,
      new ReportingJobRequest(null));
   }
   
   public void handleNewSubObject(DatabaseObject parent, ReportingEventObject e, java.sql.Connection connection ) throws ReportingException {
      if (e.reportingSource == ReportingSource.ACCOUNTING) {
         DatabaseField categoryField = (DatabaseField) e.data.get("a_category");
         String category = categoryField.getValueString(false);
         // -U deadlineusers -q balrog.q,durin.q -l  arch=solaris64,mem_total=100M -P prj2
         String splitCategory[] = category.split(" +", -100);
         for (int i = 0; i < splitCategory.length; i++) {
            if (splitCategory[i].compareTo("-q") == 0) {
               storeNewRequest(parent, "queue", splitCategory[i + 1], connection );
            } else if (splitCategory[i].compareTo("-l") == 0) {
               String request[] = splitCategory[i + 1].split(",", -100);
               for (int j = 0; j < request.length; j++) {
                  String contents[] = request[j].split("=", -100);
                  if (contents.length != 2) {
                     SGELog.warning( "ReportingJobRequestManager.splitError", request[i] );
                  } else {
                     storeNewRequest(parent, contents[0], contents[1], connection);
                  }
               }
            }
         }
      }
   }
   
   public void storeNewRequest(DatabaseObject parent, String variable, String value, java.sql.Connection connection ) throws ReportingException{
      try {
         DatabaseObject obj = databaseObjectManager.newObject();
         obj.setParent(parent.getId());
         obj.getField("jr_variable").setValue(variable);
         obj.getField("jr_value").setValue(value);
         obj.store( connection );
      } catch (Exception exception) {
         ReportingException ex = new ReportingException( "ReportingJobRequestManager.createDBObjectError", exception.getMessage());
         ex.initCause( exception );
         throw ex;
      }
   }
   
   
   public void initObjectFromEvent(DatabaseObject obj, ReportingEventObject e) {
   }
}
