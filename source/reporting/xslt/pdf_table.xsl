
    <!-- table start -->
    <fo:block padding-before="1cm">
    <fo:table table-layout="fixed">
      @@COLUMNWIDTH@@
      <fo:table-header>
        <fo:table-row>
          <xsl:for-each select="column">
            <fo:table-cell border="0.5pt solid" padding="4pt" font-size="8pt" font-weight="bold">
              <fo:block> <xsl:value-of select="@name"/> </fo:block>
            </fo:table-cell>
          </xsl:for-each>
        </fo:table-row>
      </fo:table-header> 
    

      <fo:table-body>
        <xsl:for-each select="row">
        <fo:table-row>
          <xsl:for-each select="value">
            <fo:table-cell border="0.5pt solid" padding="4pt" font-size="8pt">
               <fo:block><xsl:value-of select="current()"/></fo:block>
            </fo:table-cell>
          </xsl:for-each>
        </fo:table-row>
        </xsl:for-each>
      </fo:table-body>
    </fo:table>
    </fo:block>
    <!-- table end -->
