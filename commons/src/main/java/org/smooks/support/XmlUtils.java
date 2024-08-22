/*-
 * ========================LICENSE_START=================================
 * Commons
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
package org.smooks.support;

import org.smooks.xml.HTMLEntityLookup;
import org.smooks.xml.LocalDTDEntityResolver;
import org.smooks.xml.LocalXSDEntityResolver;
import org.w3c.dom.*;
import org.xml.sax.*;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.util.Arrays;

/**
 * XMl utility methods.
 *
 * @author Tom Fennelly
 */

public final class XmlUtils {

    /**
     * Document validation types.
     */
    public enum VALIDATION_TYPE {
        /**
         * No validation.
         */
        NONE,
        /**
         * DTD based validation.
         */
        DTD,
        /**
         * XSD based validation.
         */
        XSD,
    }

    public static final char[] LT = new char[]{'&', 'l', 't', ';'};
    public static final char[] GT = new char[]{'&', 'g', 't', ';'};
    public static final char[] AMP = new char[]{'&', 'a', 'm', 'p', ';'};
    public static final char[] QUOT = new char[]{'&', 'q', 'u', 'o', 't', ';'};
    public static final char[] APOS = new char[]{'&', 'a', 'p', 'o', 's', ';'};

    private static final String COMMENT_START = "<!--";
    private static final String COMMENT_END = "-->";
    private static final String CDATA_START = "<![CDATA[";
    private static final String CDATA_END = "]]>";

    private XmlUtils() {

    }

    /**
     * Remove all entities from the supplied <code>Reader</code> stream
     * replacing them with their actual character values. <p/> Both the read and
     * write streams are returned unclosed.
     *
     * @param reader The read stream.
     * @param writer The write stream.
     */
    public static void removeEntities(Reader reader, Writer writer)
            throws IOException {
        int curChar;
        StringBuffer ent;

        if (reader == null) {
            throw new IllegalArgumentException("null reader arg");
        } else if (writer == null) {
            throw new IllegalArgumentException("null writer arg");
        }

        ent = new StringBuffer(50);
        while ((curChar = reader.read()) != -1) {
            if (curChar == '&') {
                if (ent.length() > 0) {
                    writer.write(ent.toString());
                    ent.setLength(0);
                }
                ent.append((char) curChar);
            } else if (curChar == ';' && ent.length() > 0) {
                int entLen = ent.length();

                if (entLen > 1) {
                    if (ent.charAt(1) == '#') {
                        if (entLen > 2) {
                            char char2 = ent.charAt(2);

                            try {
                                if (char2 == 'x' || char2 == 'X') {
                                    if (entLen > 3) {
                                        writer.write(Integer.parseInt(ent
                                                .substring(3), 16));
                                    } else {
                                        writer.write(ent.toString());
                                        writer.write(curChar);
                                    }
                                } else {
                                    writer.write(Integer.parseInt(ent
                                            .substring(2)));
                                }
                            } catch (NumberFormatException nfe) {
                                // bogus character ref - leave as is.
                                writer.write(ent.toString());
                                writer.write(curChar);
                            }
                        } else {
                            writer.write("&#;");
                        }
                    } else {
                        Character character = HTMLEntityLookup
                                .getCharacterCode(ent.substring(1));

                        if (character != null) {
                            writer.write(character);
                        } else {
                            // bogus entity ref - leave as is.
                            writer.write(ent.toString());
                            writer.write(curChar);
                        }
                    }
                } else {
                    writer.write("&;");
                }

                ent.setLength(0);
            } else if (ent.length() > 0) {
                ent.append((char) curChar);
            } else {
                writer.write(curChar);
            }
        }

        if (ent.length() > 0) {
            writer.write(ent.toString());
        }
    }

