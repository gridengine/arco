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

import com.iplanet.jato.model.ModelControlException;
import com.iplanet.jato.view.*;
import com.iplanet.jato.view.event.RequestInvocationEvent;
import com.sun.grid.arco.ArcoException;
import com.sun.web.ui.view.html.*;
import com.sun.web.ui.view.table.*;
import java.io.IOException;
import javax.servlet.ServletException;

public abstract class NamedObjectListView extends RequestHandlingViewBase {
   
   public static final String CHILD_TILED_VIEW              = "NamedObjectTiledView";
   public static final String CHILD_ACTION_TABLE            = "ActionTable";
   public static final String CHILD_RUN_BUTTON_NAME_TEXT    = "RunButtonNameText";
   public static final String CHILD_EDIT_BUTTON_NAME_TEXT   = "EditButtonNameText";
   public static final String CHILD_DELETE_BUTTON_NAME_TEXT = "DeleteButtonNameText";
   
   private NamedObjectTableModel model = null;
    
   /** Creates a new instance of QueryListView 
    *  @param  parent   the parent view component
    *  @param  name     name of the view
    */
   public NamedObjectListView( View parent, String name, NamedObjectTableModel model ) {
      super(parent, name); 
      this.model = model;
   }
   
    protected void registerChildren() {
       registerChild(CHILD_ACTION_TABLE, CCActionTable.class);
       registerChild(CHILD_TILED_VIEW, NamedObjectTiledView.class);
       registerChild(CHILD_RUN_BUTTON_NAME_TEXT, CCStaticTextField.class);
       registerChild(CHILD_EDIT_BUTTON_NAME_TEXT, CCStaticTextField.class);
       registerChild(CHILD_DELETE_BUTTON_NAME_TEXT, CCStaticTextField.class);
       
       model.registerChildren(this);
    }
    
    public ArcoException getError() {
       return model.getError();
    }
   
    protected View createChild(String name) {
       if( name.equals( CHILD_TILED_VIEW ) ) {
          return new NamedObjectTiledView(this, model, name);
       } else if ( name.equals( CHILD_ACTION_TABLE ) ) {
       
          // Action table.
          CCActionTable child = new CCActionTable(this, model, name);
 
             // Set the TileView object.
          child.setTiledView((ContainerView) getChild(CHILD_TILED_VIEW));       
          return child;
       } else if (model.isChildSupported(name)) {
             // Create child from action table model.
             return model.createChild(this, name);
       } else {
          return null;
       }
    }
    
    public NamedObjectTableModel getModel() {
       return model;
    }
    
    public abstract void handleNameHrefRequest(RequestInvocationEvent event, String name) 
       throws ServletException, IOException, ModelControlException;
   
}
