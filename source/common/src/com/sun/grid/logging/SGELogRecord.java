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
package com.sun.grid.logging;

import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * Log Record which is used be SGELog.
 */
public class SGELogRecord extends LogRecord {
   /** the need to infer class flag. */
   private boolean needToInferCaller = true;

   /**
    * Creates a new instance of SGELogRecord.
    * @param level level of the log record
    * @param msg   msg of the log record
    */
   public SGELogRecord(final Level level, final String msg) {
      super(level, msg);
   }

   /**
    * get the name of the source class which created this.
    * log record
    * @return name of the source class
    */
   public final String getSourceClassName() {
      if (needToInferCaller) {
         inferCaller();
      }
      return super.getSourceClassName();
   }

   /**
    * Set the name of the class that (allegedly) issued the logging request.
    *
    * @param sourceClassName the source class name
    */
   public final void setSourceClassName(final String sourceClassName) {
      super.setSourceClassName(sourceClassName);
      needToInferCaller = false;
   }

   /**
    * get the name of the source methode which created this log.
    * record
    * @return the name of the source method
    */
   public final String getSourceMethodName() {
      if (needToInferCaller) {
         inferCaller();
      }
      return super.getSourceMethodName();
   }

   /**
    * Set the name of the method that (allegedly) issued the logging request.
    *
    * @param sourceMethodName the source method name
    */
   public final void setSourceMethodName(final String sourceMethodName) {
      super.setSourceMethodName(sourceMethodName);
      needToInferCaller = false;
   }

   /** full qualified name if the class SGELog. */
   public static final String LOG_CLASSNAME = "com.sun.grid.logging.SGELog";

   /**
    * Private method to infer the caller's class and method names.
    */
   private void inferCaller() {
      needToInferCaller = false;
      // Get the stack trace.
      Throwable t = new Throwable();
      StackTraceElement [] stack = t.getStackTrace();
      // First, search back to a method in the SGELog class.
      int ix = 0;
      StackTraceElement frame;
      String cname;
      while (ix < stack.length) {
         frame = stack[ix];
         cname = frame.getClassName();
         ix++;
         if (cname.equals(LOG_CLASSNAME)) {
            break;
         }
      }
      // Now search for the first frame before the "SGELog" class.
      while (ix < stack.length) {
         frame = stack[ix];
         cname = frame.getClassName();
         if (!cname.equals(LOG_CLASSNAME)) {
            // We've found the relevant frame.
            setSourceClassName(cname);
            setSourceMethodName(frame.getMethodName());
            return;
         }
         ix++;
      }
      // We haven't found a suitable frame, so just punt.  This is
      // OK as we are only commited to making a "best effort" here.
   }

}
