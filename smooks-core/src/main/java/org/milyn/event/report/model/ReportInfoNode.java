/*
	Milyn - Copyright (C) 2006 - 2010

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
package org.milyn.event.report.model;

/**
 * Execution Node.
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class ReportInfoNode {

    private int nodeId;
    private String summary;
    private String detail;
    private String resourceXML;
    private String contextState;

    public ReportInfoNode() {
    }

    public ReportInfoNode(String summary, String detail) {
        this.summary = summary;
        this.detail = detail;
    }

    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getResourceXML() {
        return resourceXML;
    }

    public void setResourceXML(String resourceXML) {
        this.resourceXML = resourceXML;
    }

    public String getContextState() {
        return contextState;
    }

    public void setContextState(String contextState) {
        this.contextState = contextState;
    }
}
