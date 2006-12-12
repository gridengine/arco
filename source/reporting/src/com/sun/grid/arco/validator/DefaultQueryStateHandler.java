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

import java.util.*;

public class DefaultQueryStateHandler implements QueryStateHandler {
   
   private List errors = new ArrayList();
   private List warnings = new ArrayList();
   
   private boolean isRunnable = true;
   private boolean isSaveable = true;
   private boolean isConvertable = true;
   
   /** Creates a new instance of DefaultQueryStateHandler */
   public DefaultQueryStateHandler() {
   }
   
   public void clear() {
      errors.clear();
      warnings.clear();
      isRunnable = true;
   }

   public void addError(String property, String message, Object[] params) {
      errors.add(ValidatorError.createError(property, message, params));
   }

   public void addWarning(String property, String message, Object[] params) {
      warnings.add(ValidatorError.createWarning(property, message, params));
   }
   
   public ValidatorError[] getErrors() {
      ValidatorError [] ret = new ValidatorError[errors.size()];
      errors.toArray(ret);
      return ret;
   }
   
   public ValidatorError[] getWarnings() {
      ValidatorError [] ret = new ValidatorError[warnings.size()];
      warnings.toArray(ret);
      return ret;
   }

   public boolean hasErrors() {
      return errors != null && !errors.isEmpty();
   }

   public boolean hasWarnings() {
      return warnings != null && !warnings.isEmpty();
   }

   public boolean isRunnable() {
      return !hasErrors() && isRunnable;
   }

   public void setRunnable(boolean runnable) {
      isRunnable = runnable; 
   }

   public void setConvertable(boolean convertable) {
      isConvertable = convertable;
   }
   
   public boolean isSaveable() {
      return isSaveable;
   }

   public boolean isConvertable() {
      return isRunnable() && isConvertable;
   }

   public void setSaveable(boolean saveable) {
      this.isSaveable = saveable;
   }
   
}
