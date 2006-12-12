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
package com.sun.grid.reporting.dbwriter.db;

import java.sql.*;

/** Database fields of type boolean.
 */
public class BooleanField extends DatabaseField {
   private boolean value = false;
   
   /** Creates a new instance of BooleanField
    * @param p_name Name of the field in the database
    * @param p_store Is the field actually stored in the database, or
    * is it a virtual field only used temporarily
    */
   public BooleanField(String p_name, boolean p_store) {
      super(p_name, p_store);
   }
   
   /** Creates a new instance of BooleanField
    * @param p_name Name of the field in the database
    */   
   public BooleanField(String p_name) {
      super(p_name);
   }
   
   /** Sets the value of the boolean field from an input string.
    * It uses string parsing functions of the Boolean class to parse the input.
    * @param newValue The new value to set.
    */   
   public void setValue(String newValue) {
      setValue((Boolean.valueOf(newValue)).booleanValue());
   }
   
   /** Sets the value of the boolean field to the value given by the boolean parameter.
    * @param newValue The new boolean value to set.
    */   
   public void setValue(boolean newValue) {
      if (value != newValue) {
         value = newValue;
         setChanged(true);
      }
   }
   
   /** Returns the value of the boolean field as boolean.
    * @return value of the boolean field
    */   
   public boolean getValue() {
      return value;
   }
   
   /** Returns a String representation of a boolean field in the form
    * "<field name>: <field value>".
    * @return String describing the boolean field
    */   
   public String toString() {
      return new String(getName() + ": " + getValueString(false));
   }
   
   /** Returns a String containing the value of the field in a form usable in SQL
    * statements.
    * Uses the String.valueOf method to get String representation of a boolean.
    * @param quote Shall we quote the value?
    * boolean values are never quoted.
    * @return String containing the boolean value.
    */   
   public String getValueString(boolean quote) {
      return String.valueOf(value);
   }
   
   /** Set the value from another boolean field.
    * @param newValue a boolean field whose value will be read as new value
    */   
   public void setValue(DatabaseField newValue) {
      BooleanField field = (BooleanField)newValue;
      setValue(field.getValue());
   }
   
   /** Set the value of the field from a ResultSet.
    * Uses the field name to query the ResultSet for a value.
    * @param rs the result set to query
    * @param attrib
    * @throws SQLException
    */   
   public void setValueFromResultSet(ResultSet rs, String attrib) throws SQLException {
      setValue(rs.getBoolean(attrib));
   }
}
