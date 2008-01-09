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

import com.sun.grid.reporting.dbwriter.StatisticManager;
import com.sun.grid.reporting.dbwriter.event.ParserListener;
import java.io.*;
import java.util.*;
import com.sun.grid.reporting.dbwriter.db.*;
import com.sun.grid.reporting.dbwriter.event.RecordDataEvent;
import com.sun.grid.reporting.dbwriter.ReportingException;
import com.sun.grid.logging.SGELog;
import com.sun.grid.reporting.dbwriter.Controller;
import com.sun.grid.reporting.dbwriter.ReportingBatchException;
import com.sun.grid.reporting.dbwriter.ReportingParseException;
import com.sun.grid.reporting.dbwriter.event.CommitEvent;
import com.sun.grid.reporting.dbwriter.event.StatisticListener;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.logging.Level;

/**
 * This class proccess a report or accouting file and
 * informs all registered <code>ParserListener</code>s
 * about new objects.
 * After each processed line the database changes in the database
 * will be commited.
 */
public class FileParser {
   
   /** The sql String for the checkpoint update PreparedStatement */
   public static final  String CHECKPOINT_PSTM = "UPDATE sge_checkpoint SET ch_line = ?, ch_time = ? WHERE ch_id=1";
   
   /** The sql String for selecting the checkpoint from the database */
   public static final  String CHECKPOINT_SELECT = "SELECT ch_line FROM sge_checkpoint WHERE ch_id=1";
   
   /** the reporting source */
   private ReportingSource reportingSource;
   
   /** List with all registered <code>parserListeners</code> */
   private List parserListeners = new ArrayList();
   
   /** name of the proccessed file */
   private String fileName;
   
   /** delimiter in the file */
   private String delimiter;
   
   /** array with the current database fields */
   protected Field fields[];
   
   /** map with the current database fields */
   protected Map fieldMap;
   
   /** this flag signalizes that the processing should stop */
   private boolean stopped;
   
   /** map that stores the fieldMap(s) of the lines that have not been yet commited to
    * the database. The key is the line number, from which the fieldMap was created
    */
   private Map batchMap;
   
   /** contains lines that cause errors. These should be skipped when processing workingFile.
    *  This map needs to be cleared when there is no workingFile is found
    */
   protected Map errorLines;
   
   private PreparedStatement chkPstm = null;
   
   private Controller controller;
   
   /** Creates a new instance of ReportingFileReader */
   public FileParser(String p_fileName, String p_delimiter, ReportingSource p_reportingSource, Controller p_controller) {
      fileName  = p_fileName;
      delimiter = p_delimiter;
      reportingSource = p_reportingSource;
      controller = p_controller;
      batchMap = new HashMap();
      errorLines = new HashMap();
   }
   
   /**
    *  set the base information this <code>FileParser</code>
    *
    * @param p_fields    Array with the known database fields
    * @param p_fieldMap  Map the the known database fields
    * @param p_source    the reporting source
    */
   protected void setInfo(Field p_fields[], Map p_fieldMap, ReportingSource p_source) {
      fields = p_fields;
      fieldMap = p_fieldMap;
      if (fieldMap == null) {
         fieldMap = createMap(fields);
      }
      reportingSource = p_source;
   }
   
   /**
    *   stop the current run of the <code>FileParser</code>
    */
   public void stop() {
      // System.err.println( this + ".stop called" );
      stopped = true;
   }
   
   /**
    * Determine of the <code>stop</code> method has been called
    * @return  true   the <code>stop</code> method has been called
    */
   public boolean isStopped() {
      return stopped;
   }
   
