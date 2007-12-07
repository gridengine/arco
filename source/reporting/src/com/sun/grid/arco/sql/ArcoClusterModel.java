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
package com.sun.grid.arco.sql;


import java.util.*;
import java.sql.*;

import com.sun.grid.arco.model.*;
import javax.servlet.http.HttpSession;

/**
 * <p>
 *    <code>ArcoClusterModel</code>
 * </p>
 *  Should be a session scoped model bean
 */
public class ArcoClusterModel {
   
   private static final String ARCO_CLUSTER_MODEL="arco_cluster_model";
   
   public static final ArcoClusterModel getInstance(HttpSession session) {
      ArcoClusterModel model = null;
      Object obj = session.getAttribute(ARCO_CLUSTER_MODEL);
      if (obj == null) {
         model = new ArcoClusterModel();
         session.setAttribute(ARCO_CLUSTER_MODEL, model);
      } else {
         model = (ArcoClusterModel) obj;
      }
      return model;
   }
  
   private int currentCluster=0;

   public int getCurrentCluster() {
      return currentCluster;
   }

   public void setCurrentCluster(int currentCluster) {
      this.currentCluster = currentCluster;
   }


} // end of class ArcoClusterModel
