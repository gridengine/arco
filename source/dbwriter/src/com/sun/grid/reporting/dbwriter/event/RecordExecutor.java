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

package com.sun.grid.reporting.dbwriter.event;

import com.sun.grid.reporting.dbwriter.ReportingBatchException;
import com.sun.grid.reporting.dbwriter.ReportingException;
import java.sql.Connection;

public interface RecordExecutor {
   /**
    * 
    * It executes the batch by calling pstm.executeBatch. First it checks if this RecordExecutor has a parentManager.
    * If it does, it executes the parentManager's batch first. In order for the foreign key constraints to work, we need 
    * to insert the records from the parent database table before the records from the dependent table. (Bottom Up execution)
    *
    * It handles the BatchUpdateException if there was one
    */ 
   public void executeBatch(Connection connection) throws ReportingException, ReportingBatchException;
   
   /**
    * It initializes and stores the record
    * 
    * @param RecordDataEvent - the event from which the Record is initialized
    * @param connection - database connection used to bind the PreparedStatement of this Record and store it
    */
   public void processRecord(RecordDataEvent e, java.sql.Connection connection) throws ReportingException;
   
   
   /**
    * Must be overriden by all parent RecordManager to properly execute batches for it's child Managers 
    */
   public void flushBatches(Connection connection) throws ReportingBatchException;
   
}
