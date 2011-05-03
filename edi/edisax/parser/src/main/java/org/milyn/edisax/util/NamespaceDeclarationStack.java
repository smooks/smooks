/*
 * Milyn - Copyright (C) 2006 - 2011
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License (version 2.1) as published by the Free Software
 * Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU Lesser General Public License for more details:
 * http://www.gnu.org/licenses/lgpl.txt
 */
package org.milyn.edisax.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.xml.XMLConstants;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * This class is responsible for managing namespace prefix mapping stack.
 * Limitation - not supported : re-defining a namespace prefix (child element re-define namespace prefix defined by parent element)
 * 
 * @author zubairov
 */
public class NamespaceDeclarationStack {

	private final ContentHandler handler;
	
	private final Stack<List<String>> nsStack = new Stack<List<String>>();
	
	public NamespaceDeclarationStack(ContentHandler handler) {
		this.handler = handler;
	}
	
	/**
	 * Pop element out of the namespace declaration stack and notifying
	 * {@link ContentHandler} if required
	 * 
	 * @throws SAXException 
	 */
	public void pop() throws SAXException {
        List<String> pop = nsStack.pop();
        Collections.reverse(pop);
        for (String ns : pop) {
    		handler.endPrefixMapping(ns);
		}
	}
	
	/**
	 * Pushing a new element to the stack
	 * 
	 * @param attributes optional attributes or null, single element could declare multiple namespaces
	 * @return modified attributes declaration in case additional prefix mapping should be included
	 * @throws SAXException 
	 */
	public Attributes push(String prefix, String namespace, Attributes attributes) throws SAXException {
        List<String> namespaces = new ArrayList<String>();
        // Volatile array
        Map<String, String> nsToURI = new HashMap<String, String>();
    	AttributesImpl attrs;
        if(attributes != null) {
            attrs = new AttributesImpl(attributes);
        } else {
            attrs = new AttributesImpl();
        }
        // Gather namespace declarations from the attributes
        for(int i=0;i<attrs.getLength();i++) {
        	String qname = attrs.getQName(i);
        	if (qname != null && qname.startsWith(XMLConstants.XMLNS_ATTRIBUTE + ":")) {
        		// Add prefix to the list of declared namespaces
        		namespaces.add(attrs.getLocalName(i));
        		nsToURI.put(attrs.getLocalName(i), attrs.getValue(i));
        	}
        }
        if (!prefixAlreadyDeclared(prefix)) {
			// Add a new attribute to the list of attributes
			attrs.addAttribute(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, prefix, "xmlns:" + prefix, "CDATA", namespace);
			namespaces.add(prefix);
			nsToURI.put(prefix, namespace);
		}
        nsStack.push(namespaces);
        // Now call start prefixes if namespaces are not empty
        for (String nsPrefix : namespaces) {
        	String uri = nsToURI.get(nsPrefix);
			handler.startPrefixMapping(nsPrefix, uri);
		}
		return attrs;
	}

	/**
	 * This method returns true if namespace with given prefix was already declared higher
	 * the stack
	 * 
	 * @param prefix
	 * @return
	 */
	private boolean prefixAlreadyDeclared(String prefix) {
		for (List<String> set : nsStack) {
			if (set.contains(prefix)) {
				return true;
			}
		}
		return false;
	}
	
}
