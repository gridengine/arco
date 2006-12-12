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

import com.sun.grid.arco.model.*;
import java.util.*;

public class StripFieldNameUpgrader extends  AbstractUpgrader {
   
   /** Creates a new instance of StripFieldNameUpgrader */
   public StripFieldNameUpgrader() {
      super(1);
   }

   public void upgrade(NamedObject obj) throws UpgraderException {
      
      if( obj instanceof ReportingObject) {
         ReportingObject repObj = (ReportingObject)obj;
         if( repObj.isSetView() ) {            
            ViewConfiguration view = repObj.getView();
            if( view.isSetTable() ) {
               Table table = view.getTable();               
               if( table.isSetColumn() ) {
                  trimStringList(table.getColumn());
               }
            }
            
            if( view.isSetPivot() ) {
               Pivot pivot = view.getPivot();               
               if( pivot.isSetColumn() ) {
                  trimStringList(pivot.getColumn());
               }
               if( pivot.isSetData()) {
                  trimStringList(pivot.getData());
               }
               if( pivot.isSetRow()) {
                  trimStringList(pivot.getRow());
               }
            }
         }
      }
   }
      
   private void trimStringList( List list ) {
      int size = list.size();
      String elem = null;
      for( int i = 0; i< size; i++ ) {
         elem = (String)list.get(i);
         list.set(i, elem.trim() );
      }
   }
   
   
}
