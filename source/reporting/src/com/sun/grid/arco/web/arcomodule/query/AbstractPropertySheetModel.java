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

import com.sun.web.ui.model.CCPropertySheetModel;
import com.iplanet.jato.*;
import com.iplanet.jato.model.Model;
import com.iplanet.jato.view.View;
import com.iplanet.jato.view.ContainerView;
import com.sun.grid.arco.web.arcomodule.QueryModel;
import com.sun.grid.arco.web.arcomodule.QueryViewBean;
import com.sun.web.ui.view.html.CCTextField;
import com.sun.web.ui.view.html.CCTextArea;
import com.sun.web.ui.view.html.CCImageField;
import java.io.*;
import com.sun.grid.logging.SGELog;
import com.sun.grid.arco.web.ArcoServletBase;
import com.sun.grid.arco.web.arcomodule.util.Util;
import com.sun.grid.arco.web.arcomodule.ArcoServlet;

public class AbstractPropertySheetModel extends CCPropertySheetModel {

   public static final String CHILD_IMAGE_URL_TEXTFIELD = "ImageUrlTextField";
   public static final String CHILD_IMAGE_URL_IMAGE     = "ImageUrlImage";
   public static final String CHILD_CATEGORY_TEXTFIELD = "CategoryTextField";
   public static final String CHILD_DESCRIPTION_TEXTAREA = "DescriptionTextArea";
   public static final String CHILD_SQL_TEXTAREA = "SQLTextArea";
   
   protected AbstractPropertySheetModel(String resourceName) {
      super(Util.getInputStream(AbstractPropertySheetModel.class,resourceName));
   }
   
   public View createChild(View view, String name) {

      if( name.equals(CHILD_IMAGE_URL_TEXTFIELD) ) {
         return new CCTextField(view,ArcoServlet.getQueryModel(),name, "/imgURL", null );
      } else if ( name.equals( CHILD_IMAGE_URL_IMAGE ) ) { 
         return new CCImageField(view,ArcoServlet.getQueryModel(),"/imgURL", null);
      } else if( name.equals(CHILD_CATEGORY_TEXTFIELD) ) {
         return new CCTextField(view,ArcoServlet.getQueryModel(),name, "/category", null );
      } else if( name.equals(CHILD_DESCRIPTION_TEXTAREA) ) {
         return new CCTextArea(view,ArcoServlet.getQueryModel(),name, "/description", null );
      } else if( name.equals(CHILD_SQL_TEXTAREA) ) {
         QueryModel model = ArcoServlet.getQueryModel();
         CCTextArea ret = new CCTextArea(view,model,name, "/sql", null );
         ret.setDisabled(model.isSimple());
         return ret;         
      } else {
         return super.createChild(view, name);
      }
   }
   
   
   private static String getPropDoc(String resourceName) {
      java.io.InputStream in = Util.getInputStream(AbstractPropertySheetModel.class,resourceName);
      Reader rd = new InputStreamReader(in);
      
      StringBuffer buffer = new StringBuffer();
      
      try {
         char buf[] = new char[200];
         int len = 0;
         while( (len=rd.read(buf)) > 0 ) {
            buffer.append(buf,0,len);
         }
         SGELog.info("Content of " + resourceName + "\n" + buffer.toString());
         return buffer.toString();
      } catch( IOException ioe ) {
         IllegalStateException ilse = new IllegalStateException("IO Error while reading " + resourceName);
         ilse.initCause(ioe);
         throw ilse;
      }
   }
   

   

   
}
