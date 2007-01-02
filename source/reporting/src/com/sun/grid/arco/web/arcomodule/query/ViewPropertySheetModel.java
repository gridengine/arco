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

import com.iplanet.jato.RequestManager;
import com.iplanet.jato.view.View;
import com.iplanet.jato.view.ContainerView;
import com.sun.web.ui.view.html.CCCheckBox;
import com.iplanet.jato.view.html.OptionList;
import com.sun.grid.arco.ArcoConstants;
import java.util.Iterator;
import java.util.List;
import com.sun.grid.arco.web.arcomodule.QueryModel;
import com.sun.grid.arco.web.arcomodule.util.DefaultActionTable;
import com.sun.grid.arco.model.*;
import com.sun.grid.logging.SGELog;
import com.sun.grid.arco.web.arcomodule.ArcoServlet;
import com.sun.web.ui.view.addremove.CCAddRemove;
import com.sun.web.ui.view.html.CCDropDownMenu;
import com.sun.web.ui.model.CCPropertySheetModel;
import java.io.PrintWriter;
import java.io.StringWriter;
import com.sun.grid.arco.web.arcomodule.util.ModelListener;
import com.sun.web.ui.view.html.CCButton;

import com.sun.web.ui.view.html.CCHiddenField;
import com.sun.web.ui.view.html.CCRadioButton;

public class ViewPropertySheetModel extends CCPropertySheetModel implements ModelListener  {
   
   public static final String CHILD_SHOW_DESCRIPTION_VALUE = "showDescriptionValue";
   public static final String CHILD_SHOW_FILTER_VALUE = "showFilterValue";
   public static final String CHILD_SHOW_SQL_VALUE = "showSQLValue";
   public static final String CHILD_AVAILABLE_DB_FIELD_LIST = "availableDBFieldList";
   public static final String CHILD_VIEW_TABLE = "viewTable";
   public static final String CHILD_VIEW_PIVOT_TABLE = "pivotTable";
   public static final String CHILD_ADD_TABLE_BUTTON = "AddTableButton";
   public static final String CHILD_ADD_PIVOT_BUTTON = "AddPivotButton";
   public static final String CHILD_ADD_GRAPH_BUTTON = "AddGraphicButton";
   public static final String CHILD_REMOVE_TABLE_BUTTON = "RemoveTableButton";
   public static final String CHILD_REMOVE_PIVOT_BUTTON = "RemovePivotButton";
   public static final String CHILD_REMOVE_GRAPHIC_BUTTON = "RemoveGraphicButton";
   
   public static final String CHILD_TABLE_UP_BUTTON = "TableUpButton";
   public static final String CHILD_TABLE_DOWN_BUTTON = "TableDownButton";
   
   public static final String CHILD_PIVOT_UP_BUTTON = "PivotUpButton";
   public static final String CHILD_PIVOT_DOWN_BUTTON = "PivotDownButton";
   
   public static final String CHILD_GRAPHIC_UP_BUTTON = "GraphicUpButton";
   public static final String CHILD_GRAPHIC_DOWN_BUTTON = "GraphicDownButton";
   
   public static final String CHILD_VIEW_DIAGRAM_DROP_DOWN = "diagramDropDown";
   public static final String CHILD_VIEW_XAXIS_DROP_DOWN = "xaxisDropDown";
   public static final String CHILD_VIEW_LABEL_DROP_DOWN = "labelDropDown";
   public static final String CHILD_VIEW_VALUE_DROP_DOWN = "valueDropDown";
   public static final String CHILD_VIEW_COLUMN_ADD_REMOVE = "columnAddRemove";
   public static final String CHILD_VIEW_SFR_RADIO_BUTTON = "seriesFromRowRadioButton";
   public static final String CHILD_VIEW_SFC_RADIO_BUTTON = "seriesFromColumnsRadioButton";
   
   public static final String CHILD_VIEW_SERIES_TYPE_FIELD = "seriesTypeField";
   
