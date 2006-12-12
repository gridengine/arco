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

import junit.framework.*;
import java.io.*;
import java.util.*;
import java.sql.*;

import java.util.logging.*;
import com.sun.grid.reporting.dbwriter.model.*;

/**
 * This test case tests the reload function of the dbwriter
 * derived value rules and deletion rules
 *
 */
public class TestConfigReload extends AbstractDBWriterTestCase {
   
   public TestConfigReload(String name) {
      super(name);
   }
   
   public static Test suite() {
      TestSuite suite = new TestSuite(TestConfigReload.class);
      return suite;
   }
   
   public void setUp() throws Exception {
      super.setUp();
   }

   public void testConfigurationReaload() throws Exception {
      Iterator iter = getDBList().iterator();

      String testDebugLevel = DBWriterTestConfig.getTestDebugLevel();
      if( testDebugLevel == null ) {
         testDebugLevel = Level.INFO.toString();
      }
      while(iter.hasNext()) {
         TestDB db = (TestDB)iter.next();
         String orgDebugLevel = db.getDebugLevel();
         try {
            doConfigurationReload(db, testDebugLevel);
         } finally {
            db.setDebugLevel(orgDebugLevel);
         }
      }      
   }
   
   private void doConfigurationReload(TestDB db, String debugLevel) throws Exception {

      
      ReportingDBWriter dbw = createDBWriter(debugLevel,db);

      
      File calcFile = File.createTempFile( "testDelayedImport", ".xml", null );
      calcFile.deleteOnExit();
      

      ObjectFactory faq = new ObjectFactory();
      DbWriterConfig dbConfig = faq.createDbWriterConfig();
      
      FileWriter fw = new FileWriter(calcFile);
      
      
      faq.createMarshaller().marshal(dbConfig,fw);
      fw.flush();
      fw.close();
      
      dbw.setCalculationFile( calcFile.getAbsolutePath() );
      
      TestFileWriter writer = new TestFileWriter();
      
      dbw.setReportingFile( writer.getReportingFile().getAbsolutePath() );
      
      // start the dbwriter      
      dbw.initialize();      
      dbw.start();
      
      try {
         Thread.sleep(1000);

         assertEquals( "Error on dbwriter startup, dbwriter thread is not alive", dbw.isAlive(), true );

         dbConfig = dbw.getDbWriterConfig();

         assertEquals( "derived value rule list is not empty", dbConfig.getDerive().isEmpty(), true );

         DeriveRuleType derivedRule = faq.createDeriveRuleType();
         derivedRule.setVariable("blubber");      

         dbConfig.getDerive().add(derivedRule);

         fw = new FileWriter(calcFile);
         faq.createMarshaller().marshal(dbConfig,fw);
         fw.flush();
         fw.close();      

         dbConfig = dbw.getDbWriterConfig();

         assertEquals( "derived value rule list is empty", dbConfig.getDerive().isEmpty(), false );

         // Write nonsense into the calculation file
         fw = new FileWriter(calcFile);
         fw.write("blubber");

         fw.flush();
         fw.close();

         dbConfig = dbw.getDbWriterConfig();

         assertEquals( "derived value rule list is empty", dbConfig.getDerive().isEmpty(), false );

      } finally {
         // Shutdown the dbwriter
         shutdownDBWriter(dbw);
      }
    }

    
}
