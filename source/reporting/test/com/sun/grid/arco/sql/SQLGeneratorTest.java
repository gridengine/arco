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

import com.sun.grid.arco.ArcoConstants;
import java.util.logging.*;
import junit.framework.*;
import com.sun.grid.arco.model.*;
import com.sun.grid.arco.util.FieldFunction;
import com.sun.grid.logging.SGELog;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This test tests the generation of a SimpleQuery with different parameters specified,
 * row limit, sort, agreggate function etc.
 * It tests bugs: CR 6661500 - In SJWC on Oracle dates appear truncated to just MM/DD/YYYY
 *                CR 6640762 - Row Limit in Simple Query uses wrong syntax
 *                CR 6661470 - Sort order na row limit cannot be specified together ...
 * 
 */
public class SQLGeneratorTest extends TestCase {
   
   private ObjectFactory faq;
   private List<SQLGenerator> gen;
   private Map fieldMap;
   
   public SQLGeneratorTest(String testName) {
      super(testName);
   }

   /**
    * Only table name and fields configured in the fieldMap can be used in any of the tests
    */
   protected void setUpFieldMap() {
      fieldMap = new HashMap();
      Map listWithTypes = new HashMap();
      
      listWithTypes.put("j_job_number", new Integer (java.sql.Types.INTEGER));
      listWithTypes.put("j_submission_time", new Integer (java.sql.Types.DATE));
      listWithTypes.put("j_owner", new Integer (java.sql.Types.VARCHAR));
      fieldMap.put("sge_job", listWithTypes);   
      ArcoDbConnectionPool pool = ArcoDbConnectionPool.getInstance();
      pool.setFieldListMap(fieldMap);
   }
   
   protected void setUp() throws java.lang.Exception {
      faq = new ObjectFactory();     
      gen = new LinkedList<SQLGenerator>();
      gen.add(new PostgresSQLGenerator());
      gen.add(new OracleSQLGenerator());
      gen.add(new MysqlSQLGenerator());
      setUpFieldMap();
      SGELog.init( Logger.global );
   }

   protected void tearDown() throws java.lang.Exception {
   }
  
   public void testSimple() throws Exception {
      for (SQLGenerator g : gen) {
         SGELog.info("Generator: ''{0}''", g.getClass().getSimpleName());
         doTestSimple(g);
      }
   }
  
  private void doTestSimple(SQLGenerator g) throws Exception {  
      Query query = faq.createQuery();
      Field field = faq.createField();
      
      query.setTableName("sge_job");
      query.setType(ArcoConstants.SIMPLE);
      
      field.setDbName("j_job_number");
      field.setFunction(FieldFunction.VALUE.getName());
      field.setReportName("job_number");
      query.getField().add(field);
      
      field = faq.createField();
      field.setDbName("j_submission_time");
      field.setFunction(FieldFunction.VALUE.getName());
      field.setReportName("time");
      query.getField().add(field);
      
      String sql = g.generate(query, null);
      SGELog.fine("generated sql: ''{0}''", sql);
      if (g instanceof OracleSQLGenerator) {
         assertEquals("SELECT j_job_number AS \"job_number\", " +
               "to_char(j_submission_time, 'YYYY-MM-DD HH24:MI:SS')" +
               " AS \"time\" FROM sge_job", sql);
      } else {
         assertEquals("SELECT j_job_number AS \"job_number\", " +
               "j_submission_time AS \"time\" FROM sge_job", sql);
      }
   }

   public void testNoReportName() throws Exception {
      for (SQLGenerator g : gen) {
         SGELog.info("Generator: ''{0}''", g.getClass().getSimpleName());
         doTestNoReportName(g);
      }
   }
   
   private void doTestNoReportName(SQLGenerator g) throws Exception {      
      Query query = faq.createQuery();
      Field field = faq.createField();
      
      query.setTableName("sge_job");
      query.setType(ArcoConstants.SIMPLE);
      
      field.setDbName("j_job_number");
      field.setFunction(FieldFunction.VALUE.getName());
      query.getField().add(field);
      
      field = faq.createField();
      field.setDbName("j_submission_time");
      field.setFunction(FieldFunction.VALUE.getName());
      query.getField().add(field);
      
      String sql = g.generate(query, null);
      SGELog.info("generated sql: ''{0}''", sql);
      
      if (g instanceof OracleSQLGenerator) {
         assertEquals("SELECT j_job_number AS \"j_job_number\", " +
               "to_char(j_submission_time, 'YYYY-MM-DD HH24:MI:SS')" +
               " AS \"j_submission_time\" FROM sge_job", sql); 
      } else {
         assertEquals("SELECT j_job_number AS \"j_job_number\", " +
               "j_submission_time AS \"j_submission_time\" FROM sge_job", sql); 
      }
   }
   
   public void testRowLimit() throws Exception {
      for (SQLGenerator g : gen) {
         SGELog.info("Generator: ''{0}''", g.getClass().getSimpleName());
         doTestRowLimit(g);
      }
   }
   
