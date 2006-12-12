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
package com.sun.grid.reporting.dbwriter.file;

import java.io.*;
import java.util.*;
import com.sun.grid.reporting.dbwriter.db.*;
import com.sun.grid.reporting.dbwriter.ReportingEventObject;
import com.sun.grid.reporting.dbwriter.ReportingException;
import com.sun.grid.logging.SGELog;
import com.sun.grid.reporting.dbwriter.ReportingParseException;
import java.util.logging.Level;

/**
 * This class proccess a report or accouting file and
 * informs all registered <code>NewObjectListener</code>s
 * about new objects.
 * After each processed line the database changes in the database
 * will be commited.
 * 
 */
public class ReportFileReader {
   
   /** Suffix for the checkpoint file */
   public final static String CHECKPOINT_SUFFIX = ".checkpoint";
   
   /** the reporting source */
   private ReportingSource reportingSource;
   
   /** List with all registered <code>NewObjectListeners</code> */
   private List newObjectListeners = new ArrayList();
   
   /** name of the proccessed file */
   private String fileName;
   
   /** delimiter in the file */
   private String delimiter;
   
   /** array with the current database fields */
   protected DatabaseField fields[];
   
   /** map with the current database fields */
   protected Map fieldMap;

   /** this flag signalizes that the processing should stop */
   private boolean stopProcessing;
   
   /** Creates a new instance of ReportingFileReader */
   public ReportFileReader(String p_fileName, String p_delimiter, ReportingSource p_reportingSource) {
      fileName  = p_fileName;
      delimiter = p_delimiter;
      reportingSource = p_reportingSource;
   }
   
   /**
    *  set the base information this <code>ReportFileReader</code>
    *  @param   p_fields    Array with the known database fields
    *  @param   p_fieldMap  Map the the known database fields
    *  @param   p_source    the reporting source
    */
   protected void setInfo(DatabaseField p_fields[], Map p_fieldMap, ReportingSource p_source) {
      fields = p_fields;
      fieldMap = p_fieldMap;
      if (fieldMap == null) {
         fieldMap = createMap(fields);
      }      
      reportingSource = p_source;
   }
   
   /**
    *   stop the current run of the <code>ReportFileReader</code>
    */
   public void stop() {
      // System.err.println( this + ".stop called" );
      stopProcessing = true;
   }
   
   /**
    * Determine of the <code>stop</code> method has been called
    * @return  true   the <code>stop</code> method has been called
    */
   public boolean isStopped() {
      return stopProcessing;
   }
   
   /**
    *   Create a map with database fields
    *   @param   fields    array of database fields which will be stored in the
    *                      returned map
    *   @return  the map with the database fields, key the name of the fields
    */
   protected Map createMap(DatabaseField fields[]) {
      Map map = new HashMap();
      for (int i = 0; i < fields.length; i++) {
         map.put(fields[i].getName(), fields[i]);
      }
      
      return map;
   }
   
   /**
    *  get the reporting source of the reader
    *  @return the reporting source
    */
   public ReportingSource getReportingSource() {
      return reportingSource;
   }
   
   /**
    *  register a <code>NewObjectListener</code>
    *  @param l   the <code>NewObjectListener</code>
    */
   public void addNewObjectListener(NewObjectListener l) {
      if (!newObjectListeners.contains(l)) {
         newObjectListeners.add(l);
      }
   }
   
   /**
    *  get an array with all registered <code>NewObjectListener</code>s
    *  @return array with all registered <code>NewObjectListener</code>s
    */
   public NewObjectListener[] getNewObjectListeners() {
      return (NewObjectListener[]) newObjectListeners.toArray();
   }
   
   /**
    *  remove a registered <code>NewObjectListener</code>
    *  @param  the <code>NewObjectListener</code> which will be removed
    */
   public void removeNewObjectListener(NewObjectListener l) {
      newObjectListeners.remove(l);
   }
   
   /**
    *  Write a checkpoint file
    *  @param  fileName    name of the file ( ".checkpoint" is added );
    *  @param  checkpoint  linenumber of the next line which has to be processed
    */
   public void writeCheckpoint(String fileName, int checkpoint) {
      try {         
         FileWriter fw = new FileWriter( fileName + CHECKPOINT_SUFFIX );
         PrintWriter pw = new PrintWriter( fw );
         pw.println( checkpoint );
         fw.flush();
         fw.close();
//         SGELog.info( "Checkpoint {0} written to file {1}", 
//                      new Integer( checkpoint ), fileName );
      }
      catch( IOException ioe ) {
         SGELog.severe( ioe, "ReportFileReader.writeCheckpointError" );
      }
   }
   
