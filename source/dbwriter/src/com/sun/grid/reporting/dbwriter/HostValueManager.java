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
import com.sun.grid.reporting.dbwriter.db.*;
import com.sun.grid.reporting.dbwriter.file.*;


public class HostValueManager extends ValueRecordManager {
   /**
    * Creates a new instance of ValueRecordManager
    */
   public HostValueManager(Database p_database, Controller controller) throws ReportingException {
      super(p_database, "sge_host_values", "hv_", true, controller);
   }
   
   public void handleNewSubRecord(Record parent, RecordDataEvent e, java.sql.Connection connection) throws ReportingException {
      if (e.reportingSource == ReportingSource.STATISTICS) {
         Field timeField = (Field) e.data.get("s_time");
         
         // store load
         Field loadField = (Field) e.data.get("s_load");
         storeNewValue(parent, timeField, "load", loadField.getValueString(false), null, connection, e.lineNumber);
         
         // store vmem
         Field vmemField = (Field) e.data.get("s_vmem");
         storeNewValue(parent, timeField, "vmem", vmemField.getValueString(false), null, connection, e.lineNumber);
         
         // store consumables
         Field consumableField = (Field) e.data.get("s_host_consumable");
         // split multiple consumable data (,)
         String consumableLine = consumableField.getValueString(false).trim();
         if (consumableLine.length() > 0) {
            String consumableString[] = consumableLine.split(",", -100);
            for (int i = 0; i < consumableString.length; i++) {
               if (consumableString[i].length() > 0) {
                  // split one consumable string into constituent parts (=)
                  String consumable[] = consumableString[i].split("=", -100);
                  
                  // we are interested in the consumable name and actual value
                  storeNewValue(parent, timeField, consumable[0], consumable[1], consumable[2], connection, e.lineNumber);
               }
            }
         }
      } else if (e.reportingSource == ReportingSource.REP_HOST) {
         Field timeField = (Field) e.data.get("h_time");
         
         // JG: TODO: once we have meaningful state contents, store state
         
         // store load values
         Field loadField = (Field) e.data.get("h_load");
         // split multiple load data (,)
         String loadLine = loadField.getValueString(false).trim();
         if (loadLine.length() > 0) {
            String loadString[] = loadLine.split(",", -100);
            for (int i = 0; i < loadString.length; i++) {
               if (loadString[i].length() > 0) {
                  // split one load string into constituent parts (=)
                  String load[] = loadString[i].split("=", -100);
                  
                  // we are interested in the consumable name and actual value
                  storeNewValue(parent, timeField, load[0], load[1], null, connection, e.lineNumber);
               }
            }
         }
      } else if (e.reportingSource == ReportingSource.REP_HOST_CONSUMABLE) {
         Field timeField = (Field) e.data.get("hc_time");
         
         // JG: TODO: once we have meaningful state contents, store state
         
         // store consumables
         Field consumableField = (Field) e.data.get("hc_consumables");
         // split multiple consumable data (,)
         String consumableLine = consumableField.getValueString(false).trim();
         if (consumableLine.length() > 0) {
            String consumableString[] = consumableLine.split(",", -100);
            for (int i = 0; i < consumableString.length; i++) {
               if (consumableString[i].length() > 0) {
                  // split one consumable string into constituent parts (=)
                  String consumable[] = consumableString[i].split("=", -100);
                  
                  // we are interested in the consumable name and actual value
                  storeNewValue(parent, timeField, consumable[0], consumable[1], consumable[2], connection, e.lineNumber);
               }
            }
         }
      }   
   }
   
   public void storeNewValue(Record parent, Field time, String variable, String value, String config, 
         java.sql.Connection connection, Object lineNumber) throws ReportingException {
      try {
         Record record = newDBRecord();
         record.setParentFieldValue(parent.getIdFieldValue());
         record.getField("hv_time_start").setValue(time);
         record.getField("hv_time_end").setValue(time);
         record.getField("hv_variable").setValue(variable);
         record.getField("hv_svalue").setValue(value);
         record.getField("hv_dvalue").setValue(value);
         if (config != null) {
            record.getField("hv_dconfig").setValue(config);
         }
         store(record, connection, lineNumber);
      } catch( ReportingException re ) {
         throw re;
      } catch (Exception exception) {
         ReportingException re = new ReportingException( "HostValueManager.createDBObjectError",
                                                         exception.getMessage() );
         re.initCause( exception );
         throw re;
      }
   }
   
   
   public void initRecordFromEvent(Record obj, RecordDataEvent e) {
   }

   public Record newDBRecord() {
      return new HostValue(this);
   }
}
