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
import com.sun.grid.reporting.dbwriter.file.ReportingSource;
import java.util.HashMap;
import java.util.Map;

public class AdvancedReservationLogManager extends ReportingObjectManager {
   protected Map arLogMap;
   
   /** Creates a new instance of AdvancedReservationLogManager */
   public AdvancedReservationLogManager(Database p_database) throws ReportingException {
      super(p_database, "sge_ar_log", "arl_", true, new AdvancedReservationLog(null));
      
      arLogMap = new HashMap();
      arLogMap.put("arl_time", "ar_state_change_time");
      arLogMap.put("arl_event", "ar_event");
      arLogMap.put("arl_state", "ar_state");
      arLogMap.put("arl_message", "ar_message");
   }

   public void initObjectFromEvent(DatabaseObject obj, ReportingEventObject e) throws ReportingException {
      if (e.reportingSource == ReportingSource.AR_LOG) {
         initObjectFromEventData(obj, e.data, arLogMap);
      }
   }
   
}
