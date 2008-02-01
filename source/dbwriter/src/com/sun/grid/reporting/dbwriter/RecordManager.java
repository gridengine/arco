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

import com.sun.grid.reporting.dbwriter.event.RecordDataEvent;
import java.sql.*;
import java.util.*;
import com.sun.grid.logging.SGELog;
import com.sun.grid.reporting.dbwriter.db.*;
import com.sun.grid.reporting.dbwriter.db.Database.ConnectionProxy;
import com.sun.grid.reporting.dbwriter.event.RecordExecutor;

abstract public class RecordManager implements RecordExecutor {   
   private RecordManager parentManager;
   private SortCriteria [] sortCriteria;
   protected Database database;
   protected String table;
   protected String prefix;
   protected String idFieldName;
   protected String parentFieldName;
   protected int lastId = 0;
   protected Controller controller;
   
   
   /**
    * Creates a new instance of RecordManager
    */
   public RecordManager(Database database, String table,
         String prefix, boolean hasParent, Controller controller) throws ReportingException {
      
      this.database = database;
      this.table = table;
      this.prefix = prefix;
      this.idFieldName = new String(prefix + "id");
      if (hasParent) {
         this.parentFieldName = new String(prefix + "parent");
      }
      this.controller = controller;
      readLastId();
   }
   
   // we don't know of a primary key - this function is overwritten in
   // StoredRecordManager
   public String[] getPrimaryKeyFields() {
      return null;
   }
   
   /**
    * Create the primary key object for this database object
    *
    * @return the primary key object
    */
   public PrimaryKey createPrimaryKey(String [] keys) {
      return new PrimaryKey(keys);
   }
   
   /**
    * Get the number of sort critierias
    * @return  the number of sort criterias
    */
   public int getSortCriteriaCount() {
      return sortCriteria == null ? 0 : sortCriteria.length;
   }
   
   /**
    * Get a sort criteria
    * @param index the index of the sort criteria
    * @return the sort criteria
    */
   public SortCriteria getSortCriteria(int index) {
      return sortCriteria[index];
   }
   
   
   /**
    * <p>For some database records the primary key definition is not really a primary key.
    * The select statement which is executed by the <code>RecordCache</code> to
    * find the database object for a primary key can return more the one rows.</p>
    * <p>With the sort criteria the manager can specify how the result of this query
    * is sorted. The <code>RecordCache</code> assumes that the first row
    * of the returned result contains the database for the database records.</p>
    *
    * @see com.sun.grid.reporting.dbwriter.db.RecordCache#retrieveRecordFromDB
    * @param sortCriteria  the array with the sort criterias
    */
   public void setSortCriteria(SortCriteria[] sortCriteria) {
      this.sortCriteria = sortCriteria;
   }
   
   public Record getDBRecord(PrimaryKey pk, java.sql.Connection connection) throws ReportingException {
      return null;
   }
   
   public Statement queryAllObjects( java.sql.Connection connection ) throws ReportingException {
      // Default Implementations, return nothing
      return null;
   }
   
   public void setParentManager(RecordManager parentManager) {
      this.parentManager = parentManager;
   }
   
   public RecordManager getParentManager() {
      return parentManager;
   }
   
   public Database getDatabase() {
      return database;
   }
   
   public String getTable() {
      return table;
   }
   public String getPrefix() {
      return prefix;
   }
   
   public String getIdFieldName() {
      return idFieldName;
   }
   
   public String getParentFieldName() {
      return parentFieldName;
   }
   
   /**
    * @throws com.sun.grid.reporting.dbwriter.ReportingException
    * @see {@link RecordExecutor}
    */
   public synchronized void processRecord(RecordDataEvent event, java.sql.Connection connection) throws ReportingException {
      try {
         // create new object
         Record record = newDBRecord();
         // initialize object from data provided in RecordDataEvent
         initRecordFromEvent(record, event);
         store(record, connection, event.lineNumber);
         
         // store sub objects
         initSubRecordsFromEvent(record, event, connection);
      } catch (Exception exception) {
         ReportingException re = new ReportingException( "RecordManager.newLineParsed", exception.getMessage() );
         re.initCause( exception );
         throw re;
      }
      
   }
   
