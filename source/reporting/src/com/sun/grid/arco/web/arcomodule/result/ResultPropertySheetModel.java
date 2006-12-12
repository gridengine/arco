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


import com.iplanet.jato.RequestManager;
import com.iplanet.jato.view.View;
import com.sun.grid.arco.ChartManager;
import com.sun.web.ui.view.html.CCStaticTextField;
import com.sun.web.ui.model.CCPropertySheetModel;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.io.*;
import com.sun.grid.logging.SGELog;
import com.sun.grid.arco.model.*;
import com.sun.grid.arco.web.arcomodule.ResultModel;
import com.sun.grid.arco.web.arcomodule.ArcoServlet;
import com.sun.grid.arco.QueryResult;

public class ResultPropertySheetModel extends CCPropertySheetModel implements com.sun.grid.arco.web.arcomodule.util.ModelListener {

   public static final String CHILD_CATEGORY = "categoryValue";
   public static final String CHILD_DESCRIPTION = "descriptionValue";
   public static final String CHILD_SQL = "sqlValue";
   public static final String CHILD_FILTER = "filterValue";
   public static final String CHILD_TABLE = "tableValue";
   public static final String CHILD_PIVOT = "pivotValue";
   
   public static final String PARAM_PREFIX = "Param";
   public static final String VALUE_SUFFIX = "Value";
   public static final String LABEL_SUFFIX = "Label";
   
   private ResultModel resultModel;
   private QueryResult queryResult;
   
   /** Creates a new instance of SimplePropertySheetModel */
   public ResultPropertySheetModel() {
      resultModel = ArcoServlet.getResultModel();
      
      setQueryResult(resultModel.getQueryResult());
   }
   
   private ResultTableModel resultTableModel;
   
   public ResultTableModel getResultTableModel() {
      if( resultTableModel == null ) {
         resultTableModel = (ResultTableModel)RequestManager.getRequestContext().
              getModelManager().getModel(ResultTableModel.class);
         setModel(CHILD_TABLE, resultTableModel);
         resultModel.addModelListener(this);
      }
      return resultTableModel;
   }
   
   public void valueChanged(java.lang.String name) {
      if( name.equals("/") ) {
         setQueryResult(resultModel.getQueryResult());
      }
   }

   public void valuesChanged(java.lang.String name) {
      if( name.equals("/") ) {
         setQueryResult(resultModel.getQueryResult());
      }
   }
   
   
   public void setQueryResult(QueryResult queryResult) {
      this.queryResult = queryResult;      
      
      clear();
      if( queryResult != null ) {
         QueryType query = queryResult.getQuery();
         setDocument(createDocument(query));
         getResultTableModel().setQueryResult(queryResult);
         
      }
   }

   public void registerChildren(com.iplanet.jato.view.ContainerViewBase view) {
      super.registerChildren(view);
      getResultTableModel().registerChildren(view);
   }
   
   public View createChild(View view, String name) {
      if (name.equals(CHILD_CATEGORY)) {
         return new CCStaticTextField(view,ArcoServlet.getResultModel(),name, "/query/category",null);
      } else if (name.equals(CHILD_DESCRIPTION)) {
         return new CCStaticTextField(view,ArcoServlet.getResultModel(),name, "/query/description",null);
      } else if (name.equals(CHILD_SQL)) {
         return new CCStaticTextField(view,ArcoServlet.getResultModel(),name, "/query/sql",null);
      } else if (name.equals(CHILD_FILTER)) {
         return new CCStaticTextField(view,ArcoServlet.getResultModel(),name, "/filter",null);
      } else if ( getResultTableModel().isChildSupported(name)) {
         return getResultTableModel().createChild(view,name);
      } else {
         return super.createChild(view,name);
      }
   }


   private String createDocument(QueryType query) {
      
      hasTable=false;
      hasPivotSection=false;
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      
      pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
      pw.println("<!DOCTYPE propertysheet SYSTEM \"com_sun_web_ui/dtd/propertysheet.dtd\">");
      pw.println("<propertysheet>");

      writeGeneralSection(pw, query );
      
      Object [] views = new Object[3];      
      boolean viewSet = false;
      
      List visibleElements = null;
      
      if( query.isSetView() ) {
         ViewConfiguration view = query.getView();
         if( view.isSetParameter() && view.getParameter().isVisible() ) {
            writeParameterSection(pw, query);
         }
         visibleElements = com.sun.grid.arco.Util.getSortedViewElements(query.getView());
      }
      
      if( visibleElements != null && !visibleElements.isEmpty() ) {
         Iterator iter = visibleElements.iterator();
         ViewElement elem = null;
         while( iter.hasNext() ) {
            elem = (ViewElement)iter.next();
            if( elem instanceof Table ) {
               writeTableSection(pw,query);
            } else if ( elem instanceof Pivot ) {
               writePivotSection(pw,query);
            } else if ( elem instanceof Graphic) {
               writeGraphicSection(pw, query);
            }
         }
      } else {
         writeTableSection(pw,query);
      }      
      pw.println("</propertysheet>");
      
      pw.flush();
      
      String ret = sw.getBuffer().toString();
      SGELog.fine("doc is -----\n{0}\n-----", ret);
      return ret;
   }
   