    /**
     * Remove all entities from the supplied <code>String</code> stream
     * replacing them with there actual character values.
     *
     * @param string The string on which the operation is to be carried out.
     * @return The string with its entities rewriten.
     */
    public static String removeEntities(String string) {
        if (string == null) {
            throw new IllegalArgumentException("null string arg");
        }

        try {
            StringReader reader = new StringReader(string);
            StringWriter writer = new StringWriter();

            XmlUtils.removeEntities(reader, writer);

            return writer.toString();
        } catch (Exception excep) {
            excep.printStackTrace();
            return string;
        }
    }

    /**
     * Rewrite all entities from the supplied <code>Reader</code> stream
     * replacing them with their character reference equivalents. <p/> Example:
     * <b>&ampnbsp;</b> is rewriten as <b>&amp#160;</b> <p/> Both the read and
     * write streams are returned unclosed.
     *
     * @param reader The read stream.
     * @param writer The write stream.
     */
    public static void rewriteEntities(Reader reader, Writer writer)
            throws IOException {
        int curChar;
        StringBuffer ent;
        char[] entBuf;

        if (reader == null) {
            throw new IllegalArgumentException("null reader arg");
        } else if (writer == null) {
            throw new IllegalArgumentException("null writer arg");
        }

        ent = new StringBuffer(50);
        entBuf = new char[50];
        while ((curChar = reader.read()) != -1) {
            if (curChar == '&') {
                if (ent.length() > 0) {
                    writer.write(ent.toString());
                    ent.setLength(0);
                }
                ent.append((char) curChar);
            } else if (curChar == ';' && ent.length() > 0) {
                int entLen = ent.length();

                if (entLen > 1) {
                    if (ent.charAt(1) == '#') {
                        // Already a character ref.
                        ent.getChars(0, ent.length(), entBuf, 0);
                        writer.write(entBuf, 0, ent.length());
                        writer.write(';');
                    } else {
                        Character character = HTMLEntityLookup
                                .getCharacterCode(ent.substring(1));

                        if (character != null) {
                            writer.write("&#");
                            writer.write(String.valueOf((int) character));
                            writer.write(";");
                        } else {
                            // bogus entity ref - leave as is.
                            writer.write(ent.toString());
                            writer.write(curChar);
                        }
                    }
                } else {
                    writer.write("&;");
                }

                ent.setLength(0);
            } else if (ent.length() > 0) {
                ent.append((char) curChar);
            } else {
                writer.write(curChar);
            }
        }

        if (ent.length() > 0) {
            writer.write(ent.toString());
        }
    }

    /**
     * Parse the XML stream and return the associated W3C Document object.
     *
     * @param stream           The stream to be parsed.
     * @param validation       Validation type to be carried out on the document.
     * @param expandEntityRefs Expand entity References as per
     *                         {@link DocumentBuilderFactory#setExpandEntityReferences(boolean)}.
     * @return The W3C Document object associated with the input stream.
     */
    public static Document parseStream(InputStream stream, VALIDATION_TYPE validation,
                                       boolean expandEntityRefs) throws SAXException, IOException {
        return parseStream(stream, new LocalDTDEntityResolver(), validation,
                expandEntityRefs);
    }

    /**
     * Parse the XML stream and return the associated W3C Document object.
     *
     * @param stream           The stream to be parsed.
     * @param entityResolver   Entity resolver to be used during the parse.
     * @param validation       Validation type to be carried out on the document.
     * @param expandEntityRefs Expand entity References as per
     *                         {@link javax.xml.parsers.DocumentBuilderFactory#setExpandEntityReferences(boolean)}.
     * @return The W3C Document object associated with the input stream.
     */
    public static Document parseStream(InputStream stream,
                                       EntityResolver entityResolver, VALIDATION_TYPE validation,
                                       boolean expandEntityRefs) throws SAXException, IOException {

        return parseStream(new InputStreamReader(stream), entityResolver, validation, expandEntityRefs);
    }

