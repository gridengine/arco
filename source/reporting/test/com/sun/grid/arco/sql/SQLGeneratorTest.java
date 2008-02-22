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

import java.util.logging.*;
import junit.framework.*;
import com.sun.grid.arco.ArcoConstants;
import com.sun.grid.arco.model.*;
import com.sun.grid.logging.SGELog;
import com.sun.grid.arco.util.FieldFunction;

public class SQLGeneratorTest extends TestCase {
   
   private ObjectFactory faq;
   private SQLGenerator  gen;
   
   public SQLGeneratorTest(String testName) {
      super(testName);
   }

   protected void setUp() throws java.lang.Exception {
      faq = new ObjectFactory();
      gen = new OracleSQLGenerator();
      SGELog.init( Logger.global );
   }

   protected void tearDown() throws java.lang.Exception {
   }
   
   public void testSimple() throws Exception {      
//      Query query = faq.createQuery();
//      Field field = faq.createField();
//      
//      query.setTableName("table");
//      query.setType(ArcoConstants.SIMPLE);
//      field.setDbName("col");
//      field.setFunction( FieldFunction.VALUE.getName() );
//      field.setReportName("a col");
//      query.getField().add(field);
//      
//      String sql = gen.generate(query, null);
//      SGELog.fine("generated sql: ''{0}''", sql);
//      assertEquals(sql, "SELECT col AS \"a col\" FROM table");
//   }
//
//   public void testNoReportName() throws Exception {      
//      Query query = faq.createQuery();
//      Field field = faq.createField();
//      
//      query.setTableName("table");
//      query.setType(ArcoConstants.SIMPLE);
//      field.setDbName("col");
//      field.setFunction( FieldFunction.VALUE.getName() );
//      query.getField().add(field);
//      
//      String sql = gen.generate(query, null);
//      SGELog.fine("generated sql: ''{0}''", sql);
//      assertEquals(sql, "SELECT col AS \"col\" FROM table");
   }
//   
//   public void testRowLimit() throws Exception {
//      Query query = faq.createQuery();
//      Field field = faq.createField();
//      
//      query.setTableName("table");
//      query.setType(ArcoConstants.SIMPLE);
//      query.setLimit(10);
//      
//      field.setDbName("col");
//      field.setFunction( FieldFunction.VALUE.getName() );
//      field.setReportName("a col");
//      query.getField().add(field);
//      
//      String sql = gen.generate(query,null);
//      SGELog.fine("generated sql: ''{0}''", sql);
//      assertEquals(sql, "SELECT col AS \"a col\" FROM table WHERE ROWNUM <= 10");
//      
//   }
//   
//   public void testMax() throws Exception {
//      Query query = faq.createQuery();
//      Field field = faq.createField();
//      
//      query.setTableName("table");
//      query.setType(ArcoConstants.SIMPLE);
//      query.setLimit(10);
//      
//      field.setDbName("col");
//      field.setFunction( FieldFunction.MAX.getName() );
//      field.setReportName("a col");
//      query.getField().add(field);
//      
//      field = faq.createField();
//      field.setDbName("col1");
//      field.setFunction( FieldFunction.VALUE.getName());
//      field.setReportName("a col1");
//      query.getField().add(field);
//      
//      String sql = gen.generate(query,null);
//      SGELog.fine("generated sql: ''{0}''", sql);
//      assertEquals(sql, "SELECT MAX(col) AS \"a col\", col1 AS \"a col1\" FROM table WHERE ROWNUM <= 10 GROUP BY col1");      
//   }
//   
//   public void testAddition() throws Exception {
//      Query query = faq.createQuery();
//      Field field = faq.createField();
//      
//      query.setTableName("table");
//      query.setType(ArcoConstants.SIMPLE);
//      query.setLimit(10);
//      
//      field.setDbName("col");
//      field.setFunction( FieldFunction.MAX.getName() );
//      field.setReportName("a col");
//      query.getField().add(field);
//      
//      field = faq.createField();
//      field.setDbName("col1");
//      field.setFunction( FieldFunction.ADDITION.getName());
//      field.setParameter( "1" );
//      field.setReportName("a col1");
//      query.getField().add(field);
//      
//      String sql = gen.generate(query,null);
//      SGELog.fine("generated sql: ''{0}''", sql);
//      assertEquals(sql, "SELECT MAX(col) AS \"a col\", (col1+1) AS \"a col1\" FROM table WHERE ROWNUM <= 10 GROUP BY (col1+1)");      
//   }
      

}
