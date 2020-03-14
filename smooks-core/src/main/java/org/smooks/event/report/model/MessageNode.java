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
package org.smooks.event.report.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Message Node.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class MessageNode {

    private int nodeId;
    private String elementName;
    private boolean isVisitBefore;
    private int depth;
    private List<ReportInfoNode> execInfoNodes = new ArrayList<ReportInfoNode>();

    public int getNodeId() {
        return nodeId;
    }

    public void setNodeId(int nodeId) {
        this.nodeId = nodeId;
    }

    public String getElementName() {
        return elementName;
    }

    public void setElementName(String elementName) {
        this.elementName = elementName;
    }

    public boolean isVisitBefore() {
        return isVisitBefore;
    }

    public void setVisitBefore(boolean visitBefore) {
        isVisitBefore = visitBefore;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public List<ReportInfoNode> getExecInfoNodes() {
        return execInfoNodes;
    }

    public boolean addExecInfoNode(ReportInfoNode infoNode) {
        return getExecInfoNodes().add(infoNode);
    }
}
