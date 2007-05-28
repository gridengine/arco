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
import java.util.Iterator;
import java.util.logging.Level;

public class TestInstall extends AbstractDBWriterTestCase {
   
   public TestInstall(String name) {
      super(name);
   }
   
   public void setUp() throws Exception {
      super.setUp();
   }
   
   public void testInstall() throws Exception {
      Iterator iter = getDBList().iterator();
      String debugLevel = DBWriterTestConfig.getTestDebugLevel();
      if( debugLevel == null ) {
         debugLevel = Level.INFO.toString();
      }
      
      if( debugLevel == null ) {
         debugLevel = Level.INFO.toString();
      }
      while(iter.hasNext()) {
         TestDB db = (TestDB)iter.next();
         String orgDebugLevel = db.getDebugLevel();
         db.setDebugLevel(debugLevel);
         try {
//            db.dropDB();
            int result = db.installDB();
            assertEquals("Installation on db " + db.getJDBCUrl() + " failed", 0, result);
         } finally {
            db.setDebugLevel(orgDebugLevel);
         }
      }
   }
   
}
