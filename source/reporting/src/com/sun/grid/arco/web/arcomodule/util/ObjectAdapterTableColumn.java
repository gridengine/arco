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
package com.sun.grid.arco.web.arcomodule.util;

import com.iplanet.jato.model.Model;

/**
 * Instances of these class represent a column in a action table.
 *
 */
public class ObjectAdapterTableColumn extends AbstractTableColumn {
   
   
   /** path to the object in the model. */
   private String boundPath;
   
   /** bound value in the model. */
   private String boundName;
   
   
   /**
    * the class of the bean.
    * @param name       real name of the column
    * @param boundPath  path the object with the property
    * @param boundName  bound name of the column in the model
    * @param title      title of the column
    */
   
   public ObjectAdapterTableColumn(String name, String boundPath, String boundName, String title) {
      super(name, title);
      this.setBoundPath(boundPath);
      this.boundName = boundName;
   }
   
   private String getPath(int row ) {
      return getBoundPath() + "[" + row + "]/" + boundName;
   }
   
   /**
    * Get the value of the column from a model.
    * @param model   the model
    * @return  the value of this column
    */
   public Object getValue(Model model, int row) {
      return model.getValue( getPath(row) );
   }
   
   public Object[] getValues( Model model, int row ) {
      return model.getValues( getPath(row) );
   }
   
   /**
    *  Set the value of this column in a model.
    *  @param  model   the model
    *  @param  value   the value
    */
   public void setValue(Model model, int row, Object value) {
      model.setValue( getPath(row), value );
   }
   
   public void setValues(Model model, int row, Object[] values) {
      model.setValues(getPath(row),values);
   }
   
   
   
   
   /**
    * Return the bound name (name of the value in the model).
    * @return  the bound name
    */
   public String getBoundName() {
      return boundName;
   }

   public String getBoundPath() {
      return boundPath;
   }

   public void setBoundPath(String boundPath) {
      this.boundPath = boundPath;
   }
   
}
