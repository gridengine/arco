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
import java.util.logging.Level;

/**
 * The class implements the caches for the database objects
 * All instances are stored in a static list. The clearAllCaches method
 * clear all caches. 
 */
public class DatabaseObjectCache {
   public static final  int MAX_CACHE_SIZE = 1000;
   
   DatabaseStoredObjectManager manager;

   private   Object syncObj = new Object();
   
   protected ArrayList store;
   protected Map storeMap;
   
   private static List instances = new ArrayList();
   
   /** Creates a new instance of DatabaseObjectCache 
    */
   public DatabaseObjectCache(DatabaseStoredObjectManager p_manager) {
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
            ((DatabaseObjectCache)iter.next()).clear();
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
    * Get the a object from the cache. If the object is not cached it is
    * read from the database.
    * 
    * @param  pk         the primary key of the object
    * @param  connection connection to the database
    * @return the database object
    * @throws ReportingException if any error on the database layer occurs
    * @see    #readObject
    */
   public DatabaseObject getObject(PrimaryKey pk, java.sql.Connection connection ) throws ReportingException
   {
      DatabaseObject obj = null;
      synchronized( syncObj ) {
        obj = (DatabaseObject)storeMap.get(pk);
      }
      
      if (obj == null) {
         obj = readObject(pk, connection);        
      }
      SGELog.fine("Object for key {0} = {1}", pk, obj );
      return obj;
   }
   
   /**
    *   Add a object to the cache. The object is not written to the database
    *
    *   @param obj the database object
    */
   public void addObject(DatabaseObject obj) {
      
      PrimaryKey pk = obj.getPrimaryKey();
      
      synchronized( syncObj ) {
         DatabaseObject existingObj = (DatabaseObject)storeMap.get(pk);
         if(existingObj != null) {
            SGELog.fine("replace exiting obj (old = {0}, new = {1})", existingObj, obj);
            store.remove(existingObj);
         } else {
            SGELog.fine("add new object {0} to cache", obj);
         }
         
         store.add(obj);
         storeMap.put(pk, obj);
         // check if cache size has been exceeded
         if (store.size() > MAX_CACHE_SIZE) {
            // remove object from store
            DatabaseObject toDelete = (DatabaseObject)store.remove(0);
            // remove object from hashtable
            storeMap.remove(toDelete.getPrimaryKey());
            SGELog.fine( "DatabaseObjectCache.removeObject", toDelete );
         }
      }
   }

   /**
    *   Read a object from the database an store it in the cache. The primary key
    *   object is not the primary key of the table in the database. It is possible
    *   that the select query returns more then one row. This method assumes that
    *   the first row of the result contains the data for the database object.
    *   The <code>DatabaseObjectManager</code> can define serval <code>SortCriteria</code> 
    *   objects.which defines the sort order of the result.
    *
    *   @param  pk  the primary key object of the database object
    *   @return the database object
    *   @throws ReportingException on an error on the database layer
    *   @see    com.sun.grid.reporting.dbwriter.db.DatabaseObjectManager#getSortCriteria
    */
   protected DatabaseObject readObject(PrimaryKey pk, java.sql.Connection connection ) throws ReportingException {

      DatabaseObject obj = null;
      
      try {
         StringBuffer cmd = new StringBuffer("SELECT * FROM ");
         cmd.append(manager.getTable());
         cmd.append(" WHERE ");
         String keyFields[] = manager.getPrimaryKeyFields();
         
         if (pk.getKeyCount() != keyFields.length) {
            throw new ReportingException("DatabaseObjectCache.invalidPrimaryKeySize");
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
                  obj = manager.newObject();
                  obj.initFromResultSet(rs);
                  addObject(obj);
               }
            } finally {
               rs.close();
            }
         } finally {
            stmt.close();
         }
         
      /* here we catch multiple exceptions:
       * - SQLExceptions
       * - InstantiationException and IllegalAccessException from newObject()
       * maybe a more differentiated exception handling would be preferrable.
       */
      } catch (Exception e) {
         ReportingException re = new ReportingException("DatabaseObjectCache.readObjectFailed", e.getMessage());
         re.initCause( e );
         throw re;
      }
      
      return obj;
   }

}
