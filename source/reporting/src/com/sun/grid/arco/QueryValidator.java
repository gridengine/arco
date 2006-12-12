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

import com.sun.grid.arco.model.QueryType;
import com.sun.grid.arco.validator.QueryStateHandler;
import com.sun.grid.arco.validator.Validator;
import com.sun.grid.arco.sql.SQLGenerator;

public class QueryValidator {
   
   private SQLGenerator sqlGenerator;
   
   private Validator [] commonValidators;   
   private Validator [] simpleValidators;   
   private Validator [] advancedValidators;   
   private Validator [] viewValidators;
   
   public QueryValidator(SQLGenerator sqlGenerator) {
      this.sqlGenerator = sqlGenerator;
      commonValidators = new Validator [] {
         new com.sun.grid.arco.validator.common.CommonValidator()
      };
      simpleValidators = new Validator [] {
         new com.sun.grid.arco.validator.simple.FieldListValidator(),
         new com.sun.grid.arco.validator.simple.FilterListValidator(),
         new com.sun.grid.arco.validator.simple.SQLGeneratorValidator(sqlGenerator)
      };
      advancedValidators = new Validator [] {
         new com.sun.grid.arco.validator.advanced.SQLParseValidator()
      };
      viewValidators = new Validator [] {
         new com.sun.grid.arco.validator.view.TableViewValidator(),
         new com.sun.grid.arco.validator.view.PivotViewValidator(),
         new com.sun.grid.arco.validator.view.GraphViewValidator()
      };
   }
   
   
   public void validate(QueryType query, QueryStateHandler handler) {
      validate(commonValidators, query, handler);
      if( !handler.hasErrors() ) {
         if( ArcoConstants.SIMPLE.equals(query.getType())) {
            validate(simpleValidators,query, handler);
         } else {
            validate(advancedValidators,query, handler);
         }         
      }
      if( !handler.hasErrors() && query.isSetView() ) {
         validate(viewValidators,query, handler);
      }
   }
   
   private static void validate(Validator [] validators, QueryType query,  QueryStateHandler handler) {
      for( int i = 0; i < validators.length && !handler.hasErrors(); i++ ) {
         validators[i].validate(query, handler );
      }
   }
}
