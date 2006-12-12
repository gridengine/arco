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
package com.sun.grid.arco.validator.simple;

import com.sun.grid.arco.validator.Validator;
import com.sun.grid.arco.validator.QueryStateHandler;
import com.sun.grid.arco.sql.SQLGenerator;
import com.sun.grid.arco.model.QueryType;

public class SQLGeneratorValidator implements Validator {
   
   private SQLGenerator sqlGen;
   
   /** Creates a new instance of SQLGeneratorValidator */
   public SQLGeneratorValidator(SQLGenerator sqlGen) {
      this.sqlGen = sqlGen;
   }

   public void validate(QueryType query, QueryStateHandler handler) {
      try {
         query.setSql(sqlGen.generate(query, null));
      } catch(com.sun.grid.arco.sql.SQLGeneratorException sge ) {
         handler.addError("sql", sge.getMessage(), sge.getParameter());
      }
   }
   
}
