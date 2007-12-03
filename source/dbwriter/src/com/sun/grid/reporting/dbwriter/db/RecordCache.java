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
import com.sun.grid.reporting.dbwriter.StoredRecordManager;

/**
 * The class implements the caches for the database records
 * All instances are stored in a static list. The clearAllCaches method
 * clear all caches. 
 */
public class RecordCache {
   public static final  int MAX_CACHE_SIZE = 1000;
   
   final StoredRecordManager manager;

   private final Object syncObj = new Object();
   
   protected final ArrayList store;
   protected final Map storeMap;
   
   private final static List instances = new ArrayList();
   
   /**
    * Creates a new instance of RecordCache
    */
   public RecordCache(StoredRecordManager p_manager) {
      manager = p_manager;

      store = new ArrayList();
      storeMap = new HashMap();      
      instances.add( this );
   }
   
   public static void clearAllCaches() {
      SGELog.fine( "clear all caches" );
      synchronized( instances ) {
         Iterator iter = instances.iterator();
         while( iter.hasNext() ) {
            ((RecordCache)iter.next()).clear();
         }
      }
   }
   
   public void clear() {
      synchronized( syncObj ) {
         store.clear();
         storeMap.clear();
      }
   }

   /**
    * Get the tored DB Record from the cache. If the record is not cached it is
    * read from the database.
    * 
    * 
    * @param pk - the primary key of the DB Record
    * @param connection - the connection to the database
    * @return the DB Record
    * @see #retrieveRecordFromDB
    * @throws ReportingException if any error on the database layer occurs
    */
   public Record getStoredDBRecord(PrimaryKey pk, java.sql.Connection connection ) throws ReportingException
   {
      Record dbRecord = null;
      synchronized( syncObj ) {
        dbRecord = (Record)storeMap.get(pk);
      }
      
      //not in cache, retrieve the record from the database
      if (dbRecord == null) {
         dbRecord = retrieveRecordFromDB(pk, connection);   

      }
      SGELog.fine("Object for key {0} = {1}", pk, dbRecord );
      return dbRecord;
   }
   
   /**
    *   Add DB Record to the cache. The DB Record is not written to the database
    * 
    * @param dbRecord the DB Record to be cached
    */
   public void addDBRecord(Record dbRecord) {
      
      PrimaryKey pk = dbRecord.getPrimaryKey();
      
      synchronized( syncObj ) {
         Record existingRecord = (Record)storeMap.get(pk);
         if(existingRecord != null) {
            SGELog.fine("replace exiting obj (old = {0}, new = {1})", existingRecord, dbRecord);
            store.remove(existingRecord);
         } else {
            SGELog.fine("add new object {0} to cache", dbRecord);
         }
         
         store.add(dbRecord);
         storeMap.put(pk, dbRecord);
         // check if cache size has been exceeded
         if (store.size() > MAX_CACHE_SIZE) {
            // remove object from store
            Record toDelete = (Record)store.remove(0);
            // remove object from hashtable
            storeMap.remove(toDelete.getPrimaryKey());
            SGELog.fine( "RecordCache.removeObject", toDelete );
         }
      }
   }

   /**
    *   Retrieve the DB Record from the database and then add it to the cache. The primary key
    *   object is not the primary key of the table in the database. It is possible
    *   that the select query returns more then one row. This method assumes that
    *   the first row of the result contains the data for the database object.
    *   The <code>DatabaseRecordManager</code> can define serval <code>SortCriteria</code> 
    *   objects.which defines the sort order of the result.
    * 
    * @param pk  the primary key of the <code>Record</code>
    * @return <code>Record</code>
    * @see com.sun.grid.reporting.dbwriter.db.RecordManager#getSortCriteria
    * @throws ReportingException on an error on the database layer
    */
   protected Record retrieveRecordFromDB(PrimaryKey pk, java.sql.Connection connection ) throws ReportingException {

      Record dbRecord = null;
      
      try {
         StringBuffer cmd = new StringBuffer("SELECT * FROM ");
         cmd.append(manager.getTable());
         cmd.append(" WHERE ");
         String keyFields[] = manager.getPrimaryKeyFields();
         
         if (pk.getKeyCount() != keyFields.length) {
            throw new ReportingException("RecordCache.invalidPrimaryKeySize");
         }
         for (int i = 0; i < keyFields.length; i++) {
            String key = pk.getKey(i);
            if (key != null) {
               if (i > 0){
                  cmd.append(" AND ");
               }
               cmd.append(keyFields[i]);
               cmd.append(" = ");
               cmd.append(key);
            }
         }

         String condition = manager.getCondition();
         if (condition != null) {
            cmd.append(" AND ");
            cmd.append(condition);
         }
         
         if (manager.getSortCriteriaCount() > 0) {
            cmd.append( " ORDER BY " );
            
            for (int i = 0; i < manager.getSortCriteriaCount(); i++) {
               if (i>0) {
                  cmd.append(", ");
               }
               SortCriteria sc = manager.getSortCriteria(i);
               cmd.append( sc.getFieldName() );
               switch (sc.getDirection()) {
                  case SortCriteria.ASCENDING:
                     cmd.append(" ASC "); 
                     break;
                  case SortCriteria.DESCENDING:
                     cmd.append(" DESC "); 
                     break;
                  default:
                     throw new IllegalStateException("Unknown direction for sort criteria " 
                                                   + sc.getFieldName() 
                                                   + "(" + sc.getDirection() + ")");
               }
            }
         }

         Statement stmt = manager.getDatabase().executeQuery(cmd.toString(), connection );
         try {
            ResultSet rs = stmt.getResultSet();
            try {
               if (rs.next()) {
                  dbRecord = manager.newDBRecord();
                  dbRecord.initFromResultSet(rs);
                  addDBRecord(dbRecord);
               }
            } finally {
               rs.close();
            }
         } finally {
            stmt.close();
         }
         
      /* here we catch multiple exceptions:
       * - SQLExceptions
       * - InstantiationException and IllegalAccessException from newDBRecord()
       * maybe a more differentiated exception handling would be preferrable.
       */
      } catch (Exception e) {
         ReportingException re = new ReportingException("RecordCache.readObjectFailed", e.getMessage());
         re.initCause( e );
         throw re;
      }
      
      return dbRecord;
   }

}
