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

import java.io.*;
import java.util.*;
import com.sun.grid.logging.SGELog;

/**
 * Instances of this class defines the parameters of an export
 */
public class ExportContext {
   
   /** result of the export will be written in a stream */
   public static final int TYPE_STREAM    = 0;
   
   /** result of the export will be stored in a directory */
   public static final int TYPE_DIRECTORY = 1;
   
   /** the result of the query. */
   private QueryResult result;
   
   private OutputStream out;

   /** the file where the output will be written
    *  Can be a directory if the export results in
    *  more then one file.
    *  Only set if type is {@link #TYPE_DIRECTORY}
    */
   private File outputFile;
   
   /** List of tmp files which will be deleted of the export
    *  has been finished.
    */
   private ArrayList tmpFiles;
   
   /**
    *  The type of export ({@link #TYPE_DIRECTORY}, {@link #TYPE_STREAM}).
    */
   private int type;
   
   /**
    *  Index if the exported page.
    */
   private int pageIndex = 0;
   
   /**
    *  Lines per page in the export result.
    */
   private int linePerPage = 20;
   
   /**
    *   The locale of the export result.
    */
   private Locale locale;
   
   private com.sun.grid.arco.model.Result xmlResult;
   
   /**
    * Create a new ExportContext of type {@link #TYPE_DIRECTORY}.
    * @param result      the result of the query
    * @param outputFile  the output directory
    * @param locale      the locale of the export result
    */
   public ExportContext( QueryResult result, File outputFile, Locale locale ) {
      this.result = result;
      this.outputFile = outputFile;
      if( outputFile.isDirectory() ) {
         type = TYPE_DIRECTORY;
      } else {
         type = TYPE_STREAM;
      }
      this.setLocale(locale);
   }
   
   /**
    * Creates a new ExportContext of type {@link #TYPE_STREAM}
    * @param result the result of the query
    * @param writer    the writer
    * @param locale  the locale of the export result
    */
   public ExportContext( QueryResult result, OutputStream out, Locale locale ) {      
      this.result = result;
      this.out = out;
      type = TYPE_STREAM;
      this.setLocale(locale);
   }
   
   /**
    *  Get the result of the query.
    *  @return  result of the query
    */
   public QueryResult getQueryResult() {
      return result;
   }
   
   /**
    *  get the export type
    *  @return export type
    *  @see #TYPE_STREAM
    *  @see #TYPE_DIRECTORY
    */
   public int getType() {
      return type;
   }

   /**
    *  get the output file.
    *  @return the output file or <code>null</code> if the export type
    *          is not {@link #TYPE_STREAM}
    *  @see #getType
    */
   public File getOutputFile() {
      return outputFile;
   }
   
   /**
    *  get the OutputStream
    *  @return the OutputStream of <code>null</code> if the export type 
    *          is not {@link #TYPE_DIRECTORY}
    */
   public OutputStream getOutputStream() {
      return out;
   }
   
   
   /**
    *  add a temp file (will be deleted, when the export has been finished)
    *  @param file the temp file
    */
   public void addTempFile( File file ) {
      if( tmpFiles == null ) {
         tmpFiles = new ArrayList();
      }
      tmpFiles.add( file );
   }
   
   /**
    *  Perform cleanup actions (delete temp files)
    */
   public void cleanup() {
      if( tmpFiles != null ) {
         Iterator iter = tmpFiles.iterator();
         File file = null;
         while( iter.hasNext() ) {
            file = (File)iter.next();
            if( !file.delete() ) {
               SGELog.warning( "Can''t delete file {0}", file.getAbsolutePath() );
            }
         }
      }
   }

   
   /**
    * get index of the page which should be exported
    * @return  index of the page
    */
   public int getPageIndex() {
      return pageIndex;
   }

   /**
    * set the index of the page which should be exported
    * @param pageIndex  index of the page
    */
   public void setPageIndex(int pageIndex) {
      this.pageIndex = pageIndex;
   }

   /**
    *  get the number of lines per page
    *  @return the number of lines per page
    */
   public int getLinePerPage() {
      return linePerPage;
   }

   /**
    * set the number of lines per page
    * @param linePerPage number of lines
    */
   public void setLinePerPage(int linePerPage) {
      this.linePerPage = linePerPage;
   }

   /**
    * get the locale of the export
    * @return the locale of the export
    */
   public Locale getLocale() {
      return locale;
   }

   /**
    * set the locale of the export
    * @param locale  locale of the export
    */
   public void setLocale(Locale locale) {
      this.locale = locale;
   }

   /**
    * Get the xml result of the query result
    * @return  the xml result
    */
   public com.sun.grid.arco.model.Result getXmlResult() {
      if( xmlResult == null ) {
         try {
            xmlResult = getQueryResult().createResult();
         } catch( javax.xml.bind.JAXBException jaxbe ) {
            IllegalStateException ilse = new IllegalStateException("JAXB error: " + jaxbe.getMessage());
            ilse.initCause(jaxbe);
            throw ilse;
         }
      }
      return xmlResult;
   }


   
}
