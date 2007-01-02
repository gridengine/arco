<xsl:stylesheet xmlns:xsl = "http://www.w3.org/1999/XSL/Transform" version = "1.0" >
<xsl:output method="text" indent="no"/>

<xsl:template match="/">
  <xsl:for-each select="Result/column">
    <xsl:if test="position() &gt; 1">
       <xsl:text>,</xsl:text>
    </xsl:if>
    <xsl:value-of select="@name" disable-output-escaping="no"/>
  </xsl:for-each>

  <xsl:text>&#xa;</xsl:text>
  <xsl:for-each select="Result/row">
    <xsl:for-each select="value">
       <xsl:if test="position() != 1">
          <xsl:text>,</xsl:text>
       </xsl:if>
       <xsl:value-of select="current()" disable-output-escaping="yes"/>
    </xsl:for-each>
    <xsl:text>&#xa;</xsl:text>    
 </xsl:for-each>

</xsl:template>

</xsl:stylesheet>
