<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
				version="1.0">

	<xsl:output method="xml" encoding="UTF-8" />

	<xsl:template match="*" name="templatelet">
        <x id="{@id}">Content from template!!</x>
    </xsl:template>

</xsl:stylesheet>
