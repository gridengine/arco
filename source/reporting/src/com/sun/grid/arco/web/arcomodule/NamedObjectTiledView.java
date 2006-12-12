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
import com.sun.web.ui.view.html.CCHref;
import com.sun.web.ui.view.html.CCStaticTextField;

public class NamedObjectTiledView extends RequestHandlingTiledViewBase  {
   
   NamedObjectTableModel model;
   
   public static final String CHILD_NAME_HREF   = "NameHref";
   public static final String CHILD_TOOL_TIP    = "TextToolTip";
   
   /** Creates a new instance of QueryTiledView */
   public NamedObjectTiledView(NamedObjectListView parent, NamedObjectTableModel model, String name) {
      super(parent, name);
      this.model = model;
      registerChildren();
      this.setPrimaryModel( model );
   }
   
   protected void registerChildren() {
      model.registerChildren(this);
   }   
   
   protected View createChild(String name) {
      if( name.equals(CHILD_NAME_HREF)) {
         return new TitledHref(this,model,name,null);
      } else if (model.isChildSupported(name)) {
         // Create child from action table model.
         return model.createChild(this, name);         
      } else {
         throw new IllegalArgumentException(
         "Invalid child name [" + name + "]");
      }
   }
   
   public void handleNameHrefRequest(RequestInvocationEvent event)
   throws ServletException, IOException, ModelControlException {
      TiledViewRequestInvocationEvent tevt = (TiledViewRequestInvocationEvent)event;
      NamedObjectListView parent = (NamedObjectListView)getParent();
      String name = model.getObjectName(tevt.getTileNumber());         
      parent.handleNameHrefRequest(event, name);
   }

   class TitledHref extends CCHref {
      private NamedObjectTableModel model;
      
      public TitledHref(View parent, NamedObjectTableModel model, String name, Object value) {
         super(parent,model,name,value);
         this.model = model;
      }
      
//      public String getTitle() {
//         return model.getDescription(model.getRowIndex());
//      }
      
      public String getExtraHtml() {
         return "onmouseover=\"showWMTT('description" + model.getRowIndex() + "')\""
                 + " onmouseout=\"hideWMTT()\"";
      }
      
   }
   

}
