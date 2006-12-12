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

import com.iplanet.jato.model.Model;

public class AbstractTableColumn {
   
   /** prefix for the column name. */
   public static final String COLUMN_PREFIX = "Col";
   
   /** prefix for the value name. */
   public static final String VALUE_PREFIX = "Value";
   
   /** the name of the column. */
   private String name;
   
   /** title of the column. */
   private String title;
   
   
   /** Creates a new instance of AbstractTableColumn */
   public AbstractTableColumn(String name, String title) {
      this.name = name;
      this.title = title;
   }
   
   /**
    *  Get the column name for the action table
    *  @return the column name for the action table
    */
   public String getColumnName() {
      return COLUMN_PREFIX + name;
   }
   
   /**
    *  Get the value name for the action table
    *  @return the value name for the action table
    */
   public String getValueName() {
      return VALUE_PREFIX + name;
   }
   
   /**
    * get the real name of this column
    * @return  the real name
    */
   public String getName() {
      return name;
   }
   
   /**
    * get the title of this column
    * @return the title of this column
    */
   public String getTitle() {
      return title;
   }
}
