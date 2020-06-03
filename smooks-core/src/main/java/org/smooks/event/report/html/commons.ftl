<#--
 ========================LICENSE_START=================================
 Smooks Core
 %%
 Copyright (C) 2020 Smooks
 %%
 Licensed under the terms of the Apache License Version 2.0 or,
 the GNU Lesser General Public License version 3.0 or later.
 
 SPDX-License-Identifier: Apache-2.0 OR LGPL-3.0-or-later
 
 ======================================================================
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
     http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 
 ======================================================================
 
 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 3 of the License, or (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.
 
 You should have received a copy of the GNU Lesser General Public License
 along with this program; if not, write to the Free Software Foundation,
 Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 =========================LICENSE_END==================================
-->
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
