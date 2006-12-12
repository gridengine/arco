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
import java.util.*;
import java.util.logging.Level;
import com.sun.grid.logging.SGELog;

public abstract class ObjectAdapterTableModel extends AbstractTableModel {
   
   private Model boundModel;
   private String boundPath;

   /** Creates a new instance of AbstractTableModel */
   protected ObjectAdapterTableModel(String name, java.io.InputStream in, Map columnMap, Model boundModel, String boundPath ) {
      super(name, in, columnMap);
      this.boundModel = boundModel;
      this.boundPath = boundPath;
      initModelRows();
   }
   
   /**
    *  Reinitialze this model    
    */
   public void reinit() {
      clear();
      initModelRows();
   }
   
   private boolean emptyTable = false;

   /**
    * initialize the rows of the model
    */
   public void initModelRows() {
      Object value = boundModel.getValue( boundPath );
      int rows = 0;
      if( value instanceof Collection ) {
        rows = ((Collection)value).size();
      } else if ( value == null ) {
         rows = 0;
      } else if( value.getClass().isArray() ) {
         Object[] obj = (Object[])value;
         rows = obj.length;
      } else {
         rows = 0;
      }
      setNumRows(rows);
      emptyTable = rows == 0;
      
   }
   
   
   
   public Object getValue( String name ) {

      ObjectAdapterTableColumn col = (ObjectAdapterTableColumn)getColumn(name);
      Object ret = null;
      if( col != null ) {
         ret = col.getValue(boundModel, getRowIndex());
      } else {
         ret = super.getValue(name);
      }
      if( SGELog.isLoggable(Level.FINE)) {
         SGELog.fine(getName() + "(" + getRowIndex() +") " + name+"=" + ret);
      }
      return ret;
   }
   
   public Object[] getValues(String name ) {
      ObjectAdapterTableColumn col = (ObjectAdapterTableColumn)getColumn(name);
      Object [] ret = null;
      int row = Math.max(0,getRowIndex());
      if( col != null ) {
         ret = col.getValues(boundModel, row );
      } else {
         ret = super.getValues(name);
      }      
      if( SGELog.isLoggable(Level.FINE)) {
         if (ret == null) {
            SGELog.fine(getName() + "(" + getRowIndex() +") " + name +"= null");
         } else {
            for( int i = 0; i < ret.length; i++ ) {
               SGELog.fine(getName() + "(" + getRowIndex() +") " + name +"["+i+"]=" + ret[i]);
            }
         }
      }
      return ret;
   }
   
   /**
    * Set a value for this model.
    * If the value is a defined column (and a attribute of
    * the query filter), the value is also stored
    * in the query model object.
    * @param name  name of the value
    * @param value the value object
    * @see com.sun.grid.arco.web.arcomodel.QueryModel
    */
   public void setValue(String name, Object value) {
      ObjectAdapterTableColumn col = (ObjectAdapterTableColumn)getColumn(name);
      if( col != null ) {
         int row = Math.max(0,getRowIndex());
         if( SGELog.isLoggable(Level.FINE)) {
            SGELog.fine("("+row+") " + name + "=" + value);
         }
         col.setValue(boundModel,row,value);
      } else {
         super.setValue(name, value);
      }
   }

   public void setValues(String name, Object[] values) {
      int row = Math.max(0,getRowIndex());
      if( SGELog.isLoggable(Level.FINE)) {
         for(int i = 0; i < values.length; i++) {
            SGELog.fine( "(" + row + ") " + name +"[" +i + "]=" + values[i] );
         }
      }
      ObjectAdapterTableColumn col = (ObjectAdapterTableColumn)getColumn(name);
      if( col != null ) {
         
         for(int i = 0 ; i < values.length; i++ ) {
            col.setValue(boundModel,i,values[i]);
         }
         if( SGELog.isLoggable(Level.FINE)) {
            for(int i = 0; i < values.length; i++) {
               SGELog.fine( "(" + row + ") " + name +"[" +i + "]=" + values[i] );
            }
         }
       } else {
         super.setValues(name, values);
       }
   }

   public int getNumRows() {

      int retValue;
      if( emptyTable ) {
         retValue = 0;
      } else {
         retValue = super.getNumRows();
      }
      if( SGELog.isLoggable(Level.FINE)) {
         SGELog.fine("numRows is " + retValue);
      }
      return retValue;
   }

}
