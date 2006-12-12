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
import com.sun.grid.logging.SGELog;
import java.io.*;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.Validator;

import com.sun.grid.arco.upgrade.*;

public class ResultManager extends AbstractXMLFileManager {
   
   
   public ResultManager( File resultDir ) {
      this(resultDir, ResultManager.class.getClassLoader());
   }
   /** Creates a new instance of ResultManager */
   public ResultManager( File resultDir, ClassLoader classLoader ) {
      super( Result.class, resultDir, classLoader );
      registerUpgrader(new StripFieldNameUpgrader() );
      registerUpgrader(new ColumnClassUpgrader() );
      registerUpgrader(new GraphicUpgrader());
   }
   
   public Result getResultByName( String name ) throws ArcoException {
      return (Result)load( name );
   }
   
   public void saveResult( Result result ) throws ArcoException {
      super.save( result );
   }
   
   public boolean validateResult( Result result ) {
      return super.validate( result );
   }
   
//   // ---- Singleton support ---------------------------
   
   public static void createInstance( File resultDir, ClassLoader classLoader ) {
      
      if( theInstance != null ) {
         throw new IllegalStateException("createInstance called twice");
      }      
      theInstance = new ResultManager( resultDir, classLoader );
   }
   
   /** the singleton instance */
   private static ResultManager theInstance;

   /**
    *  get the singleton instanceof the <code>QueryManagery/code>
    *  @return the singleton instance
    */
   public static ResultManager getInstance() {
      if( theInstance == null ) {
         throw new IllegalStateException("singleton instance is yet not initialized");
      }
      return theInstance;
   }
   
   
}
