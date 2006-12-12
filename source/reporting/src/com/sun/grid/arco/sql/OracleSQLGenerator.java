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

import com.sun.grid.arco.model.QueryType;


public class OracleSQLGenerator extends AbstractSQLGenerator {
   

   public String generateRowLimit(QueryType query) {
      return "ROWNUM <= " + query.getLimit();
   }
   
  public javax.sql.ConnectionPoolDataSource createDatasource(com.sun.grid.arco.model.DatabaseType database)
  throws java.sql.SQLException {
     
     oracle.jdbc.pool.OracleConnectionPoolDataSource oracleDS =
     new oracle.jdbc.pool.OracleConnectionPoolDataSource();
     
     String pw = com.sun.grid.arco.util.CryptoHandler.decrypt( database.getUser().getPasswd() );
     
     oracleDS.setDatabaseName(  database.getName() );
     oracleDS.setDriverType( "thin" );
     oracleDS.setPassword( pw );
     oracleDS.setPortNumber( database.getPort() );
     oracleDS.setServerName( database.getHost() );
     oracleDS.setUser( database.getUser().getName() );
     oracleDS.setNetworkProtocol( "tcp" );
     
     return oracleDS;
  }

   protected String getSubSelectAlias() {
      return "";
   }
   
   
}
