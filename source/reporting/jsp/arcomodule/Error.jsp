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

<%@page contentType="text/html" language="java"%>
<!-- Here I include the two required tag libs. -->
<%@taglib uri="/WEB-INF/tld/com_iplanet_jato/jato.tld" prefix="jato"%>
<%@taglib uri="/WEB-INF/tld/com_sun_web_ui/cc.tld" prefix="cc"%>


<!-- useViewBean -->
<jato:useViewBean className="com.sun.grid.arco.web.arcomodule.ErrorViewBean">
<!-- cc:header HEAD/TITLE/BODY/stylesheet.  It doesn't require
a peer in the view bean.  It must come before other tags so that the header can
be the first thing on the HTML page. -->
<cc:i18nbundle
  baseName="com.sun.grid.arco.web.arcomodule.Resources"
  id="ReportingBundle" />
<cc:header name="header"
           pageTitle="application.title" 
           copyrightYear="2004"
           baseName="com.sun.grid.arco.web.arcomodule.Resources"
           bundleID="arcoBundle">
           
<jato:form  name="arcoForm" method="post">

    <!-- masthead -->
    <cc:primarymasthead name="Masthead" bundleID="arcoBundle"/>
          

         <div class="TtlTxtDiv">
            <span class="TtlTxt">Unexpected Error</span>
         </div>
         
         <cc:alert name="Alert" bundleID="arcoBundle" />
         
         <div class="TtlTxtDiv">
            <cc:button name="okButton"                     
                       bundleID="arcoBundle" 
                       defaultValue="button.ok"
                       type="primary"/>
         </div>
         
         
</jato:form>
</cc:header>
<!-- Be sure to close the jato:useViewBean tag -->
</jato:useViewBean>
