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

import com.sun.web.ui.model.*;
import com.iplanet.jato.view.View;
import com.sun.grid.arco.model.QueryType;
import com.sun.web.ui.view.html.CCButton;
import com.sun.grid.arco.web.arcomodule.util.ModelListener;
import com.sun.grid.arco.web.arcomodule.util.Util;
import com.sun.grid.arco.web.arcomodule.ArcoServlet;
import com.sun.grid.arco.web.arcomodule.QueryModel;

public class QueryPageTitleModel extends CCPageTitleModel implements ModelListener {
   
   public static final String CHILD_SAVE_BUTTON = "SaveButton";
   public static final String CHILD_SAVE_AS_BUTTON = "SaveAsButton";
   public static final String CHILD_RESET_BUTTON = "ResetButton";
   public static final String CHILD_RUN_BUTTON = "RunButton";
   public static final String CHILD_TO_ADVANCED_BUTTON = "ToAdvancedButton";
   
   /** Creates a new instance of QueryPageTitleModel */
   public QueryPageTitleModel() {

      
      setDocument(Util.getInputStream(QueryPageTitleModel.class,"QueryPageTitle.xml"));
      setValue(CHILD_SAVE_BUTTON, "button.save");
      setValue(CHILD_RESET_BUTTON, "button.reset");
      setValue(CHILD_RUN_BUTTON, "button.run");
      setValue(CHILD_SAVE_AS_BUTTON, "button.saveAs");
      setValue(CHILD_TO_ADVANCED_BUTTON, "button.toAdvanced");
      
      ArcoServlet.getQueryModel().addModelListener(this);
   }
   
   private CCButton saveButton;
   private CCButton resetButton;
   private CCButton advancedButton;
   private CCButton saveAsButton;
   
   public View createChild(View parent,String name) {
   
      View ret = super.createChild(parent,name);
      if( name.equals( CHILD_SAVE_BUTTON ) ) {
         saveButton = (CCButton)ret;
         saveButton.setDisabled(!isSaveEnabled());
      } else if ( name.equals( CHILD_RESET_BUTTON )) {
         resetButton = (CCButton)ret;
         resetButton.setDisabled(!isSaveEnabled());
      } else if ( name.equals(CHILD_TO_ADVANCED_BUTTON)) {
         advancedButton = (CCButton)ret;
         advancedButton.setDisabled( !canConvert() );
      } else if ( name.equals( CHILD_RUN_BUTTON ) ) {
         CCButton runButton = (CCButton)ret;
         runButton.setDisabled(!canRun());
      } else if ( name.equals(CHILD_SAVE_AS_BUTTON)) {
         saveAsButton = (CCButton)ret;
         saveAsButton.setDisabled(!isSaveAsEnabled());
      }
      return ret;
   }
   
   public boolean isSaveAsEnabled() {
      return ArcoServlet.getCurrentInstance().hasUserWritePermission();
   }
   
   public boolean isSaveEnabled() {
      return canSave() && ArcoServlet.getQueryModel().isDirty();
   }
   public boolean canSave() {
      QueryModel model = ArcoServlet.getQueryModel();
      return ArcoServlet.getCurrentInstance().hasUserWritePermission() &&
              model.isSaveable();
   }
   
   public boolean canRun() {
      return ArcoServlet.getQueryModel().isRunnable();
   }
   
   public boolean canConvert() {
      return ArcoServlet.getQueryModel().isConvertable();
   }

   private  String pageTitleText;
   
   private void reinit() {      
      QueryModel queryModel = ArcoServlet.getQueryModel();
      QueryType query = queryModel.getQuery();
      String title = null;
      if(query.isSetName()) {
         title = query.getName();
      } else {         
         if( queryModel.isAdvanced() ) {
            title = ArcoServlet.getI18N().getMessage("query.advancedQuery");
         } else {
            title = ArcoServlet.getI18N().getMessage("query.simpleQuery");
         }
      }
      pageTitleText = title;
      setPageTitleText(title);

      boolean isSaveDisabled = !isSaveEnabled();
      if(resetButton != null ) {
         resetButton.setDisabled(isSaveDisabled);
      }
            
      if( saveButton != null ) {
         saveButton.setDisabled(isSaveDisabled);
      }
      
      if( saveAsButton != null) {
         saveAsButton.setDisabled(!isSaveAsEnabled());
      }
      
      if( advancedButton != null ) {
         advancedButton.setDisabled(!canConvert());
      }
   }
   
   public void valueChanged(java.lang.String name) {
      
      if( name.equals(QueryModel.PROP_ROOT) || name.equals("/name")) {         
         reinit();
      }
   }

   public void valuesChanged(java.lang.String name) {
      if( name.equals(QueryModel.PROP_ROOT) || name.equals("/name")) {
         reinit();
      }
   }

   public String getPageTitleText() {

      if( pageTitleText == null ) {
         reinit();
      }
      return pageTitleText;
   }
}