   public static final String CHILD_VIEW_SHOW_LEGEND = "showLegendCheckBox";
   public static final String CHILD_VIEW_PIE_SHOW_LABEL = "showLabelCheckBox";
   
   
   /** Creates a new instance of ViewPropertySheetModel */
   public ViewPropertySheetModel() {
      QueryModel queryModel = ArcoServlet.getQueryModel();
      if( queryModel.getQuery() != null ) {
         setDocument(createDocument(queryModel.getQuery()));
      }
      queryModel.addModelListener(this);
   }
   
   protected ViewTableModel getViewTableModel() {
      return (ViewTableModel)RequestManager.getRequestContext().getModelManager().getModel(ViewTableModel.class);
   }
   
   protected ViewPivotTableModel getViewPivotTableModel() {
      return (ViewPivotTableModel)RequestManager.getRequestContext().getModelManager().getModel(ViewPivotTableModel.class);
   }
   
   protected ViewSFCAddRemoveModel getViewSeriesFromColumnsAddRemoveModel() {
      return (ViewSFCAddRemoveModel)RequestManager.getRequestContext().getModelManager().getModel(ViewSFCAddRemoveModel.class);
   }
   
   
   public View createChild(View view, String name) {
      
      if (name.equals(CHILD_SHOW_DESCRIPTION_VALUE)) {
         return new CCCheckBox(view, ArcoServlet.getQueryModel(),
                 name, "/view/description/visible",
                 Boolean.TRUE, Boolean.FALSE, false, null);
      } else if (name.equals(CHILD_SHOW_FILTER_VALUE)) {
         return new CCCheckBox(view, ArcoServlet.getQueryModel(),
                 name, "/view/parameter/visible",
                 Boolean.TRUE, Boolean.FALSE, false, null);
      } else if (name.equals(CHILD_SHOW_SQL_VALUE)) {
         return new CCCheckBox(view, ArcoServlet.getQueryModel(),
                 name, "/view/sql/visible",
                 Boolean.TRUE, Boolean.FALSE, false, null);
      } else if (name.equals(CHILD_VIEW_SHOW_LEGEND)) {
         return new CCCheckBox(view, ArcoServlet.getQueryModel(),
                 name, "/view/graphic/legendVisible",
                 Boolean.TRUE, Boolean.FALSE, false, null);
      } else if (name.equals(CHILD_VIEW_PIE_SHOW_LABEL)) {
         return new CCCheckBox(view, ArcoServlet.getQueryModel(),
                 name, "/view/graphic/chart/labelsVisible",
                 Boolean.TRUE, Boolean.FALSE, false, null);
      } else if (name.equals(CHILD_VIEW_DIAGRAM_DROP_DOWN)) {
         return new CCDropDownMenu((ContainerView)view, ArcoServlet.getQueryModel(), name,
                 "/view/graphic/chart/type", null, ArcoServlet.getInstance().getChartTypeOptionList(), null);
      } else if (name.equals(CHILD_VIEW_XAXIS_DROP_DOWN)) {
         return new CCDropDownMenu((ContainerView)view, ArcoServlet.getQueryModel(), name,                 
                 "/view/graphic/chart/xaxis", null, ArcoServlet.getQueryModel().getAvailableOptionList(true), null);
      } else if (name.equals(CHILD_VIEW_LABEL_DROP_DOWN)) {
         return new CCDropDownMenu((ContainerView)view, ArcoServlet.getQueryModel(), name,
                 "/view/graphic/chart/seriesFromRow/label", null, ArcoServlet.getQueryModel().getAvailableOptionList(false), null);
      } else if (name.equals(CHILD_VIEW_VALUE_DROP_DOWN)) {
         return new CCDropDownMenu((ContainerView)view, ArcoServlet.getQueryModel(), name,
                 "/view/graphic/chart/seriesFromRow/value", null, ArcoServlet.getQueryModel().getAvailableOptionList(false), null);
      } else if ( name.equals( CHILD_VIEW_TABLE )) {
         return new DefaultActionTable((ContainerView)view, getViewTableModel(), name);
      } else if (name.equals(CHILD_VIEW_PIVOT_TABLE)) {
         return new DefaultActionTable((ContainerView)view, getViewPivotTableModel(), name);
      } else if (name.equals(CHILD_VIEW_COLUMN_ADD_REMOVE)) {
         return new CCAddRemove((ContainerView)view, getViewSeriesFromColumnsAddRemoveModel(), name);
      } else if (name.equals( CHILD_VIEW_SERIES_TYPE_FIELD )) {
         return new CCHiddenField(view,ArcoServlet.getQueryModel(),name,"/view/graphic/chart/seriesType",null,null);
      } else if (name.equals( CHILD_VIEW_SFC_RADIO_BUTTON)) {
         
         Object value = null;
         if( isSFCSelected() ) {
            value = ArcoConstants.CHART_SERIES_FROM_COL;
         }
         return new CCRadioButton((ContainerView)view, name, value);
      } else if (name.equals( CHILD_VIEW_SFR_RADIO_BUTTON)) {
         Object value = null;
         if( isSFRSelected() ) {
            value = ArcoConstants.CHART_SERIES_FROM_ROW;
         }
         return new CCRadioButton((ContainerView)view, name, value);
      } else if (name.equals(CHILD_ADD_GRAPH_BUTTON)) {
         return new CCButton((ContainerView)view,name,null);
      } else if (name.equals(CHILD_ADD_TABLE_BUTTON)) {
         return new CCButton((ContainerView)view,name,null);
      } else if (name.equals(CHILD_ADD_PIVOT_BUTTON)) {
         return new CCButton((ContainerView)view,name,null);
      } else if (name.equals(CHILD_REMOVE_TABLE_BUTTON)) {
         return new CCButton((ContainerView)view,name,null);
      } else if (name.equals(CHILD_REMOVE_PIVOT_BUTTON)) {
         return new CCButton((ContainerView)view,name,null);
      } else if (name.equals(CHILD_REMOVE_GRAPHIC_BUTTON)) {
         return new CCButton((ContainerView)view,name,null);
      } else if (name.equals(CHILD_TABLE_UP_BUTTON)) {
         return new CCButton((ContainerView)view,name,null);
      } else if (name.equals(CHILD_TABLE_DOWN_BUTTON)) {
         return new CCButton((ContainerView)view,name,null);
      } else if (name.equals(CHILD_PIVOT_UP_BUTTON)) {
         return new CCButton((ContainerView)view,name,null);
      } else if (name.equals(CHILD_PIVOT_DOWN_BUTTON)) {
         return new CCButton((ContainerView)view,name,null);
      } else if (name.equals(CHILD_GRAPHIC_UP_BUTTON)) {
         return new CCButton((ContainerView)view,name,null);
      } else if (name.equals(CHILD_GRAPHIC_DOWN_BUTTON)) {
         return new CCButton((ContainerView)view,name,null);
      } else {
         return super.createChild(view,name);
      }
   }
   
