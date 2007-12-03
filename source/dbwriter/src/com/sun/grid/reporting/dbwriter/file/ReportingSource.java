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

public class ReportingSource {
   static public final ReportingSource ACCOUNTING           = new ReportingSource("accounting");
   static public final ReportingSource STATISTICS           = new ReportingSource("statistics");
   static public final ReportingSource SHARELOG             = new ReportingSource("sharelog");
   static public final ReportingSource REPORTING            = new ReportingSource("reporting");
   static public final ReportingSource REP_HOST             = new ReportingSource("reporting host");
   static public final ReportingSource REP_HOST_CONSUMABLE  = new ReportingSource("reporting host consumable");
   static public final ReportingSource REP_QUEUE            = new ReportingSource("reporting queue");
   static public final ReportingSource REP_QUEUE_CONSUMABLE = new ReportingSource("reporting queue consumable");
   static public final ReportingSource JOBLOG               = new ReportingSource("joblog");
   static public final ReportingSource NEWJOB               = new ReportingSource("newjob");
   static public final ReportingSource DBWRITER_STATISTIC   = new ReportingSource("dbwriter statistic");
   static public final ReportingSource DATABASE_STATISTIC   = new ReportingSource("database statistic");
   static public final ReportingSource NEW_AR               = new ReportingSource("advance reservation");
   static public final ReportingSource AR_LOG               = new ReportingSource("advance reservation log");
   static public final ReportingSource AR_ATTRIBUTE         = new ReportingSource("advance reservation attribute");
   static public final ReportingSource AR_ACCOUNTING        = new ReportingSource("advance reservation accounting");
   
   private String name;
   
   /** Creates a new instance of ReportingSource */
   public ReportingSource(String p_name) {
      name = p_name;
   }
   
   public String toString() {
      return name;
   }
}
