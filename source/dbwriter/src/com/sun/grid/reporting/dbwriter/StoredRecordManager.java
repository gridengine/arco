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
import com.sun.grid.reporting.dbwriter.event.RecordDataEvent;
import java.sql.*;
import java.util.*;
import com.sun.grid.logging.SGELog;
import com.sun.grid.reporting.dbwriter.db.*;

abstract public class StoredRecordManager extends RecordManager {
   protected ValueRecordManager valueManager = null;
   protected String condition;
   protected String primaryKeyFields[];
   protected RecordCache storedObjects;
   
   /**
    * Creates a new instance of StoredRecordManager
    */
   public StoredRecordManager(Database database, String table, String prefix, boolean hasParent,
         String primaryKey[], String condition, Controller controller) throws ReportingException {
      super(database, table, prefix, hasParent, controller);
      this.condition = condition;
      this.primaryKeyFields = primaryKey;
      this.storedObjects = new RecordCache(this);
   }
   
   public String getCondition() {
      return condition;
   }
      
   public String[] getPrimaryKeyFields() {
      return primaryKeyFields;
   }
   
   public Controller getController() {
      return controller;
   }
   
   public Record getDBRecord(PrimaryKey pk, java.sql.Connection connection ) throws ReportingException {
      return storedObjects.getStoredDBRecord(pk, connection);
   }
   
   public Statement queryAllObjects( java.sql.Connection connection ) throws ReportingException {
      StringBuffer sql = new StringBuffer("SELECT * FROM ");
      sql.append(getTable());
      
      // JG: TODO: append condition, create sql statement in constructor
      
      return database.executeQuery( sql.toString(), connection );
   }
   
   public void store(Record record, java.sql.Connection connection, Object lineNumber) throws ReportingException {
      // RH: TODO: update mechanism is yet not implemented.
      super.store(record, connection, lineNumber);
      storedObjects.addDBRecord(record);
   }
   
   public synchronized void processRecord(RecordDataEvent e, java.sql.Connection connection) throws ReportingException {
      Record record = null;
      record = findRecord(e, connection);
      
      if (record == null) {
         // new template
         super.processRecord(e, connection);
      } else {
         // existing object
         // JG: TODO: we have to update the object
         initSubRecordsFromEvent(record, e, connection);
      }
   }
   
   public Record findRecordFromEventData(Map data, Map map, java.sql.Connection connection ) throws ReportingException {
      Record obj = null;
      
      // read object primary key from event data
      String keyFields[] = getPrimaryKeyFields();
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
         obj = getDBRecord(createPrimaryKey(key), connection);
      }
      
      return obj;
   }
   
   /**
    * @throws com.sun.grid.reporting.dbwriter.ReportingException
    * @see {@link RecordExecutor}
    */
   public synchronized void flushBatches(java.sql.Connection connection) throws ReportingBatchException {
      //always call super first to execute the parentManager first
      super.flushBatches(connection);
      if (valueManager != null) {
         valueManager.flushBatches(connection);
      }
      
   }
   
   private String createAutoSQL(String interval, String function, String sourceVariable) {
   
      int dbType = Database.getType();
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
      RecordManager dbParent = valueManager.getParentManager();
      StringBuffer sql = new StringBuffer();
      
      String time_startField = valueManager.getPrefix() + "time_start";
      String time_endField = valueManager.getPrefix() + "time_end";
      String table = valueManager.getTable();
      String variableField = valueManager.getPrefix() + "variable";
      String valueField = valueManager.getPrefix() + "dvalue";
      String parentField = valueManager.getParentFieldName();
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
      RecordManager dbParent = valueManager.getParentManager();
      StringBuffer sql = new StringBuffer();
      
      String time_startField = valueManager.getPrefix() + "time_start";
      String time_endField = valueManager.getPrefix() + "time_end";
      String table = valueManager.getTable();
      String variableField = valueManager.getPrefix() + "variable";
      String valueField    = valueManager.getPrefix() + "dvalue";
      String parentField = valueManager.getParentFieldName();
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
      RecordManager dbParent = valueManager.getParentManager();
      StringBuffer sql = new StringBuffer();
      
      String time_startField = valueManager.getPrefix() + "time_start";
      String time_endField = valueManager.getPrefix() + "time_end";
      String table = valueManager.getTable();
      String variableField = valueManager.getPrefix() + "variable";
      String valueField    = valueManager.getPrefix() + "dvalue";
      String parentField = valueManager.getParentFieldName();
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
         java.sql.Connection connection) throws ReportingException {
      String interval = rule.getInterval();
      String targetVariable = rule.getVariable();
      
      if (rule.isSetSql()) {
         calculateDerivedValues(timestamp, interval , targetVariable, rule.getSql(), connection);
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
   
   private void calculateDerivedValues(long timestamp, String timeRange, String variableName, String sql,
         java.sql.Connection connection) throws ReportingException {
      SGELog.config( "StoredRecordManager.executeRule", variableName );
      Timestamp timeStart;
      Timestamp timeEnd = getDerivedTimeEnd(timeRange, timestamp);

      // for all stored objects
      Statement objectsStmt = queryAllObjects(connection);
      
      try {
         ResultSet objects = objectsStmt.getResultSet();
         try {
            while (objects.next()) {
               Record record = newDBRecord();
               record.initFromResultSet(objects);
               // get timestamp of last entry for the variable
               timeStart = valueManager.getLastEntryTime(record.getIdFieldValue(), variableName, connection);

               // if time_start == time_end: nothing to do
               if (timeStart.compareTo(timeEnd) != 0) {
                  // replace placeholders in the given SQL string
                  String cmd;
                  PrimaryKey pk = record.getPrimaryKey();
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
                  Statement stmt = database.executeQuery(cmd.toString(), connection);
                  try {
                     ResultSet rs = stmt.getResultSet();
                     try {
                        while(rs.next()) {
                           valueManager.handleNewDerivedRecord(record, variableName, rs, connection);
                           
                        }
                     } finally {
                        if(rs != null) {
                        rs.close();
                     }                  
                     }
                  } finally {
                     if (stmt != null) {
                     stmt.close();
                  }               
               }
            }
            }
         } finally {
            if (objects != null) {
            objects.close();
         }
         }
      } catch( ReportingException re ) {
         SGELog.info("Exception in calculating derived: " +re.getMessage());
         database.rollback( connection );
         throw re;
      } catch (Exception e) {         
         database.rollback( connection );
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
   
   abstract public Record findRecord(RecordDataEvent e, java.sql.Connection connection ) throws ReportingException;
   abstract public void initRecordFromEvent(Record obj, RecordDataEvent e) throws ReportingException;
}