   private Chart chart;
   private boolean chartInitialized;
   
   private Chart getChart() {     
      if( !chartInitialized ) {
         QueryType query = ArcoServlet.getQueryModel().getQuery();
         if( query.isSetView() ) {
            ViewConfiguration view = query.getView();
            if( view.isSetGraphic() ) {
               Graphic graphic = view.getGraphic();
               if( graphic.isSetChart() ) {
                  chart = graphic.getChart();
               }
            }
         }
         chartInitialized = true;
      }
      return chart;
   }
   
   public boolean isSFCSelected() {
      return getChart() != null && ArcoConstants.CHART_SERIES_FROM_COL.equals(getChart().getSeriesType());
   }
   
   public boolean isSFRSelected() {
      return getChart() != null && ArcoConstants.CHART_SERIES_FROM_ROW.equals(getChart().getSeriesType());
   }
   
   // ----------------- Dynamic document ---------------------------------------
   
   public String createDocument(QueryType query) {
      
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      
      pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
      pw.println("<!DOCTYPE propertysheet SYSTEM \"com_sun_web_ui/dtd/propertysheet.dtd\">");
      pw.println("<propertysheet>");
      
      writeGeneralSection(pw, query);
      
      if( query.isSetView() ) {
         ViewConfiguration view = query.getView();
         
         List viewElements = com.sun.grid.arco.Util.getSortedViewElements(view);
         Iterator iter = viewElements.iterator();
         
         ViewElement viewElem = null;
         boolean isFirst = true;
         boolean isLast = false;
         while(iter.hasNext()) {
            viewElem = (ViewElement)iter.next();
            isLast = !iter.hasNext();
            if(viewElem instanceof Table ) {
               writeTableSection(pw, (Table)viewElem, isFirst, isLast );
            } else if ( viewElem instanceof Pivot ) {
               writePivotSection(pw, (Pivot)viewElem, isFirst, isLast );
            } else if ( viewElem instanceof Graphic ) {
               writeGraphSection(pw,  (Graphic)viewElem, isFirst, isLast);
            } else {
               throw new IllegalStateException("Unkonwn view element type " +
                       viewElem.getClass().getName());
            }
            isFirst = false;
         }
      }
      
      pw.println("</propertysheet>");
      
      pw.flush();
      
      String ret = sw.getBuffer().toString();
      SGELog.fine("doc is -----\n{0}\n-----", ret);
      return ret;
      
   }
   
