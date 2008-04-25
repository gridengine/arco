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
package com.sun.grid.arco.sql;

import com.iplanet.jato.view.html.Option;
import com.iplanet.jato.view.html.OptionList;
import com.sun.grid.arco.model.Configuration;
import com.sun.grid.logging.SGELog;

import java.io.File;
import java.util.*;
import java.sql.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import com.sun.grid.arco.ArcoConstants;
import com.sun.grid.arco.model.*;

/**
 * <p><code>ArcoDbConnectionPool</code>
 * </p>
 *
 */
public class ArcoDbConnectionPool implements ArcoConstants {

   public List getDatabaseList() {
      List list = new ArrayList();
      for (Iterator it = pools.iterator(); it.hasNext();) {
         ClusterConnectionPool pool = (ClusterConnectionPool) it.next();
         list.add(pool.getDb());
      }
      return list;
   }

   /**
    * This function should go tu util functions or somewhere to support layer
    * not to fill the model by IU related imports
    * @return
    */
   public OptionList getOptionList() {
      OptionList optList = new OptionList();
      final List dbList = this.getDatabaseList();
      for (int i = 0; i < dbList.size(); i++) {
         DatabaseType db = (DatabaseType) dbList.get(i);
         final Option opt = new Option(db.getClusterName(), db.getClusterName());
         optList.add(opt);
      }
      return optList;
   }

   /**
    * Get instance of the service
    * @return
    */
   public static ArcoDbConnectionPool getInstance() {
      synchronized (ArcoDbConnectionPool.class) {
         if (instance == null) {
            instance = new ArcoDbConnectionPool();
         }
      }

      return instance;
   }

   /**
    * Get the connection for cluster index
    * @param current String name of the cluster
    * @return the cluster connection
    * @throws java.sql.SQLException something wrong
    */
   public ArcoDbConnection getConnection(String current) throws java.sql.SQLException {
      init();
      int clusterIndex =  getClusterIndex(current);
      final ClusterConnectionPool pool = (ClusterConnectionPool) pools.get(clusterIndex);
      return pool.getConnection();
   }

   /**
    * Return conection, the right cluster is stored inside the connection 
    * @param connection to return
    */
   public void releaseConnection(ArcoDbConnection connection) {
      int index = getPoolIndex(connection.getDatasource());
      if (index < 0 && index >= pools.size()) {
         connection.closeConnection();
         return;
      }
      final ClusterConnectionPool pool = (ClusterConnectionPool) pools.get(index);
      pool.releaseConnection(connection);
   }

   /**
    * Return the cluster index for given cluster name
    * @param clusterName
    * @return int index of he cluster or negative if not exists
    */
   public int getClusterIndex(String clusterName) {
      if(clusterName==null || clusterName.length()==0){ //No cluster is default cluster
         return 0;
      }
      for (int i = 0; i < pools.size(); i++) {
         ClusterConnectionPool pool = (ClusterConnectionPool) pools.get(i);
         if (pool.getDb().getClusterName().equals(clusterName)) {
            return i;
         }
      }
      return -1; //Invalid cluster name
   }
   
   
   /**
    * Return the cluster name for given cluster index
    * @param clusterName
    * @return String name of the cluster or null
    */
   public String getClusterName(int clusterIndex) {
      if(clusterIndex < clusterIndex || clusterIndex >= pools.size()){
         return null;
      }
      return ((ClusterConnectionPool)pools.get(clusterIndex)).getDb().getClusterName();
   }   

   /**
    * Return cluster index by matching the ArcoDbConnection datasource property
    * @param datasource from connection
    * @return the index value
    */
   int getPoolIndex(javax.sql.ConnectionPoolDataSource datasource) {
      for (int i = 0; i < pools.size(); i++) {
         ClusterConnectionPool pool = (ClusterConnectionPool) pools.get(i);
         if (pool.getDatasource().equals(datasource)) {
            return i;
         }
      }
      return 0;
   }

   public void releaseConnections() {
      for (Iterator it = pools.iterator(); it.hasNext();) {
         ClusterConnectionPool pool = (ClusterConnectionPool) it.next();
         pool.releaseConnections();
      }
      pools.clear();
   }
   
