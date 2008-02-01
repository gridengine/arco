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
import com.sun.grid.reporting.dbwriter.event.RecordDataEvent;
import com.sun.grid.reporting.dbwriter.event.ParserListener;

import com.sun.grid.reporting.dbwriter.db.*;
import com.sun.grid.reporting.dbwriter.event.RecordExecutor;
import com.sun.grid.reporting.dbwriter.event.StatisticListener;
import com.sun.grid.reporting.dbwriter.file.ReportingSource;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Controller implements ParserListener, StatisticListener {   
   protected Map subscriptions;
   private List batchExecutors;
   
   /**
    * Controller controls the flow of proccesing of ParserData, StatisticData
    */
   public Controller() {
      subscriptions = new HashMap();
      batchExecutors = new ArrayList();
   }
   
   /**
    * @param connection - connection for which to flush bathches
    * It flushBatches of all the registered RecordExecutors, who in terms handle flushBatches of their
    * child's RecordExecutors
    * The caller must handle rollback and commit operations
    */
   public synchronized void flushBatchesAtEnd(java.sql.Connection connection) throws ReportingBatchException {
      if (batchExecutors != null)  {
         for (int i = 0; i < batchExecutors.size(); i++) {
            RecordExecutor executor = (RecordExecutor) batchExecutors.get(i);
            executor.flushBatches(connection);
         }
         SGELog.fine("Controller.flushBatchesAtEnd");
      }
   }
   
   public void addRecordExecutor(RecordExecutor e, Object key) {
      List executors = (List)subscriptions.get(key);
      if (executors == null) {
         executors = new ArrayList();
         subscriptions.put(key, executors);
      }
      if (!executors.contains(e)) {
         executors.add(e);
      }
      //we just need individual executors to purge the batch
      if (!batchExecutors.contains(e)) {
         batchExecutors.add(e);
      }
   }
   
   public void processParserData(RecordDataEvent e, Connection connection) throws ReportingException {
      ReportingSource source = e.reportingSource;
      Object key = source;
      List executors = (List)subscriptions.get(key);
      if (executors != null)  {
         for (int i = 0; i < executors.size(); i++) {
            RecordExecutor executor = (RecordExecutor) executors.get(i);
            executor.processRecord(e, connection);
         }
      }
   }
   
   /**
    * processes statistic data like lines_per_second, derived_time_duration, deletion_time
    * event that did not come directly from FileParser and it is not connected to the lineParsed.
    */
   public void processStatisticData(RecordDataEvent e, Connection connection) {
      ReportingSource source = e.reportingSource;
      Object key = source;
      List executors = (List)subscriptions.get(key);
      if (executors != null)  {
         for (int i = 0; i < executors.size(); i++) {
            RecordExecutor executor = (RecordExecutor) executors.get(i);
            try {
               executor.processRecord(e, connection);
            } catch (ReportingException re) {
               SGELog.warning(re, "ReportDBWriter.statisticDBError");
            }
         }
      }
   }
   
}
