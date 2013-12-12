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
package org.milyn.templating.xslt;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.cdr.annotation.ConfigParam;
import org.milyn.commons.SmooksException;
import org.milyn.commons.cdr.SmooksConfigurationException;
import org.milyn.commons.io.StreamUtils;
import org.milyn.commons.util.ClassUtil;
import org.milyn.commons.xml.DomUtils;
import org.milyn.commons.xml.XmlUtil;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.AbstractParser;
import org.milyn.delivery.FilterBypass;
import org.milyn.delivery.dom.serialize.GhostElementSerializationUnit;
import org.milyn.delivery.ordering.Consumer;
import org.milyn.event.report.annotation.VisitAfterReport;
import org.milyn.event.report.annotation.VisitBeforeReport;
import org.milyn.templating.AbstractTemplateProcessor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.StringReader;

/**
 * XSLT template application ProcessingUnit.
 *
 * @author tfennelly
 */
@VisitBeforeReport(condition = "false")
@VisitAfterReport(summary = "Applied XSL Template.", detailTemplate = "reporting/XslTemplateProcessor_After.html")
public class XslTemplateProcessor extends AbstractTemplateProcessor implements Consumer, FilterBypass {
    /**
     * Logger.
     */
    private static Log logger = LogFactory.getLog(XslTemplateProcessor.class);

    /**
     * XSL as a String.
     */
    private String xslString;
    /**
     * XSL template to be applied to the visited element.
     */
    private Templates xslTemplate;
    /**
     * Is this processor processing an XSLT <a href="#templatelets">Templatelet</a>.
     */
    private boolean isTemplatelet;
    /**
     * This Visitor implements the {@link FilterBypass} interface.  This config param allows
     * the user to enable/disable the bypass.
     */
    @ConfigParam(defaultVal = "true")
    private boolean enableFilterBypass;

    /**
     * Is the Smooks configuration, for which this visitor is a part, targeted at an XML message stream.
     * We know if it is by the XML reader configured (or not configured).
     */
    private volatile Boolean isXMLTargetedConfiguration;

    /**
     * Is the template application synchronized or not.
     * <p/>
     * Xalan v2.7.0 has/had a threading issue - kick-on effect being that template application
     * must be synchronized.
     */
    private final boolean isSynchronized = Boolean.getBoolean(XslContentHandlerFactory.ORG_MILYN_TEMPLATING_XSLT_SYNCHRONIZED);
    private final DomErrorHandler logErrorHandler = new DomErrorHandler();


    @Override
    protected void loadTemplate(SmooksResourceConfiguration resourceConfig) throws IOException, TransformerConfigurationException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        StreamSource xslStreamSource;
        boolean isInlineXSL = resourceConfig.isInline();
        byte[] xslBytes = resourceConfig.getBytes();

        xslString = new String(xslBytes, getEncoding().name());

        // If it's not a full XSL template, we need to make it so by wrapping it...
        isTemplatelet = isTemplatelet(isInlineXSL, new String(xslBytes));
        if (isTemplatelet) {
            String templateletWrapper = new String(StreamUtils.readStream(ClassUtil.getResourceAsStream("doc-files/templatelet.xsl", getClass())));
            String templatelet = new String(xslBytes);

            templateletWrapper = StringUtils.replace(templateletWrapper, "@@@templatelet@@@", templatelet);
            xslBytes = templateletWrapper.getBytes();
            xslString = new String(xslBytes, getEncoding().name());
        }

        boolean failOnWarning = resourceConfig.getBoolParameter("failOnWarning", true);

        xslStreamSource = new StreamSource(new StringReader(xslString));
        transformerFactory.setErrorListener(new XslErrorListener(failOnWarning));
        xslTemplate = transformerFactory.newTemplates(xslStreamSource);
    }

    private boolean isTemplatelet(boolean inlineXSL, String templateCode) {
        try {
            Document xslDoc = XmlUtil.parseStream(new StringReader(templateCode), logErrorHandler);
            Element rootElement = xslDoc.getDocumentElement();
            String rootElementNS = rootElement.getNamespaceURI();

            return (inlineXSL && !(rootElementNS != null && rootElementNS.equals("http://www.w3.org/1999/XSL/Transform") && DomUtils.getName(rootElement).equals("stylesheet")));
        } catch (ParserConfigurationException e) {
            throw new SmooksConfigurationException("Unable to parse XSL Document (Stylesheet/Templatelet).", e);
        } catch (IOException e) {
            throw new SmooksConfigurationException("Unable to parse XSL Document (Stylesheet/Templatelet).", e);
        } catch (SAXException e) {
            return inlineXSL;
        }
    }

    public boolean consumes(Object object) {
        if (xslString.indexOf(object.toString()) != -1) {
            return true;
        }

        return false;
    }

    @Override
    protected void visit(Element element, ExecutionContext executionContext) throws SmooksException {
        Document ownerDoc = element.getOwnerDocument();
        Element ghostElement = GhostElementSerializationUnit.createElement(ownerDoc);

        try {
            if (isSynchronized) {
                synchronized (xslTemplate) {
                    performTransform(element, ghostElement, ownerDoc);
                }
            } else {
                performTransform(element, ghostElement, ownerDoc);
            }
        } catch (TransformerException e) {
            throw new SmooksException("Error applying XSLT to node [" + executionContext.getDocumentSource() + ":" + DomUtils.getXPath(element) + "]", e);
        }

        if (getOutputStreamResource() != null || getAction() == Action.BIND_TO) {
            // For bindTo or streamTo actions, we need to serialize the content and supply is as a Text DOM node.
            // AbstractTemplateProcessor will look after the rest, by extracting the content from the
            // Text node and attaching it to the ExecutionContext...
            String serializedContent = XmlUtil.serialize(ghostElement.getChildNodes());
            Text textNode = element.getOwnerDocument().createTextNode(serializedContent);

            processTemplateAction(element, textNode, executionContext);
        } else {
            NodeList children = ghostElement.getChildNodes();

            // Process the templating action, supplying the templating result...
            if (children.getLength() == 1 && children.item(0).getNodeType() == Node.ELEMENT_NODE) {
                processTemplateAction(element, children.item(0), executionContext);
            } else {
                processTemplateAction(element, ghostElement, executionContext);
            }
        }
    }

    private void performTransform(Element element, Element transRes, Document ownerDoc) throws TransformerException {
        Transformer transformer = xslTemplate.newTransformer();

        if (element == ownerDoc.getDocumentElement()) {
            transformer.transform(new DOMSource(ownerDoc), new DOMResult(transRes));
        } else {
            transformer.transform(new DOMSource(element), new DOMResult(transRes));
        }
    }

    public boolean bypass(ExecutionContext executionContext, Source source, Result result) throws SmooksException {
        if (!enableFilterBypass) {
            return false;
        }
        if (!isXMLTargetedConfiguration(executionContext)) {
            return false;
        }
        if ((source instanceof StreamSource || source instanceof DOMSource) && (result instanceof StreamResult || result instanceof DOMResult)) {
            try {
                Transformer transformer = xslTemplate.newTransformer();
                transformer.transform(source, result);
                return true;
            } catch (TransformerConfigurationException e) {
                throw new SmooksException("Error applying XSLT.", e);
            } catch (TransformerException e) {
                throw new SmooksException("Error applying XSLT.", e);
            }
        }

        return false;
    }

    private boolean isXMLTargetedConfiguration(ExecutionContext executionContext) {
        if (isXMLTargetedConfiguration == null) {
            synchronized (this) {
                if (isXMLTargetedConfiguration == null) {
                    SmooksResourceConfiguration readerConfiguration = AbstractParser.getSAXParserConfiguration(executionContext.getDeliveryConfig());
                    if (readerConfiguration != null) {
                        // We have an reader config, if the class is not configured, we assume
                        // the expected Source to be XML...
                        isXMLTargetedConfiguration = (readerConfiguration.getResource() == null);
                    } else {
                        // If no reader config is present at all, we assume the expected Source is XML...
                        isXMLTargetedConfiguration = true;
                    }
                }
            }
        }

        return isXMLTargetedConfiguration;
    }

    private static class XslErrorListener implements ErrorListener {
        private final boolean failOnWarning;

        public XslErrorListener(boolean failOnWarning) {
            this.failOnWarning = failOnWarning;
        }

        public void warning(TransformerException exception) throws TransformerException {
            if (failOnWarning) {
                throw exception;
            } else {
                logger.debug("XSL Warning.", exception);
            }
        }

        public void error(TransformerException exception) throws TransformerException {
            throw exception;
        }

        public void fatalError(TransformerException exception) throws TransformerException {
            throw exception;
        }
    }

    /**
     * Simple ErrorHandler that only reports errors, fatals, and warnings
     * at a debug log level.
     * <p/>
     *
     * @author <a href="mailto:daniel.bevenius@gmail.com">Daniel Bevenius</a>
     */
    private static class DomErrorHandler implements ErrorHandler {
        public void error(final SAXParseException exception) throws SAXException {
            logger.debug("SaxParseException error was reported : ", exception);
        }

        public void fatalError(final SAXParseException exception) throws SAXException {
            logger.debug("SaxParseException fatal error was reported : ", exception);
        }

        public void warning(final SAXParseException exception) throws SAXException {
            logger.debug("SaxParseException warning error was reported : ", exception);
        }
    }
}
