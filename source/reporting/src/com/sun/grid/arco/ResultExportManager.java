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
package com.sun.grid.arco;

import com.sun.grid.arco.export.*;
import java.util.*;
import java.io.*;

public class ResultExportManager {
   public static final String TYPE_HTML = "HTML";
   public static final String TYPE_CSV  = "CSV";
   public static final String TYPE_PDF  = "PDF";
   public static final String TYPE_XML  = "XML";
   
   Map exportMap = new HashMap();
   
   private void reg( String type, ResultExport resultExport ) {
      exportMap.put( type.toLowerCase(), resultExport );
      exportMap.put( type.toUpperCase(), resultExport );
      
   }
   /** Creates a new instance of ResultExportManager */
   public ResultExportManager( File basedir ) {
      
      reg( TYPE_CSV, new CSVResultExport() );
      reg( TYPE_PDF, new PDFResultExport( basedir ) );
      reg( TYPE_XML, new XMLResultExport() );
      reg( TYPE_HTML, new HTMLResultExport( basedir ) );
   }
   
   private ResultExport getExportInstance( String type ) {
      return (ResultExport)exportMap.get( type );
   }
   
   /**
    *  get the mime type of a export type
    *  @param   exportType the export type
    *  @return  the mimetype or <code>null</code> if  the export type
    *           is unknown
    */
   public String getMimeType( String exportType ) {
      ResultExport export =  getExportInstance( exportType );
      if( export != null ) {
         return export.getMimeType();
      }
      return null;
   }
   
   public void export( String type, QueryResult result, File file, Locale locale )
   throws IOException, javax.xml.transform.TransformerException, QueryResultException {
      
      ResultExport export = getExportInstance( type );
      
      if( export == null ) {
         throw new IllegalArgumentException( "export type " + type + " is unknown");
      }
      
      ExportContext ctx = new ExportContext( result , file, locale );
      try {
         export.export( ctx );
      } finally {
         ctx.cleanup();
      }
      
   }
   /**
    *   export a model
    *
    *   @param  type    the export type (xml, csv, pdf, ...)
    *   @param  model   the model
    *   @param  out     the output stream
    */
   public void export( String type, QueryResult result, OutputStream out, Locale locale )
   throws IOException, javax.xml.transform.TransformerException, QueryResultException {
      
      ResultExport export = getExportInstance( type );
      
      if( export == null ) {
         throw new IllegalArgumentException( "export type " + type + " is unknown");
      }
      
      ExportContext ctx = new ExportContext( result , out, locale );
      try {
         export.export( ctx );
      } finally {
         ctx.cleanup();
      }
   }
   
   private static Properties props;
   public final static  String PROPERTIES = "com/sun/grid/arco/export/ResultExport.properties";
   
   public static Properties getProperties() {
      if( props == null ) {
         try {
            Properties tmpProps = new Properties();
            InputStream in = ResultExportManager.class.getClassLoader().getResourceAsStream( PROPERTIES );
            
            tmpProps.load( in );
            props = tmpProps;
         } catch( IOException ioe ) {
            IllegalStateException ilse = new IllegalStateException( "Can't load properties " + PROPERTIES );
            ilse.initCause( ioe );
            throw ilse;
         }
      }
      return props;
   }
}
