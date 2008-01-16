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
package com.sun.grid.arco.web.arcomodule;

import com.iplanet.jato.RequestManager;
import java.util.Date;
import java.util.Map;
import com.sun.web.ui.model.*;

import com.sun.grid.arco.AbstractXMLFileManager;
import com.sun.grid.arco.ArcoException;
import com.sun.grid.arco.ResultManager;
import com.sun.grid.arco.model.NamedObject;
import com.sun.grid.arco.sql.ArcoClusterModel;

public class NamedObjectTableModel extends CCActionTableModel {
   
   public static final String CHILD_COLUMN = "Col";
   public static final String CHILD_STATIC_TEXT = "Text";
   
   public static final String CHILD_NAME = "Name";
   public static final String CHILD_CATEGORY = "Category";
   public static final String CHILD_LAST_MODIFIED = "LastModified";
   public static final String CHILD_TYPE = "Type";
   public static final String CHILD_DESCRIPTION = "Description";
   
   
   AbstractXMLFileManager fileManager;
   
   public NamedObjectTableModel(javax.servlet.ServletContext sc, AbstractXMLFileManager fileManager, java.lang.String file) {
      super( sc, file );
      this.fileManager = fileManager;
      init();
   }
   
   public void reinit() {
      clearAll();
      init();
   }
   
   protected void init() {
      initHeaders();
      initModelRows();
   }
   
// Initialize column headers.
   protected void initHeaders() {
      setActionValue( CHILD_COLUMN + CHILD_NAME, CHILD_NAME );
      setActionValue( CHILD_COLUMN + CHILD_CATEGORY, CHILD_CATEGORY );
      setActionValue( CHILD_COLUMN + CHILD_LAST_MODIFIED, CHILD_LAST_MODIFIED );
      setActionValue( CHILD_COLUMN + CHILD_TYPE, CHILD_TYPE );
   }
   
   private ArcoException error;
   
   private void initModelRows() {
      final ArcoClusterModel acm = ArcoClusterModel.getInstance(RequestManager.getSession());
      try {
         NamedObject[] namedObjects = fileManager.getAvailableObjects();

         for (int row = 0; row < namedObjects.length; row++) {
            final NamedObject no = namedObjects[row];
            if (fileManager instanceof ResultManager) {
               // Ignore empty clusterName
               if (no.getClusterName()!=null &&
                   no.getClusterName().length()>0 &&
                   !no.getClusterName().equals(acm.getCurrentCluster())) {
                  //Skip saved results belongs to other clusters
                  continue;
               }           
            }

            if (row > 0) {
               appendRow();
            }

            setValue(CHILD_STATIC_TEXT + CHILD_NAME, no.getName());
            setValue(CHILD_STATIC_TEXT + CHILD_CATEGORY, no.getCategory());
            setValue(CHILD_STATIC_TEXT + CHILD_LAST_MODIFIED, new Date(no.getLastModified()));
            setValue(CHILD_STATIC_TEXT + CHILD_TYPE, no.getType());
            setValue(CHILD_STATIC_TEXT + CHILD_DESCRIPTION, getDescription(no, row));

         }
      } catch (ArcoException ae) {
         error = ae;
      }
   }

   private String getDescription(NamedObject obj, int row) {
      String descr = obj.getDescription();
      if(descr == null) {
         descr = "No Description";
      }
      StringBuffer ret = new StringBuffer();
      ret.append("<div class='tooltip' id='description");
      ret.append(row);
      ret.append("'>");
      appendStr(descr, ret);
      ret.append("</div>");
      return ret.toString();
   }
   
   private void appendStr(String str, StringBuffer buffer) {
      char c;
      for(int i = 0; i < str.length(); i++ ) {
         c = str.charAt(i);
         switch( c) {
            case '<':  buffer.append("&lt;"); break;
            case '>':  buffer.append("&gt;"); break;
            case '\n': buffer.append("<br>"); break;
            default:
               buffer.append(c);
         }
      }
   } 
   
   public ArcoException getError() {
      return error;
   }

   public String getSelectedObjectName() {
      Integer[] selectedRows = getSelectedRows();
      if( selectedRows != null && selectedRows.length > 0 ) {
         return getObjectName(selectedRows[0].intValue());
      } else {         
         return null;
      }      
   }
   
   public String getObjectName(int row) {
      Map valueMap = getValueMap(row);
      Object[] ret = (Object[])valueMap.get(CHILD_STATIC_TEXT + CHILD_NAME );
      return (String)ret[0];
   }
   
   
}
