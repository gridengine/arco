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
package com.sun.grid.arco.xml;

import com.sun.grid.arco.QueryResult;
import com.sun.grid.arco.model.Result;
import com.sun.grid.arco.ResultConverter;
import com.sun.grid.arco.model.Filter;
import com.sun.grid.arco.model.ResultColumn;
import com.sun.grid.arco.model.ResultRow;
import java.util.*;

public class XMLQueryResult extends QueryResult {
   
   private Result result;
   /** Creates a new instance of XMLQueryResult */
   public XMLQueryResult(Result result) {
      super(result);
      this.result = result;
      
      List filterList = result.getFilter();
      if( filterList != null && !filterList.isEmpty()) {
         Filter filter = null;
         Iterator iter = filterList.iterator();
         while(iter.hasNext()) {
            filter = (Filter)iter.next();
            if( filter.isActive() && filter.isLateBinding() ) {
               setLateBinding(filter.getName(), filter.getParameter());
            }
         }
      }
   }

   private transient Iterator rowIterator;
   
   public java.lang.Object[] createValuesForNextRow() {
      
      if( rowIterator == null ) {
         rowIterator = result.getRow().iterator(); 
      }
      Object [] ret = null;
      
      if( rowIterator.hasNext() ) {
         ResultRow rowObj = (ResultRow)rowIterator.next();

         List values = rowObj.getValue();
         List colList = result.getColumn();

         int colCount = colList.size();

         ret = new Object[colCount];
         ResultColumn col = null;
         for(int i = 0; i < colCount; i++ ) {
            col = (ResultColumn)colList.get(i); 
            ret[i] = ResultConverter.strToObj((String)values.get(i), col.getType() );
         }
      }
      return ret;
   }
   
   private transient List columnList = null;

   public List getColumns() {
      if( columnList == null ) {
         List columns = result.getColumn();
         columnList = new ArrayList(columns.size());
         Iterator iter = columns.iterator();
         ResultColumn col = null;
         while(iter.hasNext()) {
            col = (ResultColumn)iter.next();
            columnList.add(col.getName());
         }
      }
      return columnList;
   }

   public void activate() throws com.sun.grid.arco.QueryResultException {
      rowIterator = result.getRow().iterator();
   }

   public void passivate() {
      rowIterator = null;
   }

    /**
     * Get the class of a column
     * @param index  index of the column
     * @return  the class of a column
     */
    public Class getColumnClass(int index) {
       ResultColumn col = (ResultColumn)result.getColumn().get(index);
       return ResultConverter.getColumnClass(col.getType());
    }

   /**
    * The XMLQueryResult is not editable
    * @return  false
    */
   public boolean isEditable() {
      return false;
   }

}
