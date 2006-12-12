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
package com.sun.grid.arco.web.arcomodule;

import java.util.*;
import com.iplanet.jato.view.html.OptionList;
import com.sun.grid.arco.ArcoConstants;

public class FormatHelper {
   public static final String NUMBER_FORMAT_PREFIX = "number";
   public static final String DATE_FORMAT_PREFIX = "date";
   public static final String DELIMITER = "|";
   
   private OptionList numberFormatOptionList;
   private OptionList dateFormatOptionList;
   private String     numberFormatStr;
   private String     dateFormatStr;

   /** Creates a new instance of FormatHelper */
   public FormatHelper() {
      String key = null;
      String format = null;
      ResourceBundle rb = ResourceBundle.getBundle("com.sun.grid.arco.web.arcomodule.FormatResources");
      Enumeration nameEnum = rb.getKeys();
      numberFormatOptionList = new OptionList();
      dateFormatOptionList = new OptionList();
      
      while (nameEnum.hasMoreElements()) {
         key = (String)nameEnum.nextElement();
         format = rb.getString(key);
         if( key.startsWith(NUMBER_FORMAT_PREFIX) ) {
            numberFormatOptionList.add(format, format);
         } else if ( key.startsWith(DATE_FORMAT_PREFIX)) {
            dateFormatOptionList.add(format, format);
         }         
      }
      numberFormatStr = buildFormatString(numberFormatOptionList);
      dateFormatStr = buildFormatString(dateFormatOptionList);
   }

   private static String buildFormatString(OptionList optionList) {
      StringBuffer ret = new StringBuffer();
      for(int i = 0; i < optionList.size(); i++ ) {
         if( i > 0 ) {
            ret.append(DELIMITER);
         }
         ret.append(optionList.getValue(i));
      }
      return ret.toString();
   }

   public OptionList getDateFormatOptionList() {
      return dateFormatOptionList;
   }
   
   public OptionList getNumberFormatOptionList() {
      return numberFormatOptionList;
   }
   
   public OptionList getFormatOptionList(String formatType) {
      if( ArcoConstants.COLUMN_TYPE_DATE.equals(formatType)) {
         return dateFormatOptionList;
      } else if ( ArcoConstants.COLUMN_TYPE_DECIMAL.equals(formatType)) {
         return numberFormatOptionList;
      } else {
         return null;
      }
   }
   
   public String getNumberFormatsString() {
      return numberFormatStr;
   }
   public String getDateFormatsString() {
      return dateFormatStr;
   }
}
