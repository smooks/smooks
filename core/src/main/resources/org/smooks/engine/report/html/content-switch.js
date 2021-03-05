/*-
 * ========================LICENSE_START=================================
 * Core
 * %%
 * Copyright (C) 2020 Smooks
 * %%
 * Licensed under the terms of the Apache License Version 2.0, or
 * the GNU Lesser General Public License version 3.0 or later.
 * 
 * SPDX-License-Identifier: Apache-2.0 OR LGPL-3.0-or-later
 * 
 * ======================================================================
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * ======================================================================
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * =========================LICENSE_END==================================
 */
function selectElement(elementId) {
    removeHighlight("left")
    removeHighlight("righttop")
    hideContent("righttop")
    hideContent("rightbottom")

    highlight("messageNode-" + elementId);
    showContent("block-" + elementId);
}

function selectVisitor(elementId) {
    removeHighlight("righttop")
    hideContent("rightbottom")

    highlightCode("block-details-config-" + elementId);
    highlightCode("block-details-state-" + elementId);
    highlight("block-details-link-" + elementId);
    showContent("block-details-" + elementId);
}

function showContent(contentId) {
    var contentElement = document.getElementById(contentId)
    if(contentElement != null) {
        contentElement.style.visibility = "visible";
        contentElement.style.display = "block";
        contentElement.setAttribute("visiblity", "set");

    }
}

function highlightCode(codeId) {
	var codeElement = document.getElementById(codeId)
    if(codeElement != null) {
    	SyntaxHighlighter.highlight(null, codeElement);
    }
}

function highlight(contentId) {
    var contentElement = document.getElementById(contentId)
    if(contentElement != null) {
        contentElement.style.backgroundColor = "yellow";
        contentElement.setAttribute("highlight", "set");
    }
}

function hideContent(contentContainerId) {
    var contentContainer = document.getElementById(contentContainerId)

    // Hide the currently selected content in that container...
    if(contentContainer != null) {
        var contentElements = contentContainer.getElementsByTagName("div");

        for(var i = 0; i < contentElements.length; i++) {
            if(contentElements.item(i).getAttribute("visiblity") == "set") {
                contentElements.item(i).style.display = "none";
                contentElements.item(i).style.visibility = "hidden";
                contentElements.item(i).removeAttribute("visiblity");
            }
        }
    } else {
        alert("Page error.  Unknown content container ID '" + contentContainerId + "'.");
    }
}

function removeHighlight(contentContainerId) {
    var contentContainer = document.getElementById(contentContainerId)

    // Hide the currently selected content in that container...
    if(contentContainer != null) {
        var contentElements = contentContainer.getElementsByTagName("div");

        for(var i = 0; i < contentElements.length; i++) {
            if(contentElements.item(i).getAttribute("highlight") == "set") {
                contentElements.item(i).style.backgroundColor = "white";
                contentElements.item(i).removeAttribute("highlight");
            }
        }
    } else {
        alert("Page error.  Unknown content container ID '" + contentContainerId + "'.");
    }
}

