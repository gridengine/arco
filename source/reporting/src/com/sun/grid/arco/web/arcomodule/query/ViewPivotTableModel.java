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
import com.iplanet.jato.view.html.OptionList;
import com.sun.grid.arco.ArcoConstants;
import com.sun.grid.arco.web.arcomodule.ArcoServlet;
import com.sun.grid.arco.web.arcomodule.util.ModelListener;
import com.sun.grid.arco.web.arcomodule.util.AbstractTableColumn;

import com.sun.grid.arco.web.arcomodule.util.ObjectAdapterTableColumn;
import com.sun.grid.arco.web.arcomodule.util.ObjectAdapterTableModel;

public class ViewPivotTableModel extends ObjectAdapterTableModel implements ModelListener {
   
   public static final String CHILD_NAME       = "Name";
   public static final String CHILD_FORMAT     = "Format";
   public static final String CHILD_TYPE       = "Type";
   public static final String CHILD_PIVOT_TYPE = "PivotType";
           
   private static Map columnMap = new HashMap();
   
   private static void reg(ObjectAdapterTableColumn col) {
      columnMap.put(col.getValueName(),col);
      columnMap.put(col.getName(),col);
   }
   
   public static final String BOUND_PATH = "/view/pivot/elem";
   
   static {
      reg( new ObjectAdapterTableColumn(CHILD_NAME,       BOUND_PATH, "name"  , "query.common.viewTable.Name"));
      reg( new ObjectAdapterTableColumn(CHILD_FORMAT,     BOUND_PATH, "format", "query.common.viewTable.Format"));
      reg( new ObjectAdapterTableColumn(CHILD_TYPE,       BOUND_PATH, "type"  , "query.common.viewTable.Type"));
      reg( new ObjectAdapterTableColumn(CHILD_PIVOT_TYPE, BOUND_PATH, "pivotType"  , "query.common.viewTable.PivotType"));
   };
   
   public ViewPivotTableModel() {      
      this(ArcoServlet.getQueryModel());
   }
   
   public ViewPivotTableModel(QueryModel queryModel ) {
      super("pivot", Util.getInputStream( "ViewPivotTable.xml"),columnMap, queryModel, BOUND_PATH);
      setSelectionType(MULTIPLE);
      queryModel.addModelListener(this);
   }
   
   public View createChild(View view, String name) {
      
      View ret = null;
      
      AbstractTableColumn col = getColumn( name );
      
      if( col != null ) {

         if( col.getName().equals( CHILD_FORMAT )) {
            // Each row needs it's own option list (depends of the column type)
            // the FormatDropDownMenu respects this behaviour
            AbstractTableColumn formatTypeCol = getColumn(CHILD_TYPE);
            ret = new FormatDropDownMenu(view, name, this, formatTypeCol.getValueName() );
         } else {
            ret = super.createChild(view, name );
         }   
            
         if ( col.getName().equals( CHILD_NAME ) ) {
            CCDropDownMenu ddm = (CCDropDownMenu)ret;
            ddm.setOptions(ArcoServlet.getQueryModel().getDefinedOptionList());
         } else if ( col.getName().equals(CHILD_TYPE)) {
            CCDropDownMenu ddm = (CCDropDownMenu)ret;
            ddm.setOptions(ArcoServlet.getInstance().getFormatTypeOptionList());
         } else if ( col.getName().equals(CHILD_PIVOT_TYPE)) {
            CCDropDownMenu ddm = (CCDropDownMenu)ret;
            ddm.setOptions(PIVOT_TYPE_OPTIONLIST);
         }
      } else {
         ret = super.createChild(view, name);
      }
      return ret;
   }
   
   protected void initHeaders() {
      super.initHeaders();
      
      setActionValue( ViewTab.CHILD_REMOVE_VIEW_PIVOT_BUTTON, "button.delete" );
      setActionValue( ViewTab.CHILD_ADD_VIEW_PIVOT_COLUMN_BUTTON, "button.addColumn" );
      setActionValue( ViewTab.CHILD_ADD_VIEW_PIVOT_ROW_BUTTON, "button.addRow" );
      setActionValue( ViewTab.CHILD_ADD_VIEW_PIVOT_DATA_BUTTON, "button.addData" );
   }
   
   
   private static OptionList PIVOT_TYPE_OPTIONLIST;
   
   static {
      PIVOT_TYPE_OPTIONLIST = new OptionList();
      PIVOT_TYPE_OPTIONLIST.add("query.common.viewTable.column", ArcoConstants.PIVOT_TYPE_COLUMN);
      PIVOT_TYPE_OPTIONLIST.add("query.common.viewTable.row", ArcoConstants.PIVOT_TYPE_ROW);
      PIVOT_TYPE_OPTIONLIST.add("query.common.viewTable.data", ArcoConstants.PIVOT_TYPE_DATA);
   }
   
   public void valueChanged(java.lang.String name) {
      if( name.equals( QueryModel.PROP_VIEW_PIVOT_ELEM ) || name.equals("/")) {
         reinit();
      }
   }
   
   public void valuesChanged(java.lang.String name) {
      if( name.equals( QueryModel.PROP_VIEW_PIVOT_ELEM ) || name.equals("/")) {
         reinit();
      }
   }


}
