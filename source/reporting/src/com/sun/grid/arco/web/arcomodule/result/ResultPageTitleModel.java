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
package com.sun.grid.arco.web.arcomodule.result;

import com.sun.grid.arco.model.QueryType;
import com.sun.web.ui.model.*;
import com.iplanet.jato.view.View;
import com.sun.web.ui.view.html.CCButton;
import com.sun.grid.arco.web.arcomodule.util.ModelListener;
import com.sun.grid.arco.web.arcomodule.util.Util;
import com.sun.grid.arco.QueryResult;
import com.sun.grid.arco.sql.SQLQueryResult;
import com.sun.grid.arco.web.arcomodule.ResultModel;
import com.sun.grid.arco.web.arcomodule.ArcoServlet;

public class ResultPageTitleModel extends CCPageTitleModel implements ModelListener {
   
   public static final String CHILD_EDIT_BUTTON = "EditButton";
   public static final String CHILD_SAVE_BUTTON = "SaveButton";
   
   /** Creates a new instance of QueryPageTitleModel */
   public ResultPageTitleModel() {

      setDocument(Util.getInputStream(ResultPageTitleModel.class,"ResultPageTitle.xml"));

      setValue(CHILD_EDIT_BUTTON, "button.edit");
      setValue(CHILD_SAVE_BUTTON, "button.saveResult");
      
      ResultModel resultModel = ArcoServlet.getResultModel();

      resultModel.addModelListener(this);   
      
      updateTitle();
   }
   
   
   private void updateTitle() {
      ResultModel resultModel = ArcoServlet.getResultModel();
      QueryResult queryResult = resultModel.getQueryResult();
      if(queryResult!=null ) {
         final QueryType query = queryResult.getQuery();
         if(query!=null) {
            setPageTitleText(query.getName());  
         }
          
      }
   }
   
   
   public void valueChanged(java.lang.String name) {
      updateTitle();
   }

   public void valuesChanged(java.lang.String name) {
      updateTitle();
   }

   public View createChild(View parent, String name) {

      View retValue;
      
      retValue = super.createChild(parent, name);
      
      if( name.equals( CHILD_SAVE_BUTTON )) {
         ResultModel resultModel = ArcoServlet.getResultModel();
         ((CCButton)retValue).setVisible(resultModel.getQueryResult() instanceof SQLQueryResult);
      }
      return retValue;
   }

   
}
