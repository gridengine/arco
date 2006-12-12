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

import com.sun.grid.arco.*;
import com.sun.grid.logging.SGELog;

public class ExportResultServlet extends HttpServlet {
   
   public static final String ATTR_RESULT = "RESULT";
   
   private ResultExportManager exportManager;
   

   public void init() throws ServletException {
      exportManager = new ResultExportManager( ArcoServlet.getApplDir() );
   }

   public static void setResultInRequest(HttpServletRequest req, QueryResult result) {
      req.setAttribute( ATTR_RESULT, result);
   }
   
   public static QueryResult getResultFromRequest(HttpServletRequest req) {
      ResultModel resultModel = ArcoServlet.getResultModel(req);
      if( resultModel != null ) {
         return resultModel.getQueryResult();
      } else {
         return null;
      }
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
      handle(req, resp );
   }
   
   protected void doPost(HttpServletRequest req,
                         HttpServletResponse resp)
                  throws ServletException,
                         java.io.IOException   {
      handle(req, resp );
   }
   
   /**
    *  handle a request
    *
    *  @param  req  the request object
    *  @param  resp the repsonse object
    *  @throws IOException 
    */
   private void handle( HttpServletRequest req,
                         HttpServletResponse resp)
                  throws ServletException,
                         java.io.IOException   {
      
      String exportType = req.getParameter( "type" );
      String uri = req.getRequestURI();
      if( uri.endsWith("Result")) {
         resp.sendRedirect( uri + "." + exportType + "?type=" + exportType );
      } else {
      
         OutputStream out = resp.getOutputStream();
         PrintWriter pw = null;

         pw = new PrintWriter(new OutputStreamWriter(out));

         QueryResult result = getResultFromRequest(req);

         if( result == null ) {
            
            resp.setContentType( "text/html" );
            pw.println("<html>");
            pw.println("<body>");
            pw.print("<H1 color='red'> Error </H1>");
            pw.print( "No result available" );
            pw.println("</body>");
            pw.println("</html>");
            pw.flush();
         } else {

            String orgContentType = "text/html";

            try {

               String mimeType = exportManager.getMimeType( exportType );

               if( mimeType != null ) {
                  resp.setContentType( mimeType );               
                  exportManager.export( exportType, result, out, req.getLocale() );
                  out.flush();
               } else {
                  throw new ServletException("Unknown exportTyp " + exportType );
               }
            /*} catch( javax.xml.transform.TransformerException tfe ) {
                ServletException sve = new ServletException("Transformation Error: " + tfe.getMessageAndLocation() );
                sve.initCause( tfe );
                throw sve;         */
            } catch( Throwable e ) {
               SGELog.severe( e, "Can''t export result: {0}", e.getMessage() );
               resp.setContentType( "text/html" );
               pw.println("<html>");
               pw.println("<body>");
               pw.print("<H1 color='red'> Error </H1>");
               pw.print( "Can't export result: " + e.getMessage() );
               pw.println("</body>");
               pw.println("</html>");
               pw.flush();
            }
         }
      }
   }
   
}
