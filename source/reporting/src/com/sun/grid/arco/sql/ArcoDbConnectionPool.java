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

import com.sun.grid.arco.model.Configuration;
import com.sun.grid.logging.SGELog;

import java.io.File;
import java.util.*;
import java.sql.*;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import com.sun.grid.arco.ArcoConstants;

/**
 * <p><code>ArcoDbConnectionPool</code>
 * </p>
 *
 */
public class ArcoDbConnectionPool implements ArcoConstants {
   
  private ArrayList connections = new ArrayList();
  private Stack freeConnections = new Stack();
  
  
  private javax.sql.ConnectionPoolDataSource datasource;

  private static ArcoDbConnectionPool instance = null;
  
//  private ConfigurationParser parser = null;
  
  private Configuration config = null;
    
  /**
   * Creates new ArcoDbConnectionPool
   */
  private ArcoDbConnectionPool()  {
  }
  
  public Configuration getConfig()
  {
     return config;
  }
  
  public void setConfigurationFile(String configFile) {
     setConfigurationFile( new File( configFile ) );
  }
  
  public void setConfigurationFile( File configFile ) {
     try {
      JAXBContext jc = JAXBContext.newInstance( "com.sun.grid.arco.model" );
      
      Unmarshaller um = jc.createUnmarshaller();
      
      config = (Configuration)um.unmarshal( configFile );
      
     } catch( JAXBException jaxbe ) {
        IllegalStateException ilse = new IllegalStateException("Can't read configuration file " + configFile );
        ilse.initCause( jaxbe );
        throw ilse;
     }
  }
  
  private com.sun.grid.arco.sql.SQLGenerator generator;
  
  /**
   * @throws IllegalStateException if the dbtype is unknown
   * @return the sql generator
   */  
  public com.sun.grid.arco.sql.SQLGenerator getSQLGenerator()  {
     if( generator == null ) {
        String type = config.getDatabase().getDriver().getType();
        generator = getSQLGenerator( type );
     }
     return generator;
  }
  
  public static com.sun.grid.arco.sql.SQLGenerator getSQLGenerator( String type ) {
     if ( type.equals( DB_TYPE_POSTGRES ) ) {
        return new com.sun.grid.arco.sql.PostgresSQLGenerator();
     } else if ( type.equals( DB_TYPE_ORACLE) ) {
        return new com.sun.grid.arco.sql.OracleSQLGenerator();
     } else if ( type.equals( DB_TYPE_MYSQL ) ) {
         return new com.sun.grid.arco.sql.MysqlSQLGenerator();        
     } else {        
         throw new IllegalStateException("No Generator found for database type " + type +
                                        " found");
     }     
  }
  

  public void init() throws java.sql.SQLException {
      if( datasource == null ) {
         synchronized( connections ) {
            if( datasource == null ) {
               datasource = getSQLGenerator().createDatasource( config.getDatabase() );
               
               // Initialize the free connections
               
               ArcoDbConnection conn = null;
              int max = config.getDatabase().getUser().getMaxConnections();
              for( int i = 0; i < max; i++ ) {
                 conn = new ArcoDbConnection( i, 
                                              config.getDatabase().getDriver().getType(),
                                              config.getDatabase().getSchema(), 
                                              datasource );
                 freeConnections.push( conn );
              }
              connections.notify();
            }
         }
      }
  }
  
  
  
  /**
   * @return
   */  
  public static ArcoDbConnectionPool getInstance() {
    if (instance == null) {
      synchronized( ArcoDbConnectionPool.class ) {
         if( instance == null ) {
            instance = new ArcoDbConnectionPool();
         }
      }
    }
    return instance;
  }
  
   public ArcoDbConnection getConnection() throws java.sql.SQLException {

      try {
         
         init();
         
         ArcoDbConnection ret = null;
         
          synchronized( connections ) {

             while( freeConnections.isEmpty() ) {
                SGELog.warning( "No db connection free, wait" );
                connections.wait();
             }
             
             ret = (ArcoDbConnection)freeConnections.pop();
             connections.add( ret );
          }  
          SGELog.fine( "Got connection {0}", ret.getId() );
          return ret;
      } catch( InterruptedException ire ) {
         return null;
      }
   }
   
  public void releaseConnection( ArcoDbConnection connection) {     
     synchronized( connections ) {
        connection.closeConnection();
        connections.remove( connection );
        SGELog.fine( "Connection {0} released", connection.getId() );
        freeConnections.push( connection );
        connections.notify();
     }     
  }   
  
  public void releaseConnections() {
     synchronized( connections ) {
        ArcoDbConnection conn = null;
        while( !connections.isEmpty() ) {
             conn = (ArcoDbConnection)connections.get( 0 );
             releaseConnection( conn );
        }
     }
  }  
  
  
  private ArrayList viewList;
  
  public List getViewList() throws SQLException {
     
      if( viewList == null ) {
         ArcoDbConnection conn = this.getConnection();
         try {
            ResultSet rs = conn.getViewList();
         
            ArrayList tmpViewList = new ArrayList();
            while( rs.next() ) {
               tmpViewList.add( rs.getString( 3 ).toLowerCase() );
            }
            viewList = tmpViewList;
            
         } finally {
            releaseConnection( conn );
         }
      }
      return viewList;     
  }
  
  //used by junit tests
  void setFieldListMap(Map fieldMap) {
      tableFieldListMap = fieldMap;
  }
   
  private Map tableFieldListMap = new HashMap();
  
  public Map getFieldList(String table) throws SQLException {
     
     Map ret = (Map)tableFieldListMap.get( table );     
     if( ret == null ) {
        
        ArcoDbConnection conn = getConnection();
        try {
           ResultSet rs = conn.getAttributes( table );        
           ret = new HashMap();
           
           while (rs.next()) {
              //5 position in rs returns DataType int java.sql.Types
              ret.put(rs.getString(4).toLowerCase(), new Integer(rs.getInt(5)));
           }
           synchronized( tableFieldListMap ) {
              tableFieldListMap.put( table, ret );
           }           
        } finally {
           releaseConnection( conn );
        }
        
     }
     return ret;
  }
  
  
} // end of class ArcoDbConnectionPool