   private void writeTableSection(PrintWriter pw, Table table, boolean isFirst, boolean isLast) {
      pw.println("<section name='viewTableSection' defaultValue='query.common.view.viewTable.section'>");
      pw.println("<property span='true'>");
      writeButtonProperty(pw, CHILD_REMOVE_TABLE_BUTTON, "query.common.view.removeTable");
      if(!isFirst) {
         writeButtonProperty(pw, CHILD_TABLE_UP_BUTTON, "button.up");
      }
      if(!isLast) {
         writeButtonProperty(pw, CHILD_TABLE_DOWN_BUTTON, "button.down");
      }
      pw.println("</property>");
      pw.println("<property span='true'>");
      pw.println("   <cc name='" + CHILD_VIEW_TABLE + "' tagclass='com.sun.web.ui.taglib.table.CCActionTableTag' >");
      pw.println("      <attribute name='maxRows' value='10' />");
      pw.println("      <attribute name='title' value='query.common.view.viewTable.title' />");
      pw.println("      <attribute name='page' value='1' />");
      pw.println("      <attribute name='empty' value='query.common.view.tableEmpty' />");
      pw.println("      <attribute name='showPaginationControls' value='false' />");
      pw.println("      <attribute name='showSelectionSortIcon' value='true'/>");
      pw.println("      <attribute name='showAdvancedSortIcon' value='true'/>");
      pw.println("      <attribute name='selectionType' value='multiple'/>");
      pw.println("      <attribute name='selectionJavascript' value=\"setTimeout('toggleViewTableButtons()', 0)\"/>");
      pw.println("   </cc>");
      pw.println("</property>");
      pw.println("</section>");
   }
   
