<#if bean.singleBinding??>
    <${nsp}:reader <@writeAttribs attribs="fields,separator,quote,skipLines,rootElementName,recordElementName,indent" /> >
        <${nsp}:singleBinding <@writeAttribs attribs="beanId,beanClass@class" bean=bean.singleBinding /> />
    </${nsp}:reader>
<#elseif bean.listBinding??>
    <${nsp}:reader <@writeAttribs attribs="fields,separator,quote,skipLines,rootElementName,recordElementName,indent" /> >
        <${nsp}:listBinding <@writeAttribs attribs="beanId,beanClass@class" bean=bean.listBinding /> />
    </${nsp}:reader>
<#elseif bean.mapBinding??>
    <${nsp}:reader <@writeAttribs attribs="fields,separator,quote,skipLines,rootElementName,recordElementName,indent" /> >
        <${nsp}:mapBinding <@writeAttribs attribs="beanId,beanClass@class,keyField" bean=bean.mapBinding /> />
    </${nsp}:reader>
<#else>
    <${nsp}:reader <@writeAttribs attribs="fields,separator,quote,skipLines,rootElementName,recordElementName,indent" /> />
</#if>