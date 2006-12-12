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
package com.sun.grid.arco.web.arcomodule.query;

import com.iplanet.jato.view.html.OptionList;
import com.sun.grid.arco.model.Chart;
import com.sun.grid.arco.model.Field;
import com.sun.grid.arco.model.QueryType;
import com.sun.grid.arco.model.SeriesFromColumns;
import com.sun.grid.arco.model.ViewConfiguration;
import com.sun.grid.arco.web.arcomodule.QueryModel;
import java.util.*;
import com.sun.grid.arco.web.arcomodule.ArcoServlet;
import com.sun.grid.arco.web.arcomodule.util.ModelListener;

import com.sun.web.ui.model.CCAddRemoveModel;

import com.sun.web.ui.view.addremove.CCAddRemove;

public class ViewSFCAddRemoveModel extends CCAddRemoveModel implements ModelListener {
   
   private QueryModel queryModel;
   
   public ViewSFCAddRemoveModel() {
      this(ArcoServlet.getQueryModel());
   }
   
   public ViewSFCAddRemoveModel(QueryModel queryModel ) {
      super();
      this.queryModel = queryModel;
      init();
      queryModel.addModelListener(this);
   }
   
   private void init() {
      QueryType query = queryModel.getQuery();
      ViewConfiguration view = query.getView();
      
      Chart chart = view.getGraphic().getChart();
      
      List columnList = new ArrayList(query.getField());
      List availableList = new ArrayList(columnList.size());
      Iterator iter = columnList.iterator();
      Field field = null;
      while(iter.hasNext()) {
         field = (Field)iter.next();
         availableList.add(field.getReportName());
      }
      
      OptionList selected = null;
      String column = null;
      
      if( chart.isSetSeriesFromColumns() ) {
         SeriesFromColumns sfc = chart.getSeriesFromColumns();
         if( sfc.isSetColumn() ) {
            selected = new OptionList();
            
            List columnsFromRows = sfc.getColumn();
            iter = columnsFromRows.iterator();
            while( iter.hasNext() ) {
               column = (String)iter.next();
               availableList.remove(column);
               selected.add(column, column);
            }
         }
      }
      
      OptionList available = new OptionList();
      iter = availableList.iterator();
      while(iter.hasNext()) {
         column = (String)iter.next();
         available.add(column, column);
      }
      
      this.setAvailableOptionList(available);
      this.setSelectedOptionList(selected);
   }
   
   
   
   public void valueChanged(String name) {
      if (name.equals(QueryModel.PROP_ROOT) || name.equals(QueryModel.PROP_VIEW)) {
         init();
      }
   }
   
   public void valuesChanged(String name) {
      if (name.equals(QueryModel.PROP_ROOT) || name.equals(QueryModel.PROP_VIEW)) {
         init();
      }
   }
   
   //   private void setSelectedOptionListInternal(OptionList selectedOptionList ) {
   //      super.setSelectedOptionList(selectedOptionList);
   //   }
   //
   //   public void setSelectedOptionList(OptionList selectedOptionList) {
   //
   //      ArcoServlet.getQueryModel().setViewChartSeriesFromColumns(selectedOptionList);
   //      setSelectedOptionListInternal(selectedOptionList);
   //   }
   
   public void setValues(String name, Object[] values) throws com.iplanet.jato.model.ValidationException {
      
      super.setValues(name, values);
   }
   
   public void setValue(String name, Object value) throws com.iplanet.jato.model.ValidationException {
      
      super.setValue(name, value);
      if( name.equals(CCAddRemove.SELECTED_TEXTFIELD)) {
         ArcoServlet.getQueryModel().setViewChartSeriesFromColumns(getOptionLists((String)value));
      }
   }
   
   private OptionList getOptionLists( String value ) {
      OptionList newOptionList = null;
      String[] newLabels = null;
      String[] newValues = null;
      
      if (value != null
              && !value.startsWith(" ")
              && !value.equals("null")) {
         int k = 0;
         StringTokenizer tmpStr = new StringTokenizer(value, SEPARATOR);
         int valueLen = tmpStr.countTokens()/2;
         newValues = new String[valueLen];
         newLabels = new String[valueLen];
         while (tmpStr.hasMoreTokens()) {
            newLabels[k] = tmpStr.nextToken();
            newValues[k] = tmpStr.nextToken();
            k++;
         }
         newOptionList = new OptionList(newLabels, newValues);
      }
      
      return newOptionList;
   }
}
