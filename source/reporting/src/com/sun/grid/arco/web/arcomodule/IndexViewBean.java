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
import java.lang.reflect.*;
import java.sql.*;
import java.util.*;
import javax.servlet.*;
import javax.servlet.http.*;

import com.iplanet.jato.*;
import com.iplanet.jato.model.*;
import com.iplanet.jato.model.sql.*;
import com.iplanet.jato.util.*;
import com.iplanet.jato.view.*;
import com.iplanet.jato.view.event.*;
import com.iplanet.jato.view.html.*;

import com.sun.web.ui.common.CCDebug;
import com.sun.web.ui.model.*;
import com.sun.web.ui.view.alert.CCAlertInline;
import com.sun.web.ui.view.breadcrumb.CCBreadCrumbs;
import com.sun.web.ui.view.html.*;
import com.sun.web.ui.view.masthead.CCPrimaryMasthead;
import com.sun.web.ui.view.tabs.CCTabs;
import com.sun.web.ui.view.tabs.CCNodeEventHandlerInterface;
import com.sun.web.ui.view.pagetitle.*;
import com.sun.grid.logging.SGELog;

import com.sun.grid.arco.QueryManager;
import com.sun.grid.arco.model.*;

public class IndexViewBean extends BaseViewBean
   implements CCNodeEventHandlerInterface {
   
   public static final String PAGE_NAME = "Index";

   public static final String PAGE_TITLE = "index.pagetitleText";
   
   public static final String DEFAULT_DISPLAY_URL = "/jsp/arcomodule/Index.jsp";
   
   public static final String CHILD_MASTHEAD            = "Masthead";
   public static final String CHILD_PAGETITLE           = "PageTitle";
   public static final String CHILD_QUERY_LIST_VIEW     = "QueryListView";   
   public static final String CHILD_RESULT_LIST_VIEW    = "ResultListView";
   public static final String CHILD_TABS                 = "Tabs";
   
   private static final int QUERY_LIST_MODE = 1;
   private static final int RESULT_LIST_MODE = 2;
    
    public static final String ATTR_MODE = IndexViewBean.class.getName() + ".mode";
   
   
   private CCPageTitleModel pageTitleModel;   
   private  CCTabsModel tabsModel;
   
   /** Creates a new instance of IndexViewBean */
   public IndexViewBean() {
      super(PAGE_NAME, DEFAULT_DISPLAY_URL);
   }
   
   /**
    * Register each child view.
    */
   protected void registerNewChildren() {
      registerChild(CHILD_MASTHEAD, CCPrimaryMasthead.class);
      registerChild(CHILD_PAGETITLE, CCPageTitle.class);
      registerChild(CHILD_QUERY_LIST_VIEW, QueryListView.class);
      registerChild(CHILD_RESULT_LIST_VIEW, ResultListView.class);
      registerChild(CHILD_TABS, CCTabs.class);
      getPageTitleModel().registerChildren(this);
   }
   
   /**
    * Instantiate each child view.
    *
    * @param name The child view name.
    * @return The View object.
    */
   protected View newChild(String name) {
      if (name.equals(CHILD_MASTHEAD)) {
         // Masthead
         CCPrimaryMasthead child =
                 new CCPrimaryMasthead(this, new CCMastheadModel(), name);
         return child;
      } else if (name.equals(CHILD_PAGETITLE)) {
         return new CCPageTitle(this,getPageTitleModel(),name);
      } else if (name.equals( CHILD_QUERY_LIST_VIEW )) {
         return new QueryListView(this,name);
      } else if (name.equals( CHILD_RESULT_LIST_VIEW )) {
         return new ResultListView(this,name);
      } else if (name.equals( CHILD_TABS )) {
         return new CCTabs(this, getTabsModel(), name);
      } else {
         return getPageTitleModel().createChild(this,name);
      }
   }
   
   private CCPageTitleModel getPageTitleModel() {
      if( pageTitleModel == null ) {
         pageTitleModel = new CCPageTitleModel();
      }
      return pageTitleModel;
   }
   
   private String toolTips = null;
   
   /** Create the tabs model. */
   private CCTabsModel getTabsModel() {
      if (tabsModel == null) {
         tabsModel = new CCTabsModel();
         
         CCNavNode node= new CCNavNode(QUERY_LIST_MODE,null,
                 "index.queryListTab",
                 "index.queryListTab.title",
                 "index.queryListTab.status"
                 );
         tabsModel.addNode( node );

         node = new CCNavNode(RESULT_LIST_MODE,null,
                 "index.resultListTab",
                 "index.resultListTab.title",
                 "index.resultListTab.status"
                 );
         tabsModel.addNode( node );

         tabsModel.setSelectedNode(getMode());
         CCTabs tabs = ((CCTabs) getChild(CHILD_TABS));
         if (tabs != null) {
            tabs.resetStateData();
         }
      }
      return tabsModel;
   }
   
   
   
    public boolean beginResultListContentDisplay(ChildContentDisplayEvent event) {           
      return getMode() == RESULT_LIST_MODE;
    }
    
    public String endResultListContentDisplay(ChildContentDisplayEvent event) {
       if ( getMode() == RESULT_LIST_MODE ) {
          return event.getContent();
       }
       return null;
    }
    

    public boolean beginQueryListContentDisplay(ChildContentDisplayEvent event) {
       return getMode() == QUERY_LIST_MODE; 
    }

    public String endQueryListContentDisplay(ChildContentDisplayEvent event) {
       if ( getMode() == QUERY_LIST_MODE ) {
          return event.getContent();
       }
       return null;
    }
    
    private int getMode() {
       
       HttpServletRequest req = getRequestContext().getRequest();
       Integer ret = (Integer)req.getSession().getAttribute(ATTR_MODE);
       if( ret == null ) {
          return QUERY_LIST_MODE;
       } else {
          return ret.intValue();
       }
    }
    
    public void setMode(int mode) {
       HttpServletRequest req = getRequestContext().getRequest();
       req.getSession().setAttribute(ATTR_MODE, new Integer(mode) );
    }
    
    public void beginDisplay(DisplayEvent event) throws ModelControlException {
       NamedObjectListView nolv = null;

       int mode = getMode();
       switch( mode ) {
          case QUERY_LIST_MODE:
             nolv = (NamedObjectListView)getChild(CHILD_QUERY_LIST_VIEW);
             break;
          case RESULT_LIST_MODE:
             nolv = (NamedObjectListView)getChild(CHILD_RESULT_LIST_VIEW);
             break;
          default:
             throw new IllegalStateException("Unknown mode " + mode );
       }
       
       if( nolv.getError() != null ) {
          error("index.tocError", nolv.getError());
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
      setMode(id);
      forwardTo(getRequestContext());
   }
    
}
