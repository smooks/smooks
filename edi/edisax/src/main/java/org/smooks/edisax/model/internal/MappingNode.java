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

package org.smooks.edisax.model.internal;

import org.smooks.edisax.util.EDIUtils;
import org.smooks.edisax.util.IllegalNameException;

public class MappingNode {

    private String name;
    private String namespace;
    private String documentation;

    public static final String INDEXED_NODE_SEPARATOR = "_-_-";

    private String xmltag;
    private String nodeTypeRef;
    private MappingNode parent;

    public MappingNode() {
	}
    
	public MappingNode(String xmltag, String namespace) {
		this.xmltag = xmltag;
		this.namespace = namespace;
	}

	public String getXmltag() {
        return xmltag;
    }

    public void setXmltag(String value) {
        this.xmltag = value;
    }

    public String getNodeTypeRef() {
        return nodeTypeRef;
    }

    public void setNodeTypeRef(String nodeTypeRef) {
        this.nodeTypeRef = nodeTypeRef;
    }

    public String getDocumentation() {
        return documentation;
    }

    public void setDocumentation(String documentation) {
        this.documentation = documentation;
    }

    public MappingNode getParent() {
        return parent;
    }

    public void setParent(MappingNode parent) {
        this.parent = parent;
    }

    public String getJavaName() throws IllegalNameException {
        String javaName = xmltag.replace(INDEXED_NODE_SEPARATOR, "_");

        if(name != null && name.trim().length() > 0) {
            javaName += "_" + EDIUtils.encodeClassName(name);
        }

        return javaName;
    }

    public void setName(String name) {
        if (name != null) {
            name = name.trim();
        }
        this.name = name;
    }

    public String getName() {
        return name;
    }

	public String getNamespace() {
		return namespace;
	}
	
	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}
}
