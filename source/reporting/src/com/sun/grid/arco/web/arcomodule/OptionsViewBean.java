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
package com.sun.grid.arco.web.arcomodule;

import com.iplanet.jato.model.ModelControlException;
import com.iplanet.jato.RequestManager;
import com.iplanet.jato.ModelManager;
import com.iplanet.jato.view.event.*;
import com.iplanet.jato.view.html.*;
import com.sun.grid.arco.ArcoException;
import com.sun.web.ui.model.*;
import com.sun.web.ui.view.breadcrumb.CCBreadCrumbs;
import com.sun.web.ui.view.table.CCActionTable;
import com.sun.web.ui.view.html.CCButton;
import com.sun.web.ui.view.propertysheet.CCPropertySheet;
import com.sun.web.ui.view.pagetitle.*;
import java.io.IOException;
import javax.servlet.ServletException;
import com.sun.grid.logging.*;
import com.sun.grid.arco.web.arcomodule.options.LoggingModel;
import com.sun.grid.arco.web.arcomodule.options.OptionsPageTitleModel;
import com.sun.grid.arco.web.arcomodule.options.OptionsPropertySheetModel;
import com.sun.grid.arco.web.arcomodule.options.LogFilterTableModel;


/**
 * This ViewBean displays the Options property sheet
 */
public class OptionsViewBean extends BaseViewBean {
   
   public static final String PAGE_NAME = "Options";
   
   public static final String ATTR_LOGGING = "LOGGING";
   
   public static final String DEFAULT_DISPLAY_URL=
           "/jsp/arcomodule/Options.jsp";
   
   public static final String OPTIONS_PROPERTY_SHEET = "OptionsPropertySheet";
   public static final String CHILD_PAGETITLE        = "PageTitle";
   public static final String CHILD_BREADCRUMB       = "BreadCrumb";
   public static final String CHILD_BACK_TO_INDEX    = "BackToIndexHref";
   public static final String CHILD_ADD_LOG_FILTER_BUTTON = "AddLogFilterButton";
   public static final String CHILD_REMOVE_LOG_FILTER_BUTTON = "RemoveLogFilterButton";
   
   /** Creates a new instance of OptionsViewBean */
   public OptionsViewBean() {
      super(PAGE_NAME, DEFAULT_DISPLAY_URL);
   }

   protected void registerNewChildren() {
      registerChild( OPTIONS_PROPERTY_SHEET, CCPropertySheet.class );
      registerChild(CHILD_PAGETITLE, CCPageTitle.class);
      registerChild(CHILD_BREADCRUMB, CCBreadCrumbs.class );
      registerChild(CHILD_BACK_TO_INDEX, HREF.class);
      registerChild(CHILD_ADD_LOG_FILTER_BUTTON,CCButton.class);
      registerChild(CHILD_REMOVE_LOG_FILTER_BUTTON,CCButton.class);
      getPageTitleModel().registerChildren(this);
      getPropertySheetModel().registerChildren(this);
   }
   
   public com.iplanet.jato.view.View newChild(String name) {
      
      if( name.equals(OPTIONS_PROPERTY_SHEET)) {
         return new CCPropertySheet(this,getPropertySheetModel(),name);
      } else if ( name.equals(CHILD_PAGETITLE)) {
         return new CCPageTitle(this, getPageTitleModel(),name);
      } else if (name.equals(CHILD_BREADCRUMB)) {
         
         CCBreadCrumbsModel model =
                 new CCBreadCrumbsModel("options.title");
         model.appendRow();
         model.setValue(CCBreadCrumbsModel.COMMANDFIELD, CHILD_BACK_TO_INDEX);
         model.setValue(CCBreadCrumbsModel.LABEL, IndexViewBean.PAGE_TITLE );
         
         return new CCBreadCrumbs(this, model, name);
      } else if ( name.equals(CHILD_BACK_TO_INDEX) ) {
         return new HREF(this, name, null);
      } else if ( getPageTitleModel().isChildSupported(name) ) {
         return getPageTitleModel().createChild(this,name);
      } else if (getPropertySheetModel().isChildSupported(name) ) {
         return getPropertySheetModel().createChild(this,name);
      } else if( name.equals( CHILD_ADD_LOG_FILTER_BUTTON ) 
                 || name.equals( CHILD_REMOVE_LOG_FILTER_BUTTON )) {
         return new CCButton(this,name, null);
      } else {
         return null;
      }
   }
   
   
   private OptionsPropertySheetModel getPropertySheetModel() {
      return (OptionsPropertySheetModel)RequestManager.getRequestContext().getModelManager().getModel( OptionsPropertySheetModel.class );
   }
   private OptionsPageTitleModel getPageTitleModel() {
      return (OptionsPageTitleModel)RequestManager.getRequestContext().getModelManager().getModel( OptionsPageTitleModel.class );
   }
   
   
   
   public static LoggingModel getLoggingModel() {  
      
      ModelManager mm = RequestManager.getRequestContext().getModelManager();
      
      return (LoggingModel)mm.getModel(LoggingModel.class, ATTR_LOGGING, true, true );
      
   }
   
   
   public void handleSaveButtonRequest(RequestInvocationEvent event)
   throws ServletException, IOException, ModelControlException {
      
      
      try {
         CCActionTable filterTable = (CCActionTable)getChild( OptionsPropertySheetModel.CHILD_FILTER_VALUE );
         filterTable.restoreStateData();
         
         LoggingModel loggingModel = getLoggingModel();
         ArcoServlet.getCurrentInstance().setLogging(getLoggingModel().getLogging());
         loggingModel.setLogging( ArcoServlet.getCurrentInstance().getLogging() );
         
         info("options.saved");
      } catch( ArcoException ae ) {
         error("options.saveError", ae );
      } catch( Exception e ) {
         error("Save options failed: {0}", new Object [] { e.getMessage() } );
      }
      
      forwardTo(event.getRequestContext());
   }
   
   
   public void handleAddLogFilterButtonRequest(RequestInvocationEvent event) {
      
      getLoggingModel().addNewLogFilter();
      
      this.forwardTo(event.getRequestContext());
   }
   
   public void handleRemoveLogFilterButtonRequest(RequestInvocationEvent event) 
      throws ModelControlException {
      SGELog.info("remove LogFilter" );
      
      getFilterTable().restoreStateData();
      
      
      OptionsPropertySheetModel opm = getPropertySheetModel();
      LogFilterTableModel lftm = opm.getLogFilterTableModel();
      
      Integer [] selectedRows = lftm.getSelectedRows();
      
      getLoggingModel().removeLogFilter(selectedRows);
      
      this.forwardTo(event.getRequestContext());
   }
   
   public void handleBackToIndexHrefRequest(RequestInvocationEvent event)
   throws ServletException, IOException {
      getSession().removeAttribute(ATTR_LOGGING);
      getViewBean(IndexViewBean.class).forwardTo(event.getRequestContext());
   }

   private CCActionTable getFilterTable() {
      return (CCActionTable)getChild( OptionsPropertySheetModel.CHILD_FILTER_VALUE );
   }
   
}
