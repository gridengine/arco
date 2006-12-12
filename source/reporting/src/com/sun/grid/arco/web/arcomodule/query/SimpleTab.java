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

import com.sun.grid.arco.web.arcomodule.QueryViewBean;
import com.sun.grid.arco.web.arcomodule.QueryModel;
import com.sun.grid.logging.SGELog;
import com.sun.grid.arco.web.arcomodule.ArcoServlet;

public class SimpleTab extends RequestHandlingViewBase  {
   

   public static final String CHILD_PROPERTY_SHEET = "SimplePropertySheet";
   public static final String CHILD_TABLE_CHANGED_HREF  ="tableChangedHref";
   
   public static final String CHILD_ADD_FIELD_BUTTON = "AddFieldButton";
   public static final String CHILD_REMOVE_FIELD_BUTTON = "RemoveFieldButton";
   public static final String CHILD_ADD_FILTER_BUTTON = "AddFilterButton";
   public static final String CHILD_REMOVE_FILTER_BUTTON = "RemoveFilterButton";
   
   /** Creates a new instance of SQLTab */
   public SimpleTab(View parent, String name) {
      super(parent, name);      
      registerChild(CHILD_PROPERTY_SHEET, CCPropertySheet.class );
      registerChild(CHILD_TABLE_CHANGED_HREF,CCHref.class);
      getPropertySheetModel().registerChildren(this);      
   }
   
   private SimplePropertySheetModel simplePropertySheetModel;
   
   private SimplePropertySheetModel getPropertySheetModel() {
      if(simplePropertySheetModel == null) {
         simplePropertySheetModel = (SimplePropertySheetModel)getModel(SimplePropertySheetModel.class);
         simplePropertySheetModel.setModel(SimplePropertySheetModel.CHILD_FIELD_VALUE, getFieldTableModel());
         simplePropertySheetModel.setModel(SimplePropertySheetModel.CHILD_FILTER_VALUE, getFilterTableModel());
      }
      return simplePropertySheetModel;
   }
   
   private FieldTableModel getFieldTableModel() {
      return (FieldTableModel)getModel(FieldTableModel.class);
   }
   
   private FilterTableModel getFilterTableModel() {
      return(FilterTableModel)getModel(FilterTableModel.class);
   }
   
   protected View createChild(java.lang.String name) {
      if ( name.equals(CHILD_PROPERTY_SHEET)) {   
         return new CCPropertySheet(this,getPropertySheetModel(),name);
      } else if( getPropertySheetModel().isChildSupported(name) ) {
         return getPropertySheetModel().createChild(this,name);
      } else if( name.equals(CHILD_TABLE_CHANGED_HREF) ) {
         return new CCHref(this,name,null);
      } else if( name.equals(CHILD_ADD_FIELD_BUTTON)
                 || name.equals(CHILD_REMOVE_FIELD_BUTTON)
                 || name.equals(CHILD_ADD_FILTER_BUTTON) 
                 || name.equals(CHILD_REMOVE_FILTER_BUTTON) ) {
         return new CCButton(this,name, null);
      } else {
         throw new IllegalStateException("Unknown child " + name);
      }
   }
   
   /**
    *  This method is invoked, if the value of the if the table/view
    *  drop down menu has changed.
    *  All filters and fields of the query will be deleted.
    *  @param event  the RequestInvocationEvent
    */
   public void handleTableChangedHrefRequest(RequestInvocationEvent event) {
      SGELog.fine("table value changed");    
      QueryModel model = ArcoServlet.getQueryModel();
      model.clearFieldsAndFilters();
      getViewBean(QueryViewBean.class).forwardTo(event.getRequestContext());
   }

   public void handleAddFieldButtonRequest(RequestInvocationEvent event) {
      
      QueryModel model = ArcoServlet.getQueryModel();
      model.addNewField();
      getViewBean(QueryViewBean.class).forwardTo(event.getRequestContext());
   }
   
   public void handleRemoveFieldButtonRequest(RequestInvocationEvent event)
     throws ModelControlException {
      CCActionTable fieldTable = (CCActionTable)getChild( SimplePropertySheetModel.CHILD_FIELD_VALUE);
      fieldTable.restoreStateData();
      Integer rows [] = getFieldTableModel().getSelectedRows();
      QueryModel model = ArcoServlet.getQueryModel();
      model.removeFields(rows);
      getViewBean(QueryViewBean.class).forwardTo(event.getRequestContext());
   }
   
   
   public void handleAddFilterButtonRequest(RequestInvocationEvent event) {
      QueryModel model = ArcoServlet.getQueryModel();
      model.addNewFilter();
      getViewBean(QueryViewBean.class).forwardTo(event.getRequestContext());
   }
   
   public void handleRemoveFilterButtonRequest(RequestInvocationEvent event)
      throws ModelControlException {    
      getFilterTable().restoreStateData();
      Integer rows [] = getFilterTableModel().getSelectedRows();
      QueryModel model = ArcoServlet.getQueryModel();
      model.removeFilters(rows);
      getViewBean(QueryViewBean.class).forwardTo(event.getRequestContext());
   }
   
   private CCActionTable getFilterTable() {
      return (CCActionTable)getChild( SimplePropertySheetModel.CHILD_FILTER_VALUE );
   }
}
