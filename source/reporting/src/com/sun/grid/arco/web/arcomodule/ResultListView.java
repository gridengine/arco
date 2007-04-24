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

import com.iplanet.jato.model.*;
import com.iplanet.jato.view.*;
import com.iplanet.jato.view.event.*;
import com.sun.grid.arco.ArcoException;
import com.sun.web.ui.view.html.*;
import com.sun.web.ui.view.table.*;

import com.sun.grid.logging.SGELog;
import com.sun.grid.arco.ResultManager;
import com.sun.grid.arco.QueryResultException;
import com.sun.grid.arco.xml.XMLQueryResult;
import com.sun.grid.arco.model.Result;

public class ResultListView extends NamedObjectListView {
   
   public static final String CHILD_VIEW_BUTTON_NAME_TEXT    = "ViewButtonNameText";
   public static final String CHILD_DELETE_BUTTON_NAME_TEXT = "DeleteButtonNameText";
   public static final String CHILD_DYNAMIC_ENABLE_BUTTONS_TEXT = "dynamicEnableButtons";
   
   /** Creates a new instance of ResultListView 
    *  @param  parent   the parent view component
    *  @param  name     name of the view
    */
   public ResultListView(View parent, String name) {
      super(parent, name, new ResultTableModel() );
      registerChildren();
   }
   
    protected void registerChildren() {
       super.registerChildren();
       registerChild(CHILD_VIEW_BUTTON_NAME_TEXT, CCStaticTextField.class);
       registerChild(CHILD_DELETE_BUTTON_NAME_TEXT, CCStaticTextField.class);
       registerChild(CHILD_DYNAMIC_ENABLE_BUTTONS_TEXT, CCStaticTextField.class);
    }
   
    protected View createChild(String name) {
       if ( name.equals( CHILD_VIEW_BUTTON_NAME_TEXT ) ) {
          return new CCStaticTextField( this, name, 
                                        getChild( ResultTableModel.CHILD_VIEW_BUTTON ).getQualifiedName() );
       } else if ( name.equals( CHILD_DELETE_BUTTON_NAME_TEXT ) ) {
          return new CCStaticTextField( this, name, 
                                        getChild( ResultTableModel.CHILD_DELETE_BUTTON ).getQualifiedName() );
       } else if ( name.equals( CHILD_DYNAMIC_ENABLE_BUTTONS_TEXT ) ) {
          
          String [] buttons = null;
          if (ArcoServlet.getCurrentInstance().hasUserWritePermission()) {
            buttons = new String [] { ResultTableModel.CHILD_VIEW_BUTTON, QueryTableModel.CHILD_DELETE_BUTTON };
          } else {
            buttons = new String [] { ResultTableModel.CHILD_VIEW_BUTTON };
          }
          
          StringBuffer buffer = new StringBuffer();
          
          buffer.append("var buttons = [");
          for(int i = 0; i < buttons.length; i++) {
             if(i>0) {
                buffer.append(",");
             }
             buffer.append("\"Index.ResultListView.");
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
     viewResult(event,name);
   }
   
    
   public void handleViewButtonRequest(RequestInvocationEvent event)
      throws ServletException, IOException, ModelControlException { 
         CCActionTable child = (CCActionTable) getChild(CHILD_ACTION_TABLE);
         child.restoreStateData();         
         String resultName = getModel().getSelectedObjectName();
         viewResult(event, resultName);
   }
   
   public void viewResult(RequestInvocationEvent event, String resultName) {
      try {

         ResultManager rm = ArcoServlet.getCurrentInstance().getResultManager();

         Result result = rm.getResultByName(resultName);

         XMLQueryResult queryResult = new XMLQueryResult(result);

         try {
            queryResult.execute();
//            ArcoServlet.clearResultModel();
            ResultModel resultModel = ArcoServlet.getResultModel();
            resultModel.setQueryResult(queryResult);

            getViewBean(ResultViewBean.class).forwardTo(event.getRequestContext());
         } catch( QueryResultException qre ) {
            BaseViewBean vb = (BaseViewBean)getParentViewBean();
            vb.error(qre.getMessage(), qre.getParameter() );
            vb.forwardTo(event.getRequestContext());
         }
      } catch( ArcoException ae ) {
         BaseViewBean vb = (BaseViewBean)getParentViewBean();
         vb.error("result.viewError", ae );
         vb.forwardTo(event.getRequestContext());
      }
   }
    

   public void handleDeleteButtonRequest(RequestInvocationEvent event)
     throws ServletException, IOException, ModelControlException {
      try {
         CCActionTable child = (CCActionTable) getChild(CHILD_ACTION_TABLE);
         child.restoreStateData();         
         String resultName = getModel().getSelectedObjectName();

         ResultManager rm = ArcoServlet.getCurrentInstance().getResultManager();

         SGELog.fine("delete " + resultName );
         rm.remove(resultName);
         getModel().reinit();
      } catch( ArcoException ae ) {
         IndexViewBean ivb = (IndexViewBean)getParentViewBean();
         ivb.error("result.deleteError", ae );
      }
      getParentViewBean().forwardTo(getRequestContext());
   }
   
   public void beginDisplay(DisplayEvent event) throws ModelControlException {

      super.beginDisplay(event);
      
      boolean disabled = !ArcoServlet.getCurrentInstance().hasUserWritePermission();
      
      CCButton b = (CCButton)getChild(ResultTableModel.CHILD_DELETE_BUTTON);
      b.setDisabled(disabled);
   }   

   
}
