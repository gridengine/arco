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
package com.sun.grid.arco.validator.view;

import com.sun.grid.arco.validator.QueryStateHandler;
import com.sun.grid.arco.validator.AbstractValidator;
import com.sun.grid.arco.model.QueryType;
import com.sun.grid.arco.model.ViewConfiguration;
import com.sun.grid.arco.model.Table;
import com.sun.grid.arco.model.FormattedValue;
import com.sun.grid.arco.model.Field;

import java.util.*;

public class TableViewValidator extends AbstractValidator {
   

   public void validate(QueryType query, QueryStateHandler handler) {
      
      if( query.isSetView() ) {
         ViewConfiguration view = query.getView();
         if( view.isSetTable() ) {
            Table table = view.getTable();
            if( table.isVisible() ) {
               
               List columnList = table.getColumnWithFormat();
               if( columnList.isEmpty() ) {
                  handler.addError("view/table", "query.view.table.columnRequired", null );
               } else {
                  Iterator iter = columnList.iterator();
                  FormattedValue fv = null;

                  query.getField();

                  while( iter.hasNext() ) {
                     fv = (FormattedValue)iter.next();
                     if( !existsField(query,fv.getName()) ) {
                        handler.addError("view/table", "query.view.table.unknownField", new Object[] { fv.getName() });
                        break;
                     }
                  }               
               }
            }
         }
      }
   }
   
}
