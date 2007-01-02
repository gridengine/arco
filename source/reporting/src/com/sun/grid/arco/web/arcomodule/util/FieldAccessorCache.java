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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FieldAccessorCache implements java.io.Serializable {
   
   private transient Map accessorMap = new HashMap();
   
   private void readObject(java.io.ObjectInputStream in)
     throws IOException, ClassNotFoundException {
      in.defaultReadObject();
      accessorMap = new HashMap();
   }

   public FieldAccessor getFieldAccessor(Class clazz, String fieldName ) {
      
      Map fieldMap = (Map)accessorMap.get(clazz);
      
      if( fieldMap == null ) {
         synchronized(accessorMap) {
            fieldMap = (Map)accessorMap.get(clazz);
            if( fieldMap == null ) {
               fieldMap = new HashMap();
               accessorMap.put(clazz, fieldMap);
            }
         }
      }
      
      FieldAccessor ret = (FieldAccessor)fieldMap.get(fieldName);
      if( ret == null ) {
         synchronized(fieldMap) {
            ret = (FieldAccessor)fieldMap.get(fieldName);
            if( ret == null ) {
               ret = new FieldAccessor(clazz,fieldName);
               fieldMap.put(fieldName,ret);
            }
         }
      }
      return ret;
   }
   
}
