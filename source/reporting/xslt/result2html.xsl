<xsl:stylesheet xmlns:xsl = "http://www.w3.org/1999/XSL/Transform" version = "1.0" > 
<xsl:output method="html" />

<xsl:template match="/">
<html>
  <body>
    <table border="0" width="100%">
      <xsl:comment>Header-Section</xsl:comment>
      <tr bgcolor="#cccccc">
        <xsl:for-each select="Result/column">
          <th><xsl:value-of select="@name" /></th>
        </xsl:for-each>
      </tr>
      <xsl:comment>Data-Section</xsl:comment>
      <xsl:for-each select="Result/row">
      <tr>
       <xsl:for-each select="value">
          <td><xsl:value-of select="current()" disable-output-escaping="yes"/></td>
       </xsl:for-each>
     </tr> 
      </xsl:for-each>
    </table>
  </body>
</html>
</xsl:template>

</xsl:stylesheet>
