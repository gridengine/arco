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

import java.io.*;
import javax.servlet.*;
import com.iplanet.jato.model.ModelControlException;
import com.iplanet.jato.RequestManager;
import com.iplanet.jato.view.*;
import com.iplanet.jato.view.event.*;
import com.iplanet.jato.view.html.*;
import com.sun.grid.arco.ArcoException;
import com.sun.web.ui.model.*;
import com.sun.web.ui.view.breadcrumb.CCBreadCrumbs;
import com.sun.web.ui.view.tabs.CCTabs;
import com.sun.web.ui.view.tabs.CCNodeEventHandlerInterface;
import com.sun.web.ui.view.html.CCHiddenField;
import com.sun.web.ui.view.pagetitle.*;
import com.sun.grid.arco.QueryManager;
import com.sun.grid.arco.QueryResult;
import com.sun.grid.arco.QueryResultException;
import com.sun.grid.arco.ResultManager;
import com.sun.grid.arco.sql.*;
import com.sun.grid.arco.model.*;
import com.sun.grid.arco.validator.ValidatorError;
import com.sun.grid.logging.SGELog;
import com.sun.web.ui.view.html.CCStaticTextField;
import com.sun.grid.arco.web.arcomodule.query.ViewTab;
import com.sun.grid.arco.web.arcomodule.query.FilterTableModel;
import com.sun.grid.arco.web.arcomodule.query.QueryPageTitleModel;
import com.sun.grid.arco.web.arcomodule.query.QueryTabsModel;
import com.sun.grid.arco.web.arcomodule.query.CommonTab;
import com.sun.grid.arco.web.arcomodule.query.FieldTableModel;
import com.sun.grid.arco.web.arcomodule.query.SQLTab;
import com.sun.grid.arco.web.arcomodule.query.SimpleTab;
import com.sun.grid.arco.xml.XMLQueryResult;

