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

import java.io.*; 
import java.util.*;

//JAXP
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import javax.xml.transform.sax.SAXResult;
import org.apache.avalon.framework.logger.*;

//FOP
import org.apache.fop.apps.Driver;
import org.apache.fop.messaging.MessageHandler;

// arco
import com.sun.grid.arco.model.*;
import com.sun.grid.arco.ResultManager;
import com.sun.grid.logging.SGELog;
import com.sun.grid.arco.ExportContext;
import com.sun.grid.arco.model.Result;
import com.sun.grid.arco.ChartManager;

import java.util.logging.Level;

public class PDFResultExport extends AbstractXSLResultExport {

   public final static String XSL_MASTER = "xslt/pdf_master.xsl";
   private File basedir;
   
   /** Creates a new instance of PDFResultExport */
   public PDFResultExport( File basedir ) {
      super( "application/pdf");
      this.basedir = basedir;
   }
   
   protected Reader createXSLReader( ExportContext ctx  ) throws IOException {
      
      Result result = ctx.getXmlResult();
      
      StringWriter xsl = new StringWriter();
      
      PrintWriter pw = new PrintWriter( xsl );
      this.writeXSL( ctx, pw );

      pw.flush();
      String str = xsl.getBuffer().toString();
      
      if( SGELog.isLoggable(Level.FINE)) {
         SGELog.fine("xsl doc ------------\n{0}\n-------", str);
      }
      return new StringReader( str );
   }
   
   public void export( ExportContext ctx ) throws IOException, TransformerException {
      
         OutputStream out = ctx.getOutputStream();
         
         StringWriter swr = new StringWriter();
         
         ResultManager.save( ctx.getXmlResult(), swr, ResultManager.createJAXBContext() );
         
         StreamSource xslStream = new StreamSource( createXSLReader(ctx)  );
         StreamSource xmlStream = new StreamSource( new StringReader( swr.getBuffer().toString() ) );         
         
         
         Driver driver = new Driver();//Construct driver
         
         java.util.logging.Logger fopLogger = java.util.logging.Logger.getAnonymousLogger();
         
         fopLogger.setParent( SGELog.getLogger() );
         fopLogger.setLevel( java.util.logging.Level.WARNING );
         Logger logger = new Jdk14Logger(fopLogger);

         
         driver.setLogger(logger);
         
         MessageHandler.setScreenLogger(logger);
         driver.setRenderer(Driver.RENDER_PDF);//Setup Renderer (output format)
         
         driver.setOutputStream(out);
         TransformerFactory factory = TransformerFactory.newInstance();//Setup XSLT
         Transformer transformer = factory.newTransformer(xslStream);

         //Resulting SAX events (the generated FO) must be piped through to FOP
         SAXResult res = new SAXResult(driver.getContentHandler());
         //Start XSLT transformation and FOP processing
         transformer.transform(xmlStream, res);

   }
   
   private void dumpFile( String file, PrintWriter pw, String token, String replacement ) throws IOException {

      FileReader fr = new FileReader( new File( basedir, file ) );
      BufferedReader rd = new BufferedReader( fr );
      
      String line = null;
      
      StringBuffer buffer = new StringBuffer();
      int index = 0;
      int len = token.length();
      while( (line=rd.readLine()) != null ) {
         buffer.setLength(0);
         buffer.append( line );
         index = buffer.indexOf( token );
         if( index > 0 ) {
            buffer.replace( index, index+len, replacement );
         }
         pw.println( buffer );
      }
   }
   
   private void dumpFile( String file, Writer writer ) throws IOException {

      FileReader fr = new FileReader( new File(basedir, file ) );
      char [] buf = new char[4096];
      int len = 0;
      try {
         while(  (len=fr.read( buf )) > 0 ) {
            writer.write( buf, 0, len );
         }
      } finally {
         fr.close();
      }
   }
   
   private void writeYetNotImplemented( String text, PrintWriter pw ) {
      pw.print( "<fo:block color='red'>" );
      pw.print( text );
      pw.println( "</fo:block>");
   }
   
   private boolean writeXSLView( ExportContext ctx, PrintWriter pw ) throws IOException {
      
      com.sun.grid.arco.model.Result result = ctx.getXmlResult();
      
      ViewConfiguration view = result.getView();
      List visibleElements = null;
      if( view != null ) {
         visibleElements = com.sun.grid.arco.Util.getSortedViewElements( view );
      }
 
      boolean ret = false;
      if( visibleElements != null && !visibleElements.isEmpty() ) {
         Iterator iter = visibleElements.iterator();
         ViewElement elem = null;
         
         while(iter.hasNext()) {
            elem = (ViewElement)iter.next();
            if( elem instanceof Table ) {
               writeTable(ctx,pw);
               ret = true;
            } else if ( elem instanceof Pivot ) {
               writePivot(ctx, pw);
               ret = true;
            } else if ( elem instanceof Graphic ) {
               writeChart( ctx, pw );
               ret = true;
            }
         }
      } else {
          writeTable(ctx, pw);
          ret = true;
      }
      return ret;
   }
   
