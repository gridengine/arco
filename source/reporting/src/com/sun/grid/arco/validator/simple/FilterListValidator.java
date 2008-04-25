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
package com.sun.grid.arco.validator.simple;

import com.sun.grid.arco.model.Filter;
import com.sun.grid.arco.validator.QueryStateHandler;
import com.sun.grid.arco.validator.Validator;
import com.sun.grid.arco.model.QueryType;

import java.util.*;
import com.sun.grid.arco.util.FilterType;
import com.sun.grid.arco.util.LogicalConnection;

public class FilterListValidator implements Validator {
   

   public void validate(QueryType query, QueryStateHandler handler) {
      
      if( query.isSetFilter() ) {
         
         Iterator iter = query.getFilter().iterator();
         Filter filter = null;
         int i = 0;
         boolean firstActive = true;
         while( iter.hasNext() && !handler.hasErrors() ) {
            filter = (Filter)iter.next();
            if( filter.isActive() ) {
               validateFilter(filter, i, firstActive, handler);
               firstActive=false;
            }
            i++;
         }
      }
   }
   
   private void validateFilter( Filter filter, int index, boolean firstActive, QueryStateHandler handler) {      
      // Logical Connection
      String lgName = filter.getLogicalConnection();

      LogicalConnection lg = null;
      
      try {
         if( filter.isSetLogicalConnection() ) {
            lg = LogicalConnection.getLogicalConnectionByName(lgName);
         }
         //First active
         if( firstActive ) {
            if( lg != null && lg != LogicalConnection.NONE ) {
               // We dont want to display this  
               // handler.addWarning("filter[0]", "query.simple.filter.firstLogicalConnectionIgnored", new Object[] {  filter.getName(), new Integer(index+1), lgName } );
               //Do not null the expression! This is only warning, the filter above this can be re-enabled and this will be lost
               //filter.setLogicalConnection(LogicalConnection.NONE.getName());
            }
         } else {
            if( lg == null || lg == LogicalConnection.NONE ) {
               handler.addError("filter["+index+"]", "query.simple.filter.invalidLogicalConnection", new Object[] { filter.getName(), new Integer(index+1) } );
            }
         }
      } catch( IllegalArgumentException ille ) {
         handler.addError( "filter["+index+"]", "query.simple.filter.invalidLogicalConnection", new Object[] { filter.getName(), new Integer(index+1) } );
      }
      
      // Parameter Count
      if( !filter.isLateBinding() ) {
         String condition = filter.getCondition();
         FilterType filterType = FilterType.getFilterTypeByName(condition);
         
         if( filterType.getParameterCount() > 0 && !filter.isSetParameter() ) {
            if( filterType.getParameterCount() == 1 ) {
               handler.addError("filter["+index+"]", "query.simple.filter.parameterRequired", 
                        new Object[] { filter.getName() } );
            } else {
               handler.addError("filter["+index+"]", "query.simple.filter.parametersRequired", 
                        new Object[] { filter.getName(), new Integer(filterType.getParameterCount()) } );
            }
         }
      }
   }
   
}
