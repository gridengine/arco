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
package com.sun.grid.reporting.dbwriter.file;

import java.util.*;
import com.sun.grid.reporting.dbwriter.db.*;


public class StatisticsFileReader extends ReportFileReader {
   
   /** Creates a new instance of StatisticsFileReader */
   public StatisticsFileReader(String p_fileName, String p_delimiter) {
      super(p_fileName, p_delimiter, ReportingSource.STATISTICS);
      
      DatabaseField myfields[] = {
         new DateField("s_time"),
         new StringField("s_hostname"),
         new StringField("s_qname"),
         new DoubleField("s_load"),
         new DoubleField("s_vmem"),
         new StringField("s_qstate"),
         new StringField("s_queue_consumable"),
         new StringField("s_host_consumable"),
         new StringField("s_global_consumable"),
      };
      
      super.setInfo(myfields, null, ReportingSource.STATISTICS);
   }
}
