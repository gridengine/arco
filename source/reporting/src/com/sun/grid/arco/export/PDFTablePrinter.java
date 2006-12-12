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

import java.io.PrintWriter;

public class PDFTablePrinter implements TablePrinter {
   
   
   public void printCell(java.io.PrintWriter pw, Object [] content) {
      pw.print("<fo:table-cell border='0.5pt solid' padding='4pt' font-size='8pt'");
      pw.println( "> <fo:block>");
      if( content != null ) {
         for(int i = 0; i < content.length; i++ ) {
            if( i > 0 ) {
               pw.print(' ');
            }
            pw.print( content[i] );
         }
      } else {
         pw.print(ArcoConstants.NULL_VALUE);
      }
      pw.println( "</fo:block></fo:table-cell>");
   }
   
   public void printHeaderCell(java.io.PrintWriter pw, int colSpan, int rowSpan, Object content) {
      pw.print("<fo:table-cell border='0.5pt solid' padding='4pt' font-size='8pt' font-weight='bold'");
      pw.print(" number-columns-spanned='");
      pw.print( colSpan );
      pw.print("' number-rows-spanned='");
      pw.print( rowSpan );
      pw.println( "'> <fo:block>");
      if( content != null ) {
         String str = content.toString();
//         if( str.length() > 20 ) {
//            str = str.substring( 0,20 );
//         }
         pw.print( str );
      }
      pw.println( "</fo:block></fo:table-cell>");
   }
   
   public void printTableHeaderStart(PrintWriter pw) {
      pw.println("<fo:table-header>");
   }
   
   public void printTableHeaderEnd(PrintWriter pw) {
      pw.println("</fo:table-header>");
   }
   
   
   public void printRowEnd(java.io.PrintWriter pw) {
      pw.println( "</fo:table-row>" );
   }
   
   public void printRowStart(java.io.PrintWriter pw) {
      pw.println( "<fo:table-row>" );
   }
   
   public void printTableEnd(java.io.PrintWriter pw) {
      pw.println( "</fo:table>" );
   }
   
   public void printTableStart(PrintWriter pw, int colCount) {
      pw.println( "<fo:table table-layout='fixed'>" );
      
      double width = 18.0 / colCount;
      for( int i = 0; i < colCount; i++ ) {         
         pw.print( "<fo:table-column column-width='");
         pw.print( width );
         pw.println("cm'/>");
      }
   }

   public void printTableBodyStart(PrintWriter pw) {
      pw.println("<fo:table-body>");
   }
   
   public void printTableBodyEnd(PrintWriter pw) {
      pw.println("</fo:table-body>");
   }
   
}
