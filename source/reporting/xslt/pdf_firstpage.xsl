
           <fo:block font-size="24pt" padding-after="1cm">
              <fo:external-graphic src="url(file:///var/opt/webconsole/webapps/reporting/images/logo_sge.gif)"/>           
           </fo:block>

           <fo:block font-size="24pt" padding-after="1cm">
              <xsl:value-of select="@name"/>
           </fo:block>
           
           <fo:table table-layout="fixed">
             <fo:table-column column-width="30mm"/>
             <fo:table-column column-width="120mm"/>
             <fo:table-body>
               <fo:table-row>
                 <fo:table-cell padding="3pt">
                   <fo:block>Category:</fo:block>
                 </fo:table-cell>
                 <fo:table-cell padding="3pt">
                   <fo:block><xsl:value-of select="@category"/></fo:block>
                 </fo:table-cell>
               </fo:table-row>
               <xsl:if test="view/description/@visible='true'">
                  <fo:table-row>
                    <fo:table-cell padding="3pt">
                      <fo:block>Description:</fo:block>
                    </fo:table-cell>
                    <fo:table-cell padding="3pt">
                      <fo:block><xsl:value-of select="description"/></fo:block>
                    </fo:table-cell>
                  </fo:table-row>
               </xsl:if>
               <xsl:if test="view/sql/@visible='true'">
               <fo:table-row>
                 <fo:table-cell padding="3pt">
                   <fo:block>SQL:</fo:block>
                 </fo:table-cell>
                 <fo:table-cell padding="3pt">
                   <fo:block><xsl:value-of select="sql"/></fo:block>
                 </fo:table-cell>
               </fo:table-row>
               
               </xsl:if>
             </fo:table-body>
           </fo:table>
           
