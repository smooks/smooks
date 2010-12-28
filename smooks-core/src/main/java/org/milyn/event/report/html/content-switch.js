/*
	Milyn - Copyright (C) 2006

	This library is free software; you can redistribute it and/or
	modify it under the terms of the GNU Lesser General Public
	License (version 2.1) as published by the Free Software
	Foundation.

	This library is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

	See the GNU Lesser General Public License for more details:
	http://www.gnu.org/licenses/lgpl.txt
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

