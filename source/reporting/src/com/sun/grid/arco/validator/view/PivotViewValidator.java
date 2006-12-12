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

import com.sun.grid.arco.ArcoConstants;
import com.sun.grid.arco.validator.QueryStateHandler;
import com.sun.grid.arco.validator.AbstractValidator;
import com.sun.grid.arco.model.QueryType;
import com.sun.grid.arco.model.ViewConfiguration;
import com.sun.grid.arco.model.Pivot;
import com.sun.grid.arco.model.FormattedValue;
import com.sun.grid.arco.model.PivotElement;

import java.util.*;

public class PivotViewValidator extends AbstractValidator {
   

   public void validate(QueryType query, QueryStateHandler handler) {
      
      if( query.isSetView() ) {
         ViewConfiguration view = query.getView();
         if( view.isSetPivot() ) {
            Pivot pivot = view.getPivot();
            if( pivot.isVisible() ) {
               
               int colCount = 0;
               int rowCount = 0;
               int dataCount = 0;
               
               Iterator iter = pivot.getElem().iterator();
               PivotElement elem = null;
               while( iter.hasNext() ) {
                  elem = (PivotElement)iter.next();
                  if(ArcoConstants.PIVOT_TYPE_COLUMN.equals(elem.getPivotType())) {
                     colCount++;
                  } else if ( ArcoConstants.PIVOT_TYPE_DATA.equals(elem.getPivotType())) {
                     dataCount++;
                  } else if ( ArcoConstants.PIVOT_TYPE_ROW.equals(elem.getPivotType())) {
                     rowCount++;
                  } 
               }
               
               if( colCount == 0 ) {
                  handler.addError("view/pivot", "query.view.pivot.columRequired", null );
                  return;
               } 
               if( rowCount == 0 ) {
                  handler.addError("view/pivot", "query.view.pivot.rowRequired", null );
                  return;
               }
               
               if( dataCount == 0) {
                  handler.addError("view/pivot", "query.view.pivot.dataRequired", null );
               }
               validateFormattedValueList(pivot.getElem(), query, handler );               
            }
         }
      }
   }
   
   private void validateFormattedValueList(List formattedValueList, QueryType query, 
                                           QueryStateHandler handler) {
      Iterator iter = formattedValueList.iterator();
      FormattedValue fv = null;
      
      while( iter.hasNext() ) {
         fv = (FormattedValue)iter.next();
         if( !existsField(query,fv.getName()) ) {
            handler.addError("view/table", "query.view.pivot.unknownField", 
                             new Object[] { fv.getName() == null ? "unknown" : fv.getName() });
            break;
         }
      }
   }
   
   
}
