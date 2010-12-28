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

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.TypeInfo;
import org.w3c.dom.UserDataHandler;

public class MockElement extends MockNode implements Element {
	
	private String tagName;

	public MockElement(String tagName) {
		this.tagName = tagName;
	}

	public short getNodeType() {
		return Node.ELEMENT_NODE;
	}
	
	public String getTagName() {
		return tagName;
	}

	public void removeAttribute(String name) throws DOMException {
	}

	public boolean hasAttribute(String name) {
		return false;
	}

	public String getAttribute(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public void removeAttributeNS(String namespaceURI, String localName) throws DOMException {
		// TODO Auto-generated method stub
		
	}

	public void setAttribute(String name, String value) throws DOMException {
		// TODO Auto-generated method stub
		
	}

	public boolean hasAttributeNS(String namespaceURI, String localName) {
		// TODO Auto-generated method stub
		return false;
	}

	public Attr getAttributeNode(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public Attr removeAttributeNode(Attr oldAttr) throws DOMException {
		// TODO Auto-generated method stub
		return null;
	}

	public Attr setAttributeNode(Attr newAttr) throws DOMException {
		// TODO Auto-generated method stub
		return null;
	}

	public Attr setAttributeNodeNS(Attr newAttr) throws DOMException {
		// TODO Auto-generated method stub
		return null;
	}

	public NodeList getElementsByTagName(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getAttributeNS(String namespaceURI, String localName) {
		// TODO Auto-generated method stub
		return null;
	}

	public void setAttributeNS(String namespaceURI, String qualifiedName, String value) throws DOMException {
		// TODO Auto-generated method stub
		
	}

	public Attr getAttributeNodeNS(String namespaceURI, String localName) {
		// TODO Auto-generated method stub
		return null;
	}

	public NodeList getElementsByTagNameNS(String namespaceURI, String localName) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Element#getSchemaTypeInfo()
	 */
	public TypeInfo getSchemaTypeInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Element#setIdAttribute(java.lang.String, boolean)
	 */
	public void setIdAttribute(String arg0, boolean arg1) throws DOMException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Element#setIdAttributeNS(java.lang.String, java.lang.String, boolean)
	 */
	public void setIdAttributeNS(String arg0, String arg1, boolean arg2) throws DOMException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Element#setIdAttributeNode(org.w3c.dom.Attr, boolean)
	 */
	public void setIdAttributeNode(Attr arg0, boolean arg1) throws DOMException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Node#compareDocumentPosition(org.w3c.dom.Node)
	 */
	public short compareDocumentPosition(Node arg0) throws DOMException {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Node#getBaseURI()
	 */
	public String getBaseURI() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Node#getFeature(java.lang.String, java.lang.String)
	 */
	public Object getFeature(String arg0, String arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Node#getTextContent()
	 */
	public String getTextContent() throws DOMException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Node#getUserData(java.lang.String)
	 */
	public Object getUserData(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Node#isDefaultNamespace(java.lang.String)
	 */
	public boolean isDefaultNamespace(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Node#isEqualNode(org.w3c.dom.Node)
	 */
	public boolean isEqualNode(Node arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Node#isSameNode(org.w3c.dom.Node)
	 */
	public boolean isSameNode(Node arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Node#lookupNamespaceURI(java.lang.String)
	 */
	public String lookupNamespaceURI(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Node#lookupPrefix(java.lang.String)
	 */
	public String lookupPrefix(String arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Node#setTextContent(java.lang.String)
	 */
	public void setTextContent(String arg0) throws DOMException {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.w3c.dom.Node#setUserData(java.lang.String, java.lang.Object, org.w3c.dom.UserDataHandler)
	 */
	public Object setUserData(String arg0, Object arg1, UserDataHandler arg2) {
		// TODO Auto-generated method stub
		return null;
	}
}
