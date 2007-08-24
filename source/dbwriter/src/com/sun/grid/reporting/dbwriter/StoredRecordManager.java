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

import com.sun.grid.reporting.dbwriter.event.CommitEvent;
import com.sun.grid.reporting.dbwriter.event.ParserEvent;
import java.sql.*;
import java.util.*;
import com.sun.grid.logging.SGELog;
import com.sun.grid.reporting.dbwriter.db.*;

abstract public class StoredRecordManager extends RecordManager {
   protected ValueRecordManager valueManager = null;
   
   /**
    * Creates a new instance of StoredRecordManager
    */
   public StoredRecordManager(Database p_database, String p_table, 
                                       String p_prefix, boolean hasParent, String p_primaryKey[], 
                                       Record p_template, String p_condition) 
      throws ReportingException {
      super(new StoredRecordExecutor(p_database, p_table, p_prefix, hasParent, p_primaryKey, p_template, p_condition));
   }
   
   public synchronized void newLineParsed(ParserEvent e, java.sql.Connection connection ) throws ReportingException {
      Record obj = findObject(e, connection );
      
      if (obj == null) {
         // new object
         super.newLineParsed(e, connection);
      } else {
         // existing object
         // JG: TODO: we have to update the object
         initSubRecordsFromEvent(obj, e, connection );
      }
   }
   
   public Record findObjectFromEventData(Map data, Map map, java.sql.Connection connection ) throws ReportingException {
      Record obj = null;
      
      // read object primary key from event data
      String keyFields[] = recordExecutor.getPrimaryKeyFields();
      String key[] = new String[keyFields.length];
      
      for (int i = 0; i < keyFields.length; i++) {
         String fieldName = (String) map.get(keyFields[i]);
         if (fieldName == null || fieldName.length() == 0) {
            SGELog.warning("StoredRecordManager.dataForField" , keyFields[i]);
            key = null;
            break;
         } else {
            Field field = (Field)data.get(fieldName);
            
            if (field == null) {
               SGELog.warning("StoredRecordManager.fieldNotFound" , fieldName);
               key = null;
               break;
            } else {
               key[i] = field.getValueString(true);
            }
         }
      }
      
      // lookup key in stored objects
      if (key != null) {
         obj = recordExecutor.getDBRecord(createPrimaryKey(key), connection);         
      }
      
      return obj;
   }
   