   /**
    *   Create a map with database fields
    *   @param   fields    array of database fields which will be stored in the
    *                      returned map
    *   @return  the map with the database fields, key the name of the fields
    */
   protected Map createMap(Field fields[]) {
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
    *  register a <code>ParserListener</code>
    * @param l   the <code>NParserListener/code>
    */
   public void addParserListener(ParserListener l) {
      if (!parserListeners.contains(l)) {
         parserListeners.add(l);
      }
   }
   
   /**
    *  get an array with all registered <code>ParserListener</code>s
    * @return array with all registered <code>NParserListener/code>s
    */
   public ParserListener[] getParserListeners() {
      return (ParserListener[]) parserListeners.toArray();
   }
   
   /**
    *  remove a registered <code>ParserListener</code>
    * @param the <code>NeParserListenercode> which will be removed
    */
   public void removeParserListener(ParserListener l) {
      parserListeners.remove(l);
   }
   
   private PreparedStatement getPSTM(java.sql.Connection conn) throws SQLException {
      if (chkPstm == null) {
         chkPstm = conn.prepareStatement(CHECKPOINT_PSTM);
      }
      return chkPstm;
   }
   
   /**
    *  Write a checkpoint to the database tabel sge_checkpoint
    *  Checkpoint is written every time a succesful batch execution is performed
    *  @param  checkpoint - linenumber of the next line which has to be processed
    *  @param connection - connection of the batch execution transaction
    *                      the batch execution and writeCheckpoint must be performed in one
    *                      transaction. If there is an error the whole transaction is rolled back.
    */
   public void writeCheckpoint(int checkpoint, java.sql.Connection connection) throws ReportingException {
      try {
         PreparedStatement pstm = getPSTM(connection);
         
         pstm.setInt(1, checkpoint);
         pstm.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
         int update = pstm.executeUpdate();
         if (update != 1) {
            throw new ReportingException("FileParser.checkpointUpdateError", new Integer(update), new Integer(1));
         }
      } catch (SQLException sqle) {
         ReportingException re = new ReportingException(sqle.getMessage());
         re.initCause(sqle);
         throw re;
      }
   }
   
   /**
    *  reads a checkpoint from the database table sge_checkpoint
    *  @param    database - database to connect to
    *  @return   the checkpoint or <code>-1</code> if no
    *            checkpoint was found
    */
   private int readCheckpoint(Database database) throws ReportingException {
      int checkpoint = -1;
      java.sql.Connection connection = database.getConnection();
      
      Statement stmt = database.executeQuery(CHECKPOINT_SELECT, connection);
      ResultSet rs = null;
      try {
         rs = stmt.getResultSet();
         if (rs.next()) {
            checkpoint = rs.getInt(1);
         }
      } catch (SQLException e) {
         SGELog.warning( "FileParser.noCheckpointFound", e.getMessage());
      } finally {
         try {
            rs.close();
            stmt.close();
         } catch (SQLException sqle) {
            //ignore just write to the log
            SGELog.warning("Database.closingObjectsFailed");
         }
         database.release(connection);
      }
      
      return checkpoint;
   }
   
   /**
    *   process a report file and write its contents into a database
    *   @param  database   the database object
    */
   public void processFile( Database database ) throws ReportingException {
      
      stopped = false;
      
      File originalFile   = new File(fileName);
      File workingFile    = new File(fileName + ".processing");
      
      // we still have a file left from a previous run. It probably wasn't
      // completely processed, so read checkpoint and process the rest
      if (workingFile.exists()) {
         SGELog.warning("FileParser.workingFileExists", workingFile.getName());
         
         // read checkpoint
         int firstLine = readCheckpoint(database);
         if (firstLine >= 0) {
            // if checkpoint is valid: process file
            parseFile(database, fileName, workingFile, firstLine);
         } else {
            // if we have received invalid checkpoint (should not happen) we will print log message and rename
            // the file to reporting.invalid.<timestamp>
            SGELog.severe("FileParser.invalidCheckpoint");
            File invalidFile    = new File(fileName + ".invalid." + System.currentTimeMillis());
            // move away the file used by qmaster
            if (!workingFile.renameTo(invalidFile)) {
               SGELog.severe( "FileParser.renameError", workingFile.getName(), invalidFile.getName());
            }
            return;
         }
      }
      
      if( !isStopped() ) {
         if (!originalFile.exists()) {
            SGELog.config( "FileParser.orignalFileNotExists", originalFile.getName() );
         } else {
            SGELog.info("ReportFileHeader.renameFile", originalFile.getName(), workingFile.getName());
            
            // move away the file used by qmaster
            if( !originalFile.renameTo(workingFile) ) {
               SGELog.severe( "FileParser.renameError", originalFile.getName(), workingFile.getName() );
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
    *  the parsing will stop when the <code>stopped</code> method is called
    *
    * @param database    the database
    * @param fileName    name of the file
    * @param file        the file object
    * @param firstline   first line which should be proccessed
    */
   private void parseFile( Database database, String fileName, File file, int firstLine) throws ReportingException {
      SGELog.fine( "ReportFileHander.parseFile", file.getName() );
      BufferedReader reader = getFileReader(file);
      if (reader != null) {
         int lineNumber = 0;   // the line being currently proccessed
         long startTime = System.currentTimeMillis();
         boolean eofReached = false;
         String line;
         long lastTimestamp = 0; //timestamp of last row data to send with CommitEvent
         int checkpoint = firstLine;
         
         SGELog.warning("FileParser.errorLines", new Integer(errorLines.size()));
         java.sql.Connection connection = database.getConnection();
         // Interruption of the thread will set stopped, so the Parser will gracefully
         // finish the line that is currently porcessing
         while (!isStopped() && !Thread.currentThread().isInterrupted())  {
            try {
               // read the next line
               line = reader.readLine();
            } catch( InterruptedIOException iioe ) {
               // System.err.println("InterruptedIOException");
               SGELog.info( "ReportFileHandler.interrupted" );
               break;
            } catch (IOException readException) {
               // System.err.println("IOError" + readException.getMessage() );
               SGELog.warning( readException, "FileParser.ioError", fileName, readException.getMessage() );
               break;
            }
            
            lineNumber++;
            // now we got a valid line or eof
            if (line == null) {
               // end of file reached
               eofReached = true;
               break;
            } else if (lineNumber < firstLine || line.length() == 0
                  || line.charAt(0) == '#' || errorLines.containsKey(new Integer(lineNumber))) {
               // skip lines up to firstLine (checkpoint), empty lines, comment lines
               continue;
            } else {
               if( lineNumber % 100 == 0 ) {
                  SGELog.config( "FileParser.processLine", new Integer(lineNumber) );
               }
               try {
                  // keep the parsed lines in a List per transaction, so we don't have to
                  // process the file again if there was an error but we can process it from List
                  // after commit inform the DerivedValueThread that it can start calculating
                  // derived values
                  parseLine(line, lineNumber, connection);
                  lastTimestamp = getLastTimestamp(line);
                  if(lineNumber % 1000 == 0) {
                     controller.flushBatchesAtEnd(connection);
                     checkpoint = lineNumber + 1;
                     try {
                        writeCheckpoint(checkpoint, connection);
                        database.commit(connection, CommitEvent.BATCH_INSERT, lastTimestamp);
                        errorLines.clear();
                        batchMap.clear();
                     } catch (ReportingException re) {
                        break;
                     }
                  }
               } catch (ReportingParseException rpe) { //happens from parsing a line, invalid number of fields
                  // the line could not be parsed, write a error message
                  // and continue with the next line
                  
                  Integer l = new Integer(lineNumber);
                  SGELog.severe_p(rpe, "FileParser.errorInLine", new Object [] {l, line } );
                  errorLines.put(l, l);
               } catch (ReportingBatchException rbe) { //happens if some statement in batch caused error
                  if (database.test()) {
                     Integer l = (Integer) rbe.getLineNumber();
                     if (l != null) {
                        errorLines.put(l, l);
                        SGELog.severe(rbe, "FileParser.errorInLine", l, batchMap.get(l));
                     }
                  }
                  break;
               } catch (ReportingException e) { //happens from processing Record, binding PreparedStatements, commit,
                  //if the connection is closed it means there might not be anything wrong with the line
                  //and we do not wan to skip it with the next iterration
                  if (database.test()) {
                     errorLines.put(new Integer(lineNumber), new Integer(lineNumber));
                     SGELog.severe( "FileParser.errorInLine", new Integer(lineNumber), line);
                  }
                  if ( !isStopped() ) {
                     e.log();
                  }
                  break;
               }
            }
         }
         
         
         try {
            reader.close();
         } catch (IOException e) {
            SGELog.warning( e, "ReportFileHander.closeError", fileName, e.getMessage() );
         }
         
         try {
            if (eofReached) {
               try {
                  controller.flushBatchesAtEnd(connection);
                  //wee have reached EOF we set the checkpoint to 0
                  writeCheckpoint(0, connection);
                  database.commit(connection, CommitEvent.BATCH_INSERT, lastTimestamp);
                  
                  errorLines.clear();
                  batchMap.clear();
                  SGELog.info( "FileParser.deleteFile", file.getName() );
                  // delete file after it was completely processed
                  if( !file.delete() ) {
                     SGELog.severe( "FileParser.deleteFileFailed", file.getName() );
                     //if we cannot delete the file we set the checkpoint to a bigger number than the total number of
                     //lines in the file. With the next run it should try to delete the file again.
                     writeCheckpoint(lineNumber + 2, connection);
                  }
                  try {
                     long timestamp = createStatistics(startTime, lineNumber, connection);
                     //flush the statistics
                     controller.flushBatchesAtEnd(connection);
                     database.commit(connection, CommitEvent.STATISTIC_INSERT, timestamp);
                  } catch (ReportingException re) {
                     //we don't need to differentiate between BatchUpdateException and ReportingException, since
                     //the statistics will not contain the lineNumber
                     database.rollback(connection);
                  }
                  
               } catch (ReportingBatchException rbe) { //happens if some statement in batch caused error
                  Integer l = (Integer) rbe.getLineNumber();
                  if (l != null) {
                     if(database.test()) {
                        //we only want to do this if the exception did not come from executing the statistics
                        errorLines.put(l, l);
                        SGELog.severe(rbe, "FileParser.errorInLine", l, batchMap.get(l));
                     }
                  }
                  database.rollback(connection);
                  
               } catch (ReportingException e) { //happens from commit, and writeCheckpoint
                  database.rollback(connection);
               }
               
            } else {
               database.rollback(connection);
            }
         } finally {
            //we have to set the PreparedStatement to null, becaue if there was a network error, it is invalidated and
            //needs to be recreated
            chkPstm = null;
            database.release(connection);
         }
      }
   }
   
   private long createStatistics(long startTime, int lineNumber, java.sql.Connection connection) {
      //do the statistics
      double secs = (double)(System.currentTimeMillis() - startTime ) / 1000;
      double speed;
      if( Math.abs( secs ) > 0.00001 ) {
         speed = lineNumber / secs;
      } else {
         speed = 0;
      }
      
      long lastTimestamp = System.currentTimeMillis();
      try {
         fireStatisticEvent(lastTimestamp, "lines_per_second", speed, connection);
      } catch(ReportingException re) {
         SGELog.warning(re, "FileParser.statisticDBError");
      }
      
      if( SGELog.isLoggable( Level.INFO ) ) {
         
         SGELog.info( "Processed {0} lines in {1,number,#.##}s ({2,number,#.##} lines/s)",
               new Integer( lineNumber ),
               new Double( secs ),
               new Double( speed ) );
      }
      
      return lastTimestamp;
   }
   /**
    *  parse a line
    *  @param   line   the line
    *  @throws ReportingParseExcetpion of the line contains an invalid content
    *  @throws ReportingException on any error
    */
   protected void parseLine(String line, int lineNumber, java.sql.Connection connection) throws ReportingException {
      
      String[] splitLine = line.split(delimiter, -100);
      
      if( splitLine == null ) {
         throw new ReportingParseException("FileParser.lineHasNoFields" );
      }
      
      // reporting file has different types of line contents
      parseLineType(splitLine);
      
      if (splitLine.length != fields.length) {
         if (reportingSource == ReportingSource.ACCOUNTING && splitLine.length == fields.length - 1) {
            //it is probably still the accounting line from version prior to 6.2 that did not contain
            //the ar_number. We will add the default 0 to the splitLine
            String [] tmp = new String[splitLine.length + 1];
            System.arraycopy(splitLine, 0, tmp, 0, splitLine.length);
            tmp[tmp.length - 1] = "0";
            splitLine = tmp;
         } else {
            for (int i = 0; i < splitLine.length; i++) {
               SGELog.warning( "ReportFileHeader.field", new Integer(i), splitLine[i] );
            }
            throw new ReportingParseException("FileParser.invalidNumberOfFields",
                  new Integer( splitLine.length ),
                  new Integer( fields.length ) );
         }
      }
      
      // Set the values of the fields
      for (int i = 0; i < fields.length; i++) {
         fields[i].setValue(splitLine[i]);
      }
      
      Object key = new Integer(lineNumber);
      // notify listener
      fireEvent(reportingSource, fieldMap, key, connection);
      //store the lineNumber and line
      batchMap.put(key, line);
   }
   
   private long getLastTimestamp(String line) {
      if (line == null) {
         SGELog.info("line is null");
         return -1;
      }
      String[] splitLine = line.split(delimiter,  2);
      if (splitLine == null) {
         return -1;
      } else  {
         long seconds = Long.parseLong(splitLine[0]);
         long milliseconds = seconds * 1000;
         return milliseconds;
      }
   }
   
   private void fireEvent(ReportingSource source, Map fieldMap, Object lineNumber, java.sql.Connection connection) throws ReportingException {
      RecordDataEvent e = new RecordDataEvent(this, source, fieldMap, lineNumber);
      for (int i = 0; i < parserListeners.size(); i++) {
         ParserListener listener = (ParserListener) parserListeners.get(i);
         listener.processParserData(e, connection);
      }
   }
   
   protected void fireStatisticEvent(long ts, String variable, double value, java.sql.Connection connection) throws ReportingException {
      RecordDataEvent e = StatisticManager.createStatisticEvent(this, ReportingSource.DBWRITER_STATISTIC, ts, "dbwriter", variable, value);
      for (int i = 0; i < parserListeners.size(); i++) {
         if (parserListeners.get(i) instanceof StatisticListener) {
            StatisticListener listener = (StatisticListener) parserListeners.get(i);
            listener.processStatisticData(e, connection);
         }
      }
   }
   
   
// dummy method: use default values
   protected void parseLineType(String splitLine[]) throws ReportingParseException {
   }
   
}
