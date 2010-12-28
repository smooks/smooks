<#assign orderItem = .vars["order-item"]> <#-- special assignment because order-item has a hyphen -->
<orderitem id="${orderItem.@id}" order="${order.@id}">
    <customer>
        <name>${order.header.customer}</name>
        <number>${order.header.customer.@number}</number>
    </customer>
    <details>
        <productId>${orderItem.product}</productId>
        <quantity>${orderItem.quantity}</quantity>
        <price>${orderItem.price}</price>
    </details>
</orderitem>