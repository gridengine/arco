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
package com.sun.grid.arco.upgrade;

import com.sun.grid.arco.model.NamedObject;

public abstract class AbstractUpgrader implements Upgrader, Comparable {
   
   private int version;
   
   /** Creates a new instance of Version1Upgrader */
   protected AbstractUpgrader(int version) {
      this.version = version;
   }

   public int getVersion() {
      return version;
   }

   public int compareTo(Object obj) {
      Upgrader ug = (Upgrader)obj;      
      int ret = version - ug.getVersion();
      if( ret == 0 ) {
         if( equals(obj) ) {
            ret = 0;
         } else {
            ret = 1;
         }
      }
      return ret;
   }
   
}
