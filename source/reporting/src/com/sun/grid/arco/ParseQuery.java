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

import java.io.*;

import javax.xml.bind.*;
import com.sun.grid.arco.model.*;

public class ParseQuery {
   
   /** Creates a new instance of TestJaxb */
   public ParseQuery() {
   }
   
   /**
    * @param args the command line arguments
    */
   public static void main(String[] args) {
      
      try {
         // create a JAXBContext
         JAXBContext jc = JAXBContext.newInstance( "com.sun.grid.arco.model" );

         Marshaller m = jc.createMarshaller();
         m.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE );

         Unmarshaller um = jc.createUnmarshaller();

         Object obj = um.unmarshal( new File( args[0]) );

         if( obj instanceof Query ) {
            Query query = (Query)obj;
            System.out.println("query is " + query.getSql());
            System.out.println("query.view is " + query.getView() );
         }
         m.marshal( obj, System.out );
         
         // create an ObjectFactory instance.
         // if the JAXBContext had been created with mutiple pacakge names,
         // we would have to explicitly use the correct package name when
         // creating the ObjectFactory.            
//         ObjectFactory objFactory = new ObjectFactory();

//         Query query = objFactory.createQuery();
//         
//         SimpleQuery simpleQuery = objFactory.createSimpleQuery();
//         
//         simpleQuery.setName( "host load value");
//         simpleQuery.setCategory( "host load value" );
//         simpleQuery.setDescription( "blubber blabber" );
//         simpleQuery.setSql( "select * from sge_job" );
//         simpleQuery.setTableName( "table" );
         

         // create a Marshaller and marshal to System.out
         
//         StringWriter sw = new StringWriter();
//         
//         
//         m.marshal( simpleQuery, sw );
//         
//         sw.flush();
//         sw.close();
//         
//         System.out.println( sw.getBuffer() );
//         
//         
//         InputStream in = new ByteArrayInputStream( sw.getBuffer().toString().getBytes() );
//         Object obj = um.unmarshal( in );
//
//         System.out.println("parsed object " + obj);
         
      } catch( Exception e ) {
         e.printStackTrace();
      }
   }
   
}
