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

import javax.servlet.http.*;
import javax.servlet.*;
import java.io.*;
import java.util.*;

import org.jfree.chart.*;
import org.jfree.data.*;

import com.sun.grid.logging.SGELog;
import com.sun.grid.arco.ChartManager;

public class ChartServlet extends HttpServlet {
   
   private ChartManager chartManager;
   
   public void init(ServletConfig config) throws ServletException {
      super.init(config);
      chartManager = new ChartManager();
   }
   
   /**
    *  handle a GET request
    *
    *  @param  req  the HTTP request object
    *  @param  resp the HTTP response object
    *  @throws ServletException
    *  @throws IOException
    */
   protected void doGet(HttpServletRequest req,
   HttpServletResponse resp)
   throws ServletException,
   java.io.IOException {  

      SGELog.info( "entry" );
      try {
         
         ResultModel model =ArcoServlet.getResultModel(req);

         if(model == null ) {
            SGELog.severe("No result found");
            resp.setContentType("text/html");
            PrintWriter wr = resp.getWriter();
            wr.println("<html>");
            wr.println("<body>");
            wr.println("<p>");
            wr.println("no result found");
            wr.println("</p>");
            wr.println("</body");
            wr.println("<html>");
            
         } else {
            resp.setContentType( "image/png" );      
            OutputStream out = resp.getOutputStream();         

            chartManager.writeChartAsPNG( model.getQueryResult(), out, req.getLocale() );
         }

      } catch( Throwable e ) {
         SGELog.severe( e ,"error {0}", e );
         
         req.setAttribute( "error", e );
         RequestDispatcher disp = this.getServletContext().getRequestDispatcher( "/reporting/jsp/arcomodule/Error.jsp" );
         disp.forward( req, resp );
      } finally {
         SGELog.info( "exit" );
      }
   }

   
   
}
