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

import com.iplanet.jato.RequestManager;

import com.sun.web.ui.common.*;
import com.sun.web.ui.model.*;
import com.sun.web.ui.view.alarm.*;

import java.util.*;
import com.sun.grid.arco.*;
import com.sun.grid.arco.model.NamedObject;
import com.sun.grid.logging.SGELog;

public class QueryTableModel extends NamedObjectTableModel {
   
   
   public static final String CHILD_RUN_BUTTON = "RunButton";
   public static final String CHILD_EDIT_BUTTON = "EditButton";
   public static final String CHILD_DELETE_BUTTON = "DeleteButton";
   public static final String CHILD_NEW_SIMPLE_BUTTON = "NewSimpleButton";
   public static final String CHILD_NEW_ADVANCED_BUTTON = "NewAdvancedButton";
   
   /** Creates a new instance of QueryTableModel */
   public QueryTableModel() {
     super(RequestManager.getRequestContext().getServletContext(),
           ArcoServlet.getCurrentInstance().getQueryManager(),
           "/jsp/arcomodule/IndexQueryTable.xml");      
   }
   
   protected void init() {
      super.init();
      initActionButtons();
   }
   
   
   private void initActionButtons() {
     setActionValue( CHILD_RUN_BUTTON, "button.run" );
     setActionValue( CHILD_EDIT_BUTTON, "button.edit" );
     setActionValue( CHILD_DELETE_BUTTON, "button.delete" );
     setActionValue( CHILD_NEW_SIMPLE_BUTTON, "button.newSimple");
     setActionValue( CHILD_NEW_ADVANCED_BUTTON, "button.newAdvanced");
   }
   
   
   
   
}
