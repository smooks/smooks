/*-
 * ========================LICENSE_START=================================
 * Smooks Core
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
package org.smooks.delivery.sax;

import org.smooks.assertion.AssertArgument;
import org.smooks.delivery.sax.annotation.StreamResultWriter;
import org.smooks.delivery.sax.annotation.TextConsumer;
import org.smooks.SmooksException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.AttributesImpl;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import java.io.Writer;
import java.io.StringWriter;
import java.io.IOException;
import java.util.*;

/**
 * Element details as described by the SAX even model API.
 * <p/>
 * {@link org.smooks.delivery.sax.SAXVisitor} implementations will be passed
 * an instance of this class for each of the event methods of
 * {@link org.smooks.delivery.sax.SAXVisitor} implementations.
 * 
 * <h3 id="element-writing">Element Writing/Serialization</h3>
 * Each SAXElement instance has a {@link Writer writer} set on it.
 * {@link org.smooks.delivery.sax.SAXVisitor} implementations can take care of
 * serializing the elements at which they are targeted themselves.  Alternatively, they
 * can use the {@link org.smooks.delivery.sax.SAXElementWriterUtil} class.
 * <p/>
 * {@link org.smooks.delivery.sax.SAXVisitor} implementations can also control the serialization
 * of their "child elements" by {@link #setWriter(java.io.Writer, SAXVisitor) setting the writer}
 * on the SAXElement instance they receive.  This works because Smooks passes the
 * writer instance that's set on a SAXElement instance to all of the SAXElement
 * instances created for child elements.
 * <p/>
 * Only one {@link SAXVisitor} can have access to the {@link java.io.Writer writer}
 * for any individual {@link SAXElement}.  The first visitor to request access to
 * the writer via the {@link SAXElement#getWriter(SAXVisitor)} method "owns" the writer
 * for that element.  Any other visitors requesting access to get or change the writer
 * will result in a {@link SAXWriterAccessException} being thrown.  In this situation,
 * you need to restructure the offending Smooks configuration and eliminate one of the
 * visitors attempting to gain access to the writer.  If developing a new Visitor,
 * you should annotate the new Visitor class with the {@link StreamResultWriter @StreamResultWriter}
 * annotation.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class SAXElement {

    private QName name;
    private AttributesImpl attributes;
    private SAXElement parent;
    private Writer writer;
    private List<SAXText> text;
    private StringWriter textAccumulator;
    private String accumulatedText;

    /**
     * We use a "level 1" cache so as to avoid creating the HashMap
     * where only one visitor is caching data, which is often the case.
     * 2nd, 3rd etc visitors will use the l2Caches Map.
     */
    private Object l1Cache;
    private SAXVisitor l1CacheOwner;
    private Map<SAXVisitor, Object> l2Caches;

    /**
     * Public constructor.
     *
     * @param namespaceURI The Namespace URI, or the empty string if the
     *                     element has no Namespace URI or if Namespace
     *                     processing is not being performed.
     * @param localName    The local name (without prefix), or the
     *                     empty string if Namespace processing is not being
     *                     performed.
     */
    public SAXElement(String namespaceURI, String localName) {
        this(namespaceURI, localName, null, new AttributesImpl(), null);
    }

    /**
     * Public constructor.
     *
     * @param namespaceURI The Namespace URI, or the empty string if the
     *                     element has no Namespace URI or if Namespace
     *                     processing is not being performed.
     * @param localName    The local name (without prefix), or the
     *                     empty string if Namespace processing is not being
     *                     performed.
     * @param parent       Parent element, or null if the element is the document root element.
     */
    public SAXElement(String namespaceURI, String localName, SAXElement parent) {
        this(namespaceURI, localName, null, new AttributesImpl(), parent);
    }

    /**
     * Public constructor.
     *
     * @param namespaceURI The Namespace URI, or the empty string if the
     *                     element has no Namespace URI or if Namespace
     *                     processing is not being performed.
     * @param localName    The local name (without prefix), or the
     *                     empty string if Namespace processing is not being
     *                     performed.
     * @param qName        The qualified name (with prefix), or the
     *                     empty string if qualified names are not available.
     * @param attributes   The attributes attached to the element.  If
     *                     there are no attributes, it shall be an empty
     *                     Attributes object.
     * @param parent       Parent element, or null if the element is the document root element.
     */
    public SAXElement(String namespaceURI, String localName, String qName, Attributes attributes, SAXElement parent) {
        this.name = SAXUtil.toQName(namespaceURI, localName, qName);
        this.attributes = copyAttributes(attributes);
        this.parent = parent;
    }

    /**
     * Public constructor.
     *
     * @param name       The element {@link QName}.
     * @param attributes The attributes attached to the element.  If
     *                   there are no attributes, it shall be an empty
     *                   Attributes object.
     * @param parent     Parent element, or null if the element is the document root element.
     */
    public SAXElement(QName name, Attributes attributes, SAXElement parent) {
        this.name = name;
        this.attributes = copyAttributes(attributes);
        this.parent = parent;
    }

    /**
     * Create a copy of the attributes.
     * <p/>
     * This needs to be done because some SAX parsers reuse the same {@link Attributes} instance
     * across SAX events.
     *
     * @param attributes The attributes to copy.
     * @return The new {@link Attributes} instance with a copy of the attributes.
     */
    private AttributesImpl copyAttributes(Attributes attributes) {
        AttributesImpl attributesCopy = new AttributesImpl();
        attributesCopy.setAttributes(attributes);
        return attributesCopy;
    }

    /**
     * Turn on {@link SAXText text} accumulation for this {@link SAXElement}.
     * <p/>
     * For performance reasons, {@link SAXText Text} accumulation is not on by default.
     * @see TextConsumer
     */
    public void accumulateText() {
        if(text == null) {
            text = new ArrayList<SAXText>() {
                public boolean add(SAXText saxText) {
                    if(textAccumulator != null) {
                        // Clear the accumulatedText object so as any subsequent calls to the
                        // getTextAsString method will recreate the buffer from scratch...
                        accumulatedText = null;
                    }
                    return super.add((SAXText) saxText.clone());
                }
            };
        }
    }

    /**
     * Get the child {@link SAXText text} list associated with this {@link SAXElement}.
     * @return The child {@link SAXText text} list associated with this {@link SAXElement},
     * or null if this {@link SAXElement} is not {@link #accumulateText() accumulating text}.
     * @see #accumulateText() 
     * @see TextConsumer
     */
    public List<SAXText> getText() {
        return text;
    }

    /**
     * Add a text object to this instance.
     * <p/>
     * Utility method, userd mainly for testing.
     *
     * @param text The text to be added.
     * @see #accumulateText() 
     * @see TextConsumer
     */
    public void addText (String text) {
        addText(text, TextType.TEXT);
    }

    /**
     * Add a text object to this instance.
     * <p/>
     * Utility method, userd mainly for testing.
     *
     * @param text The text to be added.
     * @param type The text type.
     * @see #accumulateText() 
     * @see TextConsumer
     */
    public void addText (String text, TextType type) {
        if(this.text == null) {
            accumulateText();
        }
        this.text.add(new SAXText(text, type));
    }

    /**
     * Get the {@link SAXText} objects associated with this {@link SAXElement},
     * as an {@link #accumulateText() accumulated} String.
     * <p/>
     * This method will produce a string containing all {@link TextType} {@link SAXText}
     * objects associated with this {@link SAXElement}.  If you need to filter out specific
     * {@link TextType} {@link SAXText} objects, use the {@link #getText()} method and manually
     * produce a String.
     *
     * @return The {@link SAXText} objects associated with this {@link SAXElement},
     * as an {@link #accumulateText() accumulated} String.
     * @throws SmooksException This {@link SAXElement} instance does not have
     * {@link #accumulateText() text accumulation} turned on.
     * @see #accumulateText() 
     * @see TextConsumer
     */
    public String getTextContent() throws SmooksException {
        if(text == null) {
            throw new SmooksException("Illegal call to getTextContent().  SAXElement instance not accumulating SAXText Objects.  You must call SAXElement.accumulateText(), or annotate the Visitor implementation class with the @TextConsumer annotation.");
        }

        if(textAccumulator == null) {
            textAccumulator = new StringWriter();
        }

        if(accumulatedText == null) {
            textAccumulator.getBuffer().setLength(0);
            for(SAXText textObj : text) {
                try {
                    textObj.toWriter(textAccumulator, false);
                } catch (IOException e) {
                    throw new RuntimeException("Unexpected IOException.", e);
                }
            }

            accumulatedText = textAccumulator.toString();
        }

        return accumulatedText;
    }

    /**
     * Get the writer to which this element should be writen to.
     * <p/>
     * See <a href="#element-writing">element writing</a>.
     *
     * @param visitor The visitor requesting access to element writer.
     * @return The element writer.
     * @throws SAXWriterAccessException Invalid access request for the element writer. See <a href="#element-writing">element writing</a>.
     * @see StreamResultWriter
     */
    public Writer getWriter(SAXVisitor visitor) throws SAXWriterAccessException {
        // This implementation doesn't actually enforce the "one writer per element" rule.  It's enforced from
        // within the SAXHandler.
        return writer;
    }

    /**
     * Set the writer to which this element should be writen to.
     * <p/>
     * See <a href="#element-writing">element writing</a>.
     *
     * @param writer  The element writer.
     * @param visitor The visitor requesting to set the element writer.
     * @throws SAXWriterAccessException Invalid access request for the element writer. See <a href="#element-writing">element writing</a>.
     * @see StreamResultWriter
     */
    public void setWriter(Writer writer, SAXVisitor visitor) throws SAXWriterAccessException {
        // This implementation doesn't actually enforce the "one writer per element" rule.  It's enforced from
        // within the SAXHandler.
        this.writer = writer;
    }

    /**
     * Is the supplied {@link SAXVisitor} the owner of the {@link Writer} associated
     * with this {@link SAXElement} instance.
     * <p/>
     * See <a href="#element-writing">element writing</a>.
     *
     * @param visitor The visitor being checked.
     * @return True if the {@link SAXVisitor} owns the {@link Writer} associated
     * with this {@link SAXElement} instance, otherwise false.
     * @see StreamResultWriter
     */
    public boolean isWriterOwner(SAXVisitor visitor) {
        // This implementation doesn't actually enforce the "one writer per element" rule.  It's enforced from
        // within the SAXHandler.
        return true;
    }

    /**
     * Get the element naming details.
     *
     * @return Element naming details.
     */
    public QName getName() {
        return name;
    }

    /**
     * Set the naming details for the Element.
     *
     * @param name The element naming details.
     */
    public void setName(QName name) {
        AssertArgument.isNotNull(name, "name");
        this.name = name;
    }

    /**
     * Get the element attributes.
     *
     * @return Element attributes.
     */
    public Attributes getAttributes() {
        return attributes;
    }

    /**
     * Set the element attributes.
     *
     * @param attributes The element attributes.
     */
    public void setAttributes(Attributes attributes) {
        AssertArgument.isNotNull(attributes, "attributes");
        this.attributes = copyAttributes(attributes);
    }

    /**
     * Get the named attribute from this element.
     * @param attribute The attribute name.
     * @return The attribute value, or an empty string if the attribute is not specified.
     */
    public String getAttribute(String attribute) {
        return SAXUtil.getAttribute(attribute, attributes);
    }

    /**
     * Get the named attribute from this element.
     * @param namespaceURI The namespace URI of the required attribute.
     * @param attribute The attribute name.
     * @return The attribute value, or an empty string if the attribute is not specified.
     */
    public String getAttributeNS(String namespaceURI, String attribute) {
        return SAXUtil.getAttribute(namespaceURI, attribute, attributes, "");
    }

    /**
     * Get the named attribute from this element.
     * @param namespaceURI The namespace URI of the required attribute.
     * @param attribute The attribute name.
     * @return The attribute value, or an empty string if the attribute is not specified.
     * @deprecated Use {@link #getAttributeNS(String, String)}.
     */
    public String getAttribute(String namespaceURI, String attribute) {
        return getAttributeNS(namespaceURI, attribute);
    }


    /**
     * Set the named attribute on this element.
     * @param attribute The attribute name.
     * @param value The attribute value.
     */
    public void setAttribute(String attribute, String value) {
        setAttributeNS(XMLConstants.NULL_NS_URI, attribute,  value);
    }

    /**
     * Set the named attribute on this element.
     * @param namespaceURI The attribute namespace URI.
     * @param name The attribute name.
     * @param value The attribute value.
     */
    public void setAttributeNS(String namespaceURI, String name, String value) {
        removeAttributeNS(namespaceURI, name);

        int prefixIndex = name.indexOf(":");
        if(prefixIndex != -1) {
            attributes.addAttribute(namespaceURI, name.substring(prefixIndex + 1), name, "CDATA", value);
        } else {
            attributes.addAttribute(namespaceURI, name, "", "CDATA", value);
        }
    }

    /**
     * Remove the named attribute.
     * @param name Attribute name.
     */
    public void removeAttribute(String name) {
        removeAttributeNS(XMLConstants.NULL_NS_URI, name);
    }

    /**
     * Remove the named attribute, having the specified namespace.
     * @param namespaceURI The attribute namespace.
     * @param name The attribute name.
     */
    public void removeAttributeNS(String namespaceURI, String name) {
        int attribCount = attributes.getLength();
        for(int i = 0; i < attribCount; i++) {
            if(namespaceURI.equals(attributes.getURI(i))) {
                boolean isQName = (name.indexOf(":") != -1);
                
                if(isQName && name.equals(attributes.getQName(i))) {
                    attributes.removeAttribute(i);
                } else if(!isQName && name.equals(attributes.getLocalName(i))) {
                    attributes.removeAttribute(i);
                }
            }
        }
    }


    /**
     * Get the <a href="#element_cache_object">element cache object</a>.
     *
     * @return The element cache Object.
     * @deprecated Use {@link #getCache(SAXVisitor)}.
     */
    public Object getCache() {
        return l1Cache;
    }

    /**
     * Set the <a href="#element_cache_object">element cache object</a>.
     *
     * @param cache The element cache Object.
     * @deprecated Use {@link #setCache(SAXVisitor, Object)}.
     */
    public void setCache(Object cache) {
        this.l1Cache = cache;
    }

    /**
     * Get the <a href="#element_cache_object">element cache object</a>.
     *
     * @param visitor The SAXElement instance associated with the cache object.
     * @return The element cache Object.
     */
    public Object getCache(SAXVisitor visitor) {
        if(visitor == l1CacheOwner) {
            // This visitor owns the level 1 cache...
            return l1Cache;
        } else if(l2Caches == null) {
            return null;
        }

        return l2Caches.get(visitor);
    }

    /**
     * Set the <a href="#element_cache_object">element cache object</a>.
     *
     * @param visitor The SAXElement instance to which the cache object is to be associated.
     * @param cache The element cache Object.
     */
    public void setCache(SAXVisitor visitor, Object cache) {
        if(l1Cache == null && l1CacheOwner == null) {
            // This visitor is going to own the level 1 cache...
            l1Cache = cache;
            l1CacheOwner = visitor;
            return;
        }

        if(l2Caches == null) {
            l2Caches = new HashMap<SAXVisitor, Object>();
        }
        l2Caches.put(visitor, cache);
    }

    /**
     * Get parent element.
     *
     * @return Parent element, or null if it's the documnent root.
     */
    public SAXElement getParent() {
        return parent;
    }

    /**
     * Set parent element.
     *
     * @param parent Parent element, or null if it's the documnent root.
     */
    public void setParent(SAXElement parent) {
        this.parent = parent;
    }

    public String toString() {
        return getName().toString();
    }

    public final boolean equals(Object obj) {
        return super.equals(obj);
    }

    public final int hashCode() {
        return super.hashCode();
    }

    /**
     * Create a DOM {@link Element} instance from this {@link SAXElement}
     * instance.
     * @param document The document to use to create the DOM Element.
     * @return The DOM Element.
     */
    public Element toDOMElement(Document document) {
        Element element;

        if(name.getNamespaceURI() != null) {
            if(name.getPrefix().length() != 0) {
                element = document.createElementNS(name.getNamespaceURI(), name.getPrefix() + ":" + name.getLocalPart());
            } else {
                element = document.createElementNS(name.getNamespaceURI(), name.getLocalPart());
            }
        } else {
            element = document.createElement(name.getLocalPart());
        }

        int attributeCount = attributes.getLength();
        for(int i = 0; i < attributeCount; i++) {
            String namespace = attributes.getURI(i);
            String value = attributes.getValue(i);

            if(namespace != null) {
                String qName = attributes.getQName(i);

                if(namespace.equals(XMLConstants.NULL_NS_URI)) {
                    if(qName.startsWith(XMLConstants.XMLNS_ATTRIBUTE)) {
                    	namespace = XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
                    } else if(qName.startsWith("xml:")) {
                    	namespace = XMLConstants.XML_NS_URI;
                    }
                }
                
                element.setAttributeNS(namespace, qName, value);
            } else {
                String localName = attributes.getLocalName(i);
                element.setAttribute(localName, value);
            }
        }

        return element;
    }
}
