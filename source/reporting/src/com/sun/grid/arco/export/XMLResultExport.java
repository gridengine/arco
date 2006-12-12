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
package com.sun.grid.arco.export;

import com.sun.grid.arco.AbstractXMLFileManager;
import com.sun.grid.arco.ResultExport;
import com.sun.grid.arco.QueryResult;
import java.io.OutputStreamWriter;
import javax.xml.bind.JAXBException;

public class XMLResultExport extends ResultExport {
   public static final String MIME_TYPE = "text/xml";
   /** Creates a new instance of CSVResultExport */
   public XMLResultExport() {
      super(MIME_TYPE);
   }

   public void export(com.sun.grid.arco.ExportContext ctx) 
       throws java.io.IOException, com.sun.grid.arco.QueryResultException {
      
      QueryResult result = ctx.getQueryResult();

      try {
         AbstractXMLFileManager.save(
                 result.createResult(), 
                 new OutputStreamWriter(ctx.getOutputStream()), 
                 AbstractXMLFileManager.createJAXBContext() );
      } catch( JAXBException jaxbe ) {
         IllegalStateException ilse = new IllegalStateException("JAXB error: " + jaxbe.getMessage());
         ilse.initCause(jaxbe);
         throw ilse;
      }
   }
   
}
