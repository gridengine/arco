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

import com.iplanet.jato.view.*;
import com.iplanet.jato.model.ModelControlException;
import com.sun.web.ui.view.html.*;
import com.sun.web.ui.view.propertysheet.*;
import com.sun.web.ui.view.table.CCActionTable;
import com.iplanet.jato.view.event.RequestInvocationEvent;
import com.sun.grid.arco.web.arcomodule.ArcoServlet;
import com.sun.grid.arco.web.arcomodule.QueryViewBean;
import com.sun.grid.arco.web.arcomodule.QueryModel;
import javax.servlet.http.HttpServletRequest;

public class ViewTab extends RequestHandlingViewBase  {
   
   public static final String CHILD_PROPERTY_SHEET = "ViewPropertySheet";
   public static final String CHILD_ADD_VIEW_TABLE_BUTTON  = "AddViewTableRowButton";
   public static final String CHILD_REMOVE_VIEW_TABLE_BUTTON  = "RemoveViewTableRowButton";
   public static final String CHILD_ADD_VIEW_PIVOT_COLUMN_BUTTON  = "AddViewPivotColumnButton";
   public static final String CHILD_ADD_VIEW_PIVOT_ROW_BUTTON  = "AddViewPivotRowButton";
   public static final String CHILD_ADD_VIEW_PIVOT_DATA_BUTTON  = "AddViewPivotDataButton";   
   public static final String CHILD_REMOVE_VIEW_PIVOT_BUTTON  = "RemoveViewPivotButton";
   public static final String CHILD_ADD_VIEW_GRAPHIC_ROW_BUTTON  = "AddViewGraphicRowButton";
   public static final String CHILD_REMOVE_VIEW_GRAPHIC_ROW_BUTTON  = "RemoveViewGraphicRowButton";
   
   public static final String CHILD_NUMBER_FORMAT_FIELD = "numberFormatField";
   public static final String CHILD_DATE_FORMAT_FIELD = "dateFormatField";
   
   
   /** Creates a new instance of SQLTab */
   public ViewTab(View parent, String name) {
      super(parent, name);
      registerChild(CHILD_PROPERTY_SHEET, CCPropertySheet.class );
      registerChild(CHILD_NUMBER_FORMAT_FIELD, CCHiddenField.class );
      registerChild(CHILD_DATE_FORMAT_FIELD, CCHiddenField.class );
      getPropertySheetModel().registerChildren(this);
   }
   
   private ViewPropertySheetModel viewPropertySheetModel;
   
   public ViewPropertySheetModel getPropertySheetModel() {
      if(viewPropertySheetModel == null) {
         viewPropertySheetModel = (ViewPropertySheetModel)getModel(ViewPropertySheetModel.class);
         viewPropertySheetModel.setModel(ViewPropertySheetModel.CHILD_VIEW_TABLE,  getViewTableModel());
         viewPropertySheetModel.setModel(ViewPropertySheetModel.CHILD_VIEW_PIVOT_TABLE,  getViewPivotTableModel());
      }
      return viewPropertySheetModel;
   }

   private ViewTableModel getViewTableModel() {
      return (ViewTableModel)getModel(ViewTableModel.class);
   }
   
   private ViewPivotTableModel getViewPivotTableModel() {
      return (ViewPivotTableModel)getModel(ViewPivotTableModel.class);
   }

   protected View createChild(java.lang.String name) {
      if ( name.equals(CHILD_PROPERTY_SHEET)) {
         return new CCPropertySheet(this,getPropertySheetModel(),name);
      } else if( getPropertySheetModel().isChildSupported(name) ) {
         return getPropertySheetModel().createChild(this,name);
      } else if( name.equals(CHILD_ADD_VIEW_TABLE_BUTTON)
                 || name.equals(CHILD_REMOVE_VIEW_TABLE_BUTTON)) {
         return new CCButton(this, name, null);
      } else if ( name.equals(CHILD_NUMBER_FORMAT_FIELD ) ) {
         // This field is needed by the javascript method changeFormatOptions
         // see arco.js
         return new CCHiddenField(this,name,
                                  ArcoServlet.getInstance().getFormatHelper().getNumberFormatsString() );
      } else if ( name.equals(CHILD_DATE_FORMAT_FIELD ) ) {
         // This field is needed by the javascript method changeFormatOptions
         // see arco.js
         return new CCHiddenField(this,name,
                                  ArcoServlet.getInstance().getFormatHelper().getDateFormatsString() );
      } else {
         throw new IllegalStateException("Unknown child " + name);
      }
   }
   
   public void handleAddViewTableRowButtonRequest(RequestInvocationEvent event) {
      QueryModel model = ArcoServlet.getQueryModel();
      model.addNewDefinedField();
      getViewBean(QueryViewBean.class).forwardTo(event.getRequestContext());
   }

