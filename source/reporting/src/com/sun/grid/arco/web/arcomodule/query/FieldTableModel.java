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
import com.iplanet.jato.view.html.OptionList;
import com.sun.grid.arco.util.SortType;
import com.sun.grid.arco.util.FieldFunction;
import com.sun.grid.arco.web.arcomodule.util.ModelListener;
import com.sun.grid.arco.web.arcomodule.util.Util;
import com.sun.grid.arco.web.arcomodule.ArcoServlet;
import com.sun.grid.arco.web.arcomodule.util.ObjectAdapterTableModel;

public class FieldTableModel extends ObjectAdapterTableModel implements ModelListener {
   
   public static final String CHILD_NAME      = "Name";
   public static final String CHILD_FUNCTION  = "Function";
   public static final String CHILD_PARAMETER = "Parameter";
   public static final String CHILD_USERNAME  = "UserName";
   public static final String CHILD_SORT      = "Sort";
   
   private static Map columnMap = new HashMap();

   private static void reg(ObjectAdapterTableColumn col) {
      columnMap.put(col.getValueName(),col);
      columnMap.put(col.getName(),col);
   }
   
   
   static {
      reg( new ObjectAdapterTableColumn(CHILD_NAME       , "/field", "dbName"    , "fieldTable.dbName"   ) );
      reg( new ObjectAdapterTableColumn(CHILD_FUNCTION   , "/field", "function"  , "fieldTable.function" ) );
      reg( new ObjectAdapterTableColumn(CHILD_PARAMETER  , "/field", "parameter" , "fieldTable.parameter") );
      reg( new ObjectAdapterTableColumn(CHILD_USERNAME   , "/field", "reportName", "fieldTable.username") );
      reg( new ObjectAdapterTableColumn(CHILD_SORT       , "/field", "sort"      , "fieldTable.sort"    ) );
   };
   
   public FieldTableModel() {
      this(ArcoServlet.getQueryModel());
   }
   public FieldTableModel(QueryModel queryModel ) {      
      super("field", Util.getInputStream( FieldTableModel.class,"FieldTable.xml"),columnMap, queryModel, "/field");
      setSelectionType(MULTIPLE);
      queryModel.addModelListener(this);
   }
   
   public View createChild(View view, String name) {

      View ret = super.createChild(view,name);
      
      ObjectAdapterTableColumn col = (ObjectAdapterTableColumn)getColumn( name );
      
      if( col != null ) {
         if( col.getName().equals( CHILD_SORT )) {
            CCDropDownMenu ddm = (CCDropDownMenu)ret;
            ddm.setOptions(SORT_OPTIONLIST);
            ddm.setLabelForNoneSelected(SortType.NOT_SORTED.getSymbol());
         } else if ( col.getName().equals( CHILD_NAME ) ) {
            CCDropDownMenu ddm = (CCDropDownMenu)ret;
            ddm.setOptions(ArcoServlet.getQueryModel().getFieldOptionList());
         } else if ( col.getName().equals( CHILD_FUNCTION )) {
            CCDropDownMenu ddm = (CCDropDownMenu)ret;
            ddm.setOptions(FUNCTION_OPTIONLIST);
         }
      }   
      return ret;
   }
   protected void initHeaders() {
      super.initHeaders();
      
      setActionValue( SimpleTab.CHILD_ADD_FIELD_BUTTON, "button.add" );
      setActionValue( SimpleTab.CHILD_REMOVE_FIELD_BUTTON, "button.delete" );
   }
   
   
   private static OptionList SORT_OPTIONLIST;
   private static OptionList FUNCTION_OPTIONLIST;
   
   static {
      SORT_OPTIONLIST = new OptionList();
      SortType st = SortType.NOT_SORTED;
      SORT_OPTIONLIST.add(st.getSymbol(), st.getName());
      st = SortType.ASC;
      SORT_OPTIONLIST.add(st.getSymbol(), st.getName());
      st = SortType.DESC;
      SORT_OPTIONLIST.add(st.getSymbol(), st.getName());
      
      FUNCTION_OPTIONLIST = new OptionList();
      Iterator iter = FieldFunction.getSelectableFieldFunctions();
      FieldFunction ff = null;
      while(iter.hasNext()) {
         ff = (FieldFunction)iter.next();
         FUNCTION_OPTIONLIST.add( ff.getSymbol(), ff.getName());
      }
      
   }   

   public void valueChanged(java.lang.String name) {
      if( name.equals( QueryModel.PROP_FIELD ) || name.equals("/")) {
         reinit();
      }
   }

   public void valuesChanged(java.lang.String name) {
      if( name.equals( QueryModel.PROP_FIELD ) || name.equals("/")) {
         reinit();
      }
   }

}
