<${nsp}:root <@writeNamespaces/>>

	<${nsp}:bbb>
	   <${nsp}:value property="${bean.floatProperty?string("0.##")}" />
	</${nsp}:bbb>
	<#list bean.aaas as aaa>
    <@writeBean bean=aaa indent="4"/>
    </#list>
	
</${nsp}:root>