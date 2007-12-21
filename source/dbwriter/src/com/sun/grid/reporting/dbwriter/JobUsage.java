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

import java.sql.*;
import java.util.*;
import com.sun.grid.reporting.dbwriter.db.*;

public class JobUsage extends Record {
 
   /** Creates a new instance of JobUsage */
   public JobUsage(RecordManager p_manager) {
      super(p_manager);
      
      Field myfields[] = {
         new DateField("ju_curr_time"),
         new StringField("ju_qname"),
         new StringField("ju_hostname"),
         new DateField("ju_start_time"),
         new DateField("ju_end_time"),
         new IntegerField("ju_failed"),
         new IntegerField("ju_exit_status"),
         new StringField("ju_granted_pe"),
         new IntegerField("ju_slots"),
         new IntegerField("ju_ru_wallclock"),
         new DoubleField("ju_ru_utime"),
         new DoubleField("ju_ru_stime"),
         new IntegerField("ju_ru_maxrss"),
         new IntegerField("ju_ru_ixrss"),
         new IntegerField("ju_ru_issmrss"),
         new IntegerField("ju_ru_idrss"),
         new IntegerField("ju_ru_isrss"),
         new IntegerField("ju_ru_minflt"),
         new IntegerField("ju_ru_majflt"),
         new IntegerField("ju_ru_nswap"),
         new IntegerField("ju_ru_inblock"),
         new IntegerField("ju_ru_outblock"),
         new IntegerField("ju_ru_msgsnd"),
         new IntegerField("ju_ru_msgrcv"),
         new IntegerField("ju_ru_nsignals"),
         new IntegerField("ju_ru_nvcsw"),
         new IntegerField("ju_ru_nivcsw"),
         new DoubleField("ju_cpu"),
         new DoubleField("ju_mem"),
         new DoubleField("ju_io"),
         new DoubleField("ju_iow"),
         new DoubleField("ju_maxvmem"),
         new IntegerField("ju_ar_number")
      };
 
      super.setFields(myfields);
   }   
}