   protected synchronized void readLastId() throws ReportingException {
      try {
         StringBuffer cmd = new StringBuffer("SELECT ");
         
         int dbType = database.getType();
         switch (dbType) {
            case Database.TYPE_MYSQL:       // same as for postgres db
            case Database.TYPE_POSTGRES:
               // CR 6274371: aggregate functions requires full table scan on
               //             postges. Use 'order by <id-field> desc limit 1'
               cmd.append( idFieldName );
               cmd.append( " as max FROM ");
               cmd.append(table);
               cmd.append(" order by ");
               cmd.append(idFieldName);
               cmd.append(" desc limit 1");
               break;
            default:
               cmd.append( " MAX(" );
               cmd.append(idFieldName);
               cmd.append(") AS max FROM ");
               cmd.append(table);
         }
         
         java.sql.Connection conn = database.getConnection();
         
         Statement stmt = database.executeQuery(cmd.toString(), conn);
         ResultSet rs = null;
         try {
            rs = stmt.getResultSet();
            if (rs.next()) {
               lastId = rs.getInt("max");
            }
         } finally {
            rs.close();
            stmt.close();
            database.release(conn);
         }
      } catch (SQLException e) {
         ReportingException re = new ReportingException("RecordExecutor.readLastIdError", e.getMessage() );
         re.initCause( e );
         throw re;
      }
   }
   
   public synchronized void store(Record record, java.sql.Connection connection, Object lineNumber) throws ReportingException {
      record.setIdFieldValue(++lastId);
      insertInBatch(record, connection, lineNumber);
   }
   
   /**
    * @throws com.sun.grid.reporting.dbwriter.ReportingException
    * @see {@link RecordExecutor}
    */
   public synchronized void executeBatch(Connection connection) throws ReportingBatchException {
      PreparedStatement pstm = null;
      try {
         pstm = ((ConnectionProxy) connection).getPreparedStatement(this);
         if (pstm != null) {
            int [] updateCounts = pstm.executeBatch();
            SGELog.fine("BatchExecution.succes", new Integer(updateCounts.length), this.getTable());
         }
      } catch (BatchUpdateException e) {
         // Not all of the statements were successfully executed
         int[] updateCounts = e.getUpdateCounts();
         // process the errors to see what happend
         //we only process the updateCounts if the connection is not closed
         if (! ((ConnectionProxy)connection).getIsClosedFlag() && connection != null) {
            batchErrorHandling(updateCounts, e, pstm, connection);
         } else {
            throw new ReportingBatchException(e.getMessage());
         }
      } catch (SQLException sqle) {
         ReportingBatchException rbe = new ReportingBatchException(sqle.getMessage());
         rbe.initCause(sqle);
         throw rbe;
      }
   }
   
   
   
   /**
    * @see {@link Statement.executeBatch}
    * ORACLE: For a prepared statement batch, it is not possible to know which operation failed. The array has one element
    * for each operation in the batch, and each element has a value of -3. According to the JDBC 2.0 specification,
    * a value of -3 indicates that an operation did not complete successfully. In this case, it was presumably just one
    * operation that actually failed, but because the JDBC driver does not know which operation that was, it labels all
    * the batched operations as failures. http://download-uk.oracle.com/docs/cd/B14117_01/java.101/b10979/oraperf.htm
    *
    * For Oracle we have to use the ConnectionProxy.batchBackup, to retrieve the backup sql Statements and execute
    * one by one, so we can report back to the FileParser which line cause the error.
    */
   private void batchErrorHandling(int[] updateCounts, SQLException sqle, PreparedStatement pstm, Connection connection)
   throws ReportingBatchException {
      SQLException e;
      if (database.getType() == database.TYPE_MYSQL) {
         e = sqle;
      } else  {
         e = sqle.getNextException();
      }

      List list = ((ConnectionProxy)connection).getBackupList(this);
      if (list != null) {
         int count = list.size();       
         // Some databases will continue to execute after one statement in batch fails
         // If not, updateCounts.length will equal the number of successfully executed statements
         if (updateCounts.length < count) {
            BackupStatement bst = (BackupStatement) list.get(updateCounts.length);
            SGELog.warning("BatchExecution.failureNotContinued", bst.getInsertSql(), e);
            throw new ReportingBatchException(e.getMessage(), bst.getLineNumber());
            // If so, updateCounts.length will equal the number of batched statements.
         } else if (updateCounts.length == count) {
            SGELog.warning("BatchExecution.failureContinued");
            if(database.getType() == database.TYPE_ORACLE) {
               //execute the statements from backup to see which is wrong
               handleBatchBackup(list, connection);
            } else {
               for (int i = 0; i < updateCounts.length; i++) {
                  if (updateCounts[i] >= 0) {
                     // Successfully executed; the number represents number of affected rows
                  } else if (updateCounts[i] == Statement.SUCCESS_NO_INFO) {
                     // Successfully executed; number of affected rows not available
                  } else if (updateCounts[i] == Statement.EXECUTE_FAILED) {
                     // Failed to execute
                     BackupStatement bst = (BackupStatement) list.get(i);
                     SGELog.warning("BatchExecution.failureContinuedStatement", bst.getInsertSql(), e);
                     throw new ReportingBatchException(e.getMessage(), bst.getLineNumber());
                  }
               }
            }
         }
      } else {
         ReportingBatchException rbe = new ReportingBatchException(e.getMessage());
         rbe.initCause(e);
         throw rbe;
      }
   }
   
