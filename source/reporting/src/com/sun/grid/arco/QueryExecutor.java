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
package com.sun.grid.arco;

import java.sql.*;
import com.sun.grid.arco.model.*;
import java.util.*;
import javax.xml.bind.JAXBException;

public class QueryExecutor {
   ObjectFactory faq;
   /** Creates a new instance of QueryExecutor */
   public QueryExecutor() {
      faq = new ObjectFactory();
   }
   
   public Result execute( Query query, Connection connection ) 
      throws JAXBException, SQLException {
      
      Statement stmt = connection.createStatement();
      
      ResultSet rs = stmt.executeQuery( query.getSql() );
      
      Result ret = faq.createResult();
      
      buildColumns(ret,query, rs.getMetaData());
      
      
      int colCount = ret.getColCount();
      
      ResultRow row = null;
      List values = null;
      Object value = null;
      
      while( rs.next() ) {         
         row = faq.createResultRow();
         values =  row.getValue();
         for( int i = 0; i < colCount; i++ ) {
            value = rs.getObject(i);
            values.add(ResultConverter.objToStr(value));
         }         
      }
      
      ret.setName(query.getName());
      ret.setCategory(query.getCategory());
      ret.setImgURL(query.getImgURL());
      ret.setLastModified(System.currentTimeMillis());
      ret.setRowCount(ret.getRow().size());
      ret.setSql(query.getSql());
      ret.setType(query.getType());

      if( query.isSetView() ) {
         ViewConfiguration vc = query.getView();
         try {
            vc = (ViewConfiguration)Util.clone(vc);
            ret.setView(vc);
         } catch( CloneNotSupportedException cnse ) {
            IllegalStateException ilse = new IllegalStateException("Can't clone viewconfiguration");
            ilse.initCause(cnse);
            throw ilse;
         }
      }
      return ret;
      
   }
   
   
   
   private void buildColumns(Result result, Query query, ResultSetMetaData rsMetaData) 
     throws JAXBException, SQLException {
      List fieldList = query.getField();
      Field field = null;
      ResultColumn col = null;
      
      
      String colName = null;
      String className = null;
      Class  columnClass = null;
      for( int i = 0; i < fieldList.size(); i++ ) {
         field = (Field)fieldList.get(i);
         col = faq.createResultColumn();
         colName = field.getReportName();
         col.setName( field.getReportName() );

         className = rsMetaData.getColumnClassName(i);
         try {
            columnClass = Class.forName(className);
         } catch( ClassNotFoundException cnfe ) {
            throw new IllegalStateException("class for column " + colName + " not found (" + className+ ")");
         }
         col.setType( ResultConverter.getColumnType(columnClass) );
      }
      result.setColCount(fieldList.size());
   }
   
}