    /**
     * Parse the XML stream and return the associated W3C Document object.
     *
     * @param stream           The stream to be parsed.
     * @param entityResolver   Entity resolver to be used during the parse.
     * @param validation       Validation type to be carried out on the document.
     * @param expandEntityRefs Expand entity References as per
     *                         {@link javax.xml.parsers.DocumentBuilderFactory#setExpandEntityReferences(boolean)}.
     * @return The W3C Document object associated with the input stream.
     */
    public static Document parseStream(Reader stream,
                                       EntityResolver entityResolver, VALIDATION_TYPE validation,
                                       boolean expandEntityRefs) throws SAXException, IOException {
        if (stream == null) {
            throw new IllegalArgumentException("null 'stream' arg in method call.");
        }

        try {
            String streamData = StreamUtils.readStream(stream);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder;

            // Setup validation...
            if (validation == VALIDATION_TYPE.DTD) {
                factory.setValidating(true);
            } else if (validation == VALIDATION_TYPE.XSD) {
                try {
                    Schema schema = getSchema(entityResolver);

                    schema.newValidator().validate(new StreamSource(new StringReader(streamData)));
                } catch (IllegalArgumentException e) {
                    throw new SAXException("Unable to validate document.  Installed parser '" + factory.getClass().getName() + "' doesn't support JAXP 1.2", e);
                }
            }

            factory.setExpandEntityReferences(expandEntityRefs);
            docBuilder = factory.newDocumentBuilder();
            if (validation == VALIDATION_TYPE.DTD) {
                docBuilder.setEntityResolver(entityResolver);
            }
            docBuilder.setErrorHandler(XMLParseErrorHandler.getInstance());

            return docBuilder.parse(new InputSource(new StringReader(streamData)));
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException("Unable to parse XML stream - XML Parser not configured correctly.", e);
        } catch (FactoryConfigurationError e) {
            throw new IllegalStateException("Unable to parse XML stream - DocumentBuilderFactory not configured correctly.", e);
        }
    }

    /**
     * Basic DOM namespace aware parse.
     *
     * @param stream Document stream.
     * @return Document instance.
     */
    public static Document parseStream(InputStream stream) throws ParserConfigurationException, IOException, SAXException {
        return parseStream(new InputStreamReader(stream));
    }

    /**
     * Basic DOM namespace aware parse.
     *
     * @param stream Document stream.
     * @return Document instance.
     */
    public static Document parseStream(Reader stream) throws ParserConfigurationException, IOException, SAXException {
        return parseStream(stream, null);
    }

    /**
     * Basic DOM namespace aware parse.
     *
     * @param stream       Document stream.
     * @param errorHandler {@link ErrorHandler} to be set on the DocumentBuilder.
     *                     This can be used to controll error reporting. If null
     *                     the default error handler will be used.
     * @return Document instance.
     */
    public static Document parseStream(final Reader stream, final ErrorHandler errorHandler) throws ParserConfigurationException, IOException, SAXException {
        final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        documentBuilderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        documentBuilderFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        documentBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        documentBuilderFactory.setXIncludeAware(false);
        documentBuilderFactory.setExpandEntityReferences(false);
        documentBuilderFactory.setNamespaceAware(true);

        final DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        if (errorHandler != null) {
            documentBuilder.setErrorHandler(errorHandler);
        }
        return documentBuilder.parse(new InputSource(stream));
    }

    private static Schema getSchema(EntityResolver entityResolver) throws SAXException, IOException {
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

        if (entityResolver instanceof LocalXSDEntityResolver) {
            return schemaFactory.newSchema(((LocalXSDEntityResolver) entityResolver).getSchemaSources());
        }

        return schemaFactory.newSchema(new StreamSource(entityResolver.resolveEntity("default", "default").getByteStream()));
    }

    private static final String ELEMENT_NAME_FUNC = "/name()";
    private static final XPathFactory XPATH_FACTORY = XPathFactory.newInstance();

    /**
     * Get the W3C NodeList instance associated with the XPath selection
     * supplied.
     *
     * @param node  The document node to be searched.
     * @param xpath The XPath String to be used in the selection.
     * @return The W3C NodeList instance at the specified location in the
     * document, or null.
     */
    public static NodeList getNodeList(Node node, String xpath) {
        if (node == null) {
            throw new IllegalArgumentException(
                    "null 'document' arg in method call.");
        } else if (xpath == null) {
            throw new IllegalArgumentException(
                    "null 'xpath' arg in method call.");
        }
        try {
            XPath xpathEvaluater = XPATH_FACTORY.newXPath();

            if (xpath.endsWith(ELEMENT_NAME_FUNC)) {
                return (NodeList) xpathEvaluater.evaluate(xpath.substring(0,
                                xpath.length() - ELEMENT_NAME_FUNC.length()), node,
                        XPathConstants.NODESET);
            } else {
                return (NodeList) xpathEvaluater.evaluate(xpath, node,
                        XPathConstants.NODESET);
            }
        } catch (XPathExpressionException e) {
            throw new IllegalArgumentException("bad 'xpath' expression ["
                    + xpath + "].");
        }
    }

    /**
     * Get the W3C Node instance associated with the XPath selection supplied.
     *
     * @param node  The document node to be searched.
     * @param xpath The XPath String to be used in the selection.
     * @return The W3C Node instance at the specified location in the document,
     * or null.
     */
    public static Node getNode(Node node, String xpath) {
        NodeList nodeList = getNodeList(node, xpath);

        if (nodeList == null || nodeList.getLength() == 0) {
            return null;
        } else {
            return nodeList.item(0);
        }
    }

    /**
     * Get the String data associated with the XPath selection supplied.
     *
     * @param node  The node to be searched.
     * @param xpath The XPath String to be used in the selection.
     * @return The string data located at the specified location in the
     * document, or an empty string for an empty resultset query.
     */
    public static String getString(Node node, String xpath) {
        NodeList nodeList = getNodeList(node, xpath);

        if (nodeList == null || nodeList.getLength() == 0) {
            return "";
        }

        if (xpath.endsWith(ELEMENT_NAME_FUNC)) {
            if (nodeList.getLength() > 0) {
                return nodeList.item(0).getNodeName();
            } else {
                return "";
            }
        } else {
            return serialize(nodeList, false);
        }
    }

    public static String serialize(Node node) throws DOMException {
        return serialize(node, false, false);
    }

    /**
     * Serialise the supplied W3C DOM subtree.
     * <p/>
     * The output is unformatted.
     *
     * @param nodeList The DOM subtree as a NodeList.
     * @return The subtree in serailised form.
     * @throws DOMException Unable to serialise the DOM.
     */
    public static String serialize(NodeList nodeList, boolean closeEmptyElements) throws DOMException {
        return serialize(nodeList, false, closeEmptyElements);
    }

    /**
     * Serialise the supplied W3C DOM subtree.
     *
     * @param node   The DOM node to be serialized.
     * @param format Format the output.
     * @return The subtree in serailised form.
     * @throws DOMException Unable to serialise the DOM.
     */
    public static String serialize(final Node node, boolean format, boolean closeEmptyElements) throws DOMException {
        StringWriter writer = new StringWriter();
        serialize(node, format, writer, closeEmptyElements);
        return writer.toString();
    }

    /**
     * Serialise the supplied W3C DOM subtree.
     *
     * @param node   The DOM node to be serialized.
     * @param format Format the output.
     * @param writer The target writer for serialization.
     * @throws DOMException Unable to serialise the DOM.
     */
    public static void serialize(final Node node, boolean format, Writer writer, boolean closeEmptyElements) throws DOMException {
        if (node.getNodeType() == Node.DOCUMENT_NODE) {
            serialize(node.getChildNodes(), format, writer, closeEmptyElements);
        } else {
            serialize(new NodeList() {
                @Override
                public Node item(int index) {
                    return node;
                }

                @Override
                public int getLength() {
                    return 1;
                }
            }, format, writer, closeEmptyElements);
        }
    }

