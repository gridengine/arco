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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Inmutable primary key object.
 *
 * A primary key object holds the primary key values of a database object. The
 * order of the values corresponds to the order of the primary key fields of the
 * RecordManager.
 * A primary key object can be used as key for a Map.
 *
 * @see    com.sun.grid.reporting.dbwriter.db.RecordManager#getPrimaryKeyFields
 */
public class PrimaryKey {
   
   /** the keys for with the primary information */
   private String [] keys;
   
   /** the primary key object is a inmutable object
    *  we can cache the hascode */
   private int hashcode;
   
   /**
    * Create a new primary key object
    * @param keys  the primary keys values
    */
   public PrimaryKey(String [] keys) {
      this.keys = new String[keys.length];
      if(keys.length == 1) {
         this.keys[0] = keys[0];
         hashcode = keys[0] == null ? 0 : keys[0].hashCode();
      } else {
         hashcode = 17;
         for(int i = 0; i < keys.length; i++) {
            this.keys[i] = keys[i];
            hashcode = hashcode * 37 + keys[i] == null ? 0 : keys[i].hashCode();
         }
      }
   }
   
   public int hashCode() {
      return hashcode;
   }
   
   public boolean equals(Object obj) {
      
      if (obj == this) {
         return true;
      }
      if (!(obj instanceof PrimaryKey)) {
         return false;
      }
      
      
      PrimaryKey pk = (PrimaryKey)obj;
      
      if (keys.length != pk.keys.length) {
         return false;
      }
      if (hashCode() != pk.hashCode()) {
         return false;
      }
      
      for (int i = 0; i < keys.length; i++) {
         if (keys[i] == null) {
            if (pk.keys[i] != null) {
               return false;
            }
         } else if (!keys[i].equals(pk.keys[i])) {
            return false;
         }
      }
      return true;
   }
   
   public int getKeyCount() {
      return keys.length;
   }
   
   public String getKey(int index) {
      return keys[index];
   }
   
   
   private String toStringStr;
   
   public String toString() {
      if(toStringStr == null) {
         StringBuffer ret = new StringBuffer();
         
         for(int i = 0; i < keys.length; i++ ) {
            if(i>0) {
               ret.append(":");
            }
            ret.append(keys[i]);
         }
         toStringStr = ret.toString();
      }
      return toStringStr;
   }
}
