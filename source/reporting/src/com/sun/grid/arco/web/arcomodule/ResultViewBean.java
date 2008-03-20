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

import com.iplanet.jato.view.html.*;
import com.iplanet.jato.view.event.*;
import com.iplanet.jato.RequestManager;
import com.iplanet.jato.model.ModelControlException;
import com.sun.grid.arco.ArcoException;
import com.sun.web.ui.model.*;
import com.sun.web.ui.view.html.*;
import com.sun.web.ui.view.pagetitle.CCPageTitle;
import com.sun.web.ui.view.breadcrumb.CCBreadCrumbs;
import com.sun.web.ui.view.propertysheet.CCPropertySheet;

import com.sun.grid.arco.QueryResult;
import com.sun.grid.arco.QueryResultException;
import com.sun.grid.arco.ResultManager;
import com.sun.grid.arco.web.arcomodule.result.ResultPropertySheetModel;
import com.sun.grid.arco.web.arcomodule.result.ResultPageTitleModel;
import com.sun.grid.arco.model.Result;
import com.sun.grid.arco.sql.ArcoClusterModel;
import com.sun.grid.arco.sql.ArcoDbConnectionPool;
import com.sun.grid.arco.sql.SQLQueryResult;
import com.sun.grid.arco.xml.XMLQueryResult;

public class ResultViewBean extends BaseViewBean {
  
   public static final String PAGE_NAME = "Result";
   public static final String DEFAULT_DISPLAY_URL = "/jsp/arcomodule/Result.jsp";
   
   public static final String CHILD_BREADCRUMB       = "BreadCrumb";
   public static final String CHILD_BACK_TO_INDEX    = "BackToIndexHref";
   public static final String CHILD_BACK_TO_QUERY    = "BackToQueryHref";
   public static final String CHILD_PAGETITLE        = "PageTitle";
 
   public static final String CHILD_CLUSTER_MENU        = "ClusterMenu";   
   public static final String CHILD_CLUSTER_MENU_HREF   = "ClusterMenuHref";   
   public static final String CHILD_CLUSTER_MENU_LABEL  = "ClusterMenuLabel";    
   
   public static final String CHILD_PROP_SHEET       = "ResultPropertySheet";
   
   public static final String CHILD_SAVE_RESULT_NAME = "SaveAsResultName";
   
   public static final String CHILD_PAGE_VIEW_MENU_HREF = "PageViewMenuHref";
   public static final String CHILD_PAGE_VIEW_MENU      = "PageViewMenu";
   
   public static final String CHILD_CALLED_FROM_QUERY = "calledFromQuery";
   
   private Boolean calledFromQueryViewBean;
   
   /** Creates a new instance of ResultViewBean */
   public ResultViewBean() {
      super(PAGE_NAME,DEFAULT_DISPLAY_URL);
   }

   protected com.iplanet.jato.view.View newChild(String name) {
      if (name.equals(CHILD_PAGETITLE)) {        
        return new CCPageTitle(this,getPageTitleModel(),name);
      } else if (name.equals(CHILD_BREADCRUMB)) {
         
         CCBreadCrumbsModel model =
                 new CCBreadCrumbsModel("result.title");
         
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
      } else if ( name.equals(CHILD_PAGE_VIEW_MENU_HREF)) {
         return new CCHref(this,name,null);
      } else if ( name.equals(CHILD_PROP_SHEET)) {
         return new CCPropertySheet(this,getPropertySheetModel(),name);
      } else if ( name.equals(CHILD_SAVE_RESULT_NAME)){
         return new CCHiddenField(this,name,null);
      } else if ( name.equals(CHILD_CLUSTER_MENU)){
         return new CCDropDownMenu(this,name,null);
      } else if ( name.equals(CHILD_CLUSTER_MENU_HREF)){
         return new HREF(this,name,null);
      } else if ( name.equals(CHILD_CLUSTER_MENU_LABEL)){
         return new CCLabel(this,name,null);
      } else if ( name.equals( CHILD_CALLED_FROM_QUERY )) {
         return new CCHiddenField(this,name, calledFromQueryViewBean );
      } else if ( getPropertySheetModel().isChildSupported(name)) {
         return getPropertySheetModel().createChild(this,name);
      } else if( getPageTitleModel().isChildSupported(name)) {
         return getPageTitleModel().createChild(this,name);
      } else {      
         return null;
      }
   }
   
