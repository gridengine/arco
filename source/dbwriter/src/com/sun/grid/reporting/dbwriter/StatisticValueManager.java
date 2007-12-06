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
import com.sun.grid.logging.SGELog;
import com.sun.grid.reporting.dbwriter.db.*;
import com.sun.grid.reporting.dbwriter.file.*;

public class StatisticValueManager extends ValueRecordManager {
   
   /**
    * Creates a new instance of StatisticValueManager
    */
   public StatisticValueManager(Database p_database, Controller controller) throws ReportingException {
      super(p_database, "sge_statistic_values", "sv_", true, controller);
   }
   
   public void handleNewSubRecord(Record parent, RecordDataEvent e, java.sql.Connection connection) throws ReportingException {

       if(e.reportingSource == ReportingSource.DBWRITER_STATISTIC ||
          e.reportingSource == ReportingSource.DATABASE_STATISTIC) {
           Field timeField = (Field)e.data.get("time");

           Iterator iter = e.data.keySet().iterator();
           while(iter.hasNext()) {
               String key = (String)iter.next();
               Field field = (Field)e.data.get(key);
               if(field instanceof DoubleField) {
                    storeNewValue(parent, timeField, key, ((DoubleField)field).getValue(), connection, e.lineNumber);
               }
           }
       }
   }
   
   public void storeNewValue(Record parent, Field time, String variable, double value, java.sql.Connection connection, 
         Object lineNumber) throws ReportingException {       
      try {
         Record record = newDBRecord();
         record.setParentFieldValue(parent.getIdFieldValue());
         record.getField(StatisticValue.TIME_START_FIELD).setValue(time);
         record.getField(StatisticValue.TIME_END_FIELD).setValue(time);
         record.getField(StatisticValue.VARIABLE_FIELD).setValue(variable);
         ((DoubleField)record.getField(StatisticValue.VALUE_FIELD)).setValue(value);
         store(record, connection, lineNumber);
      } catch( ReportingException re ) {
         throw re;
      } catch (Exception exception) {
         ReportingException re = new ReportingException( "StatisticValueManager.createDBObjectError", exception.getMessage() );
         re.initCause( exception );
         throw re;
      }
   }
   
   
   public void initRecordFromEvent(Record obj, RecordDataEvent e) {
   }

   public Record newDBRecord() {
      return new StatisticValue(this);
}
}