    /**
     * Serialise the supplied W3C DOM subtree.
     *
     * @param nodeList The DOM subtree as a NodeList.
     * @param format   Format the output.
     * @return The subtree in serailised form.
     * @throws DOMException Unable to serialise the DOM.
     */
    public static String serialize(NodeList nodeList, boolean format, boolean closeEmptyElements) throws DOMException {
        StringWriter writer = new StringWriter();
        serialize(nodeList, format, writer, closeEmptyElements);
        return writer.toString();
    }

    /**
     * Serialise the supplied W3C DOM subtree.
     *
     * @param nodeList The DOM subtree as a NodeList.
     * @param format   Format the output.
     * @param writer   The target writer for serialization.
     * @throws DOMException Unable to serialise the DOM.
     */
    public static void serialize(NodeList nodeList, boolean format, Writer writer, boolean closeEmptyElements) throws DOMException {
        if (nodeList == null) {
            throw new IllegalArgumentException("null 'subtree' NodeIterator arg in method call.");
        }

        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer;

            if (format) {
                try {
                    transformerFactory.setAttribute("indent-number", 4);
                } catch (Exception e) {
                    // Ignore... Xalan may throw on this!!
                    // We handle Xalan indentation below (yeuckkk) ...
                }
            }
            transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            if (!closeEmptyElements) {
                transformer.setOutputProperty(OutputKeys.METHOD, "html");
            }
            if (format) {
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xalan}indent-amount", "4");
            }

            int listLength = nodeList.getLength();

