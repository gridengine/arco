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

import com.sun.grid.reporting.dbwriter.db.Database;
import com.sun.grid.reporting.dbwriter.db.DatabaseObject;
import com.sun.grid.reporting.dbwriter.db.DateField;
import com.sun.grid.reporting.dbwriter.file.ReportingSource;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class AdvancedReservationManager extends ReportingStoredObjectManager {
   
   static String primaryKeyFields[] = {
      "ar_number",
   };
   
   protected Map arMap;  
   protected Map arLookupMap;
   protected AdvancedReservationAttributeManager arAttrManager;
   protected AdvancedReservationLogManager arLogManager;
   protected AdvancedReservationUsageManager arUsageManager;
   protected AdvancedReservationResourceManager arResourceManager;
 
   /** Creates a new instance of AdvancedReservationManager */
   public AdvancedReservationManager(Database p_database) 
      throws ReportingException {
      
      super(p_database, "sge_ar", "ar_", false, primaryKeyFields, 
            new AdvancedReservation(null), null);
      
      arMap = new HashMap();
      arMap.put("ar_number", "ar_number");
      arMap.put("ar_owner", "ar_owner");
      arMap.put("ar_submission_time", "ar_submission_time");  
      
      arLookupMap = new HashMap();
      arLookupMap.put("ar_number", "ar_number");
      
      arLogManager = new AdvancedReservationLogManager(p_database); 
      arUsageManager = new AdvancedReservationUsageManager(p_database);
      arResourceManager = new AdvancedReservationResourceManager(p_database);
      arAttrManager = new AdvancedReservationAttributeManager(p_database);
   }

   public DatabaseObject findObject(ReportingEventObject e, Connection connection) throws ReportingException {
      DatabaseObject obj = null;
      
      if (e.reportingSource == ReportingSource.NEW_AR) {
         //we always create new entry
         return null;
      } else {
         obj = findObjectFromEventData(e.data, arLookupMap, connection);
      }
      
      return obj;
   }

   public void initObjectFromEvent(DatabaseObject obj, ReportingEventObject e) throws ReportingException {
      if (e.reportingSource == ReportingSource.NEW_AR) {
         initObjectFromEventData(obj, e.data, arMap);
      }
   }
   
   public void initSubObjectsFromEvent(DatabaseObject obj, ReportingEventObject e, java.sql.Connection connection) 
      throws ReportingException {
      if (e.reportingSource == ReportingSource.AR_ATTRIBUTE) {
         arAttrManager.handleNewSubObject(obj, e, connection);
         arResourceManager.handleNewSubObject(obj, e, connection);
      }  
      else if (e.reportingSource == ReportingSource.AR_LOG) {
         arLogManager.handleNewSubObject(obj, e, connection);
      }
      else if (e.reportingSource == ReportingSource.AR_ACCOUNTING) {
         arUsageManager.handleNewSubObject(obj, e, connection);
      }
   }
   
   public String[] getDeleteRuleSQL(long timestamp, String time_range, int time_amount, java.util.List values) {
      String result[] = new String[4];
      Timestamp time = getDeleteTimeEnd(timestamp, time_range, time_amount);
      
      // we select all the records from sge_ar_attribute where ara_end_time is < time
      // this is our common delete part for all the ar tables
      StringBuffer subSelect = new StringBuffer("FROM sge_ar_attribute WHERE ara_end_time < ");
      subSelect.append(DateField.getValueString(time));
      
      // delete statement for sge_ar_log
      StringBuffer sql = new StringBuffer("DELETE FROM sge_ar_log WHERE arl_parent IN (SELECT ara_parent ");
      sql.append(subSelect.toString());
      sql.append(")");
      result[0] = sql.toString();
      
      // delete statement for sge_ar_usage
      sql = new StringBuffer("DELETE FROM sge_ar_usage WHERE aru_parent IN (SELECT ara_parent ");
      sql.append(subSelect.toString());
      sql.append(")");
      result[1] = sql.toString();
      
      // delete statement for sge_ar_resource_usage
      sql = new StringBuffer("DELETE FROM sge_ar_resource_usage WHERE arru_parent IN (SELECT ara_parent ");
      sql.append(subSelect.toString());
      sql.append(")");
      result[2] = sql.toString();
      
      //delete from sge_ar_attribute is done with the CASCADE DELETE Rule
      
      // finally delete from the parent table sge_ar
      sql = new StringBuffer("DELETE FROM sge_ar WHERE ar_id IN (SELECT ara_parent ");
      sql.append(subSelect.toString());
      sql.append(")");
      result[3] = sql.toString();  
      
      return result;
   }
}
