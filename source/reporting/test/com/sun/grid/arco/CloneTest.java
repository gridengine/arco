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

import junit.framework.*;

import java.util.*;
import java.util.logging.*;
import javax.xml.bind.*;
import java.io.*;

import com.sun.grid.arco.model.*;
import com.sun.grid.logging.SGELog;

public class CloneTest extends TestCase {
   
   ObjectFactory faq;
   JAXBContext   jc;
   
   public CloneTest(String testName) {
      super(testName);
   }

   protected void setUp() throws java.lang.Exception {
      jc = JAXBContext.newInstance( "com.sun.grid.arco.model" );
      faq = new ObjectFactory();
      SGELog.init(Logger.global);
   }

   protected void tearDown() throws java.lang.Exception {
   }
   
   public void testCollection() throws Exception {
      
      List list = new ArrayList();
      
      list.add( new Integer(0));
      list.add( new Double(0.1));
      list.add( new Date() );
      
      List cloneList = (List)Util.clone(list);
      
      this.assertEquals(list.size(),cloneList.size());
      
      Object obj = null;
      Object clone = null;
      for(int i = 0; i < list.size(); i++ ) {
         obj = list.get(i);
         clone = cloneList.get(i);
         assertEquals(obj, clone);
      }
   }
   
   public void testJaxB() throws Exception {
      
      Query query = faq.createQuery();
      query.setName("name");
      
      Field field = faq.createField();
      field.setDbName("dbname");
      query.getField().add(field);
      
      Query queryClone = (Query)Util.clone(query);
      

      StringWriter sw = new StringWriter();
      
      save(query,sw);
      sw.close();
      String s1 = sw.getBuffer().toString();
      
      SGELog.fine("s1 = {0}", s1);
      sw = new StringWriter();
      save(queryClone,sw);
      sw.close();
      String s2 = sw.getBuffer().toString();
      SGELog.fine("s2 = {0}", s2);
      
      assertEquals( s1, s2 );
   }
   
   private void save( Object obj, Writer writer )  {
         Marshaller marshaller = null;
         try {
            marshaller = jc.createMarshaller();
            marshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );
         } catch( JAXBException jaxbe ) {
            IllegalStateException ilse = new IllegalStateException("Can't create marshaller");
            ilse.initCause( jaxbe );
            throw ilse;
         }
         try {
            marshaller.marshal( obj, writer );
         } catch( JAXBException jaxbe ) {
            IllegalStateException ilse = new IllegalStateException("marshal error");
            ilse.initCause( jaxbe );
            throw ilse;
         }      
   }
   
   
}