   public void handleRemoveViewTableRowButtonRequest(RequestInvocationEvent event)
     throws ModelControlException {
      CCActionTable viewTable = (CCActionTable)getChild( ViewPropertySheetModel.CHILD_VIEW_TABLE);
      viewTable.restoreStateData();
      Integer rows [] = getViewTableModel().getSelectedRows();
      QueryModel model = ArcoServlet.getQueryModel();
      model.removeDefinedFields(rows);
      getViewBean(QueryViewBean.class).forwardTo(event.getRequestContext());
   }
   
   public void handleAddViewPivotColumnButtonRequest(RequestInvocationEvent event) {      
      QueryModel model = ArcoServlet.getQueryModel();
      model.addNewPivotColumn();
      getViewBean(QueryViewBean.class).forwardTo(event.getRequestContext());
   }

   public void handleAddViewPivotRowButtonRequest(RequestInvocationEvent event) {
      QueryModel model = ArcoServlet.getQueryModel();
      model.addNewPivotRow();
      getViewBean(QueryViewBean.class).forwardTo(event.getRequestContext());
   }

   public void handleAddViewPivotDataButtonRequest(RequestInvocationEvent event) {
      QueryModel model = ArcoServlet.getQueryModel();
      model.addNewPivotData();
      getViewBean(QueryViewBean.class).forwardTo(event.getRequestContext());
   }

   public void handleRemoveViewPivotButtonRequest(RequestInvocationEvent event)
     throws ModelControlException {
      CCActionTable pivotTable = (CCActionTable)getChild( ViewPropertySheetModel.CHILD_VIEW_PIVOT_TABLE);
      pivotTable.restoreStateData();
      Integer rows [] = getViewPivotTableModel().getSelectedRows();
      QueryModel model = ArcoServlet.getQueryModel();
      model.removePivotElement(rows);
      getViewBean(QueryViewBean.class).forwardTo(event.getRequestContext());
   }

   
   public void handleAddTableButtonRequest(RequestInvocationEvent event) {
      QueryModel model = ArcoServlet.getQueryModel();
      model.addTableView();
      getViewBean(QueryViewBean.class).forwardTo(event.getRequestContext());
   }
   
   public void handleRemoveTableButtonRequest(RequestInvocationEvent event) {
      QueryModel model = ArcoServlet.getQueryModel();
      model.removeTableView();
      getViewBean(QueryViewBean.class).forwardTo(event.getRequestContext());
   }
   
   public void handleTableUpButtonRequest(RequestInvocationEvent event) {
      QueryModel model = ArcoServlet.getQueryModel();
      model.moveTableViewUp();
      getViewBean(QueryViewBean.class).forwardTo(event.getRequestContext());
   }

   public void handleTableDownButtonRequest(RequestInvocationEvent event) {
      QueryModel model = ArcoServlet.getQueryModel();
      model.moveTableViewDown();
      getViewBean(QueryViewBean.class).forwardTo(event.getRequestContext());
   }
   
   public void handleAddPivotButtonRequest(RequestInvocationEvent event) {
      QueryModel model = ArcoServlet.getQueryModel();
      model.addPivotView();
      getViewBean(QueryViewBean.class).forwardTo(event.getRequestContext());
   }

   public void handleRemovePivotButtonRequest(RequestInvocationEvent event) {
      QueryModel model = ArcoServlet.getQueryModel();
      model.removePivotView();      
      getViewBean(QueryViewBean.class).forwardTo(event.getRequestContext());
   }
   
   public void handlePivotUpButtonRequest(RequestInvocationEvent event) {
      QueryModel model = ArcoServlet.getQueryModel();
      model.movePivotViewUp();
      getViewBean(QueryViewBean.class).forwardTo(event.getRequestContext());
   }

   public void handlePivotDownButtonRequest(RequestInvocationEvent event) {
      QueryModel model = ArcoServlet.getQueryModel();
      model.movePivotViewDown();
      getViewBean(QueryViewBean.class).forwardTo(event.getRequestContext());
   }
   
   public void handleAddGraphicButtonRequest(RequestInvocationEvent event)  {
      QueryModel model = ArcoServlet.getQueryModel();
      model.addGraphicView();
      getViewBean(QueryViewBean.class).forwardTo(event.getRequestContext());
   }
   
   public void handleRemoveGraphicButtonRequest(RequestInvocationEvent event) {
      ArcoServlet.getQueryModel().removeGraphicView();      
      getViewBean(QueryViewBean.class).forwardTo(event.getRequestContext());
   }

   public void handleGraphicUpButtonRequest(RequestInvocationEvent event) {
      ArcoServlet.getQueryModel().moveGraphicViewUp();
      getViewBean(QueryViewBean.class).forwardTo(event.getRequestContext());
   }

   public void handleGraphicDownButtonRequest(RequestInvocationEvent event) {
      ArcoServlet.getQueryModel().moveGraphicViewDown();
      getViewBean(QueryViewBean.class).forwardTo(event.getRequestContext());
   }

   
}

