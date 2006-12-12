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
package com.sun.grid.arco.export;

import com.sun.grid.logging.SGELog;
import com.sun.grid.arco.QueryResult;
import com.sun.grid.arco.model.*;
import com.sun.grid.arco.ResultExport;
import com.sun.grid.arco.ExportContext;
import com.sun.grid.arco.ChartManager;
import com.sun.grid.arco.ResultExportManager;
import com.sun.grid.arco.QueryResultException;
import java.io.*;
import java.util.*;

public class HTMLResultExport extends ResultExport {
   
   private File baseDir;
   

   /** Creates a new instance of HTMLResltExport */
   public HTMLResultExport( File baseDir ) {
      
      super( "text/html" );
      this.baseDir = baseDir;
   }
   
   public void export(ExportContext ctx) throws java.io.IOException, 
           javax.xml.transform.TransformerException, QueryResultException {
            
         if( ctx.getType() == ExportContext.TYPE_STREAM ) {
            // Output is a stream
            // print the pageable html
            export( ctx, new PrintWriter( ctx.getOutputStream() ) );

         } else {
            // Output is a directory
            // print in files
            exportInDirectory( ctx );
         }
      
   }
      
   private void export( ExportContext ctx, PrintWriter pw ) throws IOException, QueryResultException {

      QueryResult queryResult = ctx.getQueryResult();
      QueryType query = queryResult.getQuery();
      
      // print header
      
      pw.println("<html>");      
      pw.println("<head>");
      pw.println("<link rel='stylesheet' type='text/css' href='style.css' />" );
      pw.print("<title> - @@ARCO_NAME@@ -");
      pw.print( query.getName() );
      pw.println("</title>");
      pw.println("</head>");
      pw.println("<body>");
      
      ViewConfiguration view = query.getView();
      Vstring description = null;
      Vstring sql = null;
      if( view != null ) {
         description = query.getView().getDescription();
         sql = query.getView().getSql();
      }
              
      // category, description, filter, sql
      int headerCount =  4;
      if( description == null || !description.isVisible() ) {
         headerCount--;
      }
      if( sql == null || !sql.isVisible() ) {
         headerCount--;
      }
      
      String[][] header = new String[headerCount][];
      
      int headerIndex = 0;
      header[headerIndex++] = new String[] { "Category:", query.getCategory() };
      
      if( description != null && description.isVisible() ) {
         header[headerIndex++] = new String[] { "Description:", query.getDescription() };
      }
      header[headerIndex++] = new String[] { "Filter:", "Not implemented" };
      if( sql != null && sql.isVisible() ) {
         header[headerIndex++] = new String[] { "SQL:", query.getSql() };
      }
      
      
      pw.println("<h1 class='TtlTxt'>");
      pw.println( query.getName() );
      pw.println("</h1>");
      
      pw.println("<table>");      

      for( int i = 0; i < header.length; i++ ) {
         pw.println("<tr>");
         pw.print("<td valign=\"top\" align=\"left\">");
         pw.print("<div class='ConTblCl1Div'><span class='LblLev2Txt'><label for='header"+i+"'>");
         pw.print( header[i][0]);
         
         pw.print("</label></span></div></td>");
         pw.print("<td><div class='ConTblCl2Div'><span id='header"+i+"' class='ConDefTxt'>");
         pw.print( header[i][1] == null ? "" : header[i][1] );
         pw.print("</label></span></div></td>");
         pw.println("</tr>");
      }
      pw.println("</table>");
      
      
      // print views
      
      pw.println("<center>");
      
      List visibleElements = null;
      if( view != null ) {
         visibleElements = com.sun.grid.arco.Util.getSortedViewElements(view);
      }

      if( visibleElements != null && !visibleElements.isEmpty() ) {
         Iterator iter = visibleElements.iterator();
         ViewElement elem = null;
         while( iter.hasNext() ) {
            elem = (ViewElement)iter.next();
            pw.println("<div>");
            pw.println("<br>");
            if( elem instanceof Table ) {
               printHtmlTabular( ctx, pw );
            } else if ( elem instanceof Pivot ) {
               printPivot( ctx, pw );
            } else if ( elem instanceof Graphic ) {
               printGraphic( ctx, pw );
            }
            pw.println("</div>");
         }
      } else {      
         // If no view configuration is available, we print a table
         printHtmlTabular(ctx,pw);
      }
      

      pw.println("</center>");
      
      // print footer
      pw.println("</body>");
      pw.println("</html>");
      
   }
   
   
   private void exportInDirectory( ExportContext ctx ) throws IOException, QueryResultException {
      
      File outputFile = new File( ctx.getOutputFile(), "index.html" );
      FileWriter fw = new FileWriter( outputFile );
      
      PrintWriter pw = new PrintWriter( fw );
      
      export( ctx, pw );
      
      // copy style sheet into the target diretory
      
      outputFile = new File( ctx.getOutputFile(), "style.css" );
      FileOutputStream fos = new FileOutputStream( outputFile );
      
      File inputFile = new File( baseDir, getStyleSheet() );
      FileInputStream fin = new FileInputStream( inputFile );
      
      fin.getChannel().transferTo( 0, fin.getChannel().size(), fos.getChannel() );
      
      fin.close();
      fos.close();
      pw.flush();
      pw.close();
      fw.close();      
   }
   

   
   private void printGraphic( ExportContext ctx, PrintWriter pw ) throws IOException {
      
      String url = null;
      if( ctx.getType() == ExportContext.TYPE_STREAM ) {
         // TODO  replace the hard coded url
         url = "/reporting/chart_servlet?timestamp=" + System.currentTimeMillis();
      } else {
         url = "chart.png";
         // Create the chart file
          File file = new File( ctx.getOutputFile(), url );
         try {           
            FileOutputStream fo = new FileOutputStream( file );
            try {
               
               new ChartManager().writeChartAsPNG( ctx.getQueryResult(), fo, ctx.getLocale() );
               pw.print("<img name='chart' src='");
               pw.print(url);
               pw.println("' alt='chart'/>");
            } finally {
               fo.flush();
               fo.close();
            }
            
         } catch( Exception ae ) {
            pw.print( "<div color='red'>");
            pw.print( ae.getLocalizedMessage() );
            pw.println("</div>");
            file.delete(); 
         }
      }
   }
   
   
   /**
    *   Print the pivov configuration 
    *
    *   @param  ctx the export context
    *   @param  pw  the print writer    
    */
   private void printPivot(ExportContext ctx, PrintWriter pw ) {

      TablePrinter tablePrinter = new HTMLTablePrinter("Pivot Table");
      
      PivotModel pivotModel = ctx.getQueryResult().createPivotModel(ctx.getLocale());
      PivotTableGenerator pivotGen = new PivotTableGenerator(pivotModel, tablePrinter );      
      pivotGen.print(pw);
   }
   
