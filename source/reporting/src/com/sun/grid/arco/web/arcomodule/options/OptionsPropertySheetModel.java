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

import com.sun.web.ui.model.CCPropertySheetModel;
import com.iplanet.jato.RequestManager;
import com.iplanet.jato.view.View;
import com.iplanet.jato.view.ContainerView;
import com.iplanet.jato.model.object.ObjectAdapterModel;

import com.sun.web.ui.view.html.CCTextArea;
import com.sun.web.ui.view.html.CCTextField;
import com.sun.web.ui.view.html.CCDropDownMenu;
import com.sun.web.ui.view.table.CCActionTable;
import com.iplanet.jato.view.html.OptionList;

import java.io.*;
import com.sun.grid.logging.SGELog;
import com.sun.grid.arco.web.arcomodule.util.Util;
import com.sun.grid.arco.web.arcomodule.util.DefaultActionTable;
import com.sun.grid.arco.web.arcomodule.ArcoServlet;
import com.sun.grid.arco.web.arcomodule.OptionsViewBean;
import java.util.logging.Level;

public class OptionsPropertySheetModel extends CCPropertySheetModel {
   
   public static final String CHILD_LEVEL_VALUE = "levelValue";
   public static final String CHILD_FILTER_VALUE = "filterValue";
   
   /** Creates a new instance of CommonPropertySheetModel */
   public OptionsPropertySheetModel() {
      super(Util.getInputStream(OptionsPropertySheetModel.class,"OptionsPropertySheet.xml"));  

      setModel(CHILD_FILTER_VALUE, getLogFilterTableModel() );
   }
   
   public LogFilterTableModel getLogFilterTableModel() {
      return (LogFilterTableModel)RequestManager.getRequestContext()
                 .getModelManager().getModel(LogFilterTableModel.class);
   }
   
   public View createChild(View view, String name) {
         
      if( name.equals(CHILD_LEVEL_VALUE)) {
         
         CCDropDownMenu ret = new CCDropDownMenu(view
                 , OptionsViewBean.getLoggingModel(), name
                 , "/level", (Object)null );             
         ret.setOptions(LEVEL_OPTION_LIST);
         return ret; 
      } else if( name.equals(CHILD_FILTER_VALUE)) {
         return new DefaultActionTable((ContainerView)view,getLogFilterTableModel(),name);
      } else {
         return super.createChild(view,name);
      }
   }
   
   public final static OptionList LEVEL_OPTION_LIST;
   
   static {
      LEVEL_OPTION_LIST = new OptionList();
      
      Level [] levels = { Level.ALL, Level.SEVERE, Level.WARNING, 
                          Level.CONFIG, Level.INFO, Level.FINE,
                          Level.FINER, Level.FINEST, Level.OFF };
                          
      String level = null;
      for( int i = 0; i < levels.length; i++ ) {
          level = levels[i].toString();
          LEVEL_OPTION_LIST.add(level, level);
      }
   }
   
   
}
