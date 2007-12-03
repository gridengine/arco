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
 *   Copyright: 2007 by Sun Microsystems, Inc.
 *
 *   All Rights Reserved.
 *
 ************************************************************************/
/*___INFO__MARK_END__*/

package com.sun.grid.reporting.dbwriter;

public class ReportingBatchException extends ReportingException {
   private Object lineNumber = null;
   
   /** Creates a new instance of ReportingBatchException */
   public ReportingBatchException() {
   }
   
   
   /**
    * Constructs an instance of <code>ReportingBatchException</code> with the specified detail message 
    * @param msg the detail message.
    */
   public ReportingBatchException(String msg) {
      super(msg);
   }
   
   /**
    * Constructs an instance of <code>ReportingBatchException</code> with the specified detail message and the 
    * line number that has caused the error
    * @param msg the detail message.
    * @param Object lineNumber
    */
   public ReportingBatchException(String msg, Object lineNumber) {
      super(msg, lineNumber);
      this.lineNumber = lineNumber;  
   }
   
   /**
    * @return lineNumber - line number that caused error. Rollback (clears preparedStatements), remove the wrong
    * line from the list of parsed lines and process again.
    *
    * returns null if it was not able to detect which line caused the error. Error might've been in 
    * getting the PreparedStatement, connection could've been closed. In that case we want to rollback, 
    * set checkpoint to the last safe point and try again with next iteration
    */
   public Object getLineNumber() {
      return lineNumber;
   }
}