   /**
    * Get the cached List of the database views, tables and synonyms
    * @param current the <code>String</code> name of the cluster
    * @return the list of the <code>String</code> names
    * @throws java.sql.SQLException problems on getting the list
    */
   public List getViewList(String current) throws SQLException {
      int clusterIndex =  getClusterIndex(current);
      final ClusterConnectionPool pool = (ClusterConnectionPool) pools.get(clusterIndex);
      return pool.getViewList();
   }
   
   /**
    * Get the cached List of the fiels names for specified table, view or synonym
    * @param table view or synonym <code>String</code> name
    * @param current <code>String</code> name of the cluster
    * @return the list of the <code>String</code> fields names
    * @throws java.sql.SQLException
    */
   public Map getFieldList(String table, String current) throws SQLException {
      int clusterIndex =  getClusterIndex(current);
      final ClusterConnectionPool pool = (ClusterConnectionPool) pools.get(clusterIndex);
      return pool.getFieldList(table);
   }
   
   // Required by tests
   ClusterConnectionPool getTestPool() {
      //No test dataabase specific ClusteConnectionPool
      if(pools.size()==0) {
         //Lets create one
         pools.add(new ClusterConnectionPool());
      }
      return (ClusterConnectionPool) pools.get(0);
   }

   /**
    * Inner class which holds all the data related to one databese connection pool
    */
   class ClusterConnectionPool {

      private ArrayList connections = new ArrayList();
      private Stack freeConnections = new Stack();
      private javax.sql.ConnectionPoolDataSource datasource;
      private DatabaseType db;
      
      /**
       * The List of the cached table fields names
       */
      private Map tableFieldListMap = new HashMap();
      /**
       * The List of the cache tables, viuews and synonyms
       */
      private ArrayList viewList;

      public ArcoDbConnection getConnection() throws java.sql.SQLException {

         try {
            ArcoDbConnection ret = null;

            synchronized (connections) {

               while (freeConnections.isEmpty()) {
                  SGELog.warning("No db connection free, wait");
                  connections.wait();
               }

               ret = (ArcoDbConnection) freeConnections.pop();
               connections.add(ret);
            }
            SGELog.fine("Got db:{0} connection id {1}", db.getName(), ret.getId());
            return ret;
         } catch (InterruptedException ire) {
            return null;
         }
      }

      public void releaseConnection(ArcoDbConnection connection) {
         synchronized (connections) {
            connection.closeConnection();
            connections.remove(connection);
            SGELog.fine("Connection db:{0} released id {1}", db.getName(), connection.getId());
            freeConnections.push(connection);
            connections.notify();
         }
      }

      public void releaseConnections() {
         synchronized (connections) {
            ArcoDbConnection conn = null;
            while (!connections.isEmpty()) {
               conn = (ArcoDbConnection) connections.get(0);
               releaseConnection(conn);
            }
         }
         if (connections != null) {
            connections.clear();
         }
         if (freeConnections != null) {
            freeConnections.clear();
         }
      }

      private void init(DatabaseType db) throws java.sql.SQLException {
         this.db = db;
         synchronized (connections) {
            String type = db.getDriver().getType();
            setDatasource(getSQLGenerator(type).createDatasource(db));

            // Initialize the free connections

            ArcoDbConnection conn = null;
            int max = db.getUser().getMaxConnections();
            for (int i = 0; i < max; i++) {
               conn = new ArcoDbConnection(i,
                  db.getDriver().getType(),
                  db.getSchema(), datasource);
               freeConnections.push(conn);
            }
            connections.notify();
         }
      }

      public javax.sql.ConnectionPoolDataSource getDatasource() {
         return datasource;
      }

      public void setDatasource(javax.sql.ConnectionPoolDataSource datasource) {
         this.datasource = datasource;
      }

      public DatabaseType getDb() {
         return db;
      }

      public void setDb(DatabaseType db) {
         this.db = db;
      }
      
