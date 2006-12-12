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
import com.iplanet.jato.*;
import com.iplanet.jato.model.Model;
import com.sun.grid.arco.web.arcomodule.QueryViewBean;
import com.sun.web.ui.view.html.*;
import com.sun.grid.arco.web.arcomodule.QueryModel;
import com.sun.web.ui.view.propertysheet.*;
import com.sun.grid.arco.web.arcomodule.ArcoServlet;

public class SQLTab extends RequestHandlingViewBase  {
   
   public static final String CHILD_SQL_PROPERTY_SHEET  = "SqlPropertySheet";
   
   /** Creates a new instance of SQLTab */
   public SQLTab(View parent, String name) {
      super(parent, name);
      registerChild(CHILD_SQL_PROPERTY_SHEET,CCPropertySheet.class);
      getPropertySheetModel().registerChildren(this);
   }
   private AdvancedSqlPropertySheetModel getPropertySheetModel() {
      return (AdvancedSqlPropertySheetModel)getModel(AdvancedSqlPropertySheetModel.class);
   }
   
   
   protected View createChild(java.lang.String name) {
      if ( name.equals(CHILD_SQL_PROPERTY_SHEET)) {    
         Model model = ArcoServlet.getQueryModel();
         return new CCPropertySheet(this,getPropertySheetModel(),name);
      } else if( getPropertySheetModel().isChildSupported(name) ) {
         return getPropertySheetModel().createChild(this,name);
      } else {
         throw new IllegalStateException("Unknown child " + name);
      }
   }
   
}
