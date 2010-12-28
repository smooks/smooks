<#if bean.singleBinding??>
    <${nsp}:reader <@writeAttribs attribs="fields,separator,quote,skipLines,rootElementName,recordElementName,indent,strict,validateHeader" /> >
        <${nsp}:singleBinding <@writeAttribs attribs="beanId,beanClass@class" bean=bean.singleBinding /> />
    </${nsp}:reader>
<#elseif bean.listBinding??>
    <${nsp}:reader <@writeAttribs attribs="fields,separator,quote,skipLines,rootElementName,recordElementName,indent,strict,validateHeader" /> >
        <${nsp}:listBinding <@writeAttribs attribs="beanId,beanClass@class" bean=bean.listBinding /> />
    </${nsp}:reader>
<#elseif bean.mapBinding??>
    <${nsp}:reader <@writeAttribs attribs="fields,separator,quote,skipLines,rootElementName,recordElementName,indent,strict,validateHeader" /> >
        <${nsp}:mapBinding <@writeAttribs attribs="beanId,beanClass@class,keyField" bean=bean.mapBinding /> />
    </${nsp}:reader>
<#else>
    <${nsp}:reader <@writeAttribs attribs="fields,separator,quote,skipLines,rootElementName,recordElementName,indent,strict,validateHeader" /> />
</#if>