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
package com.sun.grid.reporting.dbwriter;

import com.sun.grid.logging.SGELog;
import java.io.*;

public class TestFileWriter {
   
    PrintWriter pw;
    File  reportingFile;
    File  tmpFile;
    
    public TestFileWriter() throws IOException {
         reportingFile = File.createTempFile( "TestFileWriter", ".txt", null );
         reportingFile.delete();
         tmpFile = File.createTempFile( "TestFileWriter", ".txt", null );
         tmpFile.deleteOnExit();
    }
    
    public File getReportingFile() {
       return reportingFile;
    }
    
    private void initWriters() throws IOException {
       if( pw == null ) {
          pw = new PrintWriter(new FileWriter(tmpFile));
       }
    }

    public PrintWriter getPrintWriter() throws IOException {
       initWriters();
       return pw;
    }
    
    /**
     *  write a line in the reporting file
     *  which contain report variables from a host
     *  @param  timestamp    timestamp of the line
     */
    public void writeHostLine( long timestamp ) throws IOException {    
       initWriters();
       long seconds = timestamp / 1000;
       pw.print( seconds );
       pw.print( ":host:schrotty:");
       pw.print( seconds );
       pw.println( ":X:cpu=1.800000,mem_free=235.679688M,np_load_avg=0.370000");
       pw.flush();
    }
    
    /**
     * Write a line with host load values into the reporting file
     * @param timestamp   timestamp of the line in seconds
     * @param hostname    name of the host
     * @param loadNames   array with the names of the load values
     * @param loadValues  array with the values of the load values
     *
     * @throws java.io.IOException 
     */
    public void writeHostLine(long timestamp, String hostname, String[] loadNames, 
                                              Object [] loadValues)
        throws IOException {
       initWriters();
       pw.print(timestamp);
       pw.print( ":host:");
       pw.print( hostname );
       pw.print( ":" );
       pw.print( timestamp );
       pw.print( ":X:");
       for(int i = 0; i < loadNames.length; i++ ) {
          if( i > 0 ) {
             pw.print(',');
          }
          pw.print(loadNames[i]);
          pw.print('=');
          if(loadValues[i] != null) {
             pw.print(loadValues[i]);
          }
       }
       pw.println();
       pw.flush();
    }
    
    public void writeNewJob(long timestamp, int jobNumber, int taskNumber, String peTaskId, String jobName, String owner, 
                            String group, String project, String department, String account, int priority ) throws IOException {
       initWriters();
       pw.print(timestamp);
       pw.print(':');
       pw.print("new_job");
       pw.print(':');
       pw.print(timestamp);
       pw.print(':');
       pw.print(jobNumber);
       pw.print(':');
       pw.print(taskNumber);
       pw.print(':');
       pw.print(peTaskId == null ? "NONE" : peTaskId);
       pw.print(':');
       pw.print(jobName == null ? "" : jobName );
       pw.print(':');
       pw.print(owner);
       pw.print(':');
       pw.print(group);
       pw.print(':');
       pw.print(project);
       pw.print(':');
       pw.print(department);
       pw.print(':');
       pw.print(account);
       pw.print(':');
       pw.println(priority);
       pw.flush();
    }
    
    public void writeJobLog(long timestamp, String event, int jobNumber, int taskNumber, String peTaskId, String state, String host, String user,
                            long submissionTime, String jobName, String owner, String group, String project, String department, String account, 
                            int priority, String message) throws IOException {
        
       initWriters();
       Object [] fields = new Object [] {
         new Long(timestamp),
         "job_log",
         new Long(timestamp),
         event,
         new Integer(jobNumber),
         new Integer(taskNumber),
         peTaskId == null ? "NONE" : peTaskId,
         state,
         user,
         host,
         new Integer(0),
         new Integer(priority),
         new Long(submissionTime),
         jobName,
         owner,
         group,
         project,
         department,
         account,
         message
       };
       
       for(int i=0; i < fields.length -1; i++) {
          pw.print(fields[i]);
          pw.print(":");          
       }
       pw.print(fields[fields.length-1]);
       pw.flush();
    }
    
    
    public boolean rename() {
       if( pw == null ) {
          throw new IllegalStateException("No line written");
       }
       pw.close();
       pw = null;
       SGELog.fine("rename " + tmpFile + " -> " + reportingFile);
       return tmpFile.renameTo( reportingFile );
    }
    
    public void waitUntilFileIsDeleted() throws InterruptedException {
       
       int i = 0;
       do {
          Thread.sleep(100);
          i++;
       } while( reportingFile.exists() && i < 100 );
       
       File processingFile = new File(reportingFile.getAbsolutePath() + ".processing");
       
       i = 0;
       do {
          Thread.sleep(100);
          i++;
       } while( processingFile.exists() && i < 100 );
    }
   
}