   private void writePivotSection(PrintWriter pw, Pivot pivot, boolean isFirst, boolean isLast) {
      
      pw.println("<section name='viewPivotSection' defaultValue='query.common.view.pivotTable.section'>");
      pw.println("<property span='true'>");
      writeButtonProperty(pw, CHILD_REMOVE_PIVOT_BUTTON, "query.common.view.removePivot");
      if(!isFirst) {
         writeButtonProperty(pw, CHILD_PIVOT_UP_BUTTON, "button.up");
      }
      if(!isLast) {
         writeButtonProperty(pw, CHILD_PIVOT_DOWN_BUTTON, "button.down");
      }
      pw.println("</property>");
      pw.println("<property span='true'>");
      pw.println("   <cc name='" + CHILD_VIEW_PIVOT_TABLE + "' tagclass='com.sun.web.ui.taglib.table.CCActionTableTag' >");
      pw.println("      <attribute name='maxRows' value='10' />");
      pw.println("      <attribute name='title' value='query.common.view.pivotTable.title' />");
      pw.println("      <attribute name='page' value='1' />");
      pw.println("      <attribute name='empty' value='query.common.view.tableEmpty' />");
      pw.println("      <attribute name='showPaginationControls' value='false' />");
      pw.println("      <attribute name='showSelectionSortIcon' value='true'/>");
      pw.println("      <attribute name='showAdvancedSortIcon' value='true'/>");
      pw.println("      <attribute name='selectionType' value='multiple'/>");
      pw.println("      <attribute name='selectionJavascript' value=\"setTimeout('toggleViewPivotButtons()', 0)\"/>");
      pw.println("   </cc>");
      pw.println("</property>");
      pw.println("</section>");
   }
   
