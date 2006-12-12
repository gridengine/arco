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
package com.sun.grid.arco.web.arcomodule.options;

import com.iplanet.jato.view.View;
import java.util.*;
import com.sun.web.ui.view.html.CCDropDownMenu;


import com.sun.grid.arco.web.arcomodule.util.ObjectAdapterTableColumn;
import com.sun.grid.arco.web.arcomodule.util.Util;
import com.sun.grid.arco.web.arcomodule.OptionsViewBean;
import com.sun.grid.arco.web.arcomodule.util.ModelListener;
import com.sun.grid.arco.web.arcomodule.util.ObjectAdapterTableModel;

public class LogFilterTableModel extends ObjectAdapterTableModel implements ModelListener {
   
   public static final String CHILD_CLASS_PATTERN   = "ClassPattern";
   public static final String CHILD_METHOD_PATTERN = "MethodPattern";
   public static final String CHILD_LEVEL        = "Level";
   public static final String CHILD_ACTIVE       = "Active";
   
           
   private static Map columnMap = new HashMap();

   private static void reg(ObjectAdapterTableColumn col) {
      columnMap.put(col.getValueName(),col);
      columnMap.put(col.getName(),col);
   }
   
   static {
      reg( new ObjectAdapterTableColumn(CHILD_CLASS_PATTERN  ,"/filter", "classPattern", "options.logging.filter.class") );
      reg( new ObjectAdapterTableColumn(CHILD_METHOD_PATTERN ,"/filter", "methodPattern", "options.logging.filter.method") );
      reg( new ObjectAdapterTableColumn(CHILD_LEVEL     ,"/filter", "level",  "options.logging.level") );
      reg( new ObjectAdapterTableColumn(CHILD_ACTIVE    ,"/filter", "active", "options.logging.filter.active") );
   };
   
   public LogFilterTableModel() {
      this( OptionsViewBean.getLoggingModel() );
   }
   
   public LogFilterTableModel(LoggingModel model) {
      super("logFilter", Util.getInputStream( LogFilterTableModel.class, "LogFilterTable.xml"), columnMap, model, "/filter" );
      setSelectionType(MULTIPLE);
      model.addModelListener(this);
   }
   
   public View createChild(View view, String name) {

      View ret = super.createChild(view,name);
      
      ObjectAdapterTableColumn col = (ObjectAdapterTableColumn)getColumn( name );
      
      if( col != null ) {
         if( col.getName().equals( CHILD_LEVEL ) ) {
            CCDropDownMenu ddm = (CCDropDownMenu)ret;
            ddm.setOptions(OptionsPropertySheetModel.LEVEL_OPTION_LIST);
         } 
//         else if ( col.getName().equals(CHILD_ACTIVE)) {
//            CCCheckBox cb = (CCCheckBox)ret;
//            cb.setUncheckedValue("unchecked");
//            cb.setCheckedValue("checked");
//         }
      }
      return ret;
   }
   
   protected void initHeaders() {
      super.initHeaders();      
      setActionValue( OptionsViewBean.CHILD_ADD_LOG_FILTER_BUTTON, "button.add" );
      setActionValue( OptionsViewBean.CHILD_REMOVE_LOG_FILTER_BUTTON, "button.delete" );
   }

   public void valueChanged(java.lang.String name) {
      if( name.equals(LoggingModel.PROP_ROOT) || name.equals(LoggingModel.PROP_FILTER)) {
         reinit();
      }
   }

   public void valuesChanged(java.lang.String name) {
      if( name.equals(LoggingModel.PROP_ROOT) || name.equals(LoggingModel.PROP_FILTER)) {
         reinit();
      }
   }


}
