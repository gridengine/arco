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

import java.sql.*;
import java.util.*;
import com.sun.grid.logging.SGELog;
import com.sun.grid.reporting.dbwriter.ReportingException;


public class StoredRecordExecutor extends RecordExecutor {
   protected String condition;
   protected String primaryKeyFields[];
   protected RecordCache storedObjects;
   
   
   /** Creates a new instance of StoredRecordExecutor */
   public StoredRecordExecutor(Database p_database, String p_table,
   String p_prefix, boolean hasParent, String p_primaryKey[],
   Record p_template, String p_condition) throws ReportingException {
      super(p_database, p_table, p_prefix, hasParent, p_template);
      condition = p_condition;
      primaryKeyFields = p_primaryKey;
      storedObjects = new RecordCache(this);
   }
   
   public String getCondition() {
      return condition;
   }
   
   public String[] getPrimaryKeyFields() {
      return primaryKeyFields;
   }
   
   public Record getDBRecord(PrimaryKey pk, java.sql.Connection connection ) throws ReportingException {
      return storedObjects.getStoredDBRecord( pk, connection );
   }
   
   public Statement queryAllObjects( java.sql.Connection connection ) throws ReportingException {
      StringBuffer sql = new StringBuffer("SELECT * FROM ");
      sql.append(getTable());
      
      // JG: TODO: append condition, create sql statement in constructor

      return database.executeQuery( sql.toString(), connection );
   }
   
   public void store(Record obj, java.sql.Connection connection ) throws ReportingException {
      // RH: TODO: update mechanism is yet not implemented.
      super.store(obj, connection );
      storedObjects.addDBRecord(obj);
   }
}
