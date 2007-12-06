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

package com.sun.grid.reporting.dbwriter.event;

import java.sql.SQLException;

/**
 * Commit event is triggered by the <code>Database</code> 
 * the database will inform all listeners when 
 * commit has finished. This is used by the tests to listen for events from database.
 */
public class CommitEvent {
   
   public static final int DELETE = 1;
   public static final int INSERT = 2;
   public static final int UPDATE = 3;
   public static final int BATCH_INSERT = 4;
   public static final int STATISTIC_INSERT = 5;
   
   private String threadName;
   private int id;
   private SQLException error;
   private long lastTimestamp;
   
   /** Creates a new instance of CommitEv */
   public CommitEvent(String threadName, int id) {
      this(threadName, id, null);
 
   }

   public CommitEvent(String threadName, int id, long time) {
      this(threadName, id, null);
      this.setLastTimestamp(time);
   }
   
   public CommitEvent(String threadName, int id, long time, SQLException error) {
      this(threadName, id, error);
      this.setLastTimestamp(time);
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
   
   public long getLastTimestamp() {
      return lastTimestamp;
}

   public void setLastTimestamp(long lastTimestamp) {
      this.lastTimestamp = lastTimestamp;
   }
   
}
