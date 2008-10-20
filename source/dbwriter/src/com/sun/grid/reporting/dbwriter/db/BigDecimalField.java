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
 *   Copyright: 2008 by Sun Microsystems, Inc.
 *
 *   All Rights Reserved.
 *
 ************************************************************************/
/*___INFO__MARK_END__*/

package com.sun.grid.reporting.dbwriter.db;

import com.sun.grid.logging.SGELog;
import com.sun.grid.reporting.dbwriter.ReportingParseException;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BigDecimalField extends Field {
   private BigDecimal optionalDefaultValue = new BigDecimal("0.0");
   private BigDecimal value = new BigDecimal("0");
   private boolean parseDouble = false;
   
   /** Creates a new instance of BigDecimalField */
   public BigDecimalField(String p_name) {
      super(p_name);
   }
   
   /** Marker method */
   public BigDecimalField parseDouble(){
      this.parseDouble=true;
      return this;
   }
   
   /**
    * Marker method for DoubleField.
    * Indicate the field is optional
    */
   public BigDecimalField setOptional() {
      this.setOptional(true);
      return this;
   }
   
   
   public void setValue(String newValue) throws ReportingParseException {
      try {
         value = new BigDecimal(newValue);
      } catch( NumberFormatException nfe ) {
         if(isOptional()) {
            SGELog.fine("IntegerField.invalidValueForOptionaField", getName(), newValue);
            setValue(optionalDefaultValue);
         } else {
            throw new ReportingParseException("IntegerField.invalidValue", getName(), newValue );
         }
      }
   }
   
   public void setValue(BigDecimal newValue) {
      if (value.compareTo(newValue) != 0) {
         value = newValue;
         setChanged(true);
      }
   }
   
   public BigDecimal getValue() {
      return value;
   }
   
   public String toString() {
      return new String(getName() + ": " + getValueString(false));
   }
   
   public String getValueString(boolean quote) {
      return String.valueOf(value);
   }
   
   public void setValue(Field newValue) {
      BigDecimalField field = (BigDecimalField)newValue;
      setValue(field.getValue());
   }
   
   public void setValueFromResultSet(ResultSet rs, String attrib) throws SQLException {
      setValue(rs.getBigDecimal(attrib));
   }
   
   public void setValueForPSTM(PreparedStatement pstm, int index) throws SQLException {
      pstm.setBigDecimal(index, this.getValue());
   }

}
