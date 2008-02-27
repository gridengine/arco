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

import com.iplanet.jato.RequestManager;
import java.io.*;
import javax.servlet.*;
import com.iplanet.jato.model.*;
import com.iplanet.jato.view.*;
import com.iplanet.jato.view.event.*;
import com.sun.grid.arco.ArcoException;
import com.sun.web.ui.view.html.*;
import com.sun.web.ui.view.table.*;

import com.sun.grid.logging.SGELog;
import com.sun.grid.arco.model.*;
import com.sun.grid.arco.QueryManager;
import com.sun.grid.arco.QueryValidator;
import com.sun.grid.arco.sql.ArcoClusterModel;
import com.sun.grid.arco.sql.SQLQueryResult;
import com.sun.grid.arco.validator.DefaultQueryStateHandler;
import com.sun.grid.arco.validator.ValidatorError;

public class QueryListView extends NamedObjectListView {
   
   public static final String CHILD_RUN_BUTTON_NAME_TEXT    = "RunButtonNameText";
   public static final String CHILD_EDIT_BUTTON_NAME_TEXT   = "EditButtonNameText";
   public static final String CHILD_DELETE_BUTTON_NAME_TEXT = "DeleteButtonNameText";
   
   public static final String CHILD_DYNAMIC_ENABLE_BUTTONS_TEXT = "dynamicEnableButtons";
   
   /** Creates a new instance of QueryListView 
    *  @param  parent   the parent view component
    *  @param  name     name of the view
    */
   public QueryListView( View parent, String name ) {
      super(parent, name, new QueryTableModel() );
      registerChildren();
   }
   
    protected void registerChildren() {
       super.registerChildren();
       registerChild(CHILD_RUN_BUTTON_NAME_TEXT, CCStaticTextField.class);
       registerChild(CHILD_EDIT_BUTTON_NAME_TEXT, CCStaticTextField.class);
       registerChild(CHILD_DELETE_BUTTON_NAME_TEXT, CCStaticTextField.class);
       registerChild(CHILD_DYNAMIC_ENABLE_BUTTONS_TEXT, CCStaticTextField.class);
    }
   
    protected View createChild(String name) {
       if ( name.equals( CHILD_RUN_BUTTON_NAME_TEXT ) ) {
          return new CCStaticTextField( this, name, 
                                        getChild( QueryTableModel.CHILD_RUN_BUTTON ).getQualifiedName() );
       } else if ( name.equals( CHILD_EDIT_BUTTON_NAME_TEXT ) ) {
          return new CCStaticTextField( this, name, 
                                        getChild( QueryTableModel.CHILD_EDIT_BUTTON ).getQualifiedName() );
       } else if ( name.equals( CHILD_DELETE_BUTTON_NAME_TEXT ) ) {
          return new CCStaticTextField( this, name, 
                                        getChild( QueryTableModel.CHILD_DELETE_BUTTON ).getQualifiedName() );
       } else if ( name.equals( CHILD_DYNAMIC_ENABLE_BUTTONS_TEXT ) ) {
          
          String [] buttons = null;
          if (ArcoServlet.getCurrentInstance().hasUserWritePermission()) {
            buttons = new String [] { QueryTableModel.CHILD_RUN_BUTTON, QueryTableModel.CHILD_EDIT_BUTTON,
                                      QueryTableModel.CHILD_DELETE_BUTTON };
          } else {
            buttons = new String [] { QueryTableModel.CHILD_RUN_BUTTON, QueryTableModel.CHILD_EDIT_BUTTON };
          }
          
          StringBuffer buffer = new StringBuffer();
          
          buffer.append("var buttons = [");
          for(int i = 0; i < buttons.length; i++) {
             if(i>0) {
                buffer.append(",");
             }
             buffer.append("\"Index.QueryListView.");
             buffer.append(buttons[i]);
             buffer.append('\"');
          }
          buffer.append("];");
          
          CCStaticTextField ret = new CCStaticTextField(this, name, buffer.toString());
          ret.setEscape(false);
          return ret;
       } else {
          View ret = super.createChild( name );
          if( ret == null ) {
             throw new IllegalArgumentException("child with name " + name + " is unkonwn");
          } else {
             return ret;
          }
       }       
    }

   public void handleNameHrefRequest(RequestInvocationEvent event, String name) 
     throws ServletException, IOException, ModelControlException {
     runQuery(event,name);
   }

   public void handleRunButtonRequest(RequestInvocationEvent event)
      throws ServletException, IOException, ModelControlException {            
         CCActionTable child = (CCActionTable) getChild(CHILD_ACTION_TABLE);
         child.restoreStateData();         
         String queryName = getModel().getSelectedObjectName();
         runQuery(event,queryName);
   }   
   
