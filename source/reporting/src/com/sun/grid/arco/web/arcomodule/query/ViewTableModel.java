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

import com.iplanet.jato.view.View;
import com.sun.web.ui.view.html.CCDropDownMenu;
import com.sun.grid.arco.web.arcomodule.QueryModel;
import java.util.*;
import com.sun.grid.arco.web.arcomodule.util.ObjectAdapterTableColumn;
import com.sun.grid.arco.web.arcomodule.ArcoServlet;
import com.sun.grid.arco.web.arcomodule.util.AbstractTableColumn;
import com.sun.grid.arco.web.arcomodule.util.ModelListener;
import com.sun.grid.arco.web.arcomodule.util.ObjectAdapterTableModel;

public class ViewTableModel extends ObjectAdapterTableModel implements ModelListener {
   
   public static final String CHILD_NAME    = "Name";
   public static final String CHILD_TYPE    = "Type";
   public static final String CHILD_FORMAT  = "Format";
   
   private static Map columnMap = new HashMap();

   private static void reg(ObjectAdapterTableColumn col) {
      columnMap.put(col.getValueName(),col);
      columnMap.put(col.getName(),col);
   }
   
   
   static {
      reg( new ObjectAdapterTableColumn(CHILD_NAME       , QueryModel.PROP_VIEW_TABLE_COL, "name"    , "query.common.viewTable.Name"   ) );
      reg( new ObjectAdapterTableColumn(CHILD_TYPE   , QueryModel.PROP_VIEW_TABLE_COL, "type"  , "query.common.viewTable.Type" ) );
      reg( new ObjectAdapterTableColumn(CHILD_FORMAT   , QueryModel.PROP_VIEW_TABLE_COL, "format"  , "query.common.viewTable.Format" ) );
   };
   
   public ViewTableModel() {
      this(ArcoServlet.getQueryModel());
   }
   
   public ViewTableModel(QueryModel queryModel ) {      
      super("view", Util.getInputStream( "ViewTable.xml"),columnMap, queryModel, QueryModel.PROP_VIEW_TABLE_COL);
      setSelectionType(MULTIPLE);
      queryModel.addModelListener(this);
   }
   
   public View createChild(View view, String name) {

      View ret = null;
      ObjectAdapterTableColumn col = (ObjectAdapterTableColumn)getColumn( name );

      if( col != null ) {
      
         if( col.getName().equals(CHILD_FORMAT) ) {
            // Each row needs it's own option list (depends of the column type)
            // the FormatDropDownMenu respects this behaviour
            AbstractTableColumn formatTypeCol = getColumn(CHILD_TYPE);
            ret = new FormatDropDownMenu(view, name, this, formatTypeCol.getValueName() );
         } else {
            ret = super.createChild(view, name);
         }
      
         if( col.getName().equals( CHILD_TYPE )) {
            CCDropDownMenu ddm = (CCDropDownMenu)ret;
            ddm.setOptions(ArcoServlet.getInstance().getFormatTypeOptionList());        
         } else if ( col.getName().equals( CHILD_NAME ) ) {
            CCDropDownMenu ddm = (CCDropDownMenu)ret;
            ddm.setOptions(ArcoServlet.getQueryModel().getDefinedOptionList());
         }   
      } else {
         ret = super.createChild(view, name);
      }
      return ret;
   }
   

   protected void initHeaders() {
      super.initHeaders();
      
      setActionValue( ViewTab.CHILD_ADD_VIEW_TABLE_BUTTON, "button.add" );
      setActionValue( ViewTab.CHILD_REMOVE_VIEW_TABLE_BUTTON, "button.delete" );
   }
   
   
   
   public void valueChanged(java.lang.String name) {
      if( name.equals( QueryModel.PROP_VIEW_TABLE_COL ) || name.equals("/")) {
         reinit();
      }
   }

   public void valuesChanged(java.lang.String name) {
      if( name.equals( QueryModel.PROP_VIEW_TABLE_COL ) || name.equals("/")) {
         reinit();
      }
   }

   
}