      /**
       * Get the database specific List of cached views, tables and synonyms
       * @return The List of the <code>String</code> names
       * @throws java.sql.SQLException a problem retrieving this
       */
      public List getViewList() throws SQLException {

         if (viewList == null) {
            ArcoDbConnection conn = this.getConnection();
            try {
               ResultSet rs = conn.getViewList();

               ArrayList tmpViewList = new ArrayList();
               while (rs.next()) {
                  tmpViewList.add(rs.getString(3).toLowerCase());
               }
               viewList = tmpViewList;

            } finally {
               releaseConnection(conn);
            }
         }
         return viewList;
      }      
      
      //used by junit tests
      void setFieldListMap(Map fieldMap) {
         tableFieldListMap = fieldMap;
      }
      /**
       * The database specific list of cached filaeld for all tables, views and synonyms
       * @param table view or synonym for particulat list
       * @return the List of the <code>String</code> fields names
       * @throws java.sql.SQLException a problem to get this list
       */
      public Map getFieldList(String table) throws SQLException {

         Map ret = (Map) tableFieldListMap.get(table);
         if (ret == null) {
            ArcoDbConnection conn = getConnection();
            try {
               ResultSet rs = conn.getAttributes(table);
               ret = new HashMap();

               while (rs.next()) {
                  //5 position in rs returns DataType int java.sql.Types
                  ret.put(rs.getString(4).toLowerCase(), new Integer(rs.getInt(5)));
               }
               synchronized (tableFieldListMap) {
                  tableFieldListMap.put(table, ret);
               }
            } finally {
               releaseConnection(conn);
            }

         }
         return ret;
      }
      
      
      public void finalize() {
         connections.clear();
         freeConnections.clear();
         tableFieldListMap.clear();
         viewList.clear();
      }
   } // end of ClusterConnectionPool
   
   private static ArcoDbConnectionPool instance = null;

   private Configuration config = null;

   List pools = new ArrayList();
   
   /**
    * Creates new ArcoDbConnectionPool
    */
   private ArcoDbConnectionPool() {
   }

   public Configuration getConfig() {
      return config;
   }

   public void setConfigurationFile(String configFile) {
      setConfigurationFile(new File(configFile));
   }

   public void setConfigurationFile(File configFile) {
      try {
         JAXBContext jc = JAXBContext.newInstance("com.sun.grid.arco.model");

         Unmarshaller um = jc.createUnmarshaller();

         config = (Configuration) um.unmarshal(configFile);

      } catch (JAXBException jaxbe) {
         IllegalStateException ilse = new IllegalStateException("Can't read configuration file " + configFile);
         ilse.initCause(jaxbe);
         throw ilse;
      }
   }
   private com.sun.grid.arco.sql.SQLGenerator generator;

   /**
    * @throws IllegalStateException if the dbtype is unknown
    * @return the sql generator
    */
   public com.sun.grid.arco.sql.SQLGenerator getSQLGenerator() {
      if (generator == null) {
         List databaseList = config.getDatabase();
         DatabaseType currentDatabase = ((DatabaseType) databaseList.get(0));
         String type = currentDatabase.getDriver().getType();
         generator = getSQLGenerator(type);
      }
      return generator;
   }

   public static com.sun.grid.arco.sql.SQLGenerator getSQLGenerator(String type) {
      if (type.equals(DB_TYPE_POSTGRES)) {
         return new com.sun.grid.arco.sql.PostgresSQLGenerator();
      } else if (type.equals(DB_TYPE_ORACLE)) {
         return new com.sun.grid.arco.sql.OracleSQLGenerator();
      } else if (type.equals(DB_TYPE_MYSQL)) {
         return new com.sun.grid.arco.sql.MysqlSQLGenerator();
      } else {
         throw new IllegalStateException("No Generator found for database type " + type +
            " found");
      }
   }

   public void init() throws java.sql.SQLException {
      if (pools.size() == 0) {
         synchronized (pools) {
            for (Iterator it = config.getDatabase().iterator(); it.hasNext();) {
               DatabaseType db = (DatabaseType) it.next();
               ClusterConnectionPool pool = new ClusterConnectionPool();
               pool.init(db);
               pools.add(pool);
            }
         }
      }   
   } 
   
   public void finalize(){
      pools.clear();
   }
   
} // end of class ArcoDbConnectionPool

