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
package com.sun.grid.arco.web.arcomodule.query;

import com.iplanet.jato.model.Model;
import com.iplanet.jato.view.ContainerView;
import com.iplanet.jato.view.View;
import com.sun.grid.arco.ArcoConstants;
import com.sun.grid.arco.web.arcomodule.ArcoServlet;
import com.sun.web.ui.view.html.CCDropDownMenu;

public class FormatDropDownMenu extends CCDropDownMenu {
   

   private String formatTypeValueName;
   
   
   public FormatDropDownMenu(View view, String name, Model model, String formatTypeValueName) {
      super((ContainerView)view, model,name,null);
      this.formatTypeValueName = formatTypeValueName;
   }
   
   public com.iplanet.jato.view.html.OptionList getOptions() {
      
      Object formatType = getModel().getValue(formatTypeValueName);
      
      if( ArcoConstants.COLUMN_TYPE_DATE.equals(formatType) ) {
         return ArcoServlet.getInstance().getFormatHelper().getDateFormatOptionList();
      } else if ( ArcoConstants.COLUMN_TYPE_DECIMAL.equals(formatType) ) {
         return ArcoServlet.getInstance().getFormatHelper().getNumberFormatOptionList();
      } else {
         return super.getOptions();
      }
   }
   
   
}