            // Iterate through the Node List.
            for (int i = 0; i < listLength; i++) {
                Node node = nodeList.item(i);

                if (XmlUtils.isTextNode(node)) {
                    writer.write(node.getNodeValue());
                } else if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
                    writer.write(((Attr) node).getValue());
                } else if (node.getNodeType() == Node.ELEMENT_NODE) {
                    transformer.transform(new DOMSource(node), new StreamResult(writer));
                }
            }
        } catch (Exception e) {
            DOMException domExcep = new DOMException(DOMException.INVALID_ACCESS_ERR, "Unable to serailise DOM subtree.");
            domExcep.initCause(e);
            throw domExcep;
        }
    }

    /**
     * Indent the supplied XML string by the number of spaces specified in the
     * 'indent' param.
     * <p/>
     * The indents are only inserted after newlines, where the first non-whitespace character
     * is '<'.
     *
     * @param xml    The XML to indent.
     * @param indent The number of spaces to insert as the indent.
     * @return The indented XML string.
     */
    public static String indent(String xml, int indent) {
        StringBuilder indentedXml = new StringBuilder();
        int xmlLen = xml.length();
        char[] indentChars = new char[indent];

        Arrays.fill(indentChars, ' ');

        int i = 0;
        while (i < xmlLen) {
            if (isStartOf(xml, i, COMMENT_START)) {
                int commentEnd = xml.indexOf(COMMENT_END, i);
                indentedXml.append(xml, i, commentEnd);
                i = commentEnd;
            } else if (isStartOf(xml, i, CDATA_START)) {
                int cdataEnd = xml.indexOf(CDATA_END, i);
                indentedXml.append(xml, i, cdataEnd);
                i = cdataEnd;
            } else {
                char nextChar = xml.charAt(i);

                indentedXml.append(nextChar);

                if (nextChar == '\n') {
                    // We're at the start of a new line.  Need to determine
                    // if the next sequence of non-whitespace characters are the start/end of
                    // an XML element.  If it is... add an indent before....
                    while (true) {
                        i++;

                        char preXmlChar = xml.charAt(i);
                        if (!Character.isWhitespace(preXmlChar)) {
                            if (preXmlChar == '<') {
                                if (!isStartOf(xml, i, COMMENT_START) && !isStartOf(xml, i, CDATA_START)) {
                                    indentedXml.append(indentChars);
                                }
                            }
                            break;
                        } else {
                            indentedXml.append(preXmlChar);
                        }
                    }
                } else {
                    i++;
                }
            }
        }

        return indentedXml.toString();
    }

    private static boolean isStartOf(String xml, int i, String substring) {
        return xml.regionMatches(i, substring, 0, substring.length());
    }

    /**
     * Is the supplied W3C DOM Node a text node.
     *
     * @param node The node to be tested.
     * @return True if the node is a text node, otherwise false.
     */
    public static boolean isTextNode(Node node) {
        short nodeType;

        if (node == null) {
            return false;
        }
        nodeType = node.getNodeType();

        return nodeType == Node.CDATA_SECTION_NODE
                || nodeType == Node.TEXT_NODE;
    }

    public static void encodeTextValue(char[] characters, int offset, int length, Writer writer) throws IOException {
        for (int i = offset; i < offset + length; i++) {
            char c = characters[i];
            switch (c) {
                case '<':
                    writer.write(LT, 0, LT.length);
                    break;
                case '>':
                    writer.write(GT, 0, GT.length);
                    break;
                case '&':
                    writer.write(AMP, 0, AMP.length);
                    break;
                default:
                    writer.write(c);
            }
        }
    }

    public static void encodeAttributeValue(char[] characters, int offset, int length, Writer writer) throws IOException {
        for (int i = offset; i < offset + length; i++) {
            char c = characters[i];
            switch (c) {
                case '<':
                    writer.write(LT, 0, LT.length);
                    break;
                case '>':
                    writer.write(GT, 0, GT.length);
                    break;
                case '&':
                    writer.write(AMP, 0, AMP.length);
                    break;
                case '\'':
                    writer.write(APOS, 0, APOS.length);
                    break;
                case '\"':
                    writer.write(QUOT, 0, QUOT.length);
                    break;
                default:
                    writer.write(c);
            }
        }
    }

    /**
     * XML Parse error handler.
     *
     * @author tfennelly
     */
    static class XMLParseErrorHandler implements ErrorHandler {

        /**
         * Singleton instance reference of this class.
         */
        private static final XMLParseErrorHandler singleton = new XMLParseErrorHandler();

        /**
         * Private constructor.
         */
        private XMLParseErrorHandler() {
        }

        /**
         * Get this classes singleton reference.
         *
         * @return This classes singleton reference.
         */
        private static XMLParseErrorHandler getInstance() {
            return singleton;
        }

        /*
         * (non-Javadoc)
         *
         * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
         */
        @Override
        public void warning(SAXParseException arg0) throws SAXException {
            throw arg0;
        }

        /*
         * (non-Javadoc)
         *
         * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
         */
        @Override
        public void error(SAXParseException arg0) throws SAXException {
            throw arg0;
        }

        /*
         * (non-Javadoc)
         *
         * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
         */
        @Override
        public void fatalError(SAXParseException arg0) throws SAXException {
            throw arg0;
        }
    }

    public static QName toQName(String namespaceURI, String localName, String qName) {
        if (namespaceURI != null) {
            int colonIndex;

            if (!namespaceURI.isEmpty() && qName != null && (colonIndex = qName.indexOf(':')) != -1) {
                String prefix = qName.substring(0, colonIndex);
                String qNameLocalName = qName.substring(colonIndex + 1);

                return new QName(namespaceURI.intern(), qNameLocalName, prefix);
            } else if (localName != null && !localName.isEmpty()) {
                return new QName(namespaceURI, localName);
            } else if (qName != null && !qName.isEmpty()) {
                return new QName(namespaceURI, qName);
            } else {
                throwInvalidNameException(namespaceURI, localName, qName);
            }
        } else if (localName != null && !localName.isEmpty()) {
            return new QName(localName);
        } else {
            throwInvalidNameException(null, localName, qName);
        }

        return null;
    }

    private static void throwInvalidNameException(String namespaceURI, String localName, String qName) {
        throw new IllegalArgumentException("Invalid QName: namespaceURI='" + namespaceURI + "', localName='" + localName + "', qName='" + qName + "'.");
    }
}