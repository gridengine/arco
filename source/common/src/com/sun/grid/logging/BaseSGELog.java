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
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 *  Base class for SGELog.
 */
public class BaseSGELog {
   /** logger for SGELog. */
   private static Logger logger;

   /**
    *  It's not possible to instatiate the
    *  BaseSGELog class.
    */
   protected BaseSGELog() {
   }

   /**
    * initialize the SGELog system.
    * @param aLogger the logger
    */
   public static void init(final Logger aLogger) {
      logger = aLogger;
   }
   /**
    * is a level loggable for the SGELog.
    * @param level  the level
    * @return  true  this level is loggable
    */
   public static boolean isLoggable(final Level level) {
        return logger.isLoggable(level);
   }

   /**
    * Create a log record for SGELog.
    * @param level  level of the log record
    * @param msg    msg of the log record
    * @return  the log record
    */
   protected static LogRecord createRecord(final Level level,
                                           final String msg) {
      LogRecord ret = new SGELogRecord(level, msg);
      ret.setResourceBundle(logger.getResourceBundle());
      ret.setResourceBundleName(logger.getResourceBundleName());
      ret.setLoggerName(logger.getName());
      return ret;
   }

   /**
    * get the logger of the SGELog.
    * @return the logger
    */
   public static Logger getLogger() {
      return logger;
   }
}

