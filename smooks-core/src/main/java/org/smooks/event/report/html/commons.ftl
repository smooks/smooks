<#macro outputMessageNodes messageNodes>
    <#list messageNodes as messageNode>
        <#assign nodeDepth = messageNode.depth * 20>
        <div id="messageNode-${messageNode.nodeId}" style="margin-left: ${nodeDepth}px;">
            <#if (messageNode.execInfoNodes?size > 0)>
            	<a href='#' onclick="return selectElement('${messageNode.nodeId}');" style="text-decoration:none;">
            </#if>
            <#if messageNode.visitBefore>
                &lt;${messageNode.elementName}&gt;
            <#else>
                	&lt;/${messageNode.elementName}&gt;
            </#if>
            <#if (messageNode.execInfoNodes?size > 0)>
                <a> <a href='#' onclick="return selectElement('${messageNode.nodeId}');">*</a>
            </#if>
        </div>
    </#list>
</#macro>
<#macro outputMessageSummaries messageNodes>
    <#list messageNodes as messageNode>
        <#if (messageNode.execInfoNodes?size > 0)>
            <div id="block-${messageNode.nodeId}" style="display:none;">
                <#list messageNode.execInfoNodes as execInfoNode>
                <div id="block-details-link-${execInfoNode.nodeId}">
                    <a href='#' onclick="return selectVisitor('${execInfoNode.nodeId}');">${execInfoNode.summary}</a>
                </div>
                </#list>
            </div>
        </#if>
    </#list>
</#macro>
<#macro outputMessageDetails messageNodes>
    <#list messageNodes as messageNode>
        <#if (messageNode.execInfoNodes?size > 0)>
            <#list messageNode.execInfoNodes as execInfoNode>
                <div id="block-details-${execInfoNode.nodeId}" style="display:none;">
                <#if execInfoNode.detail??>
                <b><u>Details:</u></b><br/>
                ${execInfoNode.detail}
                <p/>
                </#if>
                <b><u>Resource Configuration:</u></b>
                <pre class="brush: xml" id="block-details-config-${execInfoNode.nodeId}"><@htmlEscape>${execInfoNode.resourceXML}</@htmlEscape></pre>
                <p/>
                <b><u>Execution Context State:</u></b> (After Visitor Execution)
                <pre class="brush: js"  id="block-details-state-${execInfoNode.nodeId}"><@htmlEscape>${execInfoNode.contextState}</@htmlEscape></pre></div>
            </#list>
        </#if>
    </#list>
</#macro>