   private void doTestRowLimit(SQLGenerator g) throws Exception {
      Query query = faq.createQuery();
      Field field = faq.createField();
      
      query.setTableName("sge_job");
      query.setType(ArcoConstants.SIMPLE);
      query.setLimit(10);
      
      field.setDbName("j_job_number");
      field.setFunction(FieldFunction.VALUE.getName());
      field.setReportName("job_number");
      query.getField().add(field);
      
      String sql = g.generate(query, null);
      SGELog.info("generated sql: ''{0}''", sql);
      if (g instanceof OracleSQLGenerator) {
         assertEquals("SELECT j_job_number AS \"job_number\" FROM sge_job WHERE ROWNUM <= 10", sql);
      } else {
         assertEquals("SELECT j_job_number AS \"job_number\" FROM sge_job  LIMIT 10", sql);
      }
      
      //test the limit with filter specified
      com.sun.grid.arco.model.Filter filter = faq.createFilter();
      filter.setName("j_job_number");
      filter.setParameter("60");
      filter.setCondition("greater");
      query.getFilter().add(filter);
      
      sql = g.generate(query, null);
      SGELog.info("generated sql: ''{0}''", sql);
      if (g instanceof OracleSQLGenerator) {
         assertEquals("SELECT j_job_number AS \"job_number\" FROM sge_job " +
               "WHERE j_job_number > '60' AND ROWNUM <= 10", sql);
      } else {
         assertEquals("SELECT j_job_number AS \"job_number\" FROM sge_job " +
               "WHERE j_job_number > '60' LIMIT 10", sql);
      }
      
      //test the limit with sort order specified
      query.unsetFilter();
      field.setSort("DESC");
      sql = g.generate(query, null);
      SGELog.info("generated sql: ''{0}''", sql);
      if (g instanceof OracleSQLGenerator) {
         assertEquals("SELECT j_job_number AS \"job_number\" FROM sge_job " +
               "WHERE ROWNUM <= 10 ORDER BY j_job_number DESC", sql);  
      } else {
           //TODO: Uncomment when CR 6661470 is fixed
//         assertEquals("SELECT j_job_number AS \"job_number\" FROM sge_job " +
//               "ORDER BY j_job_number DESC  LIMIT 10", sql); 
      }
   }
   
   public void testMax() throws Exception {
      for (SQLGenerator g : gen) {
         SGELog.info("Generator: ''{0}''", g.getClass().getSimpleName());
         doTestMax(g);
      }
   }
   
   private void doTestMax(SQLGenerator g) throws Exception {
      Query query = faq.createQuery();
      Field field = faq.createField();
      
      query.setTableName("sge_job");
      query.setType(ArcoConstants.SIMPLE);
      query.setLimit(10);
      
      field.setDbName("j_job_number");
      field.setFunction(FieldFunction.MAX.getName());
      field.setReportName("job_number");
      query.getField().add(field);
      
      field = faq.createField();
      field.setDbName("j_submission_time");
      field.setFunction(FieldFunction.MAX.getName());
      field.setReportName("time");
      query.getField().add(field);
      
      String sql = g.generate(query, null);
      SGELog.info("generated sql: ''{0}''", sql);
      if (g instanceof OracleSQLGenerator) {
         assertEquals("SELECT MAX(j_job_number) AS \"job_number\", " +
               "to_char(MAX(j_submission_time), 'YYYY-MM-DD HH24:MI:SS') AS \"time\" " +
               "FROM sge_job WHERE ROWNUM <= 10", sql);
      } else {
         assertEquals("SELECT MAX(j_job_number) AS \"job_number\", " +
               "MAX(j_submission_time) AS \"time\" FROM sge_job  LIMIT 10", sql);
      }
      
      //test the creation of GROUP BY clause
      field = faq.createField();
      field.setDbName("j_owner");
      field.setFunction(FieldFunction.VALUE.getName());
      field.setReportName("owner");
      query.getField().add(field);
      
      sql = g.generate(query, null);
      SGELog.info("generated sql: ''{0}''", sql);
      if (g instanceof OracleSQLGenerator) {
         assertEquals("SELECT MAX(j_job_number) AS \"job_number\", " +
               "to_char(MAX(j_submission_time), 'YYYY-MM-DD HH24:MI:SS') AS \"time\", j_owner AS \"owner\" " +
               "FROM sge_job WHERE ROWNUM <= 10 GROUP BY j_owner", sql);
      } else {
           //TODO: Uncomment when CR 6661470 is fixed
//         assertEquals("SELECT MAX(j_job_number) AS \"job_number\", " +
//               "MAX(j_submission_time) AS \"time\", j_owner AS \"owner\" FROM sge_job GROUP BY j_owner  LIMIT 10", sql);
      }         
   }
   
   public void testAddition() throws Exception {
      for (SQLGenerator g : gen) {
         SGELog.info("Generator: ''{0}''", g.getClass().getSimpleName());
         doTestAddition(g);
      }
   } 
   
   private void doTestAddition(SQLGenerator g) throws Exception {
      Query query = faq.createQuery();
      Field field = faq.createField();
      
      query.setTableName("sge_job");
      query.setType(ArcoConstants.SIMPLE);
      query.setLimit(10);
      
      field.setDbName("j_job_number");
      field.setFunction( FieldFunction.MAX.getName() );
      field.setReportName("job_number");
      query.getField().add(field);
      
      field = faq.createField();
      field.setDbName("j_submission_time");
      field.setFunction( FieldFunction.ADDITION.getName());
      field.setParameter( "1" );
      field.setReportName("time");
      query.getField().add(field);
      
      String sql = g.generate(query, null);
      SGELog.info("generated sql: ''{0}''", sql);
      if (g instanceof OracleSQLGenerator) {
         assertEquals(sql, "SELECT MAX(j_job_number) AS \"job_number\", " +
               "to_char((j_submission_time+1), 'YYYY-MM-DD HH24:MI:SS') AS \"time\" FROM sge_job " +
               "WHERE ROWNUM <= 10 GROUP BY to_char((j_submission_time+1), 'YYYY-MM-DD HH24:MI:SS')");
      } else {
         //TODO: Uncomment when CR 6661470 is fixed
//         assertEquals(sql, "SELECT MAX(j_job_number) AS \"job_number\", " +
//               "(j_submission_time+1) AS \"time\" FROM sge_job " +
//               "GROUP BY (j_submission_time+1) LIMIT 10");
      }
   }
      

}