   /**
    *  reads a checkpoint of a file
    *  @param    fileName   name of the file
    *  @return   line number of the checkpoint or <code>-1</code> if no 
    *            checkpoint was found
    */
   private int readCheckpoint(String fileName) {
      int checkpoint = -1; // -1 means: invalid checkpoint
      
      File checkpointFile = new File(fileName + CHECKPOINT_SUFFIX);
      if (checkpointFile.exists()) {
         BufferedReader reader = getFileReader(checkpointFile);
         if (reader != null) {
            try {
               String line = reader.readLine();
               if (line == null) {
                  // end of file reached
               } else if (line.length() == 0) {
                  // skip empty lines
               } else {
                  try {
                     checkpoint = Integer.parseInt(line);
                  } catch (NumberFormatException e) {
                     SGELog.warning( "ReportFileReader.invalidCheckpoint", line );
                     checkpoint = -1;
                  }
               }
            } catch (IOException e) {
               SGELog.warning( e, "ReportFileReader.checkpointIOError", e.getMessage() );
               checkpoint = -1;
            } finally {
               // close the checkpoint file
               try {
                  reader.close();
               } catch( IOException ce ) {
                  SGELog.warning( ce, "ReportFileReader.checkpointCloseError", ce.getMessage() );
               }
            }
         }
         // now delete the checkpoint file
         if( !checkpointFile.delete() ) {
            SGELog.warning( "ReportFileReader.deleteCheckpointFileError", checkpointFile.getAbsolutePath() );
         }
      }
      
      if (checkpoint == -1) {
         SGELog.warning( "ReportFileReader.noCheckpointFound" );
      }
      
      return checkpoint;
   }
   
   /**
    *   process a report file and write its contents into a database
    *   @param  database   the database object
    */
   public void processFile( Database database ) throws ReportingException {
      
      stopProcessing = false;
      
      File originalFile   = new File(fileName);
      File workingFile    = new File(fileName + ".processing");
      // we still have a file left from a previous run. It probably wasn't
      // completely processed, so read checkpoint and process the rest
      if (workingFile.exists()) {
         SGELog.warning("ReportFileReader.workingFileExists", workingFile.getName());
         
         // read checkpoint
         int firstLine = readCheckpoint(fileName);
         if (firstLine >= 0) {
            // if checkpoint is valid: process file
            parseFile( database, fileName, workingFile, firstLine);
         } else {
            // if reading the checkpoint failed: delete file
            if( !workingFile.delete() ) {
               SGELog.warning( "ReportFileReader.workingFileDeleteError", workingFile.getName() );
            }
         }
      }
      
      if( !isStopped() ) {
         if (!originalFile.exists()) {
            SGELog.config( "ReportFileReader.orignalFileNotExists", originalFile.getName() );
         } else {
            SGELog.info("ReportFileHeader.renameFile", originalFile.getName(), workingFile.getName());

            // move away the file used by qmaster
            if( !originalFile.renameTo(workingFile) ) {
               SGELog.severe( "ReportFileReader.renameError", originalFile.getName(), workingFile.getName() );            
            } else {
               // parse the renamed file
               parseFile(database, fileName, workingFile, 0);
            }
         }
      }
   }
   
   /**
    * get a buffered reader on a file
    * @return the buffered reader or <code>null</code> if the 
    *          the file could not be opened
    */
   static public BufferedReader getFileReader(File file) {
      BufferedReader reader;
      try {
         reader = new BufferedReader(new FileReader(file.toString()));
      } catch (FileNotFoundException e) {
         reader = null;
         SGELog.severe( e, "ReportFileHeader.cantOpenFile", e.getMessage() );
      }
      
      return reader;
   }
   
