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

package com.sun.grid.reporting.dbwriter.db;

import java.sql.SQLException;

public class CommitEvent {
   
   public static final int DELETE = 1;
   public static final int INSERT = 2;
   public static final int UPDATE = 3;
   
   private String threadName;
   private int id;
   private SQLException error;
   
    /** Creates a new instance of CommitEvent */
   public CommitEvent(String threadName, int id) {
      this(threadName, id, null);
 
   }

   public CommitEvent(String threadName, int id, SQLException error) {
      this.setThreadName(threadName);
      this.setId(id);
      this.setError(error);
   }
   
   public String getThreadName() {
      return threadName;
   }

   public void setThreadName(String threadName) {
      this.threadName = threadName;
   }

   public int getId() {
      return id;
   }

   public void setId(int id) {
      this.id = id;
   }

   public SQLException getError() {
      return error;
   }

   public void setError(SQLException error) {
      this.error = error;
   }
   
   public String toString() {
      return "Id: " +id + " - Thread: " + threadName;
}
   
}
