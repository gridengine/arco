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


import com.iplanet.jato.view.View;
import com.sun.web.ui.model.CCPropertySheetModel;
import java.util.Iterator;
import java.util.List;
import java.io.*;
import com.sun.grid.logging.SGELog;
import com.sun.grid.arco.model.*;
import com.sun.grid.arco.web.arcomodule.ResultModel;
import com.sun.grid.arco.web.arcomodule.ArcoServlet;
import com.sun.grid.arco.QueryResult;

public class LateBindingPropertySheetModel extends CCPropertySheetModel  {
   
   private QueryResult queryResult;
   
   /** Creates a new instance of SimplePropertySheetModel */
   public LateBindingPropertySheetModel() {
      ResultModel resultModel = ArcoServlet.getResultModel();
      
      setQueryResult(resultModel.getQueryResult());
   }
   
   public void setQueryResult(QueryResult queryResult) {
      this.queryResult = queryResult;      
      
      clear();
      if( queryResult != null ) {
         QueryType query = queryResult.getQuery();
         setDocument(createDocument(query));
      }
   }

   public View createChild(View view, String name) {
      return super.createChild(view,name);
   }


   private String createDocument(QueryType query) {
      
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      
      pw.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
      pw.println("<!DOCTYPE propertysheet SYSTEM \"com_sun_web_ui/dtd/propertysheet.dtd\">");
      pw.println("<propertysheet>");
      
      pw.println("<section name='latebinding' defaultValue=''>");
      

      List filterList = query.getFilter();
      
      Iterator iter = filterList.iterator();
      
      Filter filter = null;
      while( iter.hasNext() ) {
         filter = (Filter)iter.next();
         if( filter.isActive() && filter.isLateBinding() ) {            
            printTextProperty(pw, filter.getName(), filter.getName() );
            setValue(filter.getName(), filter.getParameter() );
         }
      }
      
      pw.println("</section>");
      pw.println("</propertysheet>");
      
      pw.flush();
      
      
      String ret = sw.getBuffer().toString();
      SGELog.fine("doc is -----\n{0}\n-----", ret);
      return ret;
   }
   

   private static void printTextProperty(PrintWriter pw, String name, String label) {
      pw.println("<property>");
      pw.println("  <label name='" + name + "Label' defaultValue='"+label+"'/>");
      pw.println("  <cc name='"+name+"' tagclass='com.sun.web.ui.taglib.html.CCTextFieldTag'>");
      pw.println("  </cc>");
      pw.println("</property>");
   }

}
