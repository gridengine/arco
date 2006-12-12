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

import java.util.*;
import com.sun.grid.arco.model.*;
import com.sun.grid.arco.upgrade.*;
import com.sun.grid.logging.SGELog;
import java.io.*;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.Validator;
import com.sun.grid.arco.upgrade.*;

public class QueryManager extends AbstractXMLFileManager {
   
   
   public QueryManager( File queryDir ) {
      this(queryDir, QueryManager.class.getClassLoader() );      
   }
   
   /** Creates a new instance of QueryManager 
    *  @param queryDir   directory where queries are stored
    */   
   protected QueryManager( File queryDir, ClassLoader classLoader ) {
      super( Query.class, queryDir, classLoader );
      registerUpgrader(new AdvancedFieldListUpgrader());
      registerUpgrader(new ColumnClassUpgrader());
      registerUpgrader(new GraphicUpgrader());
   }
   
   private void ensureRequired( Query query ) {   
      try {
         ViewConfiguration view = null;
         if( !query.isSetView() ) {
            view = getObjectFactory().createViewConfiguration();
            query.setView(view);
         } else {
            view = query.getView();
         }
         
         if( !view.isSetDescription() ) {
            view.setDescription( getObjectFactory().createVstring());
         }
         if( !view.isSetSql() ) {
            view.setSql(getObjectFactory().createVstring());
         }
         if( !view.isSetParameter() ) {
            view.setParameter(getObjectFactory().createVstring());
         }
      } catch( JAXBException jaxbe ) {
         IllegalStateException ilse = new IllegalStateException("JAXB error: " + jaxbe.getMessage());
         ilse.initCause(jaxbe);
         throw ilse;
      }
   }
   
   public Query getQueryByName( String name ) throws ArcoException {
      Query ret = (Query)super.load( name );
      if( ret != null ) {
         ensureRequired(ret);
      }
      return ret;
   }
   
   public Query createQuery() {
      try {
         Query ret = getObjectFactory().createQuery();
         ensureRequired(ret);
         return ret;
      } catch( JAXBException jaxbe ) {
         IllegalStateException ilse = new IllegalStateException("Can't create instance of Query");
         ilse.initCause( jaxbe );
         throw ilse;
      }      
   }
   
   public Query createSimpleQuery() {
      Query ret  = createQuery();
      ret.setType( ArcoConstants.SIMPLE );
      return ret;
   }
   
   public Query createAdvancedQuery() {
      Query ret  = createQuery();
      ret.setType( ArcoConstants.ADVANCED );
      return ret;
   }


   public boolean validateQuery( Query query ) {
      return super.validate( query );
   }
   
   
   public void saveQuery( Query query ) throws ArcoException {
        save( query );
   }
   

   // ---- Singleton support ---------------------------
   
   public static void createInstance( File queryDir, ClassLoader classLoader ) {
      
      if( theInstance != null ) {
         throw new IllegalStateException("createInstance called twice");
      }      
      theInstance = new QueryManager( queryDir, classLoader );
   }
   
   /** the singleton instance */
   private static QueryManager theInstance;

   /**
    *  get the singleton instanceof the <code>QueryManagery/code>
    *  @return the singleton instance
    */
   public static QueryManager getInstance() {
      if( theInstance == null ) {
         throw new IllegalStateException("singleton instance is yet not initialized");
      }
      return theInstance;
   }
}
