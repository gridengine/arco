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


import com.sun.web.ui.model.*;
import com.sun.web.ui.view.html.*;
import com.sun.grid.arco.web.arcomodule.QueryViewBean;
import com.sun.grid.arco.web.arcomodule.QueryModel;
import com.sun.grid.arco.web.arcomodule.util.ModelListener;
import com.sun.grid.arco.web.arcomodule.ArcoServlet;
import com.sun.grid.arco.ArcoConstants;

public class QueryTabsModel extends CCTabsModel implements ModelListener {
   
   public static final int TAB_COMMON = 1;
   public static final int TAB_VIEW = 2;
   public static final int TAB_SQL = 3;
   public static final int TAB_SIMPLE = 4;
   
   /** Creates a new instance of QueryTabsModel */
   public QueryTabsModel() {
      ArcoServlet.getQueryModel().addModelListener(this);
      init();
   }

   public void reinit() {
      removeAllNodes();
      init();
   }
   private void init() {      
      QueryModel model = ArcoServlet.getQueryModel();
      
      CCNavNode node = new CCNavNode(TAB_COMMON,null,
              "query.commonTab",
              "query.commonTab.title",
              "query.commonTab.status"
              );
      addNode( node );

      if( !model.isResult() ) {
         if( model.isAdvanced() ) {
            node = new CCNavNode(TAB_SQL,null,
                    "query.sqlTab",
                    "query.sqlTab.title",
                    "query.sqlTab.status"
                    );
            addNode( node );
         } else {
            node = new CCNavNode(TAB_SIMPLE,null,
                    "query.simpleTab",
                    "query.simpleTab.title",
                    "query.simpleTab.status"
                    );
            addNode( node );

         }
      }      
      node = new CCNavNode(TAB_VIEW,null,
              "query.viewTab",
              "query.viewTab.title",
              "query.viewTab.status"
              );
      addNode( node );
   }
   
   public int getSelectedTab() {
      CCNavNodeInterface node = getSelectedNode();
      if( node == null ) {
         return TAB_COMMON;
      } else {
         return node.getId();
      }
   }
   
    public boolean isCommonTabSelected() {
       return getSelectedTab() == TAB_COMMON;
    }
    
    public boolean isSqlTabSelected() {
       return getSelectedTab() == TAB_SQL;
    }
    
    public boolean isSimpleTabSelected() {
       return getSelectedTab() == TAB_SIMPLE;
    }
    
    public boolean isViewTabSelected() {
       return getSelectedTab() == TAB_VIEW;
    }

   public void valueChanged(java.lang.String name) {
      if( name.equals(QueryModel.PROP_ROOT) ) {
         reinit();
      }
   }

   public void valuesChanged(java.lang.String name) {
      if( name.equals(QueryModel.PROP_ROOT) ) {
         reinit();
      }
   }

   
}
