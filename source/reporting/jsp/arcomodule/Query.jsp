<%--
/*
 * JSP for displaying a Advanced Query
 * 
 */
--%>
<%--
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
--%>

<%@ page language="java" %> 
<%@taglib uri="/WEB-INF/tld/com_iplanet_jato/jato.tld" prefix="jato"%>
<%@taglib uri="/WEB-INF/tld/com_sun_web_ui/cc.tld" prefix="cc"%>

<jato:useViewBean className="com.sun.grid.arco.web.arcomodule.QueryViewBean">

<!-- Header -->
<cc:header name="Header"
 pageTitle="application.title"
 copyrightYear="2004"
 baseName="com.sun.grid.arco.web.arcomodule.Resources"
 bundleID="arcoBundle"
 onLoad="">

 
<cc:form name="arcoForm" method="post">



<!-- Masthead -->
<cc:primarymasthead name="Masthead" bundleID="arcoBundle" />

<div class="ConMgn">  
<cc:alertinline name="Alert" bundleID="arcoBundle"/>
</div>

<cc:breadcrumbs name="BreadCrumb" bundleID="arcoBundle" />

<cc:pagetitle name="PageTitle" bundleID="arcoBundle"
    pageTitleText="query.pagetitleText"
    showPageTitleSeparator="true"
    pageTitleHelpMessage="query.pagetitle.pagehelp"
    showPageButtonsTop="true"
    showPageButtonsBottom="true">

<script type="text/javascript" src="/com_sun_web_ui/js/browserVersion.js"></script>
<script type="text/javascript" src="/com_sun_web_ui/js/dynamic.js"></script>    
<script type="text/javascript" src="../js/arco.js"></script>
    
<script type="text/javascript"> 

  function setDirty() {
      <cc:text name="setDirtyJavaScript"/>
  }
  
  function isUnsignedInteger(s) {
     return (s.toString().search(/^[0-9]+$/) == 0);
  }
  
  function validate_integer(field) {
     with(field){
        if (value==null||value==""||isUnsignedInteger(value)==false)  {
           alert("The value must be an integer");
           field.value="0";
           field.select();
           return false;
        }  else   {
           return true;
        }
     }
  }
  
  /**
   * This function is call if in the SimpleTab the onChange event of the
   * table/view DropDownMenu occurs.
   */
  function changeTableConfirm() {

     if( window.confirm("When changing the table/view all fields and filters\nwill be deleted!\nContinue?") ) {
        var f=document.arcoForm;        
        if (f != null) {
           f.action='../arcomodule/Query?Query.SimpleTab.tableChangedHref=';
           f.submit();
        }
     } else {
        var tableValueSelect = ccGetElement('Query.SimpleTab.tableValue','arcoForm');
        var orgTableValue = ccGetElement('Query.SimpleTab.orgTableValue','arcoForm');
        var i;
        var len;
        var eq;
        
        len = tableValueSelect.options.length;
        for( i = 0; i < len; i++ ) {
           if( tableValueSelect.options[i].value == orgTableValue.value ) {
               var orgOnChange = tableValueSelect.onChange;
               tableValueSelect.onChange = null;
               tableValueSelect.selectedIndex = i;
               tableValueSelect.onChange = orgOnChange;
               break;
           }
        }
     }
  }
  
  function toggleButtons() {
       toggleFieldButtons();
       toggleFilterButtons();
       toggleViewTableButtons();
       toggleViewPivotButtons();
  }
  
   function toggleFieldButtons() {
      var buttons = ["Query.SimpleTab.EditFieldButton",
                     "Query.SimpleTab.RemoveFieldButton"];
      toggleDisabledState('Query.SimpleTab.fieldValue',buttons);
   }

   function toggleFilterButtons() {
      var buttons = ["Query.SimpleTab.EditFilterButton",
                     "Query.SimpleTab.RemoveFilterButton"];
      toggleDisabledState('Query.SimpleTab.filterValue',buttons);
   }

   function toggleViewTableButtons() {
      var buttons = ["Query.ViewTab.RemoveViewTableRowButton"];
      toggleDisabledState('Query.ViewTab.viewTable',buttons);
   }
   
   function toggleViewPivotButtons() {
      var buttons = ["Query.ViewTab.RemoveViewPivotButton"];
      toggleDisabledState('Query.ViewTab.pivotTable',buttons);
   }
   
   
   function saveAs() {   
      <cc:text name="SaveAsPrompt"/>
      if( prompt_result != null ) {
        var f=document.arcoForm; 
        if( f != null ) {
           var saveAsNameField = ccGetElement( 'Query.SaveAsQueryName', f.name );
           saveAsNameField.value = prompt_result;
           return true;
        }
      } else {
        return false;
      }
   }
  
</script>
    
<cc:hidden name="SaveAsQueryName"/>
<cc:hidden name="LastVisitedTab"/>

<br>    
<div class="ConMgn">    

<!-- Navigation Tabs -->
<cc:tabs name="Tabs" bundleID="arcoBundle" submitFormData="true"/>

<jato:content name="sqlTabContent">
   <jato:containerView name="SQLTab">
      <cc:propertysheet name="SqlPropertySheet" 
          bundleID="arcoBundle" 
          showJumpLinks="true"
          addJavaScript="true" />
   </jato:containerView>
   
</jato:content>
  
<jato:content name="commonTabContent">
   <jato:containerView name="CommonTab">
      <cc:propertysheet name="CommonPropertySheet" 
          bundleID="arcoBundle" 
          showJumpLinks="true"
          addJavaScript="true" />
   </jato:containerView>
</jato:content>

<jato:content name="simpleTabContent">
   <jato:containerView name="SimpleTab">
      <cc:propertysheet name="SimplePropertySheet" 
          bundleID="arcoBundle" 
          showJumpLinks="true"
          addJavaScript="true" />
   </jato:containerView>
</jato:content>

<jato:content name="viewTabContent">

   <jato:containerView name="ViewTab">
      <cc:hidden name="numberFormatField"/>
      <cc:hidden name="dateFormatField"/>
      <cc:propertysheet name="ViewPropertySheet" 
          bundleID="arcoBundle" 
          showJumpLinks="true"
          addJavaScript="true" />
   </jato:containerView>
</jato:content>
  
</div>
</cc:pagetitle>
</cc:form>
</cc:header>
</jato:useViewBean>
