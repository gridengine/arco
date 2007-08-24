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

/** Base class for data fields stored in a database table.
 * It provides basic functionality like field naming and change management.
 */
abstract public class Field {
   
   public static final String INF = "inf";
   public static final String NAN = "NaN";

   public static final Double BINARY_GIGA = new Double(1024.0*1024.0*1024.0);
   public static final Double BINARY_MEGA = new Double(1024.0*1024.0);
   public static final Double BINARY_KILO = new Double(1024.0);
   
   public static final Double DECIMAL_GIGA = new Double(1000000000.0);
   public static final Double DECIMAL_MEGA = new Double(1000000.0);
   public static final Double DECIMAL_KILO = new Double(1000.0);
   
   /** Name of the field.
    * This is the column name in the database.
    */   
   protected String name;
   /** Has the field been changed? */   
   private boolean changed;
   /** Shall the field be stored in database insert/update operations? */   
   protected boolean store;
   /** this is optional property */
   protected boolean optional=false;
   
   private Record obj;
   
   /**
    * Creates a new instance of Field
    * 
    * @param p_name Name of the new Field.
    */
   public Field(String p_name) {
      this(null, p_name);
   }

   /**
    * Creates a new instance of Field.
    * 
    * @param p_name Name of the new Field.
    * @param p_store Shall the field be considered in database operations?
    */   
   public Field(String name, boolean store) {
      this(null,name,store);
   }

   /**
    * Creates a new instance of a Field.
    * 
    * @param obj   the database object to which this field belongs
    * @param name  name of the database field
    */
   public Field(Record obj, String name) {
      this(obj, name, true);
   }
   
   /**
    * Creates a new instance of a Field.
    * 
    * @param obj   the database object to which this field belongs
    * @param name  name of the database field
    * @param store Shall the field be considered in database operations?
    */
   public Field(Record obj, String name, boolean store) {
      this.obj = obj;
      this.name = name;
      this.store = store;
      setChanged(false);
   }
      
   /**
    *   Set the database object to which this database field belongs. The
    *   The database object will be informed if the content of will has changed.
    * 
    * @param obj   the database object
    * @see com.sun.grid.reporting.dbwriter.DatabasDatabaseRecordseFieldChanged
    */
   public void setRecord(Record obj) {
      this.obj = obj;
   }
   
   /**
    *   Get the database object to which this database fields belongs to.
    *   @return  the database object
    */
   public Record getRecord() {
      return obj;
   }
   
   /**
    * Returns the name of the Field.
    * 
    * @return name{@link name}
    */   
   public String getName() {
      return name;
   }
   
   /**
    * Shall the Field be stored?
    * 
    * @return true, if the field shall be stored, else false
    */   
   public boolean doStore() {
      return store;
   }
   
   /** Has the value of the field changed since the last storing operation?
    * @return true, if the value is changed,
    * else false
    */   
   public boolean isChanged() {
      return changed;
   }

   /**
    * Set the changed flag for this field. If the database object is set, it
    *  will be informed that the value of this field has been changed.
    * 
    * @param changed   the changed flag
    * @see com.sun.grid.reporting.dbwriter.db.DataDatabaseRecordabaseFieldChanged
    */
   public void setChanged(boolean changed) {
      this.changed = changed;
      if(changed && obj != null) {
         obj.databaseFieldChanged(this);
      }
   }
   
   /** Is the value of the field optional? The results of the optional is 
    * the field can be skiped.  
    * @return true, if the value is optional,
    * else false
    */
   public boolean isOptional() {
       return optional;
   }
  
      /** Set the optional flag for this field.
    *  @param   optional  the  optional flag
    */
   public void setOptional(boolean optional) {
      this.optional = optional;
   }

