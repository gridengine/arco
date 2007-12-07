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

<jato:useViewBean className="com.sun.grid.arco.web.arcomodule.ResultViewBean">

<!-- Header -->
<cc:header name="Header"
 pageTitle="application.title"
 copyrightYear="2004"
 baseName="com.sun.grid.arco.web.arcomodule.Resources"
 bundleID="arcoBundle"
 onLoad="document.arcoForm.elements[0].focus();">

 
<cc:form name="arcoForm" method="post">



<!-- Masthead -->
<cc:primarymasthead name="Masthead" bundleID="arcoBundle" />

<div class="ConMgn">  
<cc:alertinline name="Alert" bundleID="arcoBundle"/>
</div>

<cc:breadcrumbs name="BreadCrumb" bundleID="arcoBundle" />

<script type="text/javascript" src="/com_sun_web_ui/js/browserVersion.js"></script>
<script type="text/javascript" src="/com_sun_web_ui/js/dynamic.js"></script>    
<script type="text/javascript" src="../js/arco.js"></script>

<script type="text/javascript"> 
   function edit() {   
        var f=document.arcoForm; 
        f.action='../arcomodule/Result?Result.EditButton=';
        f.submit();
   }
    function save() {   
        var f=document.arcoForm; 
        if( f != null ) {
            var pvm = ccGetElement( 'Result.PageViewMenu', f.name );
            if( pvm != null ) {
                if( pvm.value!="HTML") {
                    pvm.value="HTML";
                }
            }
            var prompt_result = prompt("Enter the result name:", "");
            if( prompt_result != null ) {
                var saveAsNameField = ccGetElement( 'Result.SaveAsResultName', f.name );
                saveAsNameField.value = prompt_result;
                return true;
            } else {
                return false;
            }
        }
    }
</script>
<cc:hidden name="calledFromQuery"/>
<cc:hidden name="SaveAsResultName"/>

<cc:pagetitle name="PageTitle" bundleID="arcoBundle"
    viewMenuLabel="export.name" 
    pageTitleText="result.pagetitleText"
    showPageTitleSeparator="true"    
    showPageButtonsTop="true"
    showPageButtonsBottom="true">
    
   
   <cc:propertysheet name="ResultPropertySheet" 
       bundleID="arcoBundle" 
       showJumpLinks="false"
       addJavaScript="true" />

</cc:pagetitle>
</cc:form>
</cc:header>
</jato:useViewBean>
