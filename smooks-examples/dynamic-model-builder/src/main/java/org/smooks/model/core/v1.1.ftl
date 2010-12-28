<?xml version="1.0"?>
<smooks-resource-list <@writeNamespaces indent="22"/>>

<#list bean.readers as reader><@writeBean bean=reader /></#list>
<#list bean.beans as bean><@writeBean bean=bean /></#list>

</smooks-resource-list>