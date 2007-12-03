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
import com.sun.grid.reporting.dbwriter.db.Record;
import com.sun.grid.reporting.dbwriter.event.RecordDataEvent;
import com.sun.grid.reporting.dbwriter.file.ReportingSource;
import java.util.HashMap;
import java.util.Map;

public class AdvanceReservationUsageManager extends RecordManager {
   
   protected Map acctMap;
   /**
    * Creates a new instance of AdvanceReservationUsageManager
    */
   public AdvanceReservationUsageManager(Database p_database, Controller controller) throws ReportingException {
      super(p_database, "sge_ar_usage", "aru_", true, controller);
      
      acctMap = new HashMap();
      acctMap.put("aru_termination_time", "ar_termination_time");
      acctMap.put("aru_qname", "ar_qname");
      acctMap.put("aru_hostname", "ar_hostname");
      acctMap.put("aru_slots", "ar_slots");
      
   }

   public void initRecordFromEvent(Record obj, RecordDataEvent e) throws ReportingException {
      if(e.reportingSource == ReportingSource.AR_ACCOUNTING) {
         initRecordFromEventData(obj, e.data, acctMap);
      }
   }

   public Record newDBRecord() {
      return new AdvanceReservationUsage(this);
   }
   
}
