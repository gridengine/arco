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

import com.iplanet.jato.view.*;
import com.sun.web.ui.view.masthead.CCPrimaryMasthead;
import com.sun.web.ui.view.alert.CCAlertInline;
import com.sun.web.ui.view.alert.CCAlert;
import com.sun.web.ui.model.*;
import com.iplanet.jato.*;
import com.sun.grid.arco.ArcoException;

public abstract class BaseViewBean extends ViewBeanBase {
   
   
   public static final String CHILD_MASTHEAD = "Masthead";
   public static final String CHILD_ALERT    = "Alert";
   public static final String CHILD_HEADER   = "Header";
   
   /** Creates a new instance of BaseViewBean */
   protected BaseViewBean(String pageName, String defaultDisplayURL) {
      super(pageName);
      this.setDefaultDisplayURL(defaultDisplayURL);
      registerChildren();
   }
   
   protected com.iplanet.jato.ModelManager getModelManager() {
      return RequestManager.getRequestContext().getModelManager();
   }
   
   protected void registerChildren() {
      registerChild(CHILD_MASTHEAD, CCPrimaryMasthead.class);     
      registerChild(CHILD_ALERT, CCAlertInline.class);
      registerNewChildren();      
   }
   
   protected abstract void registerNewChildren();
   protected abstract View newChild(String name );
     
   
   protected final View createChild(String name) {
      if (name.equals(CHILD_MASTHEAD)) {
         // Masthead
         CCMastheadModel model = (CCMastheadModel)getModel(CCMastheadModel.class);
         return new CCPrimaryMasthead(this, model, name);
      } else if (name.equals(CHILD_ALERT)) {
         return new CCAlertInline(this,name,null);
      } else {         
         View ret = newChild(name);
         if( ret == null ) {
            throw new IllegalStateException("Unknown child " + name 
                                            + " for view bean " + getClass() );
         } else {
            return ret;
         }
      }
   }
   
   public void alert( String title, String type, String message, Object[] params ) {
      CCAlertInline alert = (CCAlertInline)getChild(CHILD_ALERT);

      alert.setType( type );
      alert.setSummary(title);
      alert.setDetail(message, params);
   }
   
   public void alert( String type, String message, Object[] params) {
      CCAlertInline alert = (CCAlertInline)getChild(CHILD_ALERT);

      alert.setType( type );
      alert.setSummary(message, params);
   }
   
   
   public void error( String title, ArcoException ae ) {
      error( title, ae.getMessage(), ae.getParameter() );
   }
   
   public void error( ArcoException ae ) {
      error(ae.getMessage(),ae.getParameter());
   }
   
   
   public void error( String message ) {
      error(message,(Object[])null);
   }
   
   public void error( String message, Object[] params ) {
      alert(CCAlert.TYPE_ERROR, message, params);
   }
   
   public void error( String title, String message, Object[] params) {
      alert(title, CCAlert.TYPE_ERROR, message, params);
   }
   
   public void warning(String message) {
      warning(message, null);
   }
   public void warning(String message, Object[] params) {
      alert( CCAlert.TYPE_WARNING, message, params );
   }

   public void info(String message) {
      info(message, null);
   }
   public void info(String message, Object[] params) {
      alert( CCAlert.TYPE_INFO, message, params );
   }
   
}
