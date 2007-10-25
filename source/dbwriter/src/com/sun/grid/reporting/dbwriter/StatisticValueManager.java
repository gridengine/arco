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

public class StatisticValueManager extends ValueRecordManager {
   
   /**
    * Creates a new instance of StatisticValueManager
    */
   public StatisticValueManager(Database p_database) throws ReportingException {
      super(p_database, "sge_statistic_values", "sv_", true,
      new StatisticValue(null));
   }
   
   public void handleNewSubRecord(Record parent, ParserEvent e, java.sql.Connection connection ) throws ReportingException {

       if(e.reportingSource == ReportingSource.DBWRITER_STATISTIC ||
          e.reportingSource == ReportingSource.DATABASE_STATISTIC) {
           Field timeField = (Field)e.data.get("time");

           Iterator iter = e.data.keySet().iterator();
           while(iter.hasNext()) {
               String key = (String)iter.next();
               Field field = (Field)e.data.get(key);
               if(field instanceof DoubleField) {
                    storeNewValue(parent, timeField, key, ((DoubleField)field).getValue(), connection);
               }
           }
       }
   }
   
   public void storeNewValue(Record parent, Field time, String variable, double value,
                             java.sql.Connection connection ) throws ReportingException {
       
      try {
         Record obj = recordExecutor.newDBRecord();
         obj.setParent(parent.getId());
         obj.getField(StatisticValue.TIME_START_FIELD).setValue(time);
         obj.getField(StatisticValue.TIME_END_FIELD).setValue(time);
         obj.getField(StatisticValue.VARIABLE_FIELD).setValue(variable);
         ((DoubleField)obj.getField(StatisticValue.VALUE_FIELD)).setValue(value);
         obj.store( connection );
      } catch( ReportingException re ) {
         throw re;
      } catch (Exception exception) {
         ReportingException re = new ReportingException( "StatisticValueManager.createDBObjectError", exception.getMessage() );
         re.initCause( exception );
         throw re;
      }
   }
   
   
   public void initRecordFromEvent(Record obj, ParserEvent e) {
   }
}
