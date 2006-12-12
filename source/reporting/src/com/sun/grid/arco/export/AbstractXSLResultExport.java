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

import java.io.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;
import com.sun.grid.arco.ResultManager;
import com.sun.grid.arco.ResultExport;
import com.sun.grid.arco.ExportContext;
import com.sun.grid.arco.QueryResult;
import com.sun.grid.arco.model.Result;

public abstract class AbstractXSLResultExport extends ResultExport {
   
   /** Creates a new instance of AbstractXSLResultExport */
   public AbstractXSLResultExport( String mimeType ) {
      super( mimeType );
   }
   
   protected abstract Reader createXSLReader( ExportContext ctx ) throws IOException;
   
   
   public void export(ExportContext ctx) throws IOException, TransformerException {
      
         Result result = ctx.getXmlResult();

         OutputStream out = ctx.getOutputStream();
         
         StringWriter swr = new StringWriter();
         
         ResultManager.getInstance().save( result, swr );
         
         StreamSource xslStream = new StreamSource( createXSLReader(ctx)  );
         StreamSource xmlStream = new StreamSource( new StringReader( swr.getBuffer().toString() ) );         
         
         StreamResult outStream = new StreamResult( out ); 
         //      Tramsform the result
         TransformerFactory tFactory = TransformerFactory.newInstance();
         Transformer transformer = tFactory.newTransformer(xslStream);
         transformer.transform(xmlStream,outStream);            
   }
   
}