   protected ResultPropertySheetModel getPropertySheetModel() {
      return (ResultPropertySheetModel)RequestManager.getRequestContext().getModelManager().getModel(ResultPropertySheetModel.class);      
   }
   
   protected ResultPageTitleModel getPageTitleModel() {
      return (ResultPageTitleModel)RequestManager.getRequestContext().getModelManager().getModel(ResultPageTitleModel.class);      
   }
   
   
   
   public void beginDisplay(DisplayEvent evt) throws ModelControlException {
      super.beginDisplay(evt);


      ResultPropertySheetModel propModel = getPropertySheetModel();

      if( propModel.hasPivot() ) {

            String pivotText = propModel.getPivotHTML(getRequestContext().getRequest().getLocale());

            CCStaticTextField pivotField = (CCStaticTextField)getChild(ResultPropertySheetModel.CHILD_PIVOT);
            pivotField.setValue(pivotText);
      }  
      
       boolean hasWritePermission = ArcoServlet.getCurrentInstance().hasUserWritePermission();
       
       CCButton saveButton = (CCButton)getChild(getPageTitleModel().CHILD_SAVE_BUTTON);
       
       saveButton.setDisabled( !hasWritePermission);
   }
      
   public boolean beginChildDisplay(ChildDisplayEvent event) throws ModelControlException {
      final String childName = event.getChildName();

      if (childName.equals(CHILD_CLUSTER_MENU)) {
         // Fill option list
         CCDropDownMenu clusterMenu = (CCDropDownMenu) getChild(childName);
         final ArcoDbConnectionPool pool = ArcoServlet.getInstance().getConnectionPool();
         clusterMenu.setOptions(pool.getOptionList());
         //Select current cluster option 
         ArcoClusterModel acm = ArcoClusterModel.getInstance(getSession());
         clusterMenu.setValue(acm.getCurrentCluster());
         //Disable change cluster combo for XMLQueryResults
         ResultModel resultModel = ArcoServlet.getResultModel();
         clusterMenu.setDisabled(resultModel.getQueryResult() instanceof XMLQueryResult);
      }

      return super.beginChildDisplay(event);
   }

   protected void registerNewChildren() {
      registerChild(CHILD_PAGETITLE, CCPageTitle.class);
      registerChild(CHILD_BREADCRUMB, CCBreadCrumbs.class );
      registerChild(CHILD_BACK_TO_INDEX, HREF.class);
      registerChild(CHILD_BACK_TO_QUERY, HREF.class);
      registerChild(CHILD_PROP_SHEET, CCPropertySheet.class);
      registerChild(CHILD_SAVE_RESULT_NAME, CCHiddenField.class);
      registerChild(CHILD_CLUSTER_MENU, CCDropDownMenu.class);
      registerChild(CHILD_CLUSTER_MENU_HREF, CCHref.class);
      registerChild(CHILD_CLUSTER_MENU_LABEL, CCLabel.class);
      registerChild(CHILD_PAGE_VIEW_MENU_HREF,CCHref.class);
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


    public void handlePageViewMenuHrefRequest(RequestInvocationEvent event) { 
       
       String type = (String)getDisplayFieldValue(CHILD_PAGE_VIEW_MENU);
       
       if( "HTML".equals(type) ) {
          String saveAsResultName = (String)getDisplayFieldValue(CHILD_SAVE_RESULT_NAME);
         if( saveAsResultName == null || saveAsResultName.length() == 0 ) {
            //Just view, user select nothing
            getViewBean(ResultViewBean.class).forwardTo(event.getRequestContext());
          } else {
            //JATO calls this handler after javascript reset a Export As combo, 
            //so propagate to save handler, when save view with result name was required
             handleSaveButtonRequest(event);
          }
       } else {
           try {
              com.iplanet.jato.RequestContext ctx = event.getRequestContext();
              
              javax.servlet.http.HttpServletRequest req = ctx.getRequest();
              javax.servlet.http.HttpServletResponse resp = ctx.getResponse();
              
              String url = "/export/Result?type=" + type;
              
              javax.servlet.ServletContext sctx = ctx.getServletContext();

              sctx.getRequestDispatcher(url).forward(req, resp);
           } catch( javax.servlet.ServletException sve ) {
              ErrorViewBean evb = (ErrorViewBean)getViewBean(ErrorViewBean.class);
              evb.setError(sve);
              evb.forwardTo(event.getRequestContext());
           } catch (java.io.IOException ioe) {
              ErrorViewBean evb = (ErrorViewBean)getViewBean(ErrorViewBean.class);
              evb.setError(ioe);
              evb.forwardTo(event.getRequestContext());
           }
       }
    }
    
   /**
    * Handler for Cluster select combo
    * @param event a select event
    */
   public void handleClusterMenuHrefRequest(RequestInvocationEvent event) {
      String value = (String) getDisplayFieldValue(CHILD_CLUSTER_MENU);
      ArcoClusterModel acm = ArcoClusterModel.getInstance(getSession());
      acm.setCurrentCluster(value);
      ResultModel resultModel = ArcoServlet.getResultModel();
      QueryResult queryResult = ArcoServlet.getResultModel().getQueryResult();
      queryResult.getQuery().setClusterName(value);
      try {
         queryResult = new SQLQueryResult(queryResult.getQuery(), ArcoServlet.getCurrentInstance().getConnectionPool());
         queryResult.execute();
         resultModel.setQueryResult(queryResult);
         getViewBean(ResultViewBean.class).forwardTo(event.getRequestContext());
      } catch (QueryResultException qre) {
         this.error(qre.getMessage(), qre.getParameter());
         this.forwardTo(event.getRequestContext());
      }
   }
    
    public void handleEditButtonRequest(RequestInvocationEvent event) {
       
       if( !isCalledFromQueryViewBean() ) {
          QueryModel queryModel = ArcoServlet.getQueryModel();       
          ResultModel resultModel = ArcoServlet.getResultModel();
          
          queryModel.setQuery(resultModel.getQueryResult().getQuery());
       }
       getViewBean(QueryViewBean.class).forwardTo(event.getRequestContext());
    }

    public void handleSaveButtonRequest(RequestInvocationEvent event) {
       
       try {
          String saveAsResultName = (String)getDisplayFieldValue(CHILD_SAVE_RESULT_NAME);
          if( saveAsResultName == null || saveAsResultName.length() == 0 ) {
             error("result.invalidResultName");
          } else {

             ResultManager resMan = ArcoServlet.getCurrentInstance().getResultManager();

             if( resMan.getResultByName(saveAsResultName) != null ) {
                error("result.resultExists", new Object [] { saveAsResultName } );
             } else {
                try {

                   ResultModel resultModel = ArcoServlet.getResultModel();
                   QueryResult queryResult = resultModel.getQueryResult();

                   Result result = queryResult.createResult();
                   result.setName(saveAsResultName);
                   
                   //Store the current cluster to result xml
                   result.setClusterName(queryResult.getQuery().getClusterName());
                   ArcoServlet.getCurrentInstance().getResultManager().saveResult(result);

                   info("result.saved", new Object [] {result.getName()});

                } catch( javax.xml.bind.JAXBException jaxbe ) {
                   ErrorViewBean evb = (ErrorViewBean)getViewBean(ErrorViewBean.class);
                   evb.setError(jaxbe);
                   evb.forwardTo(event.getRequestContext());
                   return;
                }
             }
          }
       } catch( ArcoException ae ) {
          error("result.saveError", ae );
       }
       forwardTo(event.getRequestContext());
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
}
