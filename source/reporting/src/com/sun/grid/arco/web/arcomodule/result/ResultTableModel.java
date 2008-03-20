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
package com.sun.grid.arco.web.arcomodule.result;
import java.util.*;
import com.iplanet.jato.RequestManager;
import com.iplanet.jato.view.ContainerView;
import com.iplanet.jato.view.View;
import com.sun.web.ui.model.CCActionTableModel;
import com.sun.grid.arco.model.*;
import com.sun.grid.arco.QueryResult;
import com.sun.grid.arco.QueryResultListener;
import com.sun.grid.arco.util.SortType;
import com.sun.grid.arco.web.arcomodule.ArcoServlet;
import com.sun.grid.logging.SGELog;
import java.util.logging.Level;
import java.io.*;
import java.text.Format;

public class ResultTableModel extends CCActionTableModel
   implements QueryResultListener {
   
   private QueryResult result;
   private java.text.Format formater[];
   private Map formaterMap = new HashMap();
   
   /** Creates a new instance of ResultTableModel */
   public ResultTableModel() {
     setQueryResult(ArcoServlet.getResultModel().getQueryResult());
   }
   
   public View createChild(View view, String name) {

      Format format = (Format)formaterMap.get(name);
      if( format != null ) {
         // we have a column index
         return new FormatStaticTextField((ContainerView)view, this, name, name, null, format);
         
      } else {
         return super.createChild(view, name);
      }
      
   }
   
   /**
    * Set Query Result 
    * @param result can be null value
    */
   public void setQueryResult( QueryResult result ) {
      // Unregister if result has changed
      if(this.result != null && !this.result.equals(result)) {
         this.result.removeQueryResultListener(this);
      }
      // Change document just for another result
      if(result != null && !result.equals(this.result)) {
         clearAll();
         setDocument(createDocument(result));
         this.result = result;
         initHeaders();
         initSort(result);
         result.addQueryResultListener(this);
      }
   }
   
   
   
   private static boolean isTableViewDefined(QueryType query) {

      Table table = null;
      
      if( query.isSetView() ) {
         table = query.getView().getTable();
      }      
      return table != null && table.isVisible() && !table.getColumnWithFormat().isEmpty();      
   }
   
   protected void initHeaders() {
      QueryType query = result.getQuery();
      if( isTableViewDefined(query) ) {
         List columns = query.getView().getTable().getColumnWithFormat();
         Iterator iter = columns.iterator();
         FormattedValue col = null;
         int i = 0;
         while(iter.hasNext()) {
            col = (FormattedValue)iter.next();
            String name = com.sun.grid.arco.Util.fixSpecialChar(col.getName());
            if( SGELog.isLoggable(Level.FINE)) {
               SGELog.fine("header for col[" + i + "]=" + name);
            }
            setActionValue( "Col" + i, name);  
            i++;
         }      
      } else {
         List columns = result.getColumns();
         Iterator iter = columns.iterator();
         String col = null;
         int i = 0;
         while(iter.hasNext()) {
            col = com.sun.grid.arco.Util.fixSpecialChar((String)iter.next());
            setActionValue( "Col" + i, col );  
            i++;
         }      
      }
      setNumRows(result.getRowCount());
   } 
   
   /**
    * Create Begin of the table of the result Document
    * @param queryResult, base of the result
    * @return a whole string, possible performance killer
    */
   public String createDocument(QueryResult queryResult) {
   
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      
      pw.println("<?xml version='1.0' encoding='UTF-8'?>");
      pw.println("<!DOCTYPE table SYSTEM 'tags/dtd/table.dtd'>");
      pw.println("<table>");
 
      QueryType query = queryResult.getQuery();
      Table table = null;
      
      if( query.isSetView() ) {
         table = query.getView().getTable();
      }
      
      if( isTableViewDefined(query) ) {      
         List columnList = table.getColumnWithFormat();
         Iterator columnIter = columnList.iterator();

         FormattedValue column = null;

         int columnCount = 0;
         String columnType = null;
         
         while( columnIter.hasNext() ) {
            column = (FormattedValue)columnIter.next();
            int columnIndex = queryResult.getColumnIndex(column.getName());
            pw.println("<column name='Col"+ columnCount
                       + "' sortname='" + columnIndex
                       + "' extrahtml=\"nowrap='nowrap'\">");
            pw.println("<cc name='" + columnIndex + 
                       "' tagclass='com.sun.web.ui.taglib.html.CCStaticTextFieldTag'>");
            
            if( column.getFormat() != null ) {               
               Format format  =   com.sun.grid.arco.Util.createFormat(column, RequestManager.getRequest().getLocale());
               formaterMap.put(Integer.toString(columnIndex), format);                              
            }
            
            pw.println("</cc>");
            pw.println("</column>");
            columnCount++;
         }
      } else {
         // no view set, use all fields
         List columns = queryResult.getColumns();
         Iterator iter = columns.iterator();
         String col = null;
         int columnCount = 0;
         while(iter.hasNext()) {
            col = (String)iter.next();
            int columnIndex = queryResult.getColumnIndex(col);            

            pw.println("<column name='Col"+columnCount
                       + "' sortname='" + columnIndex
                       + "' extrahtml=\"nowrap='nowrap'\">");
            pw.println("<cc name='" + columnIndex + 
                       "' tagclass='com.sun.web.ui.taglib.html.CCStaticTextFieldTag'/>");
            
            pw.println("</column>");
            columnCount++;
         }
      }
      pw.println("</table>");
      
      pw.flush();
      String ret = sw.getBuffer().toString();
      if( SGELog.isLoggable(Level.FINE)) {
         SGELog.fine("ret --------\n{0}\n-----------", ret);
      }
      return ret;
   }
   

   private java.text.Format getFormat(String name, int columnIndex) {
      java.text.Format ret = (java.text.Format)formaterMap.get(name);
      if( ret == null ) {
         ret = result.createFormater(columnIndex, null, RequestManager.getRequest().getLocale());
         formaterMap.put(name,ret);
      }
      return ret;
   }

   public Object[] getValues(String name) {
      Object value = getValue(name);

      if( value == null ) {
         return new Object[0];
      } else {
         return new Object[] { value };
      }
   }
   
   public Object getValue(String name) {
      Map valueMap = getValueMap();
      Object ret = valueMap.get(name);
      if (ret == null) {
         try {
            int columnIndex = Integer.parseInt(name);
            ret = result.getValue(getRowIndex(), columnIndex);
            if (SGELog.isLoggable(Level.FINE)) {
               SGELog.fine("value[" + getRowIndex() + "][" + name + "]=" + ret);
            }
            valueMap.put(name, ret);
         } catch (NumberFormatException expected) {
           // 6549694 throw "is not a valid column index" should be ignored
         }
      }
      return ret;
   }
   
   // QueryResultListener methods ------------------------------------
   public void rowCountChanged(int rowCount) {
        setNumRows(result.getRowCount());
        sortIndex = null;
   }

   // Sort handling ------------------------------------------------------------
   
   boolean hasSortChanged;
   private int [] sortIndex;
  
   private void initSortIndex() {
      if( !hasSortChanged && sortIndex == null ) {
         sortIndex = new int[getNumRows()];
         for (int i = 0; i < getNumRows(); i++)
            sortIndex[i] = i;
      }
   }

   public void sort() {
      if( hasSortChanged ) {
         hasSortChanged = false;
         super.sort();
      } else {
         SGELog.fine("skip sort");
      }
   }

   public int[] getSortIndex() {     
      if( hasSortChanged ) {
         return super.getSortIndex();
      } else {
         initSortIndex();
         return sortIndex;
      }
   }
   
   /**
    * Set sort by ORDER BY part of sql
    * @param query  <code>QueryType</code> the sql query
    * @param fieldName <code>String</code> the field name
    * @return
    */
   private String getSortFromOrderBy(QueryType query, String fieldName) {
      final String sql = query.getSql();
      final String orderby= sql.substring(sql.toUpperCase().lastIndexOf("ORDER BY")+8);      
      String sort=null;
      final int index = orderby.indexOf(fieldName);
      if (index > 0) {
         sort="ASC";
      }
      return sort;
   }
    
   
   private void initSort(QueryResult result) {
      QueryType query = result.getQuery();
      List fieldList = query.getField();
      Iterator iter = fieldList.iterator();
      Field field = null;
      String sort = null;
      SortType sortType = null;
      String sortOrder = null;
      
      int i = 0;
      while(iter.hasNext() && i < 3) {
         field = (Field)iter.next();
         sort = field.getSort();
         // Set right sort values for advanced query
         if( sort == null ) {
            sort = getSortFromOrderBy(query, field.getDbName());
         }
         if( sort == null ) {
            sort = getSortFromOrderBy(query, field.getReportName());
         }
         if( sort != null ) {
            sortType = SortType.getSortTypeByName(sort);
            if( sortType == SortType.ASC ) {
               sortOrder = CCActionTableModel.ASCENDING;
            } else if ( sortType == SortType.DESC ) {
               sortOrder = CCActionTableModel.DESCENDING;
            } else {
               sortOrder = null;
            }
         } else {
            sortOrder = null;
         }
         /* There is sort settings */
         if( sortOrder != null ) {
            switch(i) {
               case 0:
                  setPrimarySortName(field.getReportName());
                  setPrimarySortOrder(sortOrder);
                  break;
               case 1:
                  setSecondarySortName(field.getReportName());
                  setSecondarySortOrder(sortOrder);    
                  break;
               case 2:
                  setAdvancedSortName(field.getReportName());
                  setAdvancedSortOrder(sortOrder);    
            }
            i++;
         }         
      }
      hasSortChanged = false;
   }

   private static boolean equals(String s1, String s2) {
      if( s1 == null ) {
         return s2 == null || s2.length() == 0;
      } else if ( s2 == null ) {
         return false;
      } else {
         return s1.equals(s2);
      }
   }
   
   public void setSecondarySortOrder(String value) {
      String old = getSecondarySortOrder();
      if( !equals(old, value ) ) {
         super.setSecondarySortOrder(value);
         hasSortChanged = true;
      }
   }

   public void setSecondarySortName(String value) {
      final String old = getSecondarySortName();
      if( !equals( old, value) ) {
         super.setSecondarySortName(value);
         hasSortChanged = true;
      }
   }

   public void setPrimarySortOrder(String value) {
      final String old = getPrimarySortOrder();
      if( !equals( old, value)) {
         super.setPrimarySortOrder(value);
         hasSortChanged = true;
      }
   }

   public void setPrimarySortName(String value) {
      final String old = getPrimarySortName();
      if( !equals(old, value)) {
         super.setPrimarySortName(value);
         hasSortChanged = true;
      }
   }

   public void setAdvancedSortOrder(String value) {
      final String old = getAdvancedSortOrder();
      if( equals(old, value)) {
         super.setAdvancedSortOrder(value);
         hasSortChanged = true;
      }
   }

   public void setAdvancedSortName(String value) {
      final String old = getAdvancedSortName();
      if( equals(old,value)) {
         super.setAdvancedSortName(value);
         hasSortChanged = true;
      }
   }

   
}
