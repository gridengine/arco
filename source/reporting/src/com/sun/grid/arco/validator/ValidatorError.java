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
package com.sun.grid.arco.validator;

public class ValidatorError {
   
   public static final int TYPE_ERROR    = 1;
   public static final int TYPE_WARNING  = 2;
   
   private int type;
   private String property;
   private String message;
   private Object [] params;
   
   protected ValidatorError(int type, String property, String message, Object[] params) {
      this.type = type;
      this.property = property;
      this.message = message;
      this.params = params;
   }
   
   public static ValidatorError createError(String property, String message, Object [] params) {
      return new ValidatorError(TYPE_ERROR, property, message, params);
   }
   
   public static ValidatorError createWarning(String property, String message, Object [] params) {
      return new ValidatorError(TYPE_WARNING, property, message, params);
   }

   public String getProperty() {
      return property;
   }

   public String getMessage() {
      return message;
   }

   public Object[] getParams() {
      return params;
   }

   public boolean isError() {
      return type == TYPE_ERROR;
   }
   
   public boolean isWarning() {
      return type == TYPE_WARNING;
   }
}
