/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.smooks.engine.delivery.sax.ng.org.apache.xerces.stax.events;

import org.smooks.engine.delivery.sax.ng.org.apache.xerces.stax.events.AttributeImpl;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.events.Namespace;

/**
 * @xerces.internal
 * 
 * @author Lucian Holland
 *
 * @version $Id$
 */
public final class NamespaceImpl extends AttributeImpl implements Namespace {

    private final String fPrefix;
    private final String fNamespaceURI;

    /**
     * @param location
     * @param schemaType
     */
    public NamespaceImpl(final String prefix, final String namespaceURI, final Location location) {
        super(NAMESPACE, makeAttributeQName(prefix), namespaceURI, null, true, location);
        fPrefix = (prefix == null) ? XMLConstants.DEFAULT_NS_PREFIX : prefix;
        fNamespaceURI = namespaceURI;
    }

    /**
     * @param prefix The prefix for this namespace.
     * @return A QName for the attribute that declares this namespace.
     */
    private static QName makeAttributeQName(String prefix) {
        if (prefix == null || prefix.equals(XMLConstants.DEFAULT_NS_PREFIX)) {
            return new QName(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, XMLConstants.XMLNS_ATTRIBUTE, XMLConstants.DEFAULT_NS_PREFIX);
        }
        return new QName(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, prefix, XMLConstants.XMLNS_ATTRIBUTE);
    }

    /**
     * @see Namespace#getPrefix()
     */
    public String getPrefix() {
        return fPrefix;
    }

    /**
     * @see Namespace#getNamespaceURI()
     */
    public String getNamespaceURI() {
        return fNamespaceURI;
    }

    /**
     * @see Namespace#isDefaultNamespaceDeclaration()
     */
    public boolean isDefaultNamespaceDeclaration() {
        return fPrefix.length() == 0;
    }
}
