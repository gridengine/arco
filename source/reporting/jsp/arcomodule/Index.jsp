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

<jato:useViewBean className="com.sun.grid.arco.web.arcomodule.IndexViewBean">

<style type="text/css">
.tooltip {
	position: absolute;
	display: none;
	background-color: #D6D6D6;	
   border: solid 1px #000000;
   padding: 3px;
}
</style>

<script type="text/javascript" src="/com_sun_web_ui/js/browserVersion.js"></script>
<script type="text/javascript" src="/com_sun_web_ui/js/dynamic.js"></script>    
<script type="text/javascript" src="../js/arco.js"></script>

<!-- Header -->
<cc:header name="Header"
 pageTitle="application.title"
 copyrightYear="2004"
 baseName="com.sun.grid.arco.web.arcomodule.Resources"
 bundleID="arcoBundle"
 onLoad="toggleDisabledStatePage()">


 
<cc:form name="arcoForm" method="post">

<!-- Masthead -->
<cc:primarymasthead name="Masthead" bundleID="arcoBundle" />

<cc:pagetitle name="PageTitle" bundleID="arcoBundle"
    pageTitleText="index.pagetitleText"
    showPageTitleSeparator="true"
    pageTitleHelpMessage="index.pagetitle.pagehelp"
    showPageButtonsTop="true"
    showPageButtonsBottom="true">
    
    
<div class="ConMgn">  
<cc:alertinline name="Alert" bundleID="arcoBundle"/>
</div>


<div class="ConMgn"> 

<br/>
<cc:tabs name="Tabs" bundleID="arcoBundle"/>

<cc:spacer name="Spacer1" width="1" height="10" />

<script type="text/javascript">
      wmtt = null;

      document.onmousemove = updateWMTT;

      function updateWMTT(e) {
         x = (document.all) ? window.event.x + document.body.scrollLeft : e.pageX;
         y = (document.all) ? window.event.y + document.body.scrollTop  : e.pageY;
         if (wmtt != null) {
            wmtt.style.left = (x + 20) + "px";
            wmtt.style.top 	= (y + 20) + "px";
         }
      }

      function showWMTT(id) {
         wmtt = document.getElementById(id);
         if(wmtt != null ) {
            wmtt.style.display = "block";
         }
      }

      function hideWMTT() {
         if(wmtt != null ) {
            wmtt.style.display = "none";
         }
      }
</script>
<jato:content name="queryListContent">

   
   <jato:containerView name="QueryListView">
   
   <script type="text/javascript" src="../js/arco.js"></script>
   <script type="text/javascript">
      function toggleDisabledStatePage() {
      
         <cc:text name="dynamicEnableButtons"/>

         toggleDisabledState('Index.QueryListView.ActionTable',buttons);
      }
      
      
   </script>
   
     <cc:actiontable name="ActionTable"
                     bundleID="arcoBundle"
                     title="querylist.tabletitle"
                     summary="querylist.tableSummary"
                     empty="querylist.tableEmpty"
                     selectionJavascript="setTimeout('toggleDisabledStatePage()', 0)"
                     selectionType="single"
                     showPaginationControls="true"
                     showPaginationIcon="true"
                     page="1" /> <br/> <br/>

   </jato:containerView>
</jato:content>
<jato:content name="resultListContent">


   <jato:containerView name="ResultListView">
   <script type="text/javascript">
      function toggleDisabledStatePage() {
      
         <cc:text name="dynamicEnableButtons"/>

         toggleDisabledState('Index.ResultListView.ActionTable',buttons);
      }
   </script>
     <cc:actiontable name="ActionTable"
                     bundleID="arcoBundle"
                     title="resultlist.tabletitle"
                     summary="resultlist.tableSummary"
                     empty="resultlist.tableEmpty"
                     selectionJavascript="setTimeout('toggleDisabledStatePage()', 0)"
                     selectionType="single"
                     showPaginationControls="true"
                     showPaginationIcon="true"
                     page="1" /> <br/> <br/>

   </jato:containerView>
</jato:content>

  
  
</div>
</cc:pagetitle>
</cc:form>
</cc:header>
</jato:useViewBean>