public class QueryViewBean extends BaseViewBean 
       implements CCNodeEventHandlerInterface {

   public static final String PAGE_NAME = "Query";
   public static final String SELECTED_NODE = "selectedNode";
   
   public static final String DEFAULT_DISPLAY_URL=
           "/jsp/arcomodule/Query.jsp";
   
   public static final String CHILD_TABS             = "Tabs";
   public static final String CHILD_PAGETITLE        = "PageTitle";
   public static final String CHILD_BREADCRUMB       = "BreadCrumb";
   public static final String CHILD_BACK_TO_INDEX    = "BackToIndexHref";
   public static final String CHILD_SQL_TAB          = "SQLTab";
   public static final String CHILD_COMMON_TAB       = "CommonTab";
   public static final String CHILD_SIMPLE_TAB       = "SimpleTab";
   public static final String CHILD_VIEW_TAB         = "ViewTab";
   public static final String CHILD_SAVEAS_QUERY_NAME = "SaveAsQueryName";
   public static final String CHILD_SAVEAS_PROMPT    = "SaveAsPrompt";
   public static final String CHILD_LAST_VISITED_TAB = "LastVisitedTab";
   public static final String CHILD_BACK_TO_SIMPLE    = "BackToSimpleHRef";

   public static final String CHILD_SET_DIRTY_JAVASCRIPT = "setDirtyJavaScript";
   
   public static final String PAGE_TITLE = "query.title";
   
   /** Creates a new instance of QueryViewBean */
   public QueryViewBean() {
      super(PAGE_NAME, DEFAULT_DISPLAY_URL);
   }
   
   // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
   // Child manipulation methods
   // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
   
   /**
    * Register each child view.
    */
   public void registerNewChildren() {
      registerChild(CHILD_TABS, CCTabs.class);
      registerChild(CHILD_PAGETITLE, CCPageTitle.class);
      registerChild(CHILD_BREADCRUMB, CCBreadCrumbs.class );
      registerChild(CHILD_BACK_TO_INDEX, HREF.class);
      registerChild(CHILD_BACK_TO_SIMPLE, HREF.class);
      registerChild(CHILD_SQL_TAB, SQLTab.class);
      registerChild(CHILD_COMMON_TAB, CommonTab.class);
      registerChild(CHILD_SIMPLE_TAB, SimpleTab.class); 
      registerChild(CHILD_VIEW_TAB, ViewTab.class); 
      registerChild(CHILD_SAVEAS_QUERY_NAME, CCHiddenField.class );
      registerChild(CHILD_LAST_VISITED_TAB, CCHiddenField.class ); 
      registerChild(CHILD_SET_DIRTY_JAVASCRIPT,CCStaticTextField.class);
      registerChild(CHILD_SAVEAS_PROMPT, CCStaticTextField.class);
      getPageTitleModel().registerChildren(this);
   }

   private QueryPageTitleModel getPageTitleModel() {
      return (QueryPageTitleModel)RequestManager.getRequestContext().getModelManager().getModel( QueryPageTitleModel.class );
   }
   
   private QueryTabsModel getTabsModel() {      
      return (QueryTabsModel)RequestManager.getRequestContext().getModelManager().getModel( QueryTabsModel.class);
   }

    
   /**
    * Instantiate each child view.
    *
    * @param name The child view name.
    * @return The View object.
    */
   public View newChild(String name) {
      if (name.equals(CHILD_TABS)) {
         // Tabs
         CCTabs child = new CCTabs(this, getTabsModel(), name);
         return child;
      } else if (name.equals(CHILD_PAGETITLE)) {
        CCPageTitleModel model = getPageTitleModel();
        model.setPageTitleText( ArcoServlet.getQueryModel().getQuery().getName() );        
        return new CCPageTitle(this,model,name);
      } else if (name.equals(CHILD_BREADCRUMB)) {

         QueryModel queryModel = ArcoServlet.getQueryModel();         
         CCBreadCrumbsModel model = null;
         
         if( queryModel.isResult() ) {
             model = new CCBreadCrumbsModel("query.result");
         }
         else if( queryModel.isAdvanced() ) {
             model = new CCBreadCrumbsModel("query.advancedQuery");
         } else {
             model = new CCBreadCrumbsModel("query.simpleQuery");
         }
         
         model.appendRow();
         model.setValue(CCBreadCrumbsModel.COMMANDFIELD, CHILD_BACK_TO_INDEX);
         model.setValue(CCBreadCrumbsModel.LABEL, IndexViewBean.PAGE_TITLE );
         
         if( queryModel.isConvertedFromSimpleQuery() ) {
            model.appendRow();
            model.setValue(CCBreadCrumbsModel.COMMANDFIELD, CHILD_BACK_TO_SIMPLE);
            model.setValue(CCBreadCrumbsModel.LABEL, "query.simpleQuery" );
         }

         return new CCBreadCrumbs(this, model, name);
      } else if ( name.equals(CHILD_BACK_TO_INDEX) ) {
         return new HREF(this, name, null);
      } else if ( name.equals(CHILD_BACK_TO_SIMPLE) ) {
         return new HREF(this, name, null);
      } else if ( name.equals( CHILD_SQL_TAB )) {
         return new SQLTab(this,name);
      } else if ( name.equals( CHILD_COMMON_TAB )) {
         return new CommonTab(this,name);
      } else if ( name.equals( CHILD_SIMPLE_TAB )) {
         return new SimpleTab(this,name);
      } else if ( name.equals( CHILD_VIEW_TAB )) {
         return new ViewTab(this,name);
      } else if ( name.equals(CHILD_SAVEAS_QUERY_NAME )) {
         return new CCHiddenField(this,name,null);
      } else if ( name.equals(CHILD_SAVEAS_PROMPT)) {
         
         QueryModel queryModel = ArcoServlet.getQueryModel(); 
         
         StringBuffer text = new StringBuffer();
         text.append("var prompt_result = prompt(\"Enter the new ");
         if(queryModel.isResult()) {
            text.append("result");
         } else {
            text.append("query");
         }
         text.append(" name:\", \"\");");
         CCStaticTextField ret = new CCStaticTextField(this, name, text.toString());
         ret.setEscape(false);
         return ret;
      } else if ( name.equals(CHILD_LAST_VISITED_TAB )) {
         return new CCHiddenField(this,name,Integer.toString(QueryTabsModel.TAB_COMMON));
      } else if ( name.equals(CHILD_SET_DIRTY_JAVASCRIPT)) {
         return new CCStaticTextField(this,name,null);
      } else if ( getPageTitleModel().isChildSupported(name) ) {
         return getPageTitleModel().createChild(this,name);
      } else {
         return null;
      }
   }
   
   /**
    * Event handler for the tabs
    *
    * @param event The request invocation event
    * @param value The id of the tab that was specified when the tab
    * was created.
    */
   public void nodeClicked(RequestInvocationEvent event, int id) {
      
//      ViewTab viewTab = (ViewTab)getChild(CHILD_VIEW_TAB);
//      viewTab.updateModel();
      
      ArcoServlet.getQueryModel().validate();
      getTabsModel().setSelectedNode(id);
      setLastVisitedTab(id);
      
      

      forwardTo(getRequestContext());
   }
   
   private void setLastVisitedTab(int tab) {
      String value = Integer.toString(tab);
      setPageSessionAttribute(CHILD_LAST_VISITED_TAB, value);
      SGELog.fine("last visited tab is {0}", value );
      setDisplayFieldValue(CHILD_LAST_VISITED_TAB, value);
   }
   
   private int getLastVisitedTab() {
      String value = (String)getPageSessionAttribute(CHILD_LAST_VISITED_TAB);
      if( value == null ) {
         return QueryTabsModel.TAB_COMMON;
      } else {
         return Integer.parseInt(value);
      }
   }
   
    /**
     * Request handler for SaveButton
     */
    public void handleSaveButtonRequest(RequestInvocationEvent event)
	    throws ServletException, IOException, ModelControlException {
       
       try {
          QueryModel model = ArcoServlet.getQueryModel();

          model.validate();

          if( !model.hasErrors() ) {

             if( model.isResult() ) {
                ResultManager resultManager = ArcoServlet.getCurrentInstance().getResultManager();

                QueryType query = model.getQuery();

                resultManager.saveResult((Result)query );

                info("result.saved", new Object[] { model.getQuery().getName() } );
             } else {
                QueryManager queryManager = ArcoServlet.getCurrentInstance().getQueryManager();

                QueryType query = model.getQuery();

                queryManager.saveQuery((Query)query );

                info("query.saved", new Object[] { model.getQuery().getName() } );
             }

             ((FieldTableModel)getModel(FieldTableModel.class)).reinit();
             ((FilterTableModel)getModel(FilterTableModel.class)).reinit();
          }
       } catch( ArcoException ae ) {
          error("query.saveError", ae );
       }
       forwardTo(getRequestContext());
    }

    /**
     * Request handler for SaveAsButton
     */
    public void handleSaveAsButtonRequest(RequestInvocationEvent event)
	    throws ServletException, IOException, ModelControlException {
       
       try {
       
          QueryModel queryModel = ArcoServlet.getQueryModel();
          queryModel.validate();

          if( !queryModel.hasErrors() ) {
             String saveAsQueryName = (String)getDisplayFieldValue(CHILD_SAVEAS_QUERY_NAME);

             if( saveAsQueryName == null || saveAsQueryName.length() == 0 ) {
                warning("query.invalidQueryName");
             } else {
                
                if( queryModel.isResult() ) {
                   ResultManager resultManager = ArcoServlet.getCurrentInstance().getResultManager();
                   
                   // We need to synchronize the save as to avoid that two user
                   // uses the same name
                   synchronized( resultManager ) {
                      if( resultManager.getResultByName(saveAsQueryName) != null ) {          
                          warning( "result.saveas.exits", new Object[] { saveAsQueryName } );
                      } else {

                         queryModel.setValue("/name",saveAsQueryName);                
                         resultManager.saveResult((Result)queryModel.getQuery());

                         info("result.saved", new Object[] { saveAsQueryName} );
                      }
                   }
                } else {
                   QueryManager queryManager = ArcoServlet.getCurrentInstance().getQueryManager();

                   // We need to synchronize the save as to avoid that two user
                   // uses the same name
                   synchronized( queryManager ) {
                      if( queryManager.getQueryByName(saveAsQueryName) != null ) {          
                          warning( "query.saveas.exists", new Object[] { saveAsQueryName } );
                      } else {

                         queryModel.setValue("/name",saveAsQueryName);                
                         queryManager.saveQuery((Query)queryModel.getQuery());

                         info("query.saved", new Object[] { saveAsQueryName} );
                      }
                   }
                }
             }
          }
       } catch( ArcoException ae ) {
          error("query.saveError", ae);
       }
       forwardTo(getRequestContext());
    }
    
    /**
     * Request handler for ResetButton
     */
    public void handleResetButtonRequest(RequestInvocationEvent event) {
       
       try {
          QueryModel model = ArcoServlet.getQueryModel();
          
          if( model.isResult() ) {
             ResultManager resultManager = ArcoServlet.getCurrentInstance().getResultManager();
             model.setQuery( resultManager.getResultByName(model.getQuery().getName() ));
          } else {
             QueryManager queryManager = ArcoServlet.getCurrentInstance().getQueryManager();
             model.setQuery( queryManager.getQueryByName( model.getQuery().getName() ));
          }
          ((FieldTableModel)getModel(FieldTableModel.class)).reinit();
          ((FilterTableModel)getModel(FilterTableModel.class)).reinit();
       } catch( ArcoException ae ) {
          error("query.ResetError", ae );
       }
       forwardTo(getRequestContext());
    }
    
    /**
     * Request handler for SaveButton
     */
    public void handleRunButtonRequest(RequestInvocationEvent event) {
       
       QueryModel queryModel = ArcoServlet.getQueryModel();
       queryModel.validate();
       
       if( !queryModel.hasErrors() ) {  
          QueryResult queryResult = null;
          if( queryModel.isResult() ) {
             queryResult = new XMLQueryResult((Result)queryModel.getQuery());
          } else {
             queryResult = new SQLQueryResult(queryModel.getQuery(), 
                     ArcoServlet.getCurrentInstance().getConnectionPool());
          }
          executeQuery(this, event, queryResult );
       } else {
          forwardTo(event.getRequestContext());
       }         
    }
    
    public void handleToAdvancedButtonRequest(RequestInvocationEvent event) {
       
//      ViewTab viewTab = (ViewTab)getChild(CHILD_VIEW_TAB);
//      viewTab.updateModel();
       
       QueryModel queryModel = ArcoServlet.getQueryModel();
       queryModel.validate();
       
       if( !queryModel.hasErrors() ) {                      
           try {
              queryModel.toAdvanced();
           } catch( java.text.ParseException pe ) {
              error("query.sqlError", new Object[] {pe.getMessage()});
           }
       }
       forwardTo(event.getRequestContext());
    }
    
    public void handleBackToSimpleHRefRequest(RequestInvocationEvent event) {
       ArcoServlet.getQueryModel().toSimple();
       forwardTo(event.getRequestContext());
    }
    
    public static void executeQuery(BaseViewBean viewBean, RequestInvocationEvent event,
                                    QueryResult queryResult ) {
       
          boolean calledFromQuery = viewBean instanceof QueryViewBean;
          boolean calledFromLateBinding = viewBean instanceof LateBindingViewBean;
          
          //clear any previous model
          ArcoServlet.clearResultModel();
          
          ResultModel resultModel = ArcoServlet.getResultModel();
          
          resultModel.setQueryResult(queryResult);          
          
          if( queryResult.hasLateBinding() && !calledFromLateBinding ) {
             
             LateBindingViewBean lbvb = (LateBindingViewBean) viewBean.getViewBean(LateBindingViewBean.class);
             lbvb.setCalledFromQueryViewBean(calledFromQuery);
             lbvb.forwardTo(event.getRequestContext());
          } else {
             ResultViewBean rvb = (ResultViewBean)viewBean.getViewBean(ResultViewBean.class);

             if( calledFromLateBinding ) {
                rvb.setCalledFromQueryViewBean( 
                   ((LateBindingViewBean)viewBean).isCalledFromQueryViewBean()
                );
             } else {
                rvb.setCalledFromQueryViewBean(calledFromQuery);
             }

             try {
                queryResult.execute();

                rvb.forwardTo(event.getRequestContext());

             } catch( QueryResultException qre ) {
                viewBean.error(qre.getMessage(), qre.getParameter());
                viewBean.forwardTo(event.getRequestContext());
             }
          }
    }
    
    public void handleBackToIndexHrefRequest(RequestInvocationEvent event)
       throws ServletException, IOException
    {
       getViewBean(IndexViewBean.class).forwardTo(event.getRequestContext());
    }
   
    public boolean beginSqlTabContentDisplay(ChildContentDisplayEvent event) {
       return getTabsModel().isSqlTabSelected();
    }
    
    public String endSqlTabContentDisplay(ChildContentDisplayEvent event) {
       if ( getTabsModel().isSqlTabSelected() ) {
          return event.getContent();
       }
       return null;
    }
    
    public boolean beginCommonTabContentDisplay(ChildContentDisplayEvent event) {
       return getTabsModel().isCommonTabSelected();
    }
    
    public String endCommonTabContentDisplay(ChildContentDisplayEvent event) {
       if ( getTabsModel().isCommonTabSelected() ) {
          return event.getContent();
       }
       return null;
    }

    public boolean beginSimpleTabContentDisplay(ChildContentDisplayEvent event) {
       return getTabsModel().isSimpleTabSelected();
    }
    
    public String endSimpleTabContentDisplay(ChildContentDisplayEvent event) {
       if ( getTabsModel().isSimpleTabSelected() ) {
          return event.getContent();
       }
       return null;
    }

   public void beginDisplay(DisplayEvent displayEvent) throws ModelControlException {

      CCStaticTextField setDirtyJavaScript = (CCStaticTextField)getChild(CHILD_SET_DIRTY_JAVASCRIPT);
      
      if( getPageTitleModel().canSave() ) {         
         setDirtyJavaScript.setValue(
            "ccSetButtonDisabled('Query."+QueryPageTitleModel.CHILD_SAVE_BUTTON+"', 'arcoForm', false);" +
            "ccSetButtonDisabled('Query."+QueryPageTitleModel.CHILD_RESET_BUTTON+"', 'arcoForm', false);"   
         );
      } else {
         setDirtyJavaScript.setValue(
            "ccSetButtonDisabled('Query"+QueryPageTitleModel.CHILD_RESET_BUTTON+"', 'arcoForm', false);"
         );
      }      
   
      QueryModel queryModel = ArcoServlet.getQueryModel();
      
      if( queryModel.hasErrors() ) {
         ValidatorError [] errors = queryModel.getErrors();
         error(errors[0].getMessage(), errors[0].getParams());
      }
      
      if (queryModel.hasWarnings() ) {
         ValidatorError [] warnings = queryModel.getWarnings();
         warning(warnings[0].getMessage(), warnings[0].getParams());
      }
      
      super.beginDisplay(displayEvent);
   }
    
    public boolean beginViewTabContentDisplay(ChildContentDisplayEvent event) {
       return getTabsModel().isViewTabSelected();
    }
    
    public String endViewTabContentDisplay(ChildContentDisplayEvent event) {
       if ( getTabsModel().isViewTabSelected() ) {
          return event.getContent();
       }
       return null;
    }
    
}