   private String createAutoSQL(String interval, String function, String sourceVariable) {
   
      int dbType = valueManager.getRecordExecutor().getDatabase().getType();
      switch( dbType ) {
         
         case Database.TYPE_ORACLE:
             return createAutoSQLOracle( interval, function, sourceVariable );
         case Database.TYPE_POSTGRES:
             return createAutoSQLPostgres( interval, function, sourceVariable );
         case Database.TYPE_MYSQL:
              return createAutoSQLMysql( interval, function, sourceVariable );    
         default:
             throw new IllegalStateException( "DB Type " + dbType + " is not supported" );
      }
      
   }
   
   
   private String createAutoSQLOracle( String interval, String function, String sourceVariable) {
      
      RecordExecutor dbManager = valueManager.getRecordExecutor();
      RecordExecutor dbParent = getRecordExecutor();
      StringBuffer sql = new StringBuffer();
      
      String time_startField = dbManager.getPrefix() + "time_start";
      String time_endField = dbManager.getPrefix() + "time_end";
      String table = dbManager.getTable();
      String variableField = dbManager.getPrefix() + "variable";
      String valueField    = dbManager.getPrefix() + "dvalue";
      String parentField = dbManager.getParentFieldName();
      String parentTable = dbParent.getTable();
      String parentIdField = dbParent.getIdFieldName();
      String parentKeys[] = dbParent.getPrimaryKeyFields();
      
      
     String truncInterval;
     
     if( interval.equalsIgnoreCase( "hour") ) {
        truncInterval = "'HH24'";
     } else {
        truncInterval = '\'' + interval + '\'';
     }

     sql.append( "SELECT  time_start, time_end, "  );
     sql.append( function );
     sql.append( "(");
     sql.append( valueField );
     sql.append( ")" );
     sql.append( "AS value FROM ( ");     
     // start subselect
     
     sql.append( "SELECT ");
     sql.append( "TRUNC( ");
     sql.append( time_startField );
     sql.append( ", ");
     sql.append( truncInterval );
     sql.append( " ) as time_start, ");
     sql.append( "TRUNC( ");
     sql.append( time_startField );
     sql.append( ", " );
     sql.append( truncInterval );
     sql.append( " ) + INTERVAL '1' ");
     sql.append( interval );
     sql.append( " as time_end, " );
     sql.append( valueField );
     sql.append( " FROM ");
     sql.append( table );
     sql.append( " WHERE ");
     sql.append(variableField);
     sql.append(" = '");
     sql.append(sourceVariable);
     sql.append("' AND ");
     sql.append(parentField);
     sql.append(" = (SELECT ");
     sql.append(parentIdField);
     sql.append(" FROM ");
     sql.append(parentTable);
     sql.append(" WHERE ");
     for (int i = 0; i < parentKeys.length; i++) {
        sql.append(parentKeys[i]);
        sql.append(" = __key_");
        sql.append(i);
        sql.append("__) AND ");
     }
     sql.append(time_startField);
     sql.append(" <= {ts '__time_end__'} AND ");
     sql.append(time_endField);
     sql.append(" > {ts '__time_start__'}");
     
     
     // end subselect
     sql.append( " ) ");
     sql.append( "GROUP BY time_start, time_end" );
     
     return sql.toString();
   }
   
   
   private String createAutoSQLPostgres(String interval, String function, String sourceVariable) {
      
   
      RecordExecutor dbManager = valueManager.getRecordExecutor();
      RecordExecutor dbParent = getRecordExecutor();
      StringBuffer sql = new StringBuffer();
      
      String time_startField = dbManager.getPrefix() + "time_start";
      String time_endField = dbManager.getPrefix() + "time_end";
      String table = dbManager.getTable();
      String variableField = dbManager.getPrefix() + "variable";
      String valueField    = dbManager.getPrefix() + "dvalue";
      String parentField = dbManager.getParentFieldName();
      String parentTable = dbParent.getTable();
      String parentIdField = dbParent.getIdFieldName();
      String parentKeys[] = dbParent.getPrimaryKeyFields();
      
      sql.append("SELECT DATE_TRUNC('");
      sql.append(interval);
      sql.append("', ");
      sql.append(time_startField);
      sql.append(") AS time_start, DATE_TRUNC('");
      sql.append(interval);
      sql.append("', ");
      sql.append(time_startField);
      sql.append(") + INTERVAL '1 ");
      sql.append(interval);
      sql.append("' AS time_end, ");
      sql.append(function);
      sql.append("(");
      sql.append(valueField);
      sql.append(") AS value FROM ");
      sql.append(table);
      sql.append(" WHERE ");
      sql.append(variableField);
      sql.append(" = '");
      sql.append(sourceVariable);
      sql.append("' AND ");
      sql.append(parentField);
      sql.append(" = (SELECT ");
      sql.append(parentIdField);
      sql.append(" FROM ");
      sql.append(parentTable);
      sql.append(" WHERE ");
      for (int i = 0; i < parentKeys.length; i++) {
         sql.append(parentKeys[i]);
         sql.append(" = __key_");
         sql.append(i);
         sql.append("__) AND ");
      }
      sql.append(time_startField);
      sql.append(" <= {ts '__time_end__'} AND ");
      sql.append(time_endField);
      sql.append(" > {ts '__time_start__'} GROUP BY time_start");
      
      return sql.toString();
   }
   
   private String createAutoSQLMysql(String interval, String function, String sourceVariable) {

      RecordExecutor dbManager = valueManager.getRecordExecutor();
      RecordExecutor dbParent = getRecordExecutor();
      StringBuffer sql = new StringBuffer();
      
      String time_startField = dbManager.getPrefix() + "time_start";
      String time_endField = dbManager.getPrefix() + "time_end";
      String table = dbManager.getTable();
      String variableField = dbManager.getPrefix() + "variable";
      String valueField    = dbManager.getPrefix() + "dvalue";
      String parentField = dbManager.getParentFieldName();
      String parentTable = dbParent.getTable();
      String parentIdField = dbParent.getIdFieldName();
      String parentKeys[] = dbParent.getPrimaryKeyFields();

      sql.append("SELECT date_format(");
      sql.append(time_startField);
      sql.append(", '");
      sql.append(RecordManager.getDateTimeFormat(interval));
      sql.append("') AS time_start, date_format(");
      sql.append(time_startField);
      sql.append(", '");
      sql.append(RecordManager.getDateTimeFormat(interval));
      sql.append("') + INTERVAL 1 ");
      sql.append(interval);
      sql.append(" AS time_end, ");
      sql.append(function);
      sql.append("(");
      sql.append(valueField);
      sql.append(") AS value FROM ");
      sql.append(table);
      sql.append(" WHERE ");
      sql.append(variableField);
      sql.append(" = '");
      sql.append(sourceVariable);
      sql.append("' AND ");
      sql.append(parentField);
      sql.append(" = (SELECT ");
      sql.append(parentIdField);
      sql.append(" FROM ");
      sql.append(parentTable);
      sql.append(" WHERE ");
      for (int i = 0; i < parentKeys.length; i++) {
         sql.append(parentKeys[i]);
         sql.append(" = __key_");
         sql.append(i);
         sql.append("__) AND ");
      }
      sql.append(time_startField);
      sql.append(" <= {ts '__time_end__'} AND ");
      sql.append(time_endField);
      sql.append(" > {ts '__time_start__'} GROUP BY time_start");
      
      return sql.toString();
   }
      