   /**
    * ORACLE: processes the statements from Backup to find out which caused error
    */
   private void handleBatchBackup(List list, Connection connection) throws ReportingBatchException {
      List parentList = null;
      RecordManager parent = getParentManager();
      if (parent != null) {
         //we need to get the parentManager's list to execute first, so we don't get constraints
         //exceptions
         parentList = ((ConnectionProxy)connection).getBackupList(parent);
      }
      database.rollback(connection);
      SGELog.fine("BatchExecution.processAfterFailure");
      int parentIndex = 0;
      int index = 0;
      Connection conn = null;
      Statement stmt = null;
      
      try {
         conn = database.getConnection();
      } catch (ReportingException re) {
         throw new ReportingBatchException(re.getMessage());
      }
      
      try {
         try {
            if (parentList != null) {
               
               //we should not get any exception from this execution but if we do we report it
               //as an invalid line
               for (Iterator i = parentList.iterator(); i.hasNext();) {
                  String sql = ((BackupStatement)i.next()).getInsertSql();
                  stmt = conn.createStatement();
                  stmt.execute(sql);
                  parentIndex++;
               }
            }
         } catch (SQLException sqle) {
            BackupStatement bst = (BackupStatement) parentList.get(parentIndex);
            database.rollback(conn);
            throw new ReportingBatchException(sqle.getMessage(), bst.getLineNumber());
         } finally {
            try {
               if(stmt != null) {
                  stmt.close();
               }
            } catch (SQLException sqle) {
               SGELog.warning(sqle, "ReportingDBWriter.sqlError");
            }
         }
         
         try {
            for (Iterator it = list.iterator(); it.hasNext();) {
               String sql = ((BackupStatement)it.next()).getInsertSql();
               stmt = conn.createStatement();
               stmt.execute(sql);
               index++;
            }
         } catch (SQLException sqle) {
            BackupStatement bst = (BackupStatement) list.get(index);
            Integer num = (Integer)bst.getLineNumber();
            database.rollback(conn);
            throw new ReportingBatchException(sqle.getMessage(), new Integer(num.toString()));
         } finally {
            try {
               if(stmt != null) {
                  stmt.close();
               }
            } catch (SQLException sqle) {
               SGELog.warning(sqle, "ReportingDBWriter.sqlError");
            }
         }
      } finally {
         database.release(conn);
      }
   }
   
   
   /**
    * It binds the PreparedStatement and adds it to batch
    *
    * @param record - the record used to retrieved PreparedStatement String, and the values used for binding
    * @param connection - the connection used to create the PreparedStatement
    */
   protected synchronized void insertInBatch(Record record, Connection connection, Object lineNumber) throws ReportingException {
      try {
         
         PreparedStatement pstm = ((ConnectionProxy) connection).prepareStatement(record.getPstmString(), this);
         // used ot set proper index when looping through the fields returned
         // by record.getFields() (this array does not contain the idField, or parentField, only the fields that
         // are created when calling newDBRecord()
         int index = 1;
         
         //first value set in PSTM is always the idField
         record.getIdField().setValueForPSTM(pstm, index++);
         
         //if the record contains parentField (i.e. this is the dependent child record)
         //then the second entry in PSTM is parentField and we
         //need to increment the child variable so we can use it
         //when looping through the rest of the fields
         if (record.getParentField() != null) {
            record.getParentField().setValueForPSTM(pstm, index++);
         }
         
         //now we bind the rest of the fields
         Field [] fields = record.getFields();
         for (int i = 0; i < fields.length ; i++) {
            if (fields[i].doStore()) {
               fields[i].setValueForPSTM(pstm, i + index);
            }
         }
         
         pstm.addBatch();
         BackupStatement backup = new BackupStatement(lineNumber, record.getStatementString());
         SGELog.finest("BatchExecution.insertInBatch", backup.getInsertSql(), lineNumber);
         ((ConnectionProxy) connection).insertBackup(this, backup);
         
      } catch (SQLException ex) {
         ReportingException re = new ReportingException(ex.getMessage());
         re.initCause(ex);
         throw re;
      }
   }
   
   public void handleNewSubRecord(Record parent, RecordDataEvent e, java.sql.Connection connection) throws ReportingException {
      try {
         Record record = newDBRecord();
         record.setParentFieldValue(parent.getIdFieldValue());
         initRecordFromEvent(record, e);
         store(record, connection, e.lineNumber);
      } catch (Exception exception) {
         ReportingException re = new ReportingException( "RecordManager.handleNewSubObjectFailed", exception.getMessage() );
         re.initCause( exception );
         throw re;
      }
   }
   
