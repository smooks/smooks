<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
				version="1.0">

	<xsl:output method="xml" encoding="UTF-8" />

	<xsl:template match="Order">
		<Order orderId="{header/order-id}" statusCode="{header/status-code}" netAmount="{header/net-amount}"
						totalAmount="{header/total-amount}" tax="{header/tax}" date="{header/date/month}-{header/date/day}-{header/date/year}">
			<xsl:apply-templates select="customer-details"/>
			<OrderLines>
				<xsl:apply-templates select="order-item"/>
			</OrderLines>
		</Order>
	</xsl:template>

	<xsl:template match="customer-details">
		<Customer userName="{username}" firstName="{name/firstname}" lastName="{name/lastname}" state="{state}"/>
	</xsl:template>

	<xsl:template match="order-item">
        <order-item quantity="{quantity}" product-id="{product-id}" price="{price}">
			<xsl:value-of select="title" />
        </order-item>
	</xsl:template>

</xsl:stylesheet>