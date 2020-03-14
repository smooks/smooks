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
package org.smooks.xml;

import org.w3c.dom.*;
import org.xml.sax.SAXException;
import org.smooks.util.ClassUtil;
import org.smooks.assertion.AssertArgument;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.dom.DOMSource;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;
import java.io.InputStream;

/**
 * XSD DOM Validator.
 * <p/>
 * Iterates through the document (DOM) gathering the namespaces.  It validates
 * based on the convention that the gathered namespace XSDs are provided on the
 * classpath.  It uses the namespace path (URI.getPath()), prepending it with "/META-INF"
 * to perform a classpath resource lookup for the XSD i.e. the XSDs must be provided on
 * the classpath below the "META-INF" package.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class XsdDOMValidator extends XsdValidator {

    private Document document;
    private URI defaultNamespace;
    private List<URI> namespaces = new ArrayList<URI>();

    public XsdDOMValidator(Document document) throws SAXException {
        AssertArgument.isNotNull(document, "document");
        this.document = document;

        // Get the default namespace...
        String defaultNamespaceString = getDefaultNamespace(document.getDocumentElement());
        if(defaultNamespaceString != null) {
            try {
                defaultNamespace = new URI(defaultNamespaceString);
            } catch (URISyntaxException e) {
                throw new SAXException("Cannot validate this document with this class.  Namespaces must be valid URIs.  Default Namespace: '" + defaultNamespaceString + "'.", e);
            }
        }

        // Get the full namespace list...
        gatherNamespaces(document.getDocumentElement(), namespaces);

        // Using the namespace URI list, create the XSD Source array used to
        // create the merged Schema instance...
    	List<Source> sources = new ArrayList<Source>();
        for (int i = 0; i < namespaces.size(); i++) {
            URI namespace = namespaces.get(i);
            if(!XmlUtil.isXMLReservedNamespace(namespace.toString())) {
            	sources.add(getNamespaceSource(namespace));
            }
        }
        setXSDSources(sources);
    }

    public URI getDefaultNamespace() {
        return defaultNamespace;
    }

    public List<URI> getNamespaces() {
        return namespaces;
    }

    /**
     * Validate the document against the namespaces referenced in it.
     * @throws SAXException Validation error.
     * @throws IOException Error reading the XSD Sources.
     */
    public void validate() throws SAXException, IOException {
        validate(new DOMSource(document));
    }

    /**
     * Get the default namespace associated with the supplied element.
     * @param element The element to be checked.
     * @return The default namespace, or null if none was found.
     */
    public static String getDefaultNamespace(Element element) {
        NamedNodeMap attributes = element.getAttributes();
        int attributeCount = attributes.getLength();

        for(int i = 0; i < attributeCount; i++) {
            Attr attribute = (Attr) attributes.item(i);

            if(XMLConstants.XMLNS_ATTRIBUTE.equals(attribute.getName()) && XMLConstants.XMLNS_ATTRIBUTE.equals(attribute.getLocalName())) {
                return attribute.getValue();
            }
        }

        return null;
    }

    private void gatherNamespaces(Element element, List<URI> namespaceSources) throws SAXException {
        NamedNodeMap attributes = element.getAttributes();
        int attributeCount = attributes.getLength();

        for(int i = 0; i < attributeCount; i++) {
            Attr attribute = (Attr) attributes.item(i);
            String namespace = attribute.getNamespaceURI();

            if(XMLConstants.XMLNS_ATTRIBUTE_NS_URI.equals(namespace)) {
                try {
                    namespaceSources.add(new URI(attribute.getValue()));
                } catch (URISyntaxException e) {
                    throw new SAXException("Cannot validate this document with this class.  Namespaces must be valid URIs.  Found Namespace: '" + attribute.getValue() + "'.", e);
                }
            }
        }

        NodeList childNodes = element.getChildNodes();
        int childCount = childNodes.getLength();
        for(int i = 0; i < childCount; i++) {
            Node child = childNodes.item(i);

            if(child.getNodeType() == Node.ELEMENT_NODE) {
                gatherNamespaces((Element) child, namespaceSources);
            }
        }
    }

    private Source getNamespaceSource(URI namespace) throws SAXException {
        String resourcePath = "/META-INF" + namespace.getPath();
        InputStream xsdStream = ClassUtil.getResourceAsStream(resourcePath, getClass());

        if(xsdStream == null) {
            throw new SAXException("Failed to locate XSD resource '" + resourcePath + "' on classpath. Namespace: '" + namespace + "'.");
        }

        return new StreamSource(xsdStream);
    }

}
