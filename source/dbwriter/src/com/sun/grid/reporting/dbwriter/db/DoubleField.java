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

import com.sun.grid.logging.SGELog;
import com.sun.grid.reporting.dbwriter.ReportingParseException;
import java.sql.*;


public class DoubleField extends Field {
   
   private double value = 0.0;
   private double optionalDefaultValue = 0.0;
   
   /** Creates a new instance of DoubleField */
   public DoubleField(String p_name, boolean p_store) {
      super(p_name, p_store);
   }
   
   public DoubleField(String p_name) {
      super(p_name);
   }
   
   /**
    * Marker method for DoubleField.
    * Indicate the field is optional
    */
   public DoubleField setOptional() {
      this.setOptional(true);
      return this;
   }
   
   /**
    * Marker method for DoubleField.
    */
   public DoubleField setOptionalWithDefaultValue(double ov) {
      this.optionalDefaultValue=ov;
      return setOptional();
   }
   
   public void setValue(String newValue) throws ReportingParseException {
      try {
         setValue(parseDouble(newValue));
      } catch(NumberFormatException nfe ) {
         if(isOptional()) {
            SGELog.fine("DoubleField.invalidValueForOptionaField", getName(), newValue);
            setValue(optionalDefaultValue);
         } else {
            throw new ReportingParseException("DoubleField.invalidValue", getName(), newValue );
         }
      }
   }
   
   public void setValue(double newValue) {
      if (value != newValue) {
         value = newValue;
         setChanged(true);
      }
   }
   
   public double getValue() {
      return value;
   }
   
   public String toString() {
      return new String(getName() + ": " + getValueString(false));
   }
   
   public String getValueString(boolean quote) {
      return String.valueOf(value);
   }
   
   public void setValue(Field newValue) {
      DoubleField field = (DoubleField)newValue;
      setValue(field.getValue());
   }
   
   public void setValueFromResultSet(ResultSet rs, String attrib) throws SQLException {
      setValue(rs.getDouble(attrib));
   }
   
   public void setValueForPSTM(PreparedStatement pstm, int index) throws SQLException {
      pstm.setDouble(index, this.getValue());
   }
   
}
