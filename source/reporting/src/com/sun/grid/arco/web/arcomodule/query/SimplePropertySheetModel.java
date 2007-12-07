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


import com.iplanet.jato.RequestManager;
import com.iplanet.jato.view.View;
import com.iplanet.jato.view.ContainerView;
import com.sun.grid.arco.sql.ArcoDbConnectionPool;
import com.sun.web.ui.view.html.CCDropDownMenu;
import com.sun.web.ui.view.html.CCHiddenField;
import com.sun.web.ui.view.html.CCTextField;
import com.iplanet.jato.view.html.OptionList;
import com.sun.grid.arco.sql.ArcoClusterModel;
import java.util.Iterator;
import java.util.List;
import com.sun.grid.logging.SGELog;

import com.sun.grid.arco.web.arcomodule.util.DefaultActionTable;
import com.sun.grid.arco.web.arcomodule.ArcoServlet;
import com.sun.grid.arco.web.arcomodule.BaseViewBean;

public class SimplePropertySheetModel extends AbstractPropertySheetModel {
   
   public static final String CHILD_ORG_TABLE_VALUE = "orgTableValue";   
   public static final String CHILD_TABLE_VALUE     = "tableValue";
   public static final String CHILD_FIELD_VALUE     = "fieldValue";
   public static final String CHILD_FILTER_VALUE    = "filterValue";
   public static final String CHILD_LIMIT_VALUE     = "limitValue";
   
   /** Creates a new instance of SimplePropertySheetModel */
   public SimplePropertySheetModel() {
      super("SimplePropertySheet.xml");
   }
   
   protected FieldTableModel getFieldTableModel() {
      return (FieldTableModel)RequestManager.getRequestContext().getModelManager().getModel(FieldTableModel.class);
   }

   protected FilterTableModel getFilterTableModel() {
      return (FilterTableModel)RequestManager.getRequestContext().getModelManager().getModel(FilterTableModel.class);
   }
   
   private BaseViewBean getBaseViewBean(View view) {
      while( view != null && !(view instanceof BaseViewBean) ) {
         view = view.getParent ();
      }
      if( view == null ) {
         throw new IllegalStateException("view has no BaseViewBean as parent");
      }
      return (BaseViewBean)view;
   }
   
   public View createChild(View view, String name) {
      
      if( name.equals(CHILD_TABLE_VALUE)) {

         OptionList options = new OptionList();
         try {
            ArcoDbConnectionPool cp = ArcoServlet.getCurrentInstance().getConnectionPool();
            ArcoClusterModel acm = ArcoClusterModel.getInstance(RequestManager.getSession());               
            List viewList = cp.getViewList(acm.getCurrentCluster());         
            Iterator iter = viewList.iterator();
            String option = null;
            while( iter.hasNext() ) {
               option = (String)iter.next();
               options.add(option,option);
            }
         } catch( java.sql.SQLException sqle ) {
            BaseViewBean bv = getBaseViewBean(view);
            bv.warning("query.simple.viewListError", new Object[]{sqle.getMessage()});
            SGELog.warning("Can't read view list: " + sqle.getMessage() );
         }
         
         CCDropDownMenu ret = new CCDropDownMenu(view, ArcoServlet.getQueryModel(),
                                   name,"/tableName", null,options,null);
         ret.setLabelForNoneSelected("");
         return ret;
      } else if ( name.equals( CHILD_ORG_TABLE_VALUE ) ) {
         return new CCHiddenField(view,ArcoServlet.getQueryModel(),name,"/tableName",null,null);
      } else if ( name.equals( CHILD_LIMIT_VALUE) ) {
         return new CCTextField(view,ArcoServlet.getQueryModel(),name,"/limit",null);   
      } else if ( name.equals( CHILD_FILTER_VALUE )) {
         return new DefaultActionTable((ContainerView)view,getFilterTableModel(),name);   
      } else if ( name.equals( CHILD_FIELD_VALUE )) {
         return new DefaultActionTable((ContainerView)view,getFieldTableModel(),name);
      } else {
         return super.createChild(view,name);
      }
      
      
   }

}