   /**
    * Set a new value from a string argument.
    * The input string has to be parsed to the datatype of the Field.
    * Herefore the parsing functions of the standard datatype classes provided with
    * the JDK (Integer, Boolean ...) will be used where possible.
    * 
    * @param newValue The new field value
    */   
   abstract public void setValue(String newValue) throws ReportingParseException;
   /**
    * Set a new value by copying from another Field.
    * Prerequisit for this operation is, that the Fields are either of the same
    * class or appropriate casting code for other Field derived classes are
    * provided.
    * 
    * @param newValue A Field containing the new value to set.
    */   
   abstract public void setValue(Field newValue);
   /**
    * Return the value of the Field in a string representation.
    * Quoting can be switched on to allow the insertion into an SQL string without
    * having to handle quoting (which is data type dependent) in the calling code.
    * 
    * @param quote Shall the output string be quoted, if quoting is needed in SQL commands?
    * Example:
    * For a StringField with the value "current content",
    * getValueString(false) will return "current content",
    * getValueString(true) will return "'current content'"
    * @return The value of the database field in a string representation.
    */   
   public abstract String getValueString(boolean quote);
   /** Set a new value by reading it from a ResultSet 
    * @see java.sql.ResultSet
    * @param rs The ResultSet to read from.
    * @throws SQLException SQLExceptions thrown by the functions accessing the given ResultSet will be
    * passed on to the caller of this function.
    */   
   public void setValueFromResultSet(ResultSet rs) throws SQLException {
      setValueFromResultSet(rs, getName());
   }
   
   abstract protected void setValueFromResultSet(ResultSet rs, String attrib) throws SQLException;
   
   
   /**
    *  get the multiplier for a suffix.
    *  Known suffixes:
    *
    *   'M'  binary mega (1024*1024)
    *   'm'  decimal mega (1000*1000)
    *   'K'  binary kilo (1024)
    *   'k'  decimal kilo (1000)
    *
    *  @param suffix  the suffix
    *
    *  @return  the double object with the multiplier of <code>null</code>
    *           if the suffix unknown 
    *
    */
   public static Double getMultiplier(char suffix) {
      
      /*   Extract of man queue_conf(5)
           Memory specifiers are positive decimal, hexadecimal or octal
           integer  constants  which  may  be  followed by a multiplier
           letter. Valid multiplier letters are k, K, m and M, where  k
           means multiply the value by 1000, K multiply by 1024, m mul-
           tiply by 1000*1000 and M multiplies by 1024*1024. If no mul-
           tiplier is present, the value is just counted in bytes.        
       */
         switch (suffix) {
            case 'm': return DECIMAL_MEGA;
            case 'M': return BINARY_MEGA;
            case 'k': return DECIMAL_KILO;
            case 'K': return BINARY_KILO;
            case 'g': return DECIMAL_GIGA;
            case 'G': return BINARY_GIGA;
             default: return null;
         }
   }
   
   /**
    *  Helper function for parsing a double value
    *  This function can handle the values "inf" and "NaN"
    *  and the suffixes 'M' (binary mega), 'm' (decimal mega) and 'K' (binary kilo)
    *  and 'k' (decimal kilo).
    *  @param  value  the value that should be parsed
    *  @return the double value
    *  @throws NumberFormatException if value can not be converted into a double
    *  @see #getMultiplier
    */
   protected double parseDouble(String value) throws NumberFormatException {
      if( value == null || value.length() == 0 ) {
         return 0.0;
      } else if( INF.equalsIgnoreCase(value)) {
         SGELog.severe("Field.hasInvalidValue", getName(), value );
         return 0.0;
      } else if ( NAN.equalsIgnoreCase(value)) {
         SGELog.severe("Field.hasInvalidValue", getName(), value );
         return 0.0;
      } else {
         Double multiplier = null;
         
         int len = value.length();
         
         if( len > 1 ) {
            char suffix = value.charAt(len-1);

            multiplier = getMultiplier(suffix);
         }
         
         if( multiplier != null ) {
             value = value.substring(0, len - 1);
             return multiplier.doubleValue() * Double.parseDouble(value);
         } else {
            return Double.parseDouble(value);
         }         
      }
   }

}
