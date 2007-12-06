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

import com.sun.grid.reporting.dbwriter.ReportingParseException;
import java.sql.*;


public class DateField extends Field {
   private Timestamp value = new Timestamp(0);
   
   /** Creates a new instance of DateField */
   public DateField(String p_name, boolean p_store) {
      super(p_name, p_store);
   }
   
   public DateField(String p_name) {
      super(p_name);
   }
   
   public Timestamp getValue() {
      return value;
   }
   
   public String getValueString(boolean quote) {
      return getValueString(value);
   }
   
   public static String getValueString(Timestamp time) {
      return "{ts '" + time.toString() + "'}";
   }
   
   public void setValue(String newValue) throws ReportingParseException {
      try {
         long seconds = Long.parseLong(newValue);
         long milliseconds = seconds * 1000;
         Timestamp newTimestamp = new Timestamp(milliseconds);
         setValue(newTimestamp);
      } catch( NumberFormatException nfe ) {
         throw new ReportingParseException("DateField.invalidValue", getName(), newValue );
      }
   }
   
   public void setValue(Timestamp newValue) {
      if (!value.equals(newValue)) {
         value.setTime(newValue.getTime());
         setChanged(true);
      }
   }
   
   public void setValue(Field newValue) {
      DateField field = (DateField)newValue;
      setValue(field.getValue());
   }
   
   public void setValueFromResultSet(ResultSet rs, String attrib) throws SQLException {
      setValue(rs.getTimestamp(attrib));
   }
   

   public void setValueForPSTM(PreparedStatement pstm, int index) throws SQLException {
      pstm.setTimestamp(index, this.getValue());
}
   
}
