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
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;


public class ReportingException extends java.lang.Exception {
   
   private Object[] params;
   /**
    * Creates a new instance of <code>ReportingException</code> without detail message.
    */
   public ReportingException() {
   }
   
   
   /**
    * Constructs an instance of <code>ReportingException</code> with the specified detail message.
    * @param msg the detail message.
    */
   public ReportingException(String msg) {
      super(msg);
   }
   
   public ReportingException( String msg, Object param ) {
      this( msg, new Object[] { param } );
   }
   
   public ReportingException( String msg, Object param1, Object param2 ) {
      this( msg, new Object[] { param1, param2 } );
   }
   
   public ReportingException( String msg, Object [] params ) {
      super( msg );
      this.params = params;
   }
   
   

   public Object[] getParams() {
      return params;
   }
   
   public void log() {
      SGELog.severe_p( this, getMessage(), params );
   }

   public String getLocalizedMessage() {

      ResourceBundle bundle = SGELog.getLogger().getResourceBundle();
      String message = getMessage();
      
      if( bundle != null ) {
         try {
            message = bundle.getString(message);
         } catch( MissingResourceException mre ) {
            // do nothing
         }
      }
      
      if( params != null ) {
         message = MessageFormat.format(message, params );
      }      
      return message;      
   }
   
}
