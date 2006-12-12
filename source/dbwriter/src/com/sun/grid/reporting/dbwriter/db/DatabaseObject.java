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
import com.sun.grid.reporting.dbwriter.ReportingException;
import com.sun.grid.reporting.dbwriter.ReportingParseException;

/** Provides a base class for Objects stored in a database.
 */
public abstract class DatabaseObject {
   // JG: TODO: create an interface ManagedObject
   protected DatabaseObjectManager manager;

   protected IntegerField idField;
   protected IntegerField parentField;
   protected DatabaseField fields[];
   protected Map fieldsHash;
   
   /** Creates a new instance of DatabaseObject */
   public DatabaseObject(DatabaseObjectManager p_manager) {
      manager = p_manager;
      
      // we might get a null manager, if this object is used as template
      if (manager != null) {
         idField = new IntegerField(manager.getIdFieldName());
         idField.setDatabaseObject(this);
         String parentFieldName = manager.getParentFieldName();
         if (parentFieldName != null) {
            parentField = new IntegerField(parentFieldName);
            parentField.setDatabaseObject(this);
         }
      }
   }
   
   /**
    * The database fields informs its object that a value has been changed
    * @param field the database field
    */
   public void databaseFieldChanged(DatabaseField field) {
      clearCache();
   }
   
   protected void setFields(DatabaseField p_fields[]) {
      fields = p_fields;
      fieldsHash = new HashMap();
      for (int i = 0; i < fields.length; i++) {
         fieldsHash.put(fields[i].getName(), fields[i]);
         fields[i].setDatabaseObject(this);
      }
   }
   
   public void setId(int id) {
      idField.setValue(id);
   }
   
   public int getId() {
      return idField.getValue();
   }
   
   public void setParent(int id) {
      if (parentField != null) {
         parentField.setValue(id);
      }
   }
   
   public int getParent() {
      int ret = 0;
      
      if (parentField != null) {
         ret = parentField.getValue();
      }
      
      return ret;
   }
   
   public DatabaseField getField(String name) {
      return (DatabaseField) fieldsHash.get(name);
   }

   /** The primary key object for this database object. It is initialized
    *  lazy in methode <code>getPrimaryKey</code>.
    */
   private PrimaryKey primaryKey;
   
   /**
    * Get the primary key object. The database object caches the primary key object
    * The first call initializes lazy the cached object via the <code>createPrimaryKey</code>
    * method of the associated <code>DatabaseObjectManager</code<
    * If a database field changes the cached object is deleted.
    * @return the primary key object
    * @see com.sun.grid.reporting.dbwriter.db.DatabaseObjectManager#createPrimaryKey
    */
   public PrimaryKey getPrimaryKey() {
      if(this.primaryKey == null ) {
         String primaryKeyFields[] = manager.getPrimaryKeyFields();
         String [] keys = new String[primaryKeyFields.length];
         for (int i = 0; i < primaryKeyFields.length; i++) {
            keys[i] = getField(primaryKeyFields[i]).getValueString(true);
         }
         this.primaryKey = manager.createPrimaryKey(keys);
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
         DatabaseField newField = (DatabaseField)data.get(fields[i].getName());
         if (newField != null){
            try {
               fields[i].setValue(newField);
            } catch (Exception e) {
                SGELog.warning( e, "DatabaseObject.fieldError", 
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
         DatabaseField objField = getField(objKey);
         
         objField.setValueFromResultSet(rs, dataKey);
      }
   }
   
   public void store( java.sql.Connection connection ) throws ReportingException {
      manager.store(this, connection);
   }
   
   // JG: TODO: make access rights package default
   public void insertInDatabase( java.sql.Connection connection ) throws ReportingException {
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
      
      manager.getDatabase().execute(cmd.toString(), connection );
   }
   
   abstract public DatabaseObject newObject(DatabaseObjectManager manager);

   /**
    *  Get a human readable string representation of the database object. This
    *  string includes id, parent_id, primary key and object reference addr.
    *  @return human readable string representation of the database object
    */
   public String toString() {
      StringBuffer ret = new StringBuffer();
      ret.append('[');
      ret.append(manager.getTable());
      ret.append(", id=");
      ret.append(getId());
      ret.append(", parent=");
      ret.append(getParent());
      ret.append(", key=[");
      ret.append(getPrimaryKey());
      ret.append("], addr=0x" );
      ret.append(Integer.toHexString(hashCode()));
      ret.append("]");
      return ret.toString();
   }
}
