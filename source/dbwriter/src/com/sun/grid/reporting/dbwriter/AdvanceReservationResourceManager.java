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
import com.sun.grid.reporting.dbwriter.db.Field;
import com.sun.grid.reporting.dbwriter.db.Record;
import com.sun.grid.reporting.dbwriter.event.RecordDataEvent;
import com.sun.grid.reporting.dbwriter.file.ReportingSource;

public class AdvanceReservationResourceManager extends RecordManager {
   
   /**
    * Creates a new instance of AdvanceReservationResourceManager
    */
   public AdvanceReservationResourceManager(Database p_database, Controller controller) throws ReportingException {
      super(p_database, "sge_ar_resource_usage", "arru_", true, controller);
   }

   public void initRecordFromEvent(Record obj, RecordDataEvent e) throws ReportingException {
   }
   
   public void handleNewSubRecord(Record parent, RecordDataEvent e, java.sql.Connection connection) 
   throws ReportingException {
      if (e.reportingSource == ReportingSource.AR_ATTRIBUTE) {
         Field resourceField = (Field) e.data.get("ar_granted_resources");
         String resources = resourceField.getValueString(false);
         
         if (!(resources.equals("NONE"))) {
            String split[] = resources.split(",");

            for (int i = 0; i < split.length; i++) {
               String contents[] = split[i].split("=");            
               if (contents.length != 2) {
                  SGELog.warning("AdvanceReservationResource.splitError", split[i]);
               } else {
                  storeNewResource(parent, contents[0], contents[1], connection, e.lineNumber);
               }            
            }           
         }
      }
   }
   
   public void storeNewResource(Record parent, String variable, String value, java.sql.Connection connection, Object lineNumber) 
   throws ReportingException {
      try {
         Record record = newDBRecord();
         record.setParentFieldValue(parent.getIdFieldValue());
         record.getField("arru_variable").setValue(variable);
         record.getField("arru_value").setValue(value);
         store(record, connection, lineNumber);
      } catch (Exception exception) {
         ReportingException ex = new ReportingException("AdvanceReservationResourceManager.createDBObjectError", 
               exception.getMessage());
         ex.initCause(exception);
         throw ex;        
      }
   }

   public Record newDBRecord() {
      return new AdvanceReservationResource(this);
   }
}
