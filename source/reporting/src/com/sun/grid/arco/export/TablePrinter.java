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

import java.io.PrintWriter;

public interface TablePrinter {
   
   public void printTableStart( PrintWriter pw, int colCount );
   public void printTableEnd( PrintWriter pw );
   public void printTableHeaderStart( PrintWriter pw );
   public void printTableHeaderEnd( PrintWriter pw );
   public void printTableBodyStart( PrintWriter pw );
   public void printTableBodyEnd( PrintWriter pw );
   public void printRowStart( PrintWriter pw );
   public void printRowEnd( PrintWriter pw );
   public void printHeaderCell( PrintWriter pw, int colSpan, int rowSpan, Object content );
   public void printCell( PrintWriter pw, Object[] content );
   
}
