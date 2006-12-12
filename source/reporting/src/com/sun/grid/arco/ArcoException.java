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

import java.util.ResourceBundle;
import java.util.Locale;
import java.util.MissingResourceException;
import java.text.MessageFormat;
import com.sun.grid.logging.SGELog;

/**
 * ArcoException is a wrapper for any other exception.
 * The message of an ArcoException is a key that specifies
 * the localizable value inside the Exceptions ressource bundle.
 */
public class ArcoException extends Exception {

    private Object[] params;

    /**
     * creates a new wapper for an exception to make the message localizable.
     * @param messageKey the key of the message to be localized.
     */
    public ArcoException(String message ) {
        super(message);
    }

    /**
     * creates a new wapper for an exception to make the message localizable.
     * @param messageKey the key of the message to be localized.
     * @param value inserted into the message if a placeholder exists in the resource bundle (e.g. {0})
     */
    public ArcoException( String messageKey, Object[] params ) { 
       this( messageKey );
       this.params = params;
    }


    public ArcoException(String messageKey, Throwable cause, Object[] params) {
        this(messageKey);
        this.params = params;
        initCause( cause );
    }
    
    public Object[] getParameter() {
       return params;
    }
    
}
