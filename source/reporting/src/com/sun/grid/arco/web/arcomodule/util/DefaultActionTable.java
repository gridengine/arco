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
package com.sun.grid.arco.web.arcomodule.util;

import com.iplanet.jato.view.ContainerView;
import com.iplanet.jato.view.View;
import com.sun.web.ui.view.table.CCActionTable;
import com.sun.web.ui.model.CCActionTableModelInterface;
import com.iplanet.jato.view.RequestHandlingTiledViewBase;

/**
 * The DefaultActionTable extends the CCActionTable.
 * It has always a tiled view.
 */
public class DefaultActionTable extends CCActionTable {
   
   /** name of the tiled view of the table. */
   public static final String CHILD_TILED_VIEW = "TiledView";
   
   /** model of this table. */
   private CCActionTableModelInterface model;
   
   /**
    * Creates a new instance of DefaultActionTable
    * @param view    the container view  
    * @param model   the table model
    * @param name    name of the action table
    */
   public DefaultActionTable(ContainerView view, CCActionTableModelInterface model, 
                             String name) {
      super(view,model,name);
      this.model = model;
      registerChildren();
      setTiledView(getTiledViewInternal());
   }
   
   /**
    *  Register all children
    */
   protected void registerChildren() {
      super.registerChildren();
      registerChild(CHILD_TILED_VIEW, DefaultTiledView.class );
   }
   
   /**
    * Create a child for this view
    * @param name name of the child
    * @return  the child
    */
   protected View createChild(String name) {
      if( name.equals( CHILD_TILED_VIEW)) {
         return new DefaultTiledView(this,name);
      } else {
         return super.createChild(name);
      }
   }
   
   
   private DefaultTiledView getTiledViewInternal() {
      return (DefaultTiledView)getChild(CHILD_TILED_VIEW);
   }
   
   /**
    *  The DefaultActionTable has a tiled view
    */
   public class DefaultTiledView extends RequestHandlingTiledViewBase {
      
      /** Creates a new instance of FilterTiledView */
      public DefaultTiledView(View view, String name) {
         super(view,name);
         setPrimaryModel(model);
         registerChildren();
      }
      
      /**
       * register all children of the model in the tiled view
       */
      protected void registerChildren() {
         model.registerChildren(this);
      }
      
      
      /**
       * Create a child of the tiled view
       * @param name  name of the child
       * @return  the child
       * @throws IllegalArgumentException if no child with this name is
       *         registered
       */
      protected View createChild(String name) {
         if (model.isChildSupported(name)) {
            // Create child from action table model.
            return model.createChild(this, name);
         } else {
            throw new IllegalArgumentException(
                    "Invalid child name [" + name + "]");
         }
      }
   }
}
