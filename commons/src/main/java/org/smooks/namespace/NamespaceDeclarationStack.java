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
package org.smooks.namespace;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.XMLConstants;
import java.util.*;

/**
 * This class is responsible for managing namespace declarations.
 *
 * @author zubairov
 */
public class NamespaceDeclarationStack {
    private final Stack<Map<String, String>> namespaceStack = new Stack<Map<String, String>>();
    private final Stack<XMLReader> readerStack = new Stack<XMLReader>();

    public NamespaceDeclarationStack() {
    }

    public NamespaceDeclarationStack(XMLReader xmlReader) {
        pushReader(xmlReader);
    }

	/**
	 * Pushing a new element to the stack.
	 *
     * @param qName Element QName.
     * @param namespace Element namespace.
	 * @param attributes optional attributes or null, single element could declare multiple namespaces
	 * @return modified attributes declaration in case additional prefix mapping should be included
   *
	 * @throws SAXException if an error is encountered when attempting to push
   * the element to the stack.
	 */
	@SuppressWarnings({ "unchecked", "UnusedReturnValue" })
  public Attributes pushNamespaces(String qName, String namespace, Attributes attributes) throws SAXException {
        if(attributes == null || attributes.getLength() == 0) {
            if(namespace == null || XMLConstants.NULL_NS_URI.equals(namespace)) {
                namespaceStack.push(Collections.EMPTY_MAP);
                return attributes;
            }
        }

        Map<String, String> nsToURI = Collections.EMPTY_MAP;

        // Gather namespace declarations from the attributes
        if(attributes != null) {
            for(int i = 0; i < attributes.getLength() ; i++) {
                String attrNS = attributes.getURI(i);

                if (attrNS != null && attrNS.equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI)) {
                    // Add prefix to the list of declared namespaces
                    if(nsToURI == Collections.EMPTY_MAP) {
                        nsToURI = new LinkedHashMap<String, String>();
                    }

                    String localName = attributes.getLocalName(i);
                    if (localName.equals(XMLConstants.XMLNS_ATTRIBUTE)) {
                        nsToURI.put(XMLConstants.DEFAULT_NS_PREFIX, attributes.getValue(i));
                    } else {
                        nsToURI.put(localName, attributes.getValue(i));
                    }
                }
            }
        }

        if(!XMLConstants.NULL_NS_URI.equals(namespace)) {
            String[] qNameTokens = qName.split(":");
            String prefix;

            if(qNameTokens.length == 1) {
                prefix = XMLConstants.DEFAULT_NS_PREFIX;
            } else {
                prefix = qNameTokens[0];
            }

            if (!prefixAlreadyDeclared(prefix) && !nsToURI.containsKey(prefix)) {
                if(nsToURI == Collections.EMPTY_MAP) {
                    nsToURI = new LinkedHashMap<String, String>();
                }
                nsToURI.put(prefix, namespace);
            }
        }

        if(!nsToURI.isEmpty() && !readerStack.isEmpty()) {
            Set<Map.Entry<String,String>> namespaces = nsToURI.entrySet();

            // Now call start prefixes if namespaces are not empty
            ContentHandler contentHandler = readerStack.peek().getContentHandler();
            if (contentHandler != null) {
                for (Map.Entry<String,String> ns : namespaces) {
                    contentHandler.startPrefixMapping(ns.getKey(), ns.getValue());
                }
            }
        }

        namespaceStack.push(nsToURI);

        return attributes;
	}

    /**
     * Pop element out of the namespace declaration stack and notifying
     * {@link ContentHandler} if required.
     *
     * @throws SAXException if an error occurs when attempting to pop the
     * element out of the stack.
     */
    public void popNamespaces() throws SAXException {
        Map<String, String> namespaces = namespaceStack.pop();

        if (!namespaces.isEmpty() && !readerStack.isEmpty()) {
            Set<String> nsPrefixes = namespaces.keySet();

            ContentHandler contentHandler = readerStack.peek().getContentHandler();
            if (contentHandler != null) {
                for (String prefix : nsPrefixes) {
                        contentHandler.endPrefixMapping(prefix);
                }
            }
        }
    }

    /**
     * Push a new XMLReader instance onto the XMLReader Stack.
     * @param reader The reader instance.
     */
    public void pushReader(XMLReader reader) {
        readerStack.push(reader);
    }

    /**
     * Pop the current XMLReader off the XMLReader stack.
     * @return The reader instance that was popped from the stack.
     */
    @SuppressWarnings("UnusedReturnValue")
    public XMLReader popReader() {
        return readerStack.pop();
    }

    public String getPrefix(String uri) {
        int stackDepth = namespaceStack.size();

        for (int i = stackDepth - 1; i >= 0; i--) {
            Map<String, String> nsMap = namespaceStack.get(i);

            if(!nsMap.isEmpty()) {
                Set<Map.Entry<String,String>> nsEntries = nsMap.entrySet();
                for (Map.Entry<String,String> nsEntry : nsEntries) {
                    if(nsEntry.getValue().equals(uri)) {
                        return nsEntry.getKey();
                    }
                }
            }
        }

        return null;
    }

    public Map<String, String> getActiveNamespaces() {
        Map<String, String> activeNamespaces = new HashMap<String, String>();
        int stackDepth = namespaceStack.size();

        for (int i = stackDepth - 1; i >= 0; i--) {
            Map<String, String> nsMap = namespaceStack.get(i);

            if(!nsMap.isEmpty()) {
                activeNamespaces.putAll(nsMap);
            }
        }

        return activeNamespaces;
    }

    /**
     * Checks if a namespace with a given prefix is already declared higher in
     * the stack.
     *
     * @param prefix The prefix to check.
     * @return {@literal true} if a namespace with the specified prefix is
     * already declared within the stack.
     */
    private boolean prefixAlreadyDeclared(String prefix) {
        for (Map<String, String> set : namespaceStack) {
            if (set.containsKey(prefix)) {
                return true;
            }
        }
        return false;
    }
}
