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

package org.milyn.dom;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class MockNode implements Node {

	public void normalize() {
		// TODO Auto-generated method stub

	}

	public boolean hasAttributes() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean hasChildNodes() {
		// TODO Auto-generated method stub
		return false;
	}

	public String getLocalName() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getNamespaceURI() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getNodeName() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getNodeValue() throws DOMException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getPrefix() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setNodeValue(String nodeValue) throws DOMException {
		// TODO Auto-generated method stub

	}

	public void setPrefix(String prefix) throws DOMException {
		// TODO Auto-generated method stub

	}

	public Document getOwnerDocument() {
		// TODO Auto-generated method stub
		return null;
	}

	public NamedNodeMap getAttributes() {
		// TODO Auto-generated method stub
		return null;
	}

	public Node getFirstChild() {
		// TODO Auto-generated method stub
		return null;
	}

	public Node getLastChild() {
		// TODO Auto-generated method stub
		return null;
	}

	public Node getNextSibling() {
		// TODO Auto-generated method stub
		return null;
	}

	public Node getParentNode() {
		// TODO Auto-generated method stub
		return null;
	}

	public Node getPreviousSibling() {
		// TODO Auto-generated method stub
		return null;
	}

	public Node cloneNode(boolean deep) {
		// TODO Auto-generated method stub
		return null;
	}

	public NodeList getChildNodes() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean isSupported(String feature, String version) {
		// TODO Auto-generated method stub
		return false;
	}

	public Node appendChild(Node newChild) throws DOMException {
		// TODO Auto-generated method stub
		return null;
	}

	public Node removeChild(Node oldChild) throws DOMException {
		// TODO Auto-generated method stub
		return null;
	}

	public Node insertBefore(Node newChild, Node refChild) throws DOMException {
		// TODO Auto-generated method stub
		return null;
	}

	public Node replaceChild(Node newChild, Node oldChild) throws DOMException {
		// TODO Auto-generated method stub
		return null;
	}

}