   private void writeGraphSection(PrintWriter pw, Graphic graph, boolean isFirst, boolean isLast ) {
      pw.println("<section name='viewGraphicSection' defaultValue='query.common.view.graphics'>");
      pw.println("<property span='true'>");
      writeButtonProperty(pw, CHILD_REMOVE_GRAPHIC_BUTTON, "query.common.view.removeGraphic");
      if(!isFirst) {
         writeButtonProperty(pw, CHILD_GRAPHIC_UP_BUTTON, "button.up");
      }
      if(!isLast) {
         writeButtonProperty(pw, CHILD_GRAPHIC_DOWN_BUTTON, "button.down");
      }
      pw.println("</property>");
      // pw.println("<![CDATA[<table class='Tbl' width='100%' border='0' cellpadding='0' cellspacing='0' title=''>]]>");
      // pw.println("<![CDATA[<tr><td>]]>");
      pw.println("<property>");
      pw.println("    <label name='diagramtypeLabel' defaultValue='view.graphic.diagramtypeLabel'/>");
      pw.println("    <cc name='" + CHILD_VIEW_DIAGRAM_DROP_DOWN + "' tagclass='com.sun.web.ui.taglib.html.CCDropDownMenuTag' >");
      pw.println("        <attribute name='title' value='view.graphic.diagramtype'/>");
      pw.println("        <attribute name='onChange' value='javascript:document.arcoForm.action=\"../arcomodule/Query?Query.Tabs.TabHref=2\";document.arcoForm.submit();'/>");
      pw.println("    </cc>");
      pw.println("  <cc name='"+CHILD_VIEW_SERIES_TYPE_FIELD+"' tagclass='com.sun.web.ui.taglib.html.CCHiddenTag'/>");
      pw.println("</property>");
      // pw.println("<![CDATA[</td></tr>]]>");
      // pw.println("<![CDATA[</table>]]>");
      // pw.println("<![CDATA[<tr><td>]]>");
      pw.println("<property>");
      pw.println("    <label name='xaxisLabel' defaultValue='view.graphic.xaxisLabel'/>");
      pw.println("     <cc name='" + CHILD_VIEW_XAXIS_DROP_DOWN + "' tagclass='com.sun.web.ui.taglib.html.CCDropDownMenuTag' >");
      pw.println("        <attribute name='title' value='view.graphic.xaxis'/>");
      pw.println("    </cc>");
      pw.println("</property>");
      // pw.println("<![CDATA[</tr></td>]]>");
      // pw.println("<![CDATA[<tr><td>]]>");
      
      pw.println("<subsection name='SFC'>");
      pw.println("<property span='true'>");
      // pw.println("    <label name='sfcRadioButtonLabel' defaultValue='view.graphic.sfcRadioButtonLabel'/>");
      pw.println("    <cc name='" + CHILD_VIEW_SFC_RADIO_BUTTON + "' tagclass='com.sun.web.ui.taglib.html.CCRadioButtonTag' >");
      pw.println("      <option label='query.view.seriesFromColumnsRadioButton' value='"+ArcoConstants.CHART_SERIES_FROM_COL+"' /> ");
      pw.println("        <attribute name='dynamic' value='true'/>");
      pw.print("        <attribute name='onClick' value=\"javascript: ");
      writeSFCJavascript(pw, true);
      pw.println("\"/>");
      
      pw.println("    </cc>");
      pw.println("</property>");
      pw.println("<property>");
      // pw.println("    <label name='columnAddRemoveLabel' defaultValue='view.graphic.columnAddRemoveLabel'/>");
      pw.println("    <cc name='" + CHILD_VIEW_COLUMN_ADD_REMOVE + "' tagclass='com.sun.web.ui.taglib.addremove.CCAddRemoveTag' >");
      pw.println("        <attribute name='showMoveUpDownButtons' value='false' />");
      pw.println("        <attribute name='listboxHeight' value='10' />");
      pw.println("        <attribute name='listboxWidth' value='10' />");
      pw.println("        <attribute name='showAddAllButton' value='false' />");
      pw.println("        <attribute name='showRemoveAllButton' value='true' />");
      pw.println("    </cc>");
      pw.println("</property>");
      pw.println("</subsection>");
      
      pw.println("<subsection name='SFR'>");
      // pw.println("<![CDATA[</tr></td>]]>");
      // pw.println("<![CDATA[<tr><td>]]>");
      pw.println("<property span='true'>");
      // pw.println("    <label name='sfrRadioButton' defaultValue='view.graphic.sfrRadioButton'/>");
      pw.println("    <cc name='" + CHILD_VIEW_SFR_RADIO_BUTTON + "' tagclass='com.sun.web.ui.taglib.html.CCRadioButtonTag' >");
      pw.println("        <option label='query.view.seriesFromRowRadioButton' value='"+ArcoConstants.CHART_SERIES_FROM_ROW+"' />");
      pw.println("        <attribute name='dynamic' value='true'/>");
      pw.print("        <attribute name='onClick' value=\"javascript: ");
      writeSFCJavascript(pw, false);
      pw.println("\"/>");
      
      pw.println("    </cc>");
      pw.println("</property>");
      pw.println("<property>");
      pw.println("    <label name='labelFieldLabel' defaultValue='view.graphic.labelFieldLabel'/>");
      pw.println("    <cc name='" + CHILD_VIEW_LABEL_DROP_DOWN + "' tagclass='com.sun.web.ui.taglib.html.CCDropDownMenuTag' >");
      pw.println("        <attribute name='title' value='view.graphic.labelField'/>");
      pw.println("    </cc>");
      pw.println("</property>");
      pw.println("<property>");
      pw.println("    <label name='valueFieldLabel' defaultValue='view.graphic.valueFieldLabel'/>");
      pw.println("    <cc name='"+ CHILD_VIEW_VALUE_DROP_DOWN + "' tagclass='com.sun.web.ui.taglib.html.CCDropDownMenuTag' >");
      pw.println("        <attribute name='title' value='view.graphic.valueField'/>");
      pw.println("    </cc>");
      pw.println("</property>");
      pw.println("</subsection>");
      // pw.println("<![CDATA[</tr></td>]]>");
      // pw.println("<![CDATA[</table>]]>");
      
      writeBooleanProperty(pw, CHILD_VIEW_SHOW_LEGEND, "view.graphic.showLegend" );

      if( graph != null && graph.getChart() != null && (
          ArcoConstants.CHART_TYPE_PIE.equals(graph.getChart().getType()) ||
          ArcoConstants.CHART_TYPE_PIE_3D.equals(graph.getChart().getType()) ) ) {
         writeBooleanProperty(pw, CHILD_VIEW_PIE_SHOW_LABEL, "view.graphic.showLabels" );
      }
      
      pw.println("</section>");
   }
   
   
   private void writeSFCJavascript( PrintWriter pw, boolean enabled ) {
      
      if( enabled ) {
         pw.print("ccGetElement('Query.ViewTab."+CHILD_VIEW_SFR_RADIO_BUTTON+"', 'arcoForm').checked=false;");
         pw.print("ccGetElement('Query.ViewTab."+CHILD_VIEW_SERIES_TYPE_FIELD+"', 'arcoForm').value='" + ArcoConstants.CHART_SERIES_FROM_COL+"';");
      } else {
         pw.print("ccGetElement('Query.ViewTab."+CHILD_VIEW_SFC_RADIO_BUTTON+"', 'arcoForm').checked=false;");
         pw.print("ccGetElement('Query.ViewTab."+CHILD_VIEW_SERIES_TYPE_FIELD+"', 'arcoForm').value='" + ArcoConstants.CHART_SERIES_FROM_ROW+"';");
      }
      
      // TODO enable/disable CCAddRemove
      
      pw.print("ccGetElement('Query.ViewTab."+CHILD_VIEW_LABEL_DROP_DOWN+"', 'arcoForm').disabled=" + enabled + ";");
      pw.print("ccGetElement('Query.ViewTab."+CHILD_VIEW_VALUE_DROP_DOWN+"', 'arcoForm').disabled= " + enabled + ";");
   }
   
