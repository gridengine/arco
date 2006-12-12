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
import com.sun.grid.arco.web.arcomodule.QueryModel;
import java.util.*;
import com.sun.web.ui.view.html.CCDropDownMenu;
import com.iplanet.jato.view.html.OptionList;
import com.sun.grid.arco.util.LogicalConnection;
import com.sun.grid.arco.util.FilterType;


import com.sun.grid.arco.web.arcomodule.util.ObjectAdapterTableColumn;
import com.sun.grid.arco.web.arcomodule.util.ModelListener;
import com.sun.grid.arco.web.arcomodule.util.Util;
import com.sun.grid.arco.web.arcomodule.ArcoServlet;
import com.sun.grid.arco.web.arcomodule.util.ObjectAdapterTableModel;

public class FilterTableModel extends ObjectAdapterTableModel implements ModelListener {
   
   public static final String CHILD_AND_OR       = "AndOr";
   public static final String CHILD_FIELD_NAME   = "FieldName";
   public static final String CHILD_CONDITION    = "Condition";
   public static final String CHILD_REQUIREMENT  = "Requirement";
   public static final String CHILD_LATEBINDING  = "LateBinding";
   public static final String CHILD_INACTIVE     = "InActive";
   
           
   private static Map columnMap = new HashMap();

   private static void reg(ObjectAdapterTableColumn col) {
      columnMap.put(col.getValueName(),col);
      columnMap.put(col.getName(),col);
   }
   
   static {
      reg( new ObjectAdapterTableColumn(CHILD_AND_OR       ,"/filter", "logicalConnection", "filterTable.andor") );
      reg( new ObjectAdapterTableColumn(CHILD_FIELD_NAME   ,"/filter", "name", "filterTable.name") );
      reg( new ObjectAdapterTableColumn(CHILD_CONDITION    ,"/filter", "condition", "filterTable.condition") );
      reg( new ObjectAdapterTableColumn(CHILD_REQUIREMENT  ,"/filter", "parameter", "filterTable.parameter") );
      reg( new ObjectAdapterTableColumn(CHILD_LATEBINDING  ,"/filter", "lateBinding", "filterTable.lateBinding") );
      reg( new ObjectAdapterTableColumn(CHILD_INACTIVE     ,"/filter", "active", "filterTable.active") );
   };
   
   public FilterTableModel() {
      this(ArcoServlet.getQueryModel());
   }
   /** Creates a new instance of FieldTableModel */
   public FilterTableModel(QueryModel queryModel) {
      super("filter", Util.getInputStream( FilterTableModel.class, "FilterTable.xml"), columnMap, queryModel, "/filter" );
      setSelectionType(MULTIPLE);
      queryModel.addModelListener(this);
   }
   
   public View createChild(View view, String name) {

      View ret = super.createChild(view,name);
      
      ObjectAdapterTableColumn col = (ObjectAdapterTableColumn)getColumn( name );
      
      if( col != null ) {
         if( col.getName().equals( CHILD_AND_OR ) ) {
            CCDropDownMenu ddm = (CCDropDownMenu)ret;
            ddm.setOptions(AND_OR_OPTIONLIST);
            ddm.setLabelForNoneSelected(LogicalConnection.NONE.getName());
         } else if ( col.getName().equals(CHILD_FIELD_NAME ) ) {
            CCDropDownMenu ddm = (CCDropDownMenu)ret;
            ddm.setOptions( ArcoServlet.getQueryModel().getFilterFieldOptionList() );
         } else if ( col.getName().equals( CHILD_CONDITION ) ) {
            CCDropDownMenu ddm = (CCDropDownMenu)ret;
            ddm.setOptions( CONDITON_OPTIONLIST );
         }
      }
      return ret;
   }
   
   protected void initHeaders() {
      super.initHeaders();      
      setActionValue( SimpleTab.CHILD_ADD_FILTER_BUTTON, "button.add" );
      setActionValue( SimpleTab.CHILD_REMOVE_FILTER_BUTTON, "button.delete" );
   }
   
   
   private static OptionList AND_OR_OPTIONLIST;
   private static OptionList CONDITON_OPTIONLIST;
   
   static {
      AND_OR_OPTIONLIST = new OptionList();
      
      Iterator iter = LogicalConnection.getSelectableConection();
      LogicalConnection lc = null;
      while(iter.hasNext()) {
         lc = (LogicalConnection)iter.next();
         AND_OR_OPTIONLIST.add(lc.getSymbol(),lc.getName());
      }
      
      CONDITON_OPTIONLIST = new OptionList();
      iter = FilterType.getSelectableFilterTypes();
      FilterType filterType = null;
      while( iter.hasNext() ) {
         filterType=(FilterType)iter.next();
         CONDITON_OPTIONLIST.add( filterType.getSymbol(), filterType.getName() );
      }
   }

   public void valueChanged(java.lang.String name) {
      if( name.equals( QueryModel.PROP_FILTER) || name.equals("/")) {
         reinit();
      }
   }

   public void valuesChanged(java.lang.String name) {
      if( name.equals( QueryModel.PROP_FILTER) || name.equals("/")) {
         reinit();
      }
   }

}
