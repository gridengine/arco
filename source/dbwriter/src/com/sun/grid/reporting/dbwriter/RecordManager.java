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

import com.sun.grid.reporting.dbwriter.event.ParserEvent;
import com.sun.grid.reporting.dbwriter.event.ParserListener;
import java.sql.*;
import java.util.*;
import com.sun.grid.logging.SGELog;
import com.sun.grid.reporting.dbwriter.db.*;
import com.sun.grid.reporting.dbwriter.file.*;
import com.sun.grid.reporting.dbwriter.model.*;

abstract public class RecordManager implements ParserListener {
   protected RecordExecutor recordExecutor;
   private RecordManager parentManager;
   
   /**
    * Creates a new instance of RecordManager
    */
   public RecordManager(RecordExecutor recordExecutor) {
      this.recordExecutor = recordExecutor;
   }
   
   public RecordManager(Database database, String table,
         String prefix, boolean hasParent, Record template) throws ReportingException {
      recordExecutor = new RecordExecutor(database, table, prefix, hasParent, template);
   }
   
   /**
    *   Create the a primary key object for database objects which are handled
    *   by this ReportObjectManager
    *
    * @param key    array with all values of the primary key field. The
    *                   index corresponds to the index of the primary key fields
    * @return the primary key object
    * @see com.sun.grid.reporting.dbwriter.DatabaseObjeDatabaseRecordManagerryKey
    */
   public PrimaryKey createPrimaryKey(String [] key) {
      return recordExecutor.createPrimaryKey(key);
   }
   
   public void setParentManager(RecordManager parentManager) {
      this.parentManager = parentManager;
   }
   
   public RecordManager getParentManager() {
      return parentManager;
   }
   
   public RecordExecutor getRecordExecutor() {
      return recordExecutor;
   }
   
   public void newLineParsed(ParserEvent event, java.sql.Connection connection ) throws ReportingException {
      try {
         // create new object
         Record record = recordExecutor.newDBRecord();
         // initialize object from data provided in ParserEvent
         initRecordFromEvent(record, event);
         
         // store the object
         record.store( connection );
         
         // store sub objects
         initSubRecordsFromEvent(record, event, connection);
      } catch (Exception exception) {
         ReportingException re = new ReportingException( "RecordManager.handleNewObjectFailed", exception.getMessage() );
         re.initCause( exception );
         throw re;
      }
   }
   
   public void handleNewSubRecord(Record parent, ParserEvent e,
         java.sql.Connection connection ) throws ReportingException {
      try {
         Record obj = recordExecutor.newDBRecord();
         obj.setParent(parent.getId());
         initRecordFromEvent(obj, e);
         obj.store( connection );
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
   
   public void initSubRecordsFromEvent(Record obj, ParserEvent e,
         java.sql.Connection connection) throws ReportingException {
      // default: nothing to be done for most objects
   }
   
   abstract public void initRecordFromEvent(Record obj, ParserEvent e) throws ReportingException;
   
   public String getDeleteLimit() {
      
      int dbType = Database.getType();
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
         throw new IllegalArgumentException("Invalid timeRange " + timeRange );
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