   private void writeGeneralSection(PrintWriter pw, QueryType query) {
      
      ViewConfiguration view = query.getView();
      
      pw.println("<section name='view' defaultValue='query.common.viewSection'>");
      pw.println("<property span='true'>");
      if( !view.isSetTable() || !view.getTable().isVisible() ) {
         writeButtonProperty(pw, CHILD_ADD_TABLE_BUTTON, "query.common.view.AddTable");
      }
      if( !view.isSetPivot() || !view.getPivot().isVisible() ) {
         writeButtonProperty(pw, CHILD_ADD_PIVOT_BUTTON, "query.common.view.AddPivot");
      }
      if( !view.isSetGraphic() || !view.getGraphic().isVisible() ) {
         writeButtonProperty(pw, CHILD_ADD_GRAPH_BUTTON, "query.common.view.AddGraph");
      }
      pw.println("</property>");
      
      writeBooleanProperty(pw, CHILD_SHOW_DESCRIPTION_VALUE, "query.common.view.showDescriptionLabel");
      writeBooleanProperty(pw, CHILD_SHOW_FILTER_VALUE , "query.common.view.showFilterLabel");
      writeBooleanProperty(pw, CHILD_SHOW_SQL_VALUE, "query.common.view.showSQLLabel" );
      
      pw.println("</section>");
      
   }
   
   private void writeBooleanProperty( PrintWriter pw, String name, String resourceName ) {
      pw.println("<property>");
      pw.println("<label name = '"+ name + "Label' defaultValue='"+resourceName+"'/>" );
      pw.println("  <cc name='"+name+"' tagclass='com.sun.web.ui.taglib.html.CCCheckBoxTag'>");;
      pw.println("    <attribute name='onChange' value='javascript:setDirty()'/>");
      pw.println("     <attribute name='dynamic' value='true'/>");
      pw.println("  </cc>");
      pw.println("</property>");
   }
   
   private void writeButtonProperty( PrintWriter pw, String name, String resourceName ) {
      pw.println("  <cc name='"+name+"' tagclass='com.sun.web.ui.taglib.html.CCButtonTag'>");
      pw.println("        <attribute name='defaultValue' value='"+resourceName+"'/>");
      pw.println("        <attribute name='type' value='secondary'/>");
      pw.println("  </cc>");
   }
   
   public void valueChanged(java.lang.String name) {
      if( name.equals( QueryModel.PROP_ROOT ) || name.startsWith(QueryModel.PROP_VIEW) ) {
         setDocument(createDocument(ArcoServlet.getQueryModel().getQuery()));
      }
   }
   
   public void valuesChanged(java.lang.String name) {
      if( name.equals( QueryModel.PROP_ROOT ) || name.startsWith(QueryModel.PROP_VIEW) ) {
         setDocument(createDocument(ArcoServlet.getQueryModel().getQuery()));
      }
   }

   
   
}