   public void initRecordFromEventData(Record obj, Map data, Map map) {
      Iterator iter = map.keySet().iterator();
      // initialize object from event data
      while (iter.hasNext()) {
         String objKey = (String)iter.next();
         String dataKey = (String)map.get(objKey);
         Field objField = obj.getField(objKey);
         Field dataField = (Field) data.get(dataKey);
         objField.setValue(dataField);
      }
   }
   
   public void initSubRecordsFromEvent(Record obj, RecordDataEvent e, java.sql.Connection connection) throws ReportingException {
      // default: nothing to be done for most objects
   }
   
   /**
    * @throws com.sun.grid.reporting.dbwriter.ReportingException
    * @see {@link RecordExecutor}
    */
   public synchronized void flushBatches(java.sql.Connection connection) throws ReportingBatchException {
      this.executeBatch(connection);
   }
   
   public abstract void initRecordFromEvent(Record obj, RecordDataEvent e) throws ReportingException;
   
   /**
    * Every <code>RecordManager<code> must implement this method to return
    * the appropriate Record that it manages
    */
   public abstract Record newDBRecord();
   
   public String getDeleteLimit() {
      
      int dbType = database.getType();
      StringBuffer sql = new StringBuffer();
      
      switch (dbType) {
         case Database.TYPE_MYSQL:       // same as for postgres db
         case Database.TYPE_POSTGRES:
            // limit the number of rows deleted in one transaction keyword is limit
            sql.append(" limit ");
            sql.append(Database.DELETE_LIMIT);
            break;
         default:
            sql.append( " AND  rownum < ");
            sql.append(Database.DELETE_LIMIT + 1);
      }
      
      return sql.toString();
   }
   
   static public Timestamp getDeleteTimeEnd(long timestamp, String timeRange, int timeAmount) {
      // JG: TODO: we may need to handle some locale specific stuff
      //           do we want to use GMT as time basis? Probably better than
      //           local time.
      Calendar now = Calendar.getInstance();
      now.setTimeInMillis( timestamp );
      
      if (timeRange.compareTo("hour") == 0) {
         now.add(Calendar.HOUR_OF_DAY, -timeAmount);
      } else if (timeRange.compareTo("day") == 0) {
         now.add(Calendar.DAY_OF_MONTH, -timeAmount);
      } else if (timeRange.compareTo("month") == 0) {
         now.add(Calendar.MONTH, -timeAmount);
      } else if (timeRange.compareTo("year") == 0) {
         now.add(Calendar.YEAR, -timeAmount);
      } else {
         SGELog.warning( "RecordManager.unkownTimeRange", timeRange );
      }
      
      return new Timestamp(now.getTimeInMillis());
   }
   
   static public Timestamp getDerivedTimeEnd(String timeRange, long timestamp) {
      // JG: TODO: we may need to handle some locale specific stuff
      //           do we want to use GMT as time basis? Probably better than
      //           local time.
      Calendar now = Calendar.getInstance();
      now.setTimeInMillis( timestamp );
      
      int field;
      
      if( timeRange.equals( "hour") ) {
         field = Calendar.HOUR_OF_DAY;
      } else if ( timeRange.equals( "day") ) {
         field = Calendar.DAY_OF_MONTH;
      } else if ( timeRange.equals( "month") ) {
         field = Calendar.MONTH;
      } else if ( timeRange.equals( "year") ) {
         field = Calendar.YEAR;
      } else {
         throw new IllegalArgumentException("Invalid timeRange " + timeRange);
      }
      
      switch( field ) {
         case Calendar.YEAR:
            now.set( Calendar.MONTH, Calendar.JANUARY );
         case Calendar.MONTH:
            now.set( Calendar.DAY_OF_MONTH, 0 );
         case Calendar.DAY_OF_MONTH:
            now.set( Calendar.HOUR_OF_DAY, 0 );
      }
      now.set(Calendar.MINUTE, 0);
      now.set(Calendar.SECOND, 0);
      now.set(Calendar.MILLISECOND, 0);
      
      return new Timestamp(now.getTimeInMillis());
   }
   
   static public String getDateTimeFormat(String timeRange) {
      
      String fmt = null;
      
      if( timeRange.equalsIgnoreCase( "hour") ) {
         fmt = "%Y-%m-%d %H:00:00";
      } else if ( timeRange.equalsIgnoreCase( "day") ) {
         fmt = "%Y-%m-%d 00:00:00";
      } else if ( timeRange.equalsIgnoreCase( "month") ) {
         fmt = "%Y-%m-01 00:00:00";
      } else if ( timeRange.equalsIgnoreCase( "year") ) {
         fmt = "%Y-01-01 00:00:00";
      } else {
         throw new IllegalArgumentException("Invalid timeRange " + timeRange );
      }
      
      return fmt;
   }
   
}
