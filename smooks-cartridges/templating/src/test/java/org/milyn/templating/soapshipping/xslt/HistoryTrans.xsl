<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
				xmlns:smooks-bean="xalan://org.milyn.templating.xslt.XalanJavabeanExtension"
				extension-element-prefixes="smooks-bean" 
				version="1.0">

	<!-- Root template  -->
	<xsl:template match="*">
		<trackingNumbers>
			<xsl:call-template name="outputTrackingNumber"/>
    	</trackingNumbers>
	</xsl:template>

	<xsl:variable name="trackingNumberCount" select="smooks-bean:select('history.trackingNumbers.length')"/>
	
	<!-- Recursively called template for outputting the trackingNumber elements -->
	<xsl:template name="outputTrackingNumber">
		<xsl:param name="i" select="0"/>

		<xsl:if test="$i &lt; $trackingNumberCount">
			<trackingNumber>
				<shipperID>
					<xsl:variable name="ognl" select="concat('history.trackingNumbers[', $i, '].shipperID')" />
					<xsl:value-of select="smooks-bean:select($ognl)" />
				</shipperID>
				<shipmentNumber>
					<xsl:variable name="ognl" select="concat('history.trackingNumbers[', $i, '].shipmentNumber')" />
					<xsl:value-of select="smooks-bean:select($ognl)" />
				</shipmentNumber>
			</trackingNumber>
		
			<xsl:call-template name="outputTrackingNumber">
				<xsl:with-param name="i" select="$i + 1"/>
			</xsl:call-template>
		</xsl:if>
	</xsl:template>

</xsl:stylesheet>		