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
import java.util.*;
import com.sun.grid.logging.SGELog;
import com.sun.grid.reporting.dbwriter.RecordManager;
import com.sun.grid.reporting.dbwriter.ReportingException;
import com.sun.grid.reporting.dbwriter.StoredRecordManager;

/** Provides a base class for Objects stored in a database.
 */
public abstract class Record {
   // JG: TODO: create an interface ManagedObject
   protected RecordManager manager;
   
   protected IntegerField idField;
   protected IntegerField parentField;
   protected Field fields[];
   protected Map fieldsHash;
   protected String pstmStr = null;
   
   /**
    * Creates a new instance of Record
    */
   public Record(RecordManager manager) {
      this.manager = manager;
      
      // we might get a null manager, if this object is used as template
      if (this.manager != null) {
         idField = new IntegerField(this.manager.getIdFieldName());
         idField.setRecord(this);
         String parentFieldName = this.manager.getParentFieldName();
         if (parentFieldName != null) {
            parentField = new IntegerField(parentFieldName);
            parentField.setRecord(this);
         }
      }
   }
   
   public Field getIdField() {
      return idField;
   }
   
   public Field getParentField() {
      return parentField;
   }
   
   /**
    * The database fields informs its object that a value has been changed
    * @param field the database field
    */
   public void databaseFieldChanged(Field field) {
      clearCache();
   }
   
   protected void setFields(Field p_fields[]) {
      fields = p_fields;
      fieldsHash = new HashMap();
      for (int i = 0; i < fields.length; i++) {
         fieldsHash.put(fields[i].getName(), fields[i]);
         fields[i].setRecord(this);
      }
   }
   
   public Field[] getFields() {
      return fields;
   }
   
   public void setIdFieldValue(int id) {
      idField.setValue(id);
   }
   
   public int getIdFieldValue() {
      return idField.getValue();
   }
   
   public void setParentFieldValue(int id) {
      if (parentField != null) {
         parentField.setValue(id);
      }
   }
   
   public int getParentFieldValue() {
      int ret = 0;
      
      if (parentField != null) {
         ret = parentField.getValue();
      }
      
      return ret;
   }
   
   /**
    * @return String - the String used to create preparedStatement for this Record
    */
   public String getPstmString() {
      if (pstmStr == null) {
         StringBuffer cmd = new StringBuffer("INSERT INTO ");
         StringBuffer fieldNames = new StringBuffer(idField.getName());
         StringBuffer placeHolders = new StringBuffer("?");
         
         if (parentField != null) {
            fieldNames.append(", ");
            placeHolders.append(", ");
            fieldNames.append(parentField.getName());
            placeHolders.append("?");
         }
         
         for (int i = 0; i < fields.length; i++) {
            if (fields[i].doStore()) {
               fieldNames.append(", ");
               placeHolders.append(", ");
               fieldNames.append(fields[i].getName());
               placeHolders.append("?");
            }
         }
         
         cmd.append(manager.getTable());
         cmd.append(" (");
         cmd.append(fieldNames);
         cmd.append(") VALUES (");
         cmd.append(placeHolders);
         cmd.append(")");
         
         pstmStr = cmd.toString();
      }
      
      return pstmStr;
   }
   
   /**
    * @return String - the insert Statement String for this Record (without the '?')
    */
   public String getStatementString() {
      StringBuffer cmdFields = new StringBuffer(idField.getName());
      StringBuffer cmdValues = new StringBuffer(idField.getValueString(true));
      
      if (parentField != null) {
         cmdFields.append(", ");
         cmdValues.append(", ");
         
         cmdFields.append(parentField.getName());
         cmdValues.append(parentField.getValueString(true));
      }
      
      for (int i = 0; i < fields.length; i++) {
         if (fields[i].doStore()) {
            cmdFields.append(", ");
            cmdValues.append(", ");
            cmdFields.append(fields[i].getName());
            cmdValues.append(fields[i].getValueString(true));
         }
      }
      
      StringBuffer cmd = new StringBuffer("INSERT INTO ");
      cmd.append(manager.getTable());
      cmd.append(" (");
      cmd.append(cmdFields);
      cmd.append(") VALUES (");
      cmd.append(cmdValues);
      cmd.append(")");
      
      return cmd.toString();
      
   }
   
   /**
    * This method only returns fields that are stored in the <code>fieldsHash</code>
    * The idField and parentField are not stored in the <code>fieldsHash</code>
    * use method <code>getIdField</code> and <code>getParentField</code> to obtain those fields
    */
   public Field getField(String name) {
      return (Field) fieldsHash.get(name);
   }
   
   /** The primary key object for this database object. It is initialized
    *  lazy in methode <code>getPrimaryKey</code>.
    */
   private PrimaryKey primaryKey;
   
   /**
    * Get the primary key object. The database object caches the primary key object
    * The first call initializes lazy the cached object via the <code>createPrimaryKey</code>
    * method of the associated <code>RecordManager</code<
    * If a database field changes the cached object is deleted.
    *
    *
    *
    * @return the primary key object
    * @see com.sun.grid.reporting.dbwriter.db.RecordManager#createPrimaryKey
    */
   public PrimaryKey getPrimaryKey() {
      if(this.primaryKey == null ) {
         String primaryKeyFields[] = manager.getPrimaryKeyFields();
         if(primaryKeyFields != null) {
            String [] keys = new String[primaryKeyFields.length];
            for (int i = 0; i < primaryKeyFields.length; i++) {
               keys[i] = getField(primaryKeyFields[i]).getValueString(true);
            }
            this.primaryKey = manager.createPrimaryKey(keys);
         }
      }
      return primaryKey;
   }
   
   /** Clear all lazy initialized objects
    */
   private void clearCache() {
      primaryKey = null;
   }
   
   public boolean initFromStringArray(Map data) {
      // read all other fields
      for (int i = 0; i < fields.length; i++) {
         Field newField = (Field)data.get(fields[i].getName());
         if (newField != null){
            try {
               fields[i].setValue(newField);
            } catch (Exception e) {
               SGELog.warning( e, "Record.fieldError",
                     fields[i].getName(), newField.getValueString(false), e.getMessage() );
            }
         }
      }
      return true;
   }
   
   public void initFromResultSet(ResultSet rs) throws SQLException {
      // read id field
      //int id = rs.getInt(idField.getName());
      //idField.setValue(id);
      idField.setValueFromResultSet(rs);
      
      // read all other fields
      for (int i = 0; i < fields.length; i++) {
         fields[i].setValueFromResultSet(rs);
      }
   }
   
   public void initFromResultSet(ResultSet rs, Map map) throws SQLException {
      Iterator iter = map.keySet().iterator();
      
      while (iter.hasNext()) {
         String objKey = (String)iter.next();
         String dataKey = (String)map.get(objKey);
         Field objField = getField(objKey);
         
         objField.setValueFromResultSet(rs, dataKey);
      }
   }
   
   /**
    *  Get a human readable string representation of the database object. This
    *  string includes id, parent_id, primary key and object reference addr.
    *  @return human readable string representation of the database object
    */
   public String toString() {
      StringBuffer ret = new StringBuffer();
      ret.append("[");
      ret.append(manager.getTable());
      ret.append(", id=");
      ret.append(getIdFieldValue());
      ret.append(", parent=");
      ret.append(getParentFieldValue());
      ret.append(", ");
      if (getPrimaryKey() != null) {
         ret.append("key=[");
         ret.append(getPrimaryKey());
         ret.append("], " );
      }
      ret.append("addr=0x");
      ret.append(Integer.toHexString(hashCode()));
      ret.append("]");
      return ret.toString();
   }
   
}