   /**
    *  parse a file
    *  the parsing will stop when the <code>stopProcessing</code> method is called
    *  @param   database    the database
    *  @param   fileName    name of the file
    *  @param   file        the file object
    *  @param   firstline   first line which should be proccessed
   *  
    */
   private void parseFile( Database database, String fileName, File file, int firstLine) throws ReportingException 
   {
      SGELog.fine( "ReportFileHander.parseFile", file.getName() );
      BufferedReader reader = getFileReader(file);
      if (reader != null) {
         int lineNumber = 0;   // successfully parsed lines
         long startTime = System.currentTimeMillis();
         boolean eofReached = false;         
         String line;
         
         java.sql.Connection connection = database.getConnection();
         
         while( !isStopped() && !Thread.currentThread().isInterrupted() ) 
         {
            try {
               // read the next line
               line = reader.readLine();
            } catch( InterruptedIOException iioe ) {
               // System.err.println("InterruptedIOException");
               SGELog.info( "ReportFileHandler.interrupted" );
               break;
            } catch (IOException readException) {
               // System.err.println("IOError" + readException.getMessage() );
               SGELog.warning( readException, "ReportFileReader.ioError", fileName, readException.getMessage() );
               break;
            }
            
            lineNumber++;
            // now we got a valid line or eof
            if (line == null) {
               // end of file reached
               eofReached = true;
               break;
            } else if (lineNumber < firstLine) {
               // skip lines up to firstLine (checkpoint)
               continue;
            } else if (line.length() == 0) {
               // skip empty lines
               continue;
            } else if (line.charAt(0) == '#') {
               // skip comment lines
               continue;
            } else {
               if( lineNumber % 100 == 0 ) {
                  SGELog.config( "ReportFileReader.processLine", new Integer(lineNumber) );
               }
               try {
                  parseLine(line, connection);
                  database.commit( connection );
               } catch( ReportingParseException rpe ) {
                  // the line could not be parsed, write a error message
                  // and continue with the next line
                  SGELog.severe_p(rpe, "ReportFileReader.errorInLine", 
                                  new Object [] { new Integer (lineNumber), line } );
               } catch (ReportingException e) {          
                  // System.err.println("ReportingException " + e.getMessage() );
                  SGELog.severe( "ReportFileReader.errorInLine", new Integer (lineNumber), line );
                  database.rollback( connection );
                  if( !isStopped() ) {
                     e.log();
                  }
                  // Problem: we need to distinguish which kind of
                  //          exception occured
                  //  If a invalid line in the file has produced this exception
                  //  this may lead to an endless loop.

                  break;
               }
            }
         }

         database.release( connection );
         
         try {
            reader.close();
         } catch (IOException e) {
            SGELog.warning( e, "ReportFileHander.closeError", fileName, e.getMessage() );
         }
      
         // delete file after it was completely processed
         if ( eofReached ) {
            // System.err.println( "eofReached" );
            SGELog.info( "ReportFileReader.deleteFile", file.getName() );
            if( !file.delete() ) {
               SGELog.severe( "ReportFileReader.deleteFileFailed", file.getName() );
            }
         } else if ( lineNumber > 0 ) {
            // System.err.println("writeCheckpoint");
            // if the thread was interrupted set back the interupted 
            // to ensure that the checkpoint file can be written
            Thread.currentThread().interrupted();
            
            writeCheckpoint(fileName, lineNumber );
         }
         
         
         if( SGELog.isLoggable( Level.INFO ) ) {
            double secs = (double)(System.currentTimeMillis() - startTime ) / 1000;
            double speed;
            if( Math.abs( secs ) > 0.00001 ) {
               speed = lineNumber / secs;
            } else {
               speed = 0;
            }
            
            
            SGELog.info( "Processed {0} lines in {1,number,#.##}s ({2,number,#.##} lines/s)",
                         new Integer( lineNumber ),
                         new Double( secs ),
                         new Double( speed ) );                         
         }
      }
   }

   /**
    *  parse a line
    *  @param   line   the line 
    *  @throws ReportingParseExcetpion of the line contains an invalid content
    *  @throws ReportingException on any error
    */
   protected void parseLine(String line, java.sql.Connection connection ) throws ReportingException {
      
      String[] splitLine = line.split(delimiter, -100);

      if( splitLine == null ) {
         throw new ReportingParseException("ReportFileReader.lineHasNoFields" );
      }
      
      // reporting file has different types of line contents
      parseLineType(splitLine);
      
      if (splitLine.length != fields.length) {

         for (int i = 0; i < splitLine.length; i++) {
            SGELog.warning( "ReportFileHeader.field", new Integer(i), splitLine[i] );
         }
         throw new ReportingParseException("ReportFileReader.invalidNumberOfFields", 
                                           new Integer( splitLine.length ), 
                                           new Integer( fields.length ) );
      } else {
         
         
         // Set the values of the fields
         for (int i = 0; i < fields.length; i++) {
            fields[i].setValue(splitLine[i]);
         }

         // notify listener
         ReportingEventObject e = new ReportingEventObject(this, reportingSource, fieldMap);
         for (int i = 0; i < newObjectListeners.size(); i++) {
            NewObjectListener listener = (NewObjectListener) newObjectListeners.get(i);
            listener.handleNewObject(e, connection);
         }
      }
   }
   
   // dummy method: use default values
   protected void parseLineType(String splitLine[]) throws ReportingParseException {
   }
   
}
