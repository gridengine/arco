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

public class StringField extends DatabaseField {
  
   private String value = new String("");
   
   /** Creates a new instance of StringField */
   public StringField(String p_name, boolean p_store) {
      super(p_name, p_store);
   }
   
   public StringField(String p_name) {
      super(p_name);
   }

   /** 
    * Marker Method
    * The field is optional
    */
   public StringField setOptional() {
       this.setOptional(true);
       return this;
   }
   
   
   public void setValue(String newValue) {
      if( (value == null && newValue != null) ||
          !value.equals( newValue ) ) {
            value = newValue;
            setChanged(true);
      }
   }
   
   public String getValue() {
      return value;
   }
   
   public String toString() {
      return new String(getName() + ": " + getValueString(false));
   }
   
   public String getValueString(boolean quote) {
      if (quote) {
         StringBuffer ret = new StringBuffer();
         ret.append('\'');
         String value = getValue();
         if(value != null) {
            for(int i = 0; i < value.length(); i++) {
               char c  = value.charAt(i);
               if(c == '\'') {
                  ret.append('\'');
               }
               ret.append(c);
            }
         }
         ret.append('\'');
         return ret.toString();
      } else {
         return getValue();
      }
   }
   
   public void setValue(DatabaseField newValue) {
      StringField field = (StringField)newValue;
      setValue(field.getValue());
   }
   
   public void setValueFromResultSet(ResultSet rs, String attrib) throws SQLException {
      setValue(rs.getString(attrib));
   }
   
}