   /**
    *  Print the date as table
    *  
    */
    private void printHtmlTabular(ExportContext ctx,  PrintWriter pw) throws QueryResultException {

       SGELog.entering( getClass(), "getPageableHtmlTabular");
       
       QueryResult queryResult = ctx.getQueryResult();
       
       TablePrinter tablePrinter = new HTMLTablePrinter("Database Table");
       TableGenerator tableGen = new TableGenerator(queryResult,tablePrinter);
       
       tableGen.print(pw, ctx.getLocale() );
    }



   
    // Properties
    
    private static java.util.Properties props() {
       return ResultExportManager.getProperties();
    }

    public final static String PROPERTY_STYLE_SHEET = "html.stylesheet";
    
    public static String getStyleSheet() {
        return  props().getProperty( PROPERTY_STYLE_SHEET );
    }
    
    public static final String PROPERTY_TABLE_CLASS = "html.table.class";
    
    public static String getTableClass() {
       return props().getProperty( PROPERTY_TABLE_CLASS, "" );
    }
    
    public final static String PROPERTY_TABLE_TH_CLASS = "html.table.th.class";
    
    public static String getTableHeaderClass() {
       return props().getProperty( PROPERTY_TABLE_TH_CLASS, "" );
    }
    
    public static final String PROPERY_TABLE_TH_TEXT_CLASS = "html.table.th.textClass";
    
    public static String getTableHeaderTextClass() {
       return props().getProperty( PROPERY_TABLE_TH_TEXT_CLASS, "" );
    }
    
    public void printTHStart( PrintWriter pw ) {
        pw.print( "<th class='" );
        pw.print( getTableHeaderClass() );
        pw.println("'>");
    }
}
