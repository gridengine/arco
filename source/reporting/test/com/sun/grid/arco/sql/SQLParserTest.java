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

import junit.framework.*;
import java.util.*;

public class SQLParserTest extends TestCase {
   
   /** Creates a new instance of SQLParserTest */
   public SQLParserTest(String testName) {
      super(testName);
   }
   
   public void testDistinct() throws Exception {
      
      String sql = "select distinct h_hostname from sge_host";
      
      SQLParser p = new SQLParser( sql.toString() );
      
      p.parse();
      
      List fieldList = p.getFieldList();
      assertEquals("invalid number of fields", 1, fieldList.size() );
      
      SQLParser.Field field = (SQLParser.Field)p.getFieldList().get(0);

      
      assertEquals("invalid field name", "h_hostname", field.getExpr() );
      
   }
   

   public void testFields() throws Exception {
      
      String fields [][] = {
         { "(SELECT x , trunc( (xxx, yy ) from x))", "\"a b\"" },
         { "b"                                    , "b"   },
         { "c"                                    , null  }
      };
      
      
      StringBuffer sql = new StringBuffer();
      
      sql.append( "SELECT ");
      
      for( int i = 0; i < fields.length; i++ ) {
         if( i > 0 ) {
            sql.append( ", ");
         }
         sql.append( fields[i][0]);
         if( fields[i][1] != null ) {
            sql.append(" as ");
            sql.append( fields[i][1]);
         }
      }
      
      sql.append(" FROM yyyy");
      
//      System.out.println("sql is ----------------");
//      System.out.println( sql );
      
      SQLParser p = new SQLParser( sql.toString() );
      
      p.parse();
      
      List fieldList = p.getFieldList();
      assertEquals("invalid number of fields",fieldList.size(), fields.length );
      
      SQLParser.Field field = null;
      String str = null;
      for( int i = 0; i < fieldList.size(); i++ ) {
         field = (SQLParser.Field)fieldList.get(i);
//         System.out.print("\"");
//         System.out.print( field.getExpr());
//         System.out.print("\" -> \"" );
//         System.out.print( field.getName());
//         System.out.println("\"");
         
         assertEquals( "invalid field expr", fields[i][0].replaceAll(" ", ""), field.getExpr().replaceAll(" ", "")) ;
         str =  fields[i][1];
         if ( str != null ) {
            str = str.replaceAll("\"","");
         }
         assertEquals( "invalid field name", str, field.getName() );
      }
      
   }
   
   
   public void testLastBinding() throws Exception {
      
      StringBuffer sql = new StringBuffer();
      
      String lbs [] [] = {
         { "param1", "xd3e, sss, seee" }
      };
      sql.append( "SELECT x, y, z FROM blubber WHERE ");
      
      for( int i = 0; i < lbs.length; i++ ) {
         if( i > 0 ) {
            sql.append( " AND ");
         }
         sql.append( "LATEBINDING{");
         sql.append( lbs[i][0] );
         sql.append( ";;");
         sql.append( lbs[i][1] );
         sql.append( "}");
      }
      
      SQLParser parser = new SQLParser(sql.toString());
      
      parser.parse();
      
      List lbList = parser.getLateBindingList();
      
      assertEquals(lbs.length, lbList.size());
      
   }
   
   
}
