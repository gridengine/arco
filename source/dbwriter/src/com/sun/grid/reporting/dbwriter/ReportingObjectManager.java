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
import com.sun.grid.reporting.dbwriter.file.*;
import com.sun.grid.reporting.dbwriter.model.*;

abstract public class ReportingObjectManager implements NewObjectListener {
   protected DatabaseObjectManager databaseObjectManager;
   private ReportingObjectManager parentManager;
   
   /** Creates a new instance of ReportingObjectManager */
   public ReportingObjectManager(DatabaseObjectManager databaseObjectManager) {
      this.databaseObjectManager = databaseObjectManager;
   }
   
   public ReportingObjectManager(Database database, String table,
   String prefix, boolean hasParent,
   DatabaseObject template) throws ReportingException
   {
      databaseObjectManager = new DatabaseObjectManager(database, table, prefix, hasParent, template);
   }

   /**
    *   Create the a primary key object for database objects which are handled
    *   by this ReportObjectManager
    *
    *   @param   key    array with all values of the primary key field. The 
    *                   index corresponds to the index of the primary key fields
    *   @return  the primary key object
    *   @see com.sun.grid.reporting.dbwriter.DatabaseObjectManager#createPrimaryKey
    */
   public PrimaryKey createPrimaryKey(String [] key) {
      return databaseObjectManager.createPrimaryKey(key);
   }
   
   public void setParentManager(ReportingObjectManager parentManager) {
      this.parentManager = parentManager;
   }
   
   public ReportingObjectManager getParentManager() {
      return parentManager;
   }
   
   public DatabaseObjectManager getDatabaseObjectManager() {
      return databaseObjectManager;
   }
   
   public void handleNewObject(ReportingEventObject event, java.sql.Connection connection ) throws ReportingException { 
      try {
         // create new object
         DatabaseObject obj = databaseObjectManager.newObject();
         // initialize object from data provided in NewObjectEvent
         initObjectFromEvent(obj, event);
         
         // store the object
         obj.store( connection );
         
         // store sub objects
         initSubObjectsFromEvent(obj, event, connection);
      } catch (Exception exception) {
         ReportingException re = new ReportingException( "ReportingObjectManager.handleNewObjectFailed", exception.getMessage() );
         re.initCause( exception );
         throw re;
      }
   }
   
   public void handleNewSubObject(DatabaseObject parent, ReportingEventObject e, java.sql.Connection connection ) throws ReportingException {
      try {
         DatabaseObject obj = databaseObjectManager.newObject();
         obj.setParent(parent.getId());
         initObjectFromEvent(obj, e);
         obj.store( connection );
      } catch (Exception exception) {
         ReportingException re = new ReportingException( "ReportingObjectManager.handleNewSubObjectFailed", exception.getMessage() );
         re.initCause( exception );
         throw re;
      }
   }
   
   public void initObjectFromEventData(DatabaseObject obj, Map data, Map map) {
      Iterator iter = map.keySet().iterator();
      // initialize object from event data
      while (iter.hasNext()) {
         String objKey = (String)iter.next();
         String dataKey = (String)map.get(objKey);
         DatabaseField objField = obj.getField(objKey);
         DatabaseField dataField = (DatabaseField) data.get(dataKey);
         objField.setValue(dataField);
      }
   }
   
   public void executeDeleteRule( long timestamp, com.sun.grid.reporting.dbwriter.model.DeletionRuleType rule, java.sql.Connection connection ) throws ReportingException {
      
      executeDeleteRule(timestamp, rule.getScope(), rule.getTimeRange(), rule.getTimeAmount(), rule.getSubScope(), connection  );
      
   }
   public void executeDeleteRule(long timestamp, String rule, String time_range, int time_amount, List variables, java.sql.Connection connection )
     throws ReportingException {
      SGELog.config( "ReportingObjectManager.executeDeleteRule", time_range, new Integer( time_amount ) );
      
      String sql[] = getDeleteRuleSQL(timestamp, time_range, time_amount, variables);
      if (sql == null) {
         SGELog.warning( "ReportingObjectManager.unknownRule", rule );
      } else {
         for (int i = 0; i < sql.length; i++) {
            databaseObjectManager.execute(sql[i], connection );
         }
      }
   }

   public final String[] getDeleteRuleSQL(long timestamp, String time_range, int time_amount, String values) {
      throw new IllegalStateException("getDeleteRuleSQL should never be invoked");
   }
   
   public String[] getDeleteRuleSQL(long timestamp, String time_range, int time_amount, List values) {
      return null;
   }
   
   public void initSubObjectsFromEvent(DatabaseObject obj, ReportingEventObject e, 
                                       java.sql.Connection connection) throws ReportingException {
      // default: nothing to be done for most objects
   }
   
   abstract public void initObjectFromEvent(DatabaseObject obj, ReportingEventObject e) throws ReportingException;
   
   
   
   
   static public Timestamp getDeleteTimeEnd( long timestamp, String timeRange, int timeAmount) {
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
          SGELog.warning( "ReportingObjectManager.unkownTimeRange", timeRange );
      }
      
      return new Timestamp(now.getTimeInMillis());
   }
   
   static public Timestamp getDerivedTimeEnd(String timeRange, long timestamp ) {
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

}