   public void calculateDerivedValues( long timestamp, com.sun.grid.reporting.dbwriter.model.DeriveRuleType rule, 
                                       java.sql.Connection connection ) throws ReportingException {      
      String interval = rule.getInterval();
      String targetVariable = rule.getVariable();
      
      if (rule.isSetSql()) {
         calculateDerivedValues(timestamp, interval , targetVariable, rule.getSql(), connection );
      } else if (rule.isSetAuto()) {
         com.sun.grid.reporting.dbwriter.model.DeriveRuleType.AutoType auto = rule.getAuto();
         String function = auto.getFunction();
         String sourceVariable = auto.getVariable();
         String sql = createAutoSQL(interval, function, sourceVariable);
         if (sql != null) {
            SGELog.config("StoredRecordManager.createdAutoSQL", sql);
            
            // set the generated SQL in the rule for subsequent calls
            rule.setSql(sql);
            
            // now execute the rule
            calculateDerivedValues(timestamp, interval , targetVariable, sql, connection);
         }
      }
   }
   
   private void calculateDerivedValues(long timestamp, String timeRange, String variableName, String sql, java.sql.Connection connection ) throws ReportingException {
      SGELog.config( "StoredRecordManager.executeRule", variableName );
      Timestamp timeStart;
      Timestamp timeEnd = getDerivedTimeEnd(timeRange, timestamp);

      // for all stored objects
      Statement objectsStmt = recordExecutor.queryAllObjects(connection);
      
      try {
         ResultSet objects = objectsStmt.getResultSet();
         try {
            while (objects.next()) {
               Record obj = recordExecutor.newDBRecord();
               obj.initFromResultSet(objects);
               // get timestamp of last entry for the variable
               timeStart = valueManager.getLastEntryTime(obj.getId(), variableName, connection );

               // if time_start != time_end: nothing to do
               if (timeStart.compareTo(timeEnd) != 0) {
                  // replace placeholders in the given SQL string
                  String cmd;
                  PrimaryKey pk = obj.getPrimaryKey();
                  cmd = sql.replaceAll("__time_start__", timeStart.toString());
                  cmd = cmd.replaceAll("__time_end__", timeEnd.toString());
                  
                  for (int i = 0; i < pk.getKeyCount(); i++) {
                     StringBuffer pattern = new StringBuffer("__key_");
                     pattern.append(i);
                     pattern.append("__");
                     //cmd = cmd.replaceAll("__key_" + i + "__", key[i]);
                     cmd = cmd.replaceAll(pattern.toString(), pk.getKey(i));
                  }

                  // execute SQL
                  Statement stmt = recordExecutor.executeQuery(cmd.toString(), connection);
                  try {
                     ResultSet rs = stmt.getResultSet();
                     try {
                        while(rs.next()) {
                           valueManager.handleNewDerivedRecord(obj, variableName, rs, connection);
                           recordExecutor.getDatabase().commit(connection, CommitEvent.INSERT );
                        }
                     } finally {
                        rs.close();
                     }                  
                  } finally {
                     stmt.close();
                  }               
               }
            }
         } finally {
            objects.close();
         }
      } catch( ReportingException re ) {
         recordExecutor.getDatabase().rollback( connection );
         throw re;
      } catch (Exception e) {         
         recordExecutor.getDatabase().rollback( connection );
         SGELog.warning( e, "ReportStoredObjectManager.unknownError", e.getMessage() );
      } finally {
         try {
            objectsStmt.close();
         } catch( SQLException sqle ) {
            SGELog.warning( sqle, "StoredRecordManager.stmtCloseFailed" );
         }
      }

      // generate derived values
   }
   
   abstract public Record findObject(ParserEvent e, java.sql.Connection connection ) throws ReportingException;
   abstract public void initRecordFromEvent(Record obj, ParserEvent e) throws ReportingException;
}