   public void runQuery(RequestInvocationEvent event, String queryName ) {

      try {
         QueryManager queryManager = ArcoServlet.getCurrentInstance().getQueryManager();

         Query query = queryManager.getQueryByName(queryName);

         // Validate the query
         QueryValidator qv = ArcoServlet.getInstance().getValidator();

         DefaultQueryStateHandler qsh = new DefaultQueryStateHandler();

         qv.validate(query,qsh);

         if( qsh.hasErrors() ) {
            ValidatorError [] errors = qsh.getErrors();
            IndexViewBean ivb = (IndexViewBean)getParentViewBean();
            ivb.error("query.validateError", errors[0].getMessage(), errors[0].getParams());
            ivb.forwardTo(event.getRequestContext());
         } else {
            ArcoClusterModel acm = ArcoClusterModel.getInstance(RequestManager.getSession());
            query.setClusterName(acm.getCurrentCluster());
            SQLQueryResult queryResult = new SQLQueryResult(query, ArcoServlet.getCurrentInstance().getConnectionPool());
            QueryViewBean.executeQuery( (BaseViewBean)getParentViewBean(), event, queryResult );
         }
      } catch( ArcoException ae ) {
         IndexViewBean ivb = (IndexViewBean)getParentViewBean();
         ivb.error("query.runError", ae);
         ivb.forwardTo(event.getRequestContext());
      }
   }
   
   
   public void handleNewSimpleButtonRequest(RequestInvocationEvent event) {
      

      QueryManager queryManager = ArcoServlet.getCurrentInstance().getQueryManager();

      Query query = queryManager.createSimpleQuery();
      
      QueryModel queryModel = ArcoServlet.getQueryModel();
      queryModel.setQuery(query);
      
      getViewBean(QueryViewBean.class).forwardTo(event.getRequestContext());
   }
    
   public void handleNewAdvancedButtonRequest(RequestInvocationEvent event) {
      

      QueryManager queryManager = ArcoServlet.getCurrentInstance().getQueryManager();

      Query query = queryManager.createAdvancedQuery();
      
      QueryModel queryModel = ArcoServlet.getQueryModel();
      queryModel.setQuery(query);
      
      getViewBean(QueryViewBean.class).forwardTo(event.getRequestContext());
   }
   
   public void handleEditButtonRequest(RequestInvocationEvent event)
      throws ServletException, IOException, ModelControlException {
         
      CCActionTable child = (CCActionTable) getChild(CHILD_ACTION_TABLE);
      child.restoreStateData();         

      String queryName = getModel().getSelectedObjectName();
      
      try {
         Query query = ArcoServlet.getCurrentInstance().getQueryManager().getQueryByName(queryName);

         if( query != null ) {
            ViewBean target = getViewBean(QueryViewBean.class);

            QueryModel queryModel = ArcoServlet.getQueryModel();
            queryModel.setQuery(query);

            SGELog.info("edit {0}", queryName );
            target.forwardTo(getRequestContext());
         } else {
            throw new IllegalStateException("Query " + queryName + " not found" );
         }
      } catch( ArcoException ae ) {
         IndexViewBean ivb = (IndexViewBean)getParentViewBean();
         ivb.error("query.editError", ae);
         ivb.forwardTo(getRequestContext());
      }
   }

   public void handleDeleteButtonRequest(RequestInvocationEvent event)
     throws ServletException, IOException, ModelControlException {
      try {
         CCActionTable child = (CCActionTable) getChild(CHILD_ACTION_TABLE);
         child.restoreStateData();         
         String queryName = getModel().getSelectedObjectName();
         SGELog.info("delete " + queryName );

         QueryManager queryManager = ArcoServlet.getCurrentInstance().getQueryManager();
         queryManager.remove(queryName);      
         getModel().reinit();
         getParentViewBean().forwardTo(getRequestContext());
      } catch( ArcoException ae ) {
         IndexViewBean ivb = (IndexViewBean)getParentViewBean();
         ivb.error("query.deleteError", ae);
         ivb.forwardTo(getRequestContext());
      }
   }

   public void beginDisplay(DisplayEvent event) throws ModelControlException {

      super.beginDisplay(event);
      
      boolean disabled = !ArcoServlet.getCurrentInstance().hasUserWritePermission();
      
      CCButton b = (CCButton)getChild(QueryTableModel.CHILD_DELETE_BUTTON);
      b.setDisabled(disabled);
      
   }

   
}
