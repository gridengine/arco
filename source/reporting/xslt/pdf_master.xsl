<?xml version="1.0" encoding="utf-8"?>
<!--

#___INFO__MARK_BEGIN__
##########################################################################
#
#  The Contents of this file are made available subject to the terms of
#  the Sun Industry Standards Source License Version 1.2
#
#  Sun Microsystems Inc., March, 2001
#
#
#  Sun Industry Standards Source License Version 1.2
#  =================================================
#  The contents of this file are subject to the Sun Industry Standards
#  Source License Version 1.2 (the "License"); You may not use this file
#  except in compliance with the License. You may obtain a copy of the
#  License at http://gridengine.sunsource.net/Gridengine_SISSL_license.html
#
#  Software provided under this License is provided on an "AS IS" basis,
#  WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING,
#  WITHOUT LIMITATION, WARRANTIES THAT THE SOFTWARE IS FREE OF DEFECTS,
#  MERCHANTABLE, FIT FOR A PARTICULAR PURPOSE, OR NON-INFRINGING.
#  See the License for the specific provisions governing your rights and
#  obligations concerning the Software.
#
#  The Initial Developer of the Original Code is: Sun Microsystems, Inc.
#
#  Copyright: 2001 by Sun Microsystems, Inc.
#
#  All Rights Reserved.
#
##########################################################################
#___INFO__MARK_END__

-->
<xsl:stylesheet version="1.1" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                xmlns:java="http://xml.apache.org/xslt/java" 
                exclude-result-prefixes="fo">
<!--  <xsl:output method="xml" 
            version="1.0" 
            omit-xml-declaration="no" 
            indent="yes"/> -->
  
            
  <!-- >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>Variables start here <<<<<<<<<<<<<<<<<<<<<<<<<<<<-->
<!--@@VARIABLES@@-->
<xsl:variable name="user">Richard Hierlmeier</xsl:variable>
  <!-- >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>Variables end here <<<<<<<<<<<<<<<<<<<<<<<<<<<<-->
<xsl:template match="Result">  

<fo:root> 


  <!-- defines the layout master -->
  <fo:layout-master-set>
    <fo:simple-page-master master-name="first" 
                           page-height="29.7cm" 
                           page-width="21cm" 
                           margin-top="1cm" 
                           margin-bottom="2cm" 
                           margin-left="2.5cm" 
                           margin-right="2.5cm">
      <fo:region-body margin-top="3cm" margin-bottom="1.5cm"/>
      <fo:region-before extent="3cm"/>
      <fo:region-after extent="1.5cm"/>
    </fo:simple-page-master>
  </fo:layout-master-set>

   <fo:page-sequence master-reference="first">
    <fo:static-content flow-name="xsl-region-before" >
     <fo:block font-size="8pt" text-align="start">
         <xsl:value-of select='java:format(java:java.text.SimpleDateFormat.new("yyyy-MM-dd HH:mm:ss"), java:java.util.Date.new())'/>
     </fo:block>
     <fo:block font-size="8pt" text-align="end">         
          Page <fo:page-number/>
      </fo:block>
    </fo:static-content>

     <fo:flow flow-name="xsl-region-body">    
      <!--@@CONTENT@@-->
     </fo:flow>      
   </fo:page-sequence>
  </fo:root>
 </xsl:template>
</xsl:stylesheet>
