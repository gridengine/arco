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

import java.util.*;
import com.sun.grid.reporting.dbwriter.db.*;

public class ShareLog extends Record {
 
   /**
    * Creates a new instance of ShareLog
    */
   public ShareLog(RecordManager p_manager) {
      super(p_manager);
      
      Field myfields[] = {
         new DateField("sl_curr_time"),
         new DateField("sl_usage_time"),
         new StringField("sl_node"),
         new StringField("sl_user"),
         new StringField("sl_project"),
         new IntegerField("sl_shares"),
         new IntegerField("sl_job_count"),
         new DoubleField("sl_level"),
         new DoubleField("sl_total"),
         new DoubleField("sl_long_target_share"),
         new DoubleField("sl_short_target_share"),
         new DoubleField("sl_actual_share"),
         new DoubleField("sl_usage"),
         new DoubleField("sl_cpu"),
         new DoubleField("sl_mem"),
         new DoubleField("sl_io"),
         new DoubleField("sl_ltcpu"),
         new DoubleField("sl_ltmem"),
         new DoubleField("sl_ltio")
      };
 
      super.setFields(myfields);
   }
}
