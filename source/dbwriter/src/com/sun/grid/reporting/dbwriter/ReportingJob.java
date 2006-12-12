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

import com.sun.grid.reporting.dbwriter.db.*;


public class ReportingJob extends DatabaseObject { 
   /** Creates a new instance of ReportingJob */
   public ReportingJob(DatabaseObjectManager p_manager) {
      super(p_manager);
      
      DatabaseField myfields[] = {
         new IntegerField("j_open"),
         new IntegerField("j_job_number"),
         new IntegerField("j_task_number"),
         new StringField("j_pe_taskid"),
         new StringField("j_job_name"),
         new StringField("j_group"),
         new StringField("j_owner"),
         new StringField("j_account"),
         new IntegerField("j_priority"),
         new DateField("j_submission_time"),
         new StringField("j_project"),
         new StringField("j_department"),
      };
 
      super.setFields(myfields);
      setOpen(1);
   }
   
   public void setOpen(int value) {
      IntegerField open = (IntegerField) getField("j_open");
      open.setValue(value);
   }
   
   public DatabaseObject newObject(DatabaseObjectManager manager) {
      return new ReportingJob(manager);
   }

   public String toString() {
      StringBuffer ret = new StringBuffer();
      ret.append('[');
      ret.append(manager.getTable());
      ret.append(", id=");
      ret.append(getId());
      ret.append(", parent=");
      ret.append(getParent());
      ret.append(", key=[");
      ret.append(getPrimaryKey());
      ret.append("], name = ");
      ret.append(getField("j_job_name").getValueString(true));
      ret.append(", addr=0x" );
      ret.append(Integer.toHexString(hashCode()));
      ret.append("]");
      return ret.toString();
   }
   
}
