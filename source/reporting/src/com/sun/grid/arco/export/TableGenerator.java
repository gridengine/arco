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

import com.sun.grid.arco.ArcoConstants;
import com.sun.grid.arco.QueryResult;
import java.io.PrintWriter;
import com.sun.grid.arco.model.*;
import java.util.*;

public class TableGenerator {
   
   private QueryResult queryResult;
   private TablePrinter tablePrinter;
   
   /** Creates a new instance of TableGenerator */
   public TableGenerator(QueryResult queryResult, TablePrinter tablePrinter) {
      this.queryResult = queryResult;
      this.tablePrinter = tablePrinter;
   }
   
   public void print(PrintWriter pw, Locale locale) {
      
      QueryType query = queryResult.getQuery();
      Table table = query.getView().getTable();

      String [] columnName = null;
      int [] columnIndex = null;
      java.text.Format [] columnFormat = null;
      
      if( table != null && table.isVisible() && !table.getColumnWithFormat().isEmpty() ) {      
         List columnList = table.getColumnWithFormat();
         
         columnName = new String[columnList.size()];
         columnIndex = new int[columnList.size()];
         columnFormat = new java.text.Format[columnList.size()];
         
         Iterator columnIter = columnList.iterator();

         FormattedValue column = null;

         int i = 0;
         while( columnIter.hasNext() ) {
            column = (FormattedValue)columnIter.next();            
            columnName[i] = column.getName();
            columnIndex[i] = queryResult.getColumnIndex(column.getName());
            columnFormat[i] = com.sun.grid.arco.Util.createFormat(column, locale );
            i++;
         }
      } else {
         // no view set, use all fields
         List columnList = queryResult.getColumns();

         columnName = new String[columnList.size()];
         columnIndex = new int[columnList.size()];
         columnFormat = new java.text.Format[columnList.size()];
         
         Iterator iter = columnList.iterator();
         int i = 0;
         while(iter.hasNext()) {
            columnName[i] = (String)iter.next();
            columnIndex[i] = i;
            columnFormat[i] = queryResult.createFormater(i, null, locale);
            i++;
         }
      }
      
      tablePrinter.printTableStart(pw, columnName.length);
      tablePrinter.printTableHeaderStart(pw);
      tablePrinter.printRowStart(pw);
      
      int colCount = columnName.length;
      for( int i = 0; i < colCount; i++ ) {
         tablePrinter.printHeaderCell(pw, 0,0, columnName[i]);
      }
      tablePrinter.printRowEnd(pw);
      tablePrinter.printTableHeaderEnd(pw);
      tablePrinter.printTableBodyStart(pw);
      
      int rowCount = queryResult.getRowCount();
      int row = 0;
      int col = 0;
      Object [] content = new Object[1];
      for( row = 0; row < rowCount; row++ ) {
         tablePrinter.printRowStart(pw);
         for( col = 0; col < colCount; col++ ) {
            content[0] = queryResult.getValue(row,columnIndex[col]);
            if( content[0] == null ) {
               content[0] = ArcoConstants.NULL_VALUE;
            } else if( columnFormat[col] != null) {
               try {
                  content[0] = columnFormat[col].format(content[0]);
               } catch( IllegalArgumentException ilage ) {
                  content[0] = ArcoConstants.FORMAT_ERROR;
               }
            }
            tablePrinter.printCell(pw, content);
         }
         tablePrinter.printRowEnd(pw);
      }
      
      tablePrinter.printTableBodyEnd(pw);
      tablePrinter.printTableEnd(pw);
      
      pw.flush();
      
   }
}
