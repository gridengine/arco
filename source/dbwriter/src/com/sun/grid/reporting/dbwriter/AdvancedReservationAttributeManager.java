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

public class AdvancedReservationAttributeManager extends ReportingObjectManager {
   protected Map arAttrMap;
   
   /** Creates a new instance of AdvancedReservationAttrributeManager */
   public AdvancedReservationAttributeManager(Database p_database) throws ReportingException {
      super(p_database, "sge_ar_attribute", "ara_", true, new AdvancedReservationAttribute(null));
      
      arAttrMap = new HashMap();
      arAttrMap.put("ara_curr_time", "ar_event_time");
      arAttrMap.put("ara_name", "ar_name");
      arAttrMap.put("ara_account", "ar_account");
      arAttrMap.put("ara_start_time", "ar_start_time");
      arAttrMap.put("ara_end_time", "ar_end_time");
      arAttrMap.put("ara_granted_pe", "ar_granted_pe");
         
   }

   public void initObjectFromEvent(DatabaseObject obj, ReportingEventObject e) throws ReportingException {
      if (e.reportingSource == ReportingSource.AR_ATTRIBUTE) {
         initObjectFromEventData(obj, e.data, arAttrMap);
      }
   }
   
}
