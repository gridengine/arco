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

import java.util.*;
import com.iplanet.jato.view.View;
import com.iplanet.jato.view.html.*;
import com.iplanet.jato.view.event.*;
import com.iplanet.jato.RequestManager;
import com.iplanet.jato.model.ModelControlException;
import com.sun.web.ui.model.*;
import com.sun.web.ui.view.html.*;
import com.sun.web.ui.view.pagetitle.CCPageTitle;
import com.sun.web.ui.view.breadcrumb.CCBreadCrumbs;
import com.sun.web.ui.view.propertysheet.CCPropertySheet;

import com.sun.grid.arco.QueryResult;
import com.sun.grid.arco.ResultExportManager;
import com.sun.grid.arco.QueryResultException;
import com.sun.grid.arco.model.*;
import com.sun.grid.logging.SGELog;
import java.util.logging.Level;
import com.sun.grid.arco.web.arcomodule.result.LateBindingPropertySheetModel;
import com.sun.grid.arco.web.arcomodule.result.LateBindingPageTitleModel;

public class LateBindingViewBean extends BaseViewBean {
  
   public static final String PAGE_NAME = "LateBinding";
   public static final String DEFAULT_DISPLAY_URL = "/jsp/arcomodule/LateBinding.jsp";
   
   public static final String CHILD_BREADCRUMB       = "BreadCrumb";
   public static final String CHILD_BACK_TO_INDEX    = "BackToIndexHref";
   public static final String CHILD_BACK_TO_QUERY    = "BackToQueryHref";
   public static final String CHILD_PAGETITLE        = "PageTitle";
   
   public static final String CHILD_PROP_SHEET       = "PropertySheet";
   public static final String CHILD_CALLED_FROM_QUERY = "calledFromQuery";
   
   private Boolean calledFromQueryViewBean;
   
   /** Creates a new instance of ResultViewBean */
   public LateBindingViewBean() {
      super(PAGE_NAME,DEFAULT_DISPLAY_URL);
   }

   protected com.iplanet.jato.view.View newChild(String name) {
      if (name.equals(CHILD_PAGETITLE)) {
        return new CCPageTitle(this,getPageTitleModel(),name);
      } else if (name.equals(CHILD_BREADCRUMB)) {
         
         CCBreadCrumbsModel model =
                 new CCBreadCrumbsModel("latebinding.title");
         
         model.appendRow();
         model.setValue(CCBreadCrumbsModel.COMMANDFIELD, CHILD_BACK_TO_INDEX);
         model.setValue(CCBreadCrumbsModel.LABEL, IndexViewBean.PAGE_TITLE );
         
         if( isCalledFromQueryViewBean() ) {
            model.appendRow();
            model.setValue(CCBreadCrumbsModel.COMMANDFIELD, CHILD_BACK_TO_QUERY);
            model.setValue(CCBreadCrumbsModel.LABEL, QueryViewBean.PAGE_TITLE );
         }         

         return new CCBreadCrumbs(this, model, name);
      } else if ( name.equals(CHILD_BACK_TO_INDEX) ) {
         return new HREF(this, name, null);
      } else if ( name.equals(CHILD_BACK_TO_QUERY) ) {
         return new HREF(this, name, null);
      } else if ( name.equals(CHILD_PROP_SHEET)) {
         return new CCPropertySheet(this,getPropertySheetModel(),name);
      } else if ( name.equals( CHILD_CALLED_FROM_QUERY )) {
         return new CCHiddenField(this,name, calledFromQueryViewBean );
      } else if ( getPropertySheetModel().isChildSupported(name)) {
         return getPropertySheetModel().createChild(this,name);
      } else if ( getPageTitleModel().isChildSupported(name)) {
         return getPageTitleModel().createChild(this,name);
      } else {      
         return null;
      }
   }
   
   
   protected LateBindingPropertySheetModel getPropertySheetModel() {
      return (LateBindingPropertySheetModel)RequestManager.getRequestContext().getModelManager().getModel(LateBindingPropertySheetModel.class);      
   }
   
   
   protected LateBindingPageTitleModel getPageTitleModel() {
      return (LateBindingPageTitleModel)RequestManager.getRequestContext().getModelManager().getModel(LateBindingPageTitleModel.class);      
   }

   
   protected void registerNewChildren() {
      registerChild(CHILD_PAGETITLE, CCPageTitle.class);
      registerChild(CHILD_BREADCRUMB, CCBreadCrumbs.class );
      registerChild(CHILD_BACK_TO_INDEX, HREF.class);
      registerChild(CHILD_BACK_TO_QUERY, HREF.class);
      registerChild(CHILD_PROP_SHEET, CCPropertySheet.class);
      registerChild(CHILD_CALLED_FROM_QUERY, CCHiddenField.class);
      getPropertySheetModel().registerChildren(this);
      getPageTitleModel().registerChildren(this);
   }

    public void handleBackToIndexHrefRequest(RequestInvocationEvent event) {
       getViewBean(IndexViewBean.class).forwardTo(event.getRequestContext());
    }
    
    public void handleBackToQueryHrefRequest(RequestInvocationEvent event) {
       getViewBean(QueryViewBean.class).forwardTo(event.getRequestContext());
    }

   public boolean isCalledFromQueryViewBean() {
      if( calledFromQueryViewBean == null ) {
         CCHiddenField hf = (CCHiddenField)getChild(CHILD_CALLED_FROM_QUERY);
         calledFromQueryViewBean = Boolean.valueOf((String)hf.getValue());
      }
      return calledFromQueryViewBean.booleanValue();
   }

   public void setCalledFromQueryViewBean(boolean calledFromQueryViewBean) {
      if( calledFromQueryViewBean ) {
         this.calledFromQueryViewBean = Boolean.TRUE;
      } else {
         this.calledFromQueryViewBean = Boolean.FALSE;
      }
   }
   
   /**
    * Advaced query late binding Run button request
    * @param event run event
    */
   public void handleRunButtonRequest(RequestInvocationEvent event) {
      
      QueryResult queryResult = ArcoServlet.getResultModel().getQueryResult();
      
      //TODO -PJ- there is a null query, something is not initialised
      QueryType query = queryResult.getQuery();
      
      Iterator iter = query.getFilter().iterator();
      Filter filter = null;
      LateBindingPropertySheetModel propModel = getPropertySheetModel();
      while( iter.hasNext() ) {
         filter = (Filter)iter.next();

         queryResult.setLateBinding(filter.getName(), 
                                    propModel.getValue( filter.getName()));
      }
      QueryViewBean.executeQuery(this, event,  queryResult );
      
   }
   
    public void handleEditButtonRequest(RequestInvocationEvent event) {
       
       if( !isCalledFromQueryViewBean() ) {
          QueryModel queryModel = ArcoServlet.getQueryModel();       
          ResultModel resultModel = ArcoServlet.getResultModel();
          
          queryModel.setQuery(resultModel.getQueryResult().getQuery());
       }
       getViewBean(QueryViewBean.class).forwardTo(event.getRequestContext());
    }   
   
}
