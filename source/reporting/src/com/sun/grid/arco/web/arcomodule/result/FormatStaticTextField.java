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
package com.sun.grid.arco.web.arcomodule.result;

import com.iplanet.jato.model.Model;
import com.iplanet.jato.view.View;
import com.sun.grid.arco.ArcoConstants;
import com.sun.web.ui.view.html.CCStaticTextField;
import java.text.Format;

public class FormatStaticTextField extends CCStaticTextField {
  
      /** format object for the values */
      private Format format;
      
      
      /**
       * Create a new FormatStaticTextField
       * @param parent     the parent view
       * @param model      model for the text field
       * @param name       name of the text field
       * @param boundName  bound name in the model
       * @param value      initial value
       * @param format     format object
       */
      public FormatStaticTextField(View parent, Model model, String name,
	                                   String boundName, Object value, Format format) {
         super(parent, model, name, boundName, value );
         this.format = format;
      }
      
      /**
       * get the formatted value of the text field
       * @return the formatted value
       */
      public Object getValue() {       
         Object value = super.getValue();   
         if( value instanceof Object[] ) {
            value = ((Object[])value)[0];
         }
         if( value != null ) {
            try {
               return format.format(value);         
            } catch( IllegalArgumentException iae ) {
               return "Format Error";
            }
         } else {
            return ArcoConstants.NULL_VALUE;
         }
      }
   
}
