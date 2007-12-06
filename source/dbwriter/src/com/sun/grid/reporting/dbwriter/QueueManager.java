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
import java.util.*;
import java.sql.*;
import com.sun.grid.reporting.dbwriter.db.*;
import com.sun.grid.reporting.dbwriter.file.*;

public class QueueManager extends StoredRecordManager {
   static String primaryKeyFields[] = {
      "q_qname",
      "q_hostname"
   };
   
   protected Map accountingMap;
   protected Map statisticsMap;
   protected Map repQueueMap;
   protected Map repQueueConsumableMap;
   
   /**
    * Creates a new instance of QueueManager
    */
   public QueueManager(Database p_database, Controller controller, ValueRecordManager p_valueManager)
      throws ReportingException {
     
      super(p_database, "sge_queue", "q_", false, primaryKeyFields, null, controller);
      accountingMap = new HashMap();
      accountingMap.put("q_qname", "a_qname");
      accountingMap.put("q_hostname", "a_hostname");
      
      statisticsMap = new HashMap();
      statisticsMap.put("q_qname", "s_qname");
      statisticsMap.put("q_hostname", "s_hostname");
      
      repQueueMap = new HashMap();
      repQueueMap.put("q_qname", "q_qname");
      repQueueMap.put("q_hostname", "q_qhostname");
      
      repQueueConsumableMap = new HashMap();
      repQueueConsumableMap.put("q_qname", "qc_qname");
      repQueueConsumableMap.put("q_hostname", "qc_qhostname");
      
      valueManager = p_valueManager;
   }
      
   public void initRecordFromEvent(Record queue, RecordDataEvent e) {
      if (e.reportingSource == ReportingSource.ACCOUNTING) {
         initRecordFromEventData(queue, e.data, accountingMap);
      } else if (e.reportingSource == ReportingSource.STATISTICS) {
         initRecordFromEventData(queue, e.data, statisticsMap);
      }  else if (e.reportingSource == ReportingSource.REP_QUEUE) {
         initRecordFromEventData(queue, e.data, repQueueMap);
      } else if (e.reportingSource == ReportingSource.REP_QUEUE_CONSUMABLE) {
         initRecordFromEventData(queue, e.data, repQueueConsumableMap);
      }
   }
   
   public void initSubRecordsFromEvent(Record obj, RecordDataEvent e, java.sql.Connection connection) throws ReportingException {
      if (e.reportingSource == ReportingSource.STATISTICS ||
          e.reportingSource == ReportingSource.REP_QUEUE ||
          e.reportingSource == ReportingSource.REP_QUEUE_CONSUMABLE) {
         valueManager.handleNewSubRecord(obj, e, connection);
      }
   }

   public Record findRecord(RecordDataEvent e, java.sql.Connection connection ) throws ReportingException {
      Record obj = null;
      
      if (e.reportingSource == ReportingSource.ACCOUNTING) {
         obj = findRecordFromEventData(e.data, accountingMap, connection);
      } else if (e.reportingSource == ReportingSource.STATISTICS) {
         obj = findRecordFromEventData(e.data, statisticsMap, connection);
      } else if (e.reportingSource == ReportingSource.REP_QUEUE) {
         obj = findRecordFromEventData(e.data, repQueueMap, connection);
      } else if (e.reportingSource == ReportingSource.REP_QUEUE_CONSUMABLE) {
         obj = findRecordFromEventData(e.data, repQueueConsumableMap, connection);
      }
      
      return obj;
   }

   public Record newDBRecord() {
      return new Queue(this);
}
}
