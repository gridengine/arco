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


public class DatabaseObjectManager {
   protected Database database;
   protected DatabaseObject template;
   protected String table;
   protected String prefix;
   protected String idFieldName;
   protected String parentFieldName;
   protected int lastId = 0;
   
   /** Creates a new instance of DatabaseObjectManager */
   public DatabaseObjectManager(Database p_database, String p_table, 
                                String p_prefix, boolean hasParent,
                                DatabaseObject p_template ) 
        throws ReportingException  {
      database = p_database;
      table = p_table;
      prefix = p_prefix;
      idFieldName = new String(prefix + "id");
      if (hasParent) {
         parentFieldName = new String(prefix + "parent");
      }
      
      template = p_template;
      
      java.sql.Connection conn = database.getConnection();
      try {
         readLastId( conn );
      } finally {
         database.release( conn );
      }
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
   
   public DatabaseObject getTemplate() {
      return template;
   }
   
   // we don't know of a primary key - this function is overwritten in 
   // DatabaseStoredObjectManager
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
   
   /** the sort criterias. */
   private SortCriteria [] sortCriteria;
   
   
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
    * <p>For some database objects the primary key definition is not really a primary key.
    * The select statement which is executed by the <code>DatabaseObjectCache</code> to 
    * find the database object for a primary key can return more the one rows.</p>
    * <p>With the sort criteria the manager can specify how the result of this query
    * is sorted. The <code>DatabaseObjectCache</code> assumes that the first row
    * of the returned result contains the database for the database object.</p>
    *
    * @see com.sun.grid.reporting.dbwriter.db.DatabaseObjectCache#readObject
    * @param sortCriteria  the array with the sort criterias
    */
   public void setSortCriteria(SortCriteria[] sortCriteria) {
      this.sortCriteria = sortCriteria;
   }
   
   public DatabaseObject getObject(PrimaryKey pk, java.sql.Connection connection ) throws ReportingException {
      return null;
   }
   
   protected void readLastId( java.sql.Connection connection ) throws ReportingException {
      try {
         StringBuffer cmd = new StringBuffer("SELECT ");
         
         int dbType = ((Database.ConnectionProxy)connection).getDBType();
         switch( dbType ) {
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
         
         
         Statement stmt = database.executeQuery(cmd.toString(), connection );
         try {
            ResultSet rs = stmt.getResultSet();
            try {
               if (rs.next()) {
                  lastId = rs.getInt("max");
               }
            } finally {
               rs.close();
            }            
         } finally {
            stmt.close();
         }         
      } catch (SQLException e) {
         ReportingException re = new ReportingException("DatabaseObjectManager.readLastIdError", e.getMessage() );
         re.initCause( e );
         throw re;
      }
   }
   
   public DatabaseObject newObject() throws InstantiationException, IllegalAccessException {
      return template.newObject(this);
   }
   
   public void store(DatabaseObject obj, java.sql.Connection connection ) throws ReportingException {
      obj.setId(++lastId);
      obj.insertInDatabase( connection );
   }
   
   public void execute(String sql, java.sql.Connection connection ) throws ReportingException {
      database.execute(sql, connection );
   }
   
   public Statement executeQuery(String sql, java.sql.Connection connection ) throws ReportingException {
      return database.executeQuery(sql, connection );
   }
   
   public Statement queryAllObjects( java.sql.Connection connection ) throws ReportingException {
      // Default Implementations, return nothing
      return null;
   }
   

}
