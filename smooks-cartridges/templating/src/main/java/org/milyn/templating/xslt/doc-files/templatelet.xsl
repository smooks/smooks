<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
				xmlns:smooks-bean="xalan://org.milyn.templating.xslt.XalanJavabeanExtension"
				extension-element-prefixes="smooks-bean" 
				version="1.0">

	<xsl:output method="xml" encoding="UTF-8" />

	<xsl:template match="*" name="templatelet">@@@templatelet@@@</xsl:template>

</xsl:stylesheet>
