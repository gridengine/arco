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

import com.sun.grid.reporting.dbwriter.event.ParserEvent;
import java.sql.*;
import java.util.*;
import com.sun.grid.logging.SGELog;
import com.sun.grid.reporting.dbwriter.db.*;
import com.sun.grid.reporting.dbwriter.file.*;

public class QueueValueManager extends ValueRecordManager {
   
   /**
    * Creates a new instance of QueueValueManager
    */
   public QueueValueManager(Database p_database) throws ReportingException {
      super(p_database, "sge_queue_values", "qv_", true,
      new QueueValue(null));
   }
   
   public void handleNewSubRecord(Record parent, ParserEvent e, java.sql.Connection connection ) throws ReportingException {
      if (e.reportingSource == ReportingSource.STATISTICS) {
         Field timeField = (Field) e.data.get("s_time");
         Field stateField = (Field) e.data.get("s_qstate");
         storeNewValue(parent, timeField, "state", stateField.getValueString(false), null, connection);
         
         // store queue consumables
         Field consumableField = (Field) e.data.get("s_queue_consumable");       
         // split multiple consumable data (,)
         String consumableLine = consumableField.getValueString(false).trim();
         if (consumableLine.length() > 0) {
            String consumableString[] = consumableLine.split(",", -100);
            for (int i = 0; i < consumableString.length; i++) {
               if (consumableString[i].length() > 0) {
                  // split one consumable string into constituent parts (=)
                  String consumable[] = consumableString[i].split("=", -100);
                  
                  // we are interested in the consumable name and actual value
                  storeNewValue(parent, timeField, consumable[0], consumable[1], consumable[2], connection);
               }
            }
         }
      } else if (e.reportingSource == ReportingSource.REP_QUEUE) {
         Field timeField = (Field) e.data.get("q_time");
         
         // store state
         Field stateField = (Field) e.data.get("q_state");
         storeNewValue(parent, timeField, "state", stateField.getValueString(false), null, connection);       
         
         // store load values
         // Field loadField = (Field) e.data.get("q_load");
         // split multiple load data (,)
         //String loadLine = loadField.getValueString(false).trim();
         //if (loadLine.length() > 0) {
         //   String loadString[] = loadLine.split(",", -100);
         //   for (int i = 0; i < loadString.length; i++) {
         //      if (loadString[i].length() > 0) {
         //         // split one load string into constituent parts (=)
         //         String load[] = loadString[i].split("=", -100);
         //         
         //         // we are interested in the consumable name and actual value
         //         storeNewValue(parent, timeField, load[0], load[1]);
         //      }
         //   }
         //}
      } else if (e.reportingSource == ReportingSource.REP_QUEUE_CONSUMABLE) {
         Field timeField = (Field) e.data.get("qc_time");
         
         // store state
         Field stateField = (Field) e.data.get("qc_state");
         storeNewValue(parent, timeField, "state", stateField.getValueString(false), null, connection);       
         
         // store consumables
         Field consumableField = (Field) e.data.get("qc_consumables");
         // split multiple consumable data (,)
         String consumableLine = consumableField.getValueString(false).trim();
         if (consumableLine.length() > 0) {
            String consumableString[] = consumableLine.split(",", -100);
            for (int i = 0; i < consumableString.length; i++) {
               if (consumableString[i].length() > 0) {
                  // split one consumable string into constituent parts (=)
                  String consumable[] = consumableString[i].split("=", -100);
                  
                  // we are interested in the consumable name and actual value
                  storeNewValue(parent, timeField, consumable[0], consumable[1], consumable[2], connection);
               }
            }
         }
      }   
   }
   
   public void storeNewValue(Record parent, Field time, String variable, String value, String config,
                             java.sql.Connection connection ) throws ReportingException {
      try {
         Record obj = recordExecutor.newDBRecord();
         obj.setParent(parent.getId());
         obj.getField("qv_time_start").setValue(time);
         obj.getField("qv_time_end").setValue(time);
         obj.getField("qv_variable").setValue(variable);
         obj.getField("qv_svalue").setValue(value);
         obj.getField("qv_dvalue").setValue(value);
         if (config != null) {
            obj.getField("qv_dconfig").setValue(config);
         }
         obj.store( connection );
      } catch( ReportingException re ) {
         throw re;
      } catch (Exception exception) {
         ReportingException re = new ReportingException( "QueueValueManager.createDBObjectError", exception.getMessage() );
         re.initCause( exception );
         throw re;
      }
   }
   
   
   public void initRecordFromEvent(Record obj, ParserEvent e) {
   }
}