   private void writeGraphicSection(PrintWriter pw, QueryType query) {
      pw.println("<section name='graphic' defaultValue=''>");
      pw.println("<property span='true'>");
      pw.println(" <cc name='graphicValue' tagclass='com.sun.web.ui.taglib.html.CCImageTag'>");
      pw.println("   <attribute name='defaultValue' value='/reporting/chart_servlet?ts" + System.currentTimeMillis() +"'/>");
      pw.println("   <attribute name='width' value='"+ChartManager.DEFAULT_CHART_WIDTH + "'/>");
      pw.println("   <attribute name='height' value='"+ChartManager.DEFAULT_CHART_HEIGHT + "'/>");
      pw.println(" </cc>");
      pw.println("</property>");
      pw.println("</section>");
   }
   
   private boolean hasPivotSection;
   
   private void writePivotSection(PrintWriter pw, QueryType query) {
      hasPivotSection = true;
      pw.println("<section name='pivot' defaultValue=''>");
      pw.println("<property span='true'>");
      pw.println("  <cc name='pivotValue' tagclass='com.sun.web.ui.taglib.html.CCStaticTextFieldTag'>");
      pw.println("     <attribute name='escape' value='false'/>");
      pw.println("  </cc>");
      pw.println("</property>");
      pw.println("</section>");
   }
   
   private boolean hasTable = false;
   
   private void writeTableSection(PrintWriter pw, QueryType query) {

      hasTable = true;      
      pw.println("<section name='table' defaultValue=''>");
      
      pw.println("<property span='true'>");
      pw.println("<cc name='tableValue' tagclass='com.sun.web.ui.taglib.table.CCActionTableTag'>");
      pw.println(" <attribute name='title' value='result.table'/>");
      pw.println(" <attribute name='showPaginationControls' value='true'/>");
      pw.println(" <attribute name='showAdvancedSortIcon' value='true'/>");
      pw.println(" <attribute name='showSelectionSortIcon' value='true'/>");
      pw.println("</cc>");
      pw.println("</property>");
      
      pw.println("</section>");
      
   }
   
   private void writeGeneralSection(PrintWriter pw, QueryType query) {
      
      pw.println("<section name='general' defaultValue=''>");
      // printTextProperty(pw,"name", "query.common.name");
      printTextProperty(pw,"category", "query.common.category");
          
      boolean descriptionVisible = true;
      boolean sqlVisible = true;
      
      if( query.isSetView() ) {
         ViewConfiguration view = query.getView();
         if( view.isSetDescription() ) {
            descriptionVisible = view.getDescription().isVisible();
         }
         if( view.isSetSql() ) {
            sqlVisible = view.getSql().isVisible();
         }
      }
      
      if( descriptionVisible ) {
         printTextProperty(pw,"description", "query.common.description");
      }

      if( sqlVisible ) {
         printTextProperty(pw,"sql", "query.sql");
      }
      pw.println("</section>");
   }
   
   private void writeParameterSection(PrintWriter pw, QueryType query) {

      List filterList = query.getFilter();
      
      if( filterList != null && !filterList.isEmpty() ) {      
         pw.println("<section name='parameter' defaultValue='result.parameter'>");

         Iterator iter = filterList.iterator();
         Filter filter = null;
         
         while(iter.hasNext()) {
            filter = (Filter)iter.next();
            printTextProperty(pw, PARAM_PREFIX + filter.getName(), filter.getName() );
         }
         pw.println("</section>");
      }
   }
   
   private static void printTextProperty(PrintWriter pw, String name, String label) {
      pw.println("<property>");
      pw.println("  <label name='" + getLabelName(name) + "' defaultValue='"+label+"'/>");
      pw.println("  <cc name='"+getValueName(name)+"' tagclass='com.sun.web.ui.taglib.html.CCStaticTextFieldTag'>");
      pw.println("  </cc>");
      pw.println("</property>");
   }
   
   private static String getValueName(String name) {
      return name + VALUE_SUFFIX;
   }
   
   private static String getLabelName(String name) {
      return name + LABEL_SUFFIX;
   }
   
   private static String getNameFromValue(String name) {
      if( name.endsWith( VALUE_SUFFIX )) {
         return name.substring(0, name.length() - VALUE_SUFFIX.length());
      } else {
         return null;
      }
   }

   
   public boolean hasPivot() {
      return hasPivotSection;      
   }
   
   public boolean hasTable() {
      return hasTable;
   }
   
   public String getPivotHTML(java.util.Locale locale) {
      
      if( hasPivotSection ) {
         
         com.sun.grid.arco.export.PivotModel pivotModel = 
                 ArcoServlet.getResultModel().getQueryResult().createPivotModel(locale);
         
         com.sun.grid.arco.export.HTMLTablePrinter tablePrinter = 
                 new com.sun.grid.arco.export.HTMLTablePrinter("Pivot Table");
         com.sun.grid.arco.export.PivotTableGenerator
                 gen = new com.sun.grid.arco.export.PivotTableGenerator(pivotModel, tablePrinter);

         StringWriter sw = new StringWriter();
         PrintWriter pw = new PrintWriter(sw);
         gen.print(pw);
         pw.flush();
         return sw.getBuffer().toString();                  
      } else {
         throw new IllegalStateException("have no pivot section");
      }
   }

   public Object getValue(String name) {
      Object ret = null;
      if( name.startsWith( PARAM_PREFIX)) {
         Map lateBinding = queryResult.getLateBinding();
         if( lateBinding != null) {
            // We have an parameter
            // get the value from the latebinding
            String paramName = getNameFromValue(name);
            if( paramName != null ) {
               name = paramName.substring(PARAM_PREFIX.length());
               ret = lateBinding.get(name);
            }
         }
      }
      if( ret == null ) {
         ret = super.getValue(name);
      }
      return ret;
   }



}
