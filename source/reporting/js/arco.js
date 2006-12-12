//___INFO__MARK_BEGIN__
//
//
//  The Contents of this file are made available subject to the terms of
//  the Sun Industry Standards Source License Version 1.2
//
//  Sun Microsystems Inc., March, 2001
//
//
//  Sun Industry Standards Source License Version 1.2
//  =================================================
//  The contents of this file are subject to the Sun Industry Standards
//  Source License Version 1.2 (the "License"); You may not use this file
//  except in compliance with the License. You may obtain a copy of the
//  License at http://gridengine.sunsource.net/Gridengine_SISSL_license.html
//
//  Software provided under this License is provided on an "AS IS" basis,
//  WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING,
//  WITHOUT LIMITATION, WARRANTIES THAT THE SOFTWARE IS FREE OF DEFECTS,
//  MERCHANTABLE, FIT FOR A PARTICULAR PURPOSE, OR NON-INFRINGING.
//  See the License for the specific provisions governing your rights and
//  obligations concerning the Software.
//
//   The Initial Developer of the Original Code is: Sun Microsystems, Inc.
//
//   Copyright: 2001 by Sun Microsystems, Inc.
//
//   All Rights Reserved.
//
//
//___INFO__MARK_END__

   // This function will toggle the disabled state of action buttons
   // depending on single or multiple selections.
   function toggleDisabledState(tableName, buttons) {

       var count = getTableSelectCount( document.arcoForm, tableName);

       var disabled = true;
       if( count > 0 ) {
          disabled = false;
       }
       
       for( var i = 0; i < buttons.length; i++ ) {
         ccSetButtonDisabled(buttons[i], "arcoForm", disabled);
       }
   }

   function getTableSelectCount(form, tableName) {

       var count = 0;
       var selName = tableName + ".Selection";
       // Set flag if a selection has been made.
      
       for (i = 0; i < form.elements.length; i++) {
           var e = form.elements[i];
           var name = e.name;
           
           if ((e.type == "radio" || e.type == "checkbox") &&
               e.checked &&
               e.name.indexOf(selName) == 0 ) {
               count++;
           }
       }
       return count;   
    }


    function changeFormatOptions( typeSelect, typeSelectName, formatSelectName  ) {

       var fsn = typeSelect.name.replace(typeSelectName, formatSelectName );
       var formatSelect = ccGetElement( fsn, 'arcoForm' );

       if( typeSelect.selectedIndex >= 0 ) {
          var fieldType = typeSelect.options[typeSelect.selectedIndex];
          var myOptions = null;
          if( fieldType.value == "string" ) {              
              myOptions = null;
          } else if ( fieldType.value == "decimal" ) {
              var formatField = ccGetElement( 'Query.ViewTab.numberFormatField', 'arcoForm');
              myOptions = formatField.value.split('|');
          } else if ( fieldType.value == "date" ) {
              var formatField = ccGetElement( 'Query.ViewTab.dateFormatField', 'arcoForm');
              myOptions = formatField.value.split('|');
          }
          if( myOptions == null ) {
            formatSelect.options.length = 0;
          } else {
            formatSelect.options.length = myOptions.length;
            var i = 0;
            for( i = 0; i < myOptions.length; i++ ) {
               formatSelect.options[i] = new Option(myOptions[i], myOptions[i] );
            }
          }
       }
    }


