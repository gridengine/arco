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

public class HTMLTablePrinter implements TablePrinter {
    private String title;
    
    public HTMLTablePrinter(String title) {
      this.title = title;
    }
    public void printTableStart(PrintWriter pw, int colCount) {
      pw.print("<table class='" );
      pw.print( HTMLResultExport.getTableClass() );
      pw.println( "' width='100%' border='0' cellpadding='0' cellspacing='0' title=''>");
      
      pw.println("<caption class=\"TblTtlTxt\">"+title+"</caption>");
    }

    public void printTableEnd(PrintWriter pw) {
       pw.print( "</table>");
    }    
    
    public void printRowStart(PrintWriter pw) {
       pw.println("<tr>");

    }

    public void printRowEnd(PrintWriter pw) {
       pw.println( "</tr>" );
    }
    
    public void printHeaderCell(PrintWriter pw, int colSpan, int rowSpan, Object content) {
       
       pw.print( "<th colspan='" );
       pw.print( colSpan );
       pw.print( "' rowspan='" );
       pw.print( rowSpan );
       pw.print( "' class='");
       pw.print( HTMLResultExport.getTableHeaderClass() );
       pw.print( "' align='left'");
       pw.print( "  scope='col");
       pw.print( "'>" );
       pw.print( "<span class='");
       pw.print( HTMLResultExport.getTableHeaderTextClass() );
       pw.print( "'>");
       if( content != null ) {
          pw.print( content );
       }
       pw.println("</span></th>");
    }
    
    public void printCell(PrintWriter pw, Object[] content) {
			//the cell object from the sql table
         pw.print( "<td align='right'>" );
         if( content == null ) {
            pw.print( ArcoConstants.NULL_VALUE );
         } else {
            for(int i = 0; i < content.length; i++) {
               if( i > 0 ) {
                  pw.print(' ');
               }
               pw.print( content[i] );
            }            
         }
         pw.print( "</td>" );
    }    
    
    public void printTableHeaderStart(PrintWriter pw) {
    }
    
    public void printTableHeaderEnd(PrintWriter pw) {
    }
    
    public void printTableBodyEnd(PrintWriter pw) {
    }
    public void printTableBodyStart(PrintWriter pw) {
    }
    
}
