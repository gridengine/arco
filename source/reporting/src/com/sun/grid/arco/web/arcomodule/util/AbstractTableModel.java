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
package com.sun.grid.arco.web.arcomodule.util;

import com.sun.web.ui.model.CCActionTableModel;
import java.util.*;

public abstract class AbstractTableModel extends CCActionTableModel {
   
   private Map columnMap;
   private String name;
   
   /** Creates a new instance of AbstractTableModel */
   protected AbstractTableModel(String name, java.io.InputStream in, Map columnMap) {
      super(in);
      this.columnMap = columnMap;
      this.name = name;
      initHeaders();
   }
   
   protected void initHeaders() {
      
      Iterator iter = columnMap.values().iterator();
      AbstractTableColumn col = null;
      while(iter.hasNext()) {
         col = (AbstractTableColumn)iter.next();
         setActionValue( col.getColumnName(), col.getTitle() );         
      }
   } 
   

   protected AbstractTableColumn getColumn(String name ) {
      return (AbstractTableColumn)columnMap.get(name);
   }
   
   public String getName() {
      return this.name;
   }


}