   private void writePivot( ExportContext ctx, PrintWriter pw ) throws IOException {
      
      StringWriter sw = new StringWriter();
      PrintWriter pivotPW = new PrintWriter( sw );
      
      PDFTablePrinter tablePrinter = new PDFTablePrinter();
      PivotModel pivotModel = ctx.getQueryResult().createPivotModel(ctx.getLocale());
      PivotTableGenerator pivotGen = new PivotTableGenerator( pivotModel, tablePrinter );
      
      pw.println("<fo:block padding-before='1cm'>");
      pivotGen.print( pw );
      pw.println( "</fo:block>");
      
   }
   
   private String calculateColumnWidth( ExportContext ctx ) {
      
      com.sun.grid.arco.model.Result result = ctx.getXmlResult();
      
      int width [] = new  int[ result.getColumn().size() ];
      
      com.sun.grid.arco.model.ResultColumn column = null;
      for( int i = 0 ; i < width.length; i++ ) {
         column = (com.sun.grid.arco.model.ResultColumn)result.getColumn().get( i );         
         width [i] = column.getName().length();         
      }
      
      Iterator iter = result.getRow().iterator();
      com.sun.grid.arco.model.ResultRow row = null;
      String value = null;
      
      while( iter.hasNext() ) {
         row = (com.sun.grid.arco.model.ResultRow)iter.next();
         for( int i = 0; i < width.length; i++ ) {
            value = (String)row.getValue().get(i);
            width[i] = Math.max( width[i], value.length() );
         }
      }
      
      StringBuffer buf = new StringBuffer();
      for( int i = 0; i < width.length; i++ ) {
         buf.append( "<fo:table-column column-width='");
         buf.append( width[i]*3 );
         buf.append( "mm'/>\n");
         
      }
      return buf.toString();
   }
   
   
   private void writeChart( ExportContext ctx, PrintWriter pw ) throws IOException {
      
      File chartFile = null;
      
      try {
         ChartManager chartManager = new ChartManager();
         
         chartFile = File.createTempFile( "arco", ".jpg" );
         
         FileOutputStream fo = new FileOutputStream( chartFile );
         
         chartManager.writeChartAsPNG(ctx.getQueryResult(), fo, ctx.getLocale());
         
         fo.flush();
         fo.close();
         
         dumpFile( "xslt/pdf_graphic.xsl", pw, "@@FILE@@", chartFile.toURL().toString() );
      } catch( com.sun.grid.arco.chart.ChartException ce ) {
         pw.println( "<fo:block color='red'>");
           ce.getMessage();
         pw.println( "</fo:block>");
      } catch( java.net.MalformedURLException mfue ) {
         IllegalStateException ilse = new IllegalStateException("file.toUrl failed");
         ilse.initCause( mfue );
         throw ilse;
      }
      finally {
         if( chartFile != null && chartFile.exists() ) {
            ctx.addTempFile( chartFile );
         }
      }
      
      
      
   }
   
   private void writeContent( ExportContext ctx, PrintWriter pw ) throws IOException {
         dumpFile( "xslt/pdf_firstpage.xsl", pw );
         writeXSLView( ctx, pw );
   }
   
   private void writeXSL( ExportContext ctx , PrintWriter pw ) throws IOException {

      File master = new File( basedir, XSL_MASTER );
      
      BufferedReader rd = new BufferedReader( new FileReader( master ));
      
      String line = null;
      String str = null;
      while( (line=rd.readLine()) != null ) {
         str = line.trim();
         if( str.equals( "<!--@@CONTENT@@-->" ) )
         {
            writeContent( ctx, pw);
         } else {
            pw.println( line );
         }
      }
   }
   
   
   // --------------------   Table ---------------------------------------------
   
   public void writeTable( ExportContext ctx, PrintWriter pw ) throws IOException {

      
      PDFTablePrinter tablePrinter = new PDFTablePrinter();
      TableGenerator tableGen = new TableGenerator(ctx.getQueryResult(), tablePrinter);
      
      pw.println("<fo:block padding-before='1cm'>");
      tableGen.print(pw, ctx.getLocale() );
      pw.println( "</fo:block>");
   }
   
   
}
