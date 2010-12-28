<shipping-history date="${history.creationDate?string("yyyy-MM-dd")}">
    <warehouse id="${history.warehouse.id}" location="${history.warehouse.name}" />
    <trackingNumbers>
        <#list history.trackingNumbers as trackingNumber>
        <trackingNumber>
            <shipperID>${trackingNumber.shipperID}</shipperID>
            <shipmentNumber>${trackingNumber.shipmentNumber}</shipmentNumber>
        </trackingNumber>
        </#list>
    </trackingNumbers>
</shipping-history>
