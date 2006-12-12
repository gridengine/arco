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
package com.sun.grid.arco.export;

import com.sun.grid.arco.ResultExport;
import com.sun.grid.arco.ExportContext;
import com.sun.grid.arco.QueryResult;
import java.io.PrintWriter;
import java.util.*;

public class CSVResultExport extends ResultExport {
   public static final String MIME_TYPE = "text/csv";
   /** Creates a new instance of CSVResultExport */
   public CSVResultExport() {
      super(MIME_TYPE);
   }

   public void export(com.sun.grid.arco.ExportContext ctx) 
       throws java.io.IOException, com.sun.grid.arco.QueryResultException {
      
      QueryResult result = ctx.getQueryResult();
      
      PrintWriter pw = new PrintWriter(ctx.getOutputStream());
      
      Iterator iter = result.getColumns().iterator();
      String colName = null;
      boolean first = true;      
      while(iter.hasNext()) {
         colName = (String)iter.next();
         if( first ) {
            first = false;
         } else {
            pw.print(",");
         }
         if( colName != null && colName.indexOf(',') >= 0 ) {
            pw.print('"');
            pw.print( colName );
            pw.print('"');
         } else {
            pw.print( colName );
         }         
      }
      pw.println();
      
      int rowCount = result.getRowCount();
      int colCount = result.getColumns().size();
      int row = 0;
      int col = 0;
      Object obj = null;
      String str = null;
      
      for( row = 0; row < rowCount; row++ ) {
         for( col = 0; col < colCount; col++ ) {
            if( col > 0 ) {
               pw.print(',');
            }
            obj = result.getValue(row,col);
            if( obj == null ) {
               str = "";
            } else {
               str = obj.toString();
            }
            if( str.indexOf(',') >= 0 ) {
               pw.print('"');
               pw.print(str);
               pw.print('"');
            } else {
               pw.print(str);
            }
         }
         pw.println();
      }
      pw.flush();
   }
   
}
