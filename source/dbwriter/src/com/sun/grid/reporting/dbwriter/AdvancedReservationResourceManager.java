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
 *   Copyright: 2007 by Sun Microsystems, Inc.
 *
 *   All Rights Reserved.
 *
 ************************************************************************/
/*___INFO__MARK_END__*/

package com.sun.grid.reporting.dbwriter;

import com.sun.grid.logging.SGELog;
import com.sun.grid.reporting.dbwriter.db.Database;
import com.sun.grid.reporting.dbwriter.db.DatabaseField;
import com.sun.grid.reporting.dbwriter.db.DatabaseObject;
import com.sun.grid.reporting.dbwriter.file.ReportingSource;

public class AdvancedReservationResourceManager extends ReportingObjectManager {
   
   /** Creates a new instance of AdvancedReservationResourceManager */
   public AdvancedReservationResourceManager(Database p_database) throws ReportingException {
      super(p_database, "sge_ar_resource_usage", "arru_", true, new AdvancedReservationResource(null));
   }

   public void initObjectFromEvent(DatabaseObject obj, ReportingEventObject e) throws ReportingException {
   }
   
   public void handleNewSubObject(DatabaseObject parent, ReportingEventObject e, java.sql.Connection connection) 
   throws ReportingException {
      if (e.reportingSource == ReportingSource.AR_ATTRIBUTE) {
         DatabaseField resourceField = (DatabaseField) e.data.get("ar_granted_resources");
         String resources = resourceField.getValueString(false);
         
         if (!(resources.equals("NONE"))) {
            String split[] = resources.split(",");

            for (int i = 0; i < split.length; i++) {
               String contents[] = split[i].split("=");            
               if (contents.length != 2) {
                  SGELog.warning("AdvancedReservationResource.splitError", split[i]);
               } else {
                  storeNewResource(parent, contents[0], contents[1], connection);
               }            
            }           
         }
      }
   }
   
   public void storeNewResource(DatabaseObject parent, String variable, String value, java.sql.Connection connection) 
   throws ReportingException {
      try {
         DatabaseObject obj = databaseObjectManager.newObject();
         obj.setParent(parent.getId());
         obj.getField("arru_variable").setValue(variable);
         obj.getField("arru_value").setValue(value);
         obj.store(connection);
      } catch (Exception exception) {
         ReportingException ex = new ReportingException("AdvancedReservationResourceManager.createDBObjectError", 
               exception.getMessage());
         ex.initCause(exception);
         throw ex;        
      }
   }
}
