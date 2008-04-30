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

import com.sun.grid.reporting.dbwriter.db.Database;
import com.sun.grid.reporting.dbwriter.db.Record;
import com.sun.grid.reporting.dbwriter.db.DateField;
import com.sun.grid.reporting.dbwriter.db.IntegerField;
import com.sun.grid.reporting.dbwriter.event.RecordDataEvent;
import com.sun.grid.reporting.dbwriter.file.ReportingSource;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdvanceReservationManager extends StoredRecordManager
        implements DeleteManager {

   static String primaryKeyFields[] = {
      "ar_number",
      "ar_submission_time"
   };
   protected Map arMap;
   protected Map arLookupMap;
   protected Map arAcctMap;
   protected AdvanceReservationAttributeManager arAttrManager;
   protected AdvanceReservationLogManager arLogManager;
   protected AdvanceReservationUsageManager arUsageManager;
   protected AdvanceReservationResourceManager arResourceManager;

   /**
    * Creates a new instance of AdvanceReservationManager
    */
   public AdvanceReservationManager(Database p_database, Controller controller)
           throws ReportingException {

      super(p_database, "sge_ar", "ar_", false, primaryKeyFields, null, controller);

      arMap = new HashMap();
      arMap.put("ar_number", "ar_number");
      arMap.put("ar_owner", "ar_owner");
      arMap.put("ar_submission_time", "ar_submission_time");

      arLookupMap = new HashMap();
      arLookupMap.put("ar_number", "ar_number");
      arLookupMap.put("ar_submission_time", "ar_submission_time");

      arAcctMap = new HashMap();
      arAcctMap.put("ar_number", "a_ar_number");
      arAcctMap.put("ar_submission_time", "a_ar_submission_time");


      arLogManager = new AdvanceReservationLogManager(p_database, controller);
      arUsageManager = new AdvanceReservationUsageManager(p_database, controller);
      arResourceManager = new AdvanceReservationResourceManager(p_database, controller);
      arAttrManager = new AdvanceReservationAttributeManager(p_database, controller);

      arLogManager.setParentManager(this);
      arUsageManager.setParentManager(this);
      arResourceManager.setParentManager(this);
      arAttrManager.setParentManager(this);
   }
   
   public synchronized void processRecord(RecordDataEvent e, java.sql.Connection connection) throws ReportingException {
      Record record = null;
      record = findRecord(e, connection);

      if (record == null) {
         if (e.reportingSource == ReportingSource.NEW_AR) {
         // new template
            super.processRecord(e, connection);
         }
      } else {
         // existing object
         // JG: TODO: we have to update the object
         initSubRecordsFromEvent(record, e, connection);
      }
   }
 
   public Record findRecord(RecordDataEvent e, Connection connection) throws ReportingException {
      Record record = null;
      if (e.reportingSource == ReportingSource.ACCOUNTING) {
         IntegerField arNumberField = null;
         int id = 0;

         arNumberField = (IntegerField) e.data.get("a_ar_number");
         
         //if it is 0 means it did not run in any AR thus we don't do anything
         if (arNumberField.getValue() != 0) {
            // first we need to find the record in the cache or the sge_ar table for this AR
            record = findRecordFromEventData(e.data, arAcctMap, connection);

            if (record != null) {
               //We retrieve the ar_id field value (PRIMARY KEY)
               id = record.getIdFieldValue();
               // The acct line contains the ar_number issued by qmaster. In the sge_job_usage table we must insert the
               // primary key from the sge_ar table (ar_id). Since the ar_number can wrap, it cannot be used in the sge_job_usage
               // table to idetify which jobs ran in which AR.
               arNumberField.setValue(id);
            } else {
               // no AR record exists in the sge_ar table, reporting was probably turned off when this AR was created.
               // We'll change the event field a_ar_number to -1, to signal that the parent record does not exist 
               // in the sge_ar table
               arNumberField.setValue(-1);
            }
         }
      } else if (e.reportingSource == ReportingSource.NEW_AR) {
         //we always create new entry
         return null;
      } else {
         record = findRecordFromEventData(e.data, arLookupMap, connection);
      }

      return record;
   }

   public void initRecordFromEvent(Record obj, RecordDataEvent e) throws ReportingException {
      if (e.reportingSource == ReportingSource.NEW_AR) {
         initRecordFromEventData(obj, e.data, arMap);
      }
   }

   public void initSubRecordsFromEvent(Record obj, RecordDataEvent e, java.sql.Connection connection)
           throws ReportingException {
      if (e.reportingSource == ReportingSource.AR_ATTRIBUTE) {
         arAttrManager.handleNewSubRecord(obj, e, connection);
         arResourceManager.handleNewSubRecord(obj, e, connection);
      } else if (e.reportingSource == ReportingSource.AR_LOG) {
         arLogManager.handleNewSubRecord(obj, e, connection);
      } else if (e.reportingSource == ReportingSource.AR_ACCOUNTING) {
         arUsageManager.handleNewSubRecord(obj, e, connection);
      }
   }

   public synchronized void flushBatches(Connection connection) throws ReportingBatchException {
      //always call super first to execute the parentManager first
      super.flushBatches(connection);
      arLogManager.flushBatches(connection);
      arUsageManager.flushBatches(connection);
      arResourceManager.flushBatches(connection);
      arAttrManager.flushBatches(connection);
   }

   public String[] getDeleteRuleSQL(Timestamp time, List subScope) {
      String result[] = new String[4];
      int dbType = database.getType();

      // we select all the records from sge_ar_attribute where ara_end_time is < time
      // this is our common delete part for all the ar tables
      StringBuffer subSelect = new StringBuffer("SELECT ara_parent FROM sge_ar_attribute WHERE ara_end_time < ");
      subSelect.append(DateField.getValueString(time));

      // build delete statements
      if (dbType == Database.TYPE_MYSQL) {
         // sge_ar_log
         result[0] = constructSelectSQL("sge_ar_log", "arl_parent", subSelect.toString());
         // sge_ar_usage
         result[1] = constructSelectSQL("sge_ar_usage", "aru_parent", subSelect.toString());
         // sge_ar_resource_usage
         result[2] = constructSelectSQL("sge_ar_resource_usage", "arru_parent", subSelect.toString());
         //delete from sge_ar_attribute is done with the CASCADE DELETE Rule
         // sge_ar
         result[3] = constructSelectSQL("sge_ar", "ar_id", subSelect.toString());
      } else {
         // sge_ar_log
         result[0] = constructDeleteSQL("sge_ar_log", "arl_parent", subSelect.toString());
         // sge_ar_usage
         result[1] = constructDeleteSQL("sge_ar_usage", "aru_parent", subSelect.toString());
         // sge_ar_resource_usage
         result[2] = constructDeleteSQL("sge_ar_resource_usage", "arru_parent", subSelect.toString());
         //delete from sge_ar_attribute is done with the CASCADE DELETE Rule
         // sge_ar
         result[3] = constructDeleteSQL("sge_ar", "ar_id", subSelect.toString());
      }

      return result;
   }

   private String constructDeleteSQL(String table, String field, String subSelect) {
      StringBuffer sql = new StringBuffer("DELETE FROM ");
      sql.append(table);
      sql.append(" WHERE ");
      sql.append(field);
      sql.append(" IN (");
      sql.append(subSelect);
      sql.append(super.getDeleteLimit());
      sql.append(")");
      return sql.toString();
   }

   private String constructSelectSQL(String table, String field, String subSelect) {
      StringBuffer sql = new StringBuffer("SELECT DISTINCT ");
      sql.append(field);
      sql.append(" FROM ");
      sql.append(table);
      sql.append(" WHERE ");
      sql.append(field);
      sql.append(" IN (");
      sql.append(subSelect);
      sql.append(") ");
      sql.append(super.getDeleteLimit());
      return sql.toString();
   }

   public Record newDBRecord() {
      return new AdvanceReservation(this);
   }
}
