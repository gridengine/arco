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

import com.sun.grid.arco.ArcoConstants;
import com.sun.grid.arco.model.*;
import java.util.*;
import com.sun.grid.logging.SGELog;
import java.util.logging.Level;

/**
 * This Upgrader converts the plain column definitions of tables
 * and the data, column and row definitions of pivots into formatted
 * values.
 */
public class ColumnClassUpgrader extends AbstractUpgrader {
   
   /** Creates a new instance of ColumnClassUpgrader */
   public ColumnClassUpgrader() {
      super(1);
   }

   /**
    * upgrade the named object
    * @param obj  
    * @throws com.sun.grid.arco.upgrade.UpgraderException 
    */
   public void upgrade(com.sun.grid.arco.model.NamedObject obj) throws UpgraderException {
      
      if( obj instanceof ReportingObject ) {
         if( SGELog.isLoggable(Level.FINE)) {
            SGELog.fine("upgrade repObj {0}, version {1}", obj.getName(), 
                        new Integer(obj.getVersion()) );
         }
         ReportingObject repObj = (ReportingObject)obj;
         
         
         if( repObj.isSetView() ) {
            ViewConfiguration view = repObj.getView();
            ObjectFactory faq = new ObjectFactory();
            
            if( view.isSetTable() ) {
               Table table = view.getTable();
               if( table.isSetColumn() ) {  
                  SGELog.finer("convert table columns into formatted values");
                  upgrade(table.getColumn(),table.getColumnWithFormat(), faq);
                  table.unsetColumn();
                  table.setVisible(true);
               }
               
            }
            
            if( view.isSetPivot() ) {
               Pivot pivot = view.getPivot();
                              
               if( pivot.isSetColumn() ) {
                  SGELog.finer("convert pivot columns into formatted values");
                  upgradePivot(pivot.getColumn(), pivot, faq, ArcoConstants.PIVOT_TYPE_COLUMN );
                  pivot.unsetColumn();
                  pivot.setVisible(true);                  
               }
               if( pivot.isSetData() ) {
                  SGELog.finer("convert pivot data into formatted values");
                  upgradePivot(pivot.getData(), pivot, faq, ArcoConstants.PIVOT_TYPE_DATA );
                  pivot.unsetData();
                  pivot.setVisible(true);
               }
               if( pivot.isSetRow()) {
                  SGELog.finer("convert pivot rows into formatted values");
                  upgradePivot(pivot.getRow(), pivot, faq, ArcoConstants.PIVOT_TYPE_ROW );
                  pivot.unsetRow();
                  pivot.setVisible(true);
               }               
            }
         }
      }
   }
   

   
   private void upgrade(List columnList, List columnWithFormatList, ObjectFactory faq) {
      Iterator iter = columnList.iterator();
      String column = null;
      FormattedValue columnWithFormat = null;
      
      try {
         while( iter.hasNext() ) {
            column = (String)iter.next();
            SGELog.finer("convert column {0} into a formatted value", column);
            columnWithFormat = faq.createFormattedValue();
            columnWithFormat.setName( column.trim() );
            columnWithFormatList.add(columnWithFormat);
         }
      } catch( javax.xml.bind.JAXBException jaxbe ) {
         IllegalStateException ilse = new IllegalStateException("JAXB error: " + jaxbe.getMessage() );
         ilse.initCause(jaxbe);
         throw ilse;
      }
   }

   private void upgradePivot(List columnList, Pivot pivot, ObjectFactory faq, String pivotType) {
      Iterator iter = columnList.iterator();
      String column = null;
      PivotElement elem = null;
      List pivotElemList = pivot.getElem();
      try {
         while( iter.hasNext() ) {
            column = (String)iter.next();
            SGELog.finer("convert column {0} into a pivot element {1}", column, pivotType);
            elem = faq.createPivotElement();
            elem.setName( column.trim() );
            elem.setPivotType(pivotType);
            pivotElemList.add(elem);
         }
      } catch( javax.xml.bind.JAXBException jaxbe ) {
         IllegalStateException ilse = new IllegalStateException("JAXB error: " + jaxbe.getMessage() );
         ilse.initCause(jaxbe);
         throw ilse;
      }
   }
   
}
