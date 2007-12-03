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
import com.sun.grid.logging.SGELog;
import com.sun.grid.reporting.dbwriter.db.*;

abstract public class ValueRecordManager extends RecordManager implements DeleteManager {
   protected Map derivedMap = null;
   protected String derivedVariableField = null;
   
   /** Creates a new instance of ReportingValueManager */
   public ValueRecordManager(Database p_database, String p_table, String p_prefix, 
         boolean hasParent, Controller controller) throws ReportingException {
     
      super(p_database, p_table, p_prefix, hasParent, controller);
      
      derivedMap = new HashMap();
      derivedMap.put(new String(p_prefix + "time_start"), "time_start");
      derivedMap.put(new String(p_prefix + "time_end"), "time_end");
      derivedMap.put(new String(p_prefix + "dvalue"), "value");   
      
      derivedVariableField = new String(p_prefix + "variable");
   }
   
   public Timestamp getLastEntryTime(int parent, String variableName, java.sql.Connection connection) throws ReportingException {
      Timestamp result = null;
      
      StringBuffer cmd = new StringBuffer("SELECT ");
      
      int dbType = database.getType();
      switch( dbType ) {
         case Database.TYPE_MYSQL:      // same as for postgres db 
         case Database.TYPE_POSTGRES:            
            cmd.append(getPrefix());
            cmd.append("time_end as max FROM ");
            cmd.append(getTable());
            cmd.append(" WHERE ");
            if(parent >= 0) {
                cmd.append(getParentFieldName());
                cmd.append(" = ");
                cmd.append(parent);
                cmd.append(" AND ");
            }
            cmd.append(getPrefix());
            cmd.append("variable = '");
            cmd.append(variableName);
            cmd.append("'");
            cmd.append(" order by ");
            cmd.append(getPrefix());
            cmd.append("time_end desc limit 1");
            break;
         default:
            cmd.append("max(");
            cmd.append(getPrefix());
            cmd.append("time_end) AS max FROM ");
            cmd.append(getTable());
            cmd.append(" WHERE ");
            if(parent >= 0) {
                cmd.append(getParentFieldName());
                cmd.append(" = ");
                cmd.append(parent);
                cmd.append(" AND ");
            }
            cmd.append(getPrefix());
            cmd.append("variable = '");
            cmd.append(variableName);
            cmd.append("'");
      }
      
      SGELog.fine( cmd.toString() );
      try {
         Statement stmt = database.executeQuery(cmd.toString(), connection );
         try {
            ResultSet rs = stmt.getResultSet();
            try {
               if (rs.next()) {
                  result = rs.getTimestamp("max");
               }
            } finally {
               rs.close();
            }
         } finally {
            stmt.close();
            database.release(connection);
         }         
      } catch (SQLException e) {
         ReportingException re = new ReportingException( "ValueRecordManager.sqlError", e.getMessage() );
         re.initCause( e );
         throw re;
      }
      
      // no value for this variable stored yet, so we assume "an early time"
      if (result == null) {
         result = new Timestamp(0);
      }
      
      return result;
   }
  

   public void handleNewDerivedRecord(Record parent, String variable, ResultSet rs, java.sql.Connection connection) {
      try {

         Record record = newDBRecord();
         record.setParentFieldValue(parent.getIdFieldValue());
         record.getField(derivedVariableField).setValue(variable);
         record.initFromResultSet(rs, derivedMap);
         //we must store the derived record with the same connection that the 
         //prepared statement was created with DerivedRecord does not come from parsedLine
         store(record, connection, null);
      } catch (Exception e) {
         // we have to catch InstantiationException from newDBRecord() and 
         // SQLException from initFromResultSet()
         SGELog.warning( e, "ValueRecordManager.createDBObjectError", e.getMessage() );
      }
   }
   
   public String[] getDeleteRuleSQL(Timestamp time, List subScope) {
      int dbType = Database.getType();
      StringBuffer sql = new StringBuffer();
      
      String delete = "DELETE FROM ";
      String select = "SELECT ";
      
      if (dbType == Database.TYPE_MYSQL) {
         sql.append(select);
      } else {
         sql.append(delete);
         sql.append(getTable());
         sql.append(" WHERE ");
         sql.append(getPrefix());
         sql.append("id IN (SELECT ");
      }
      
      sql.append(getPrefix());
      sql.append("id FROM ");
      sql.append(getTable());
      sql.append(" WHERE ");
      sql.append(getPrefix());
      sql.append("time_end < ");
      sql.append(DateField.getValueString(time));
      
      if (subScope != null && !subScope.isEmpty() ) {
         sql.append(" AND ");
         sql.append(getPrefix());
         sql.append("variable IN (");
         for (int i = 0; i < subScope.size(); i++) {
            if (i > 0) {
               sql.append(", ");
            }
            sql.append("'");
            sql.append(subScope.get(i));
            sql.append("'");
         }
         sql.append(")");
      }
      
      sql.append(super.getDeleteLimit());
      
      if (dbType != Database.TYPE_MYSQL) {
         sql.append(")");
      }
      
      String result[] = new String[1];
      result[0] = sql.toString();
      return result;
   }
   


}
