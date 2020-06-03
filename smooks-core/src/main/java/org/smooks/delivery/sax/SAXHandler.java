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

import org.smooks.SmooksException;
import org.smooks.cdr.SmooksConfigurationException;
import org.smooks.cdr.SmooksResourceConfiguration;
import org.smooks.container.ExecutionContext;
import org.smooks.delivery.*;
import org.smooks.delivery.replay.EndElementEvent;
import org.smooks.delivery.replay.StartElementEvent;
import org.smooks.delivery.sax.terminate.TerminateException;
import org.smooks.event.ExecutionEventListener;
import org.smooks.event.report.AbstractReportGenerator;
import org.smooks.event.types.ElementPresentEvent;
import org.smooks.event.types.ElementVisitEvent;
import org.smooks.event.types.ResourceTargetingEvent;
import org.smooks.io.NullWriter;
import org.smooks.xml.DocType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

/**
 * SAX Handler.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
@SuppressWarnings("unchecked")
public class SAXHandler extends SmooksContentHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SAXHandler.class);
    private ExecutionContext execContext;
    private Writer writer;
    private ElementProcessor currentProcessor = null;
    private TextType currentTextType = TextType.TEXT;
    private SAXContentDeliveryConfig deliveryConfig;
    private Map<String, SAXElementVisitorMap> visitorConfigMap;
    private SAXElementVisitorMap globalVisitorConfig;
    private boolean rewriteEntities;
    private boolean defaultSerializationOn;
    private boolean maintainElementStack;
    private boolean reverseVisitOrderOnVisitAfter;
    private boolean terminateOnVisitorException;
    private DefaultSAXElementSerializer defaultSerializer = new DefaultSAXElementSerializer();
    private static ContentHandlerConfigMap defaultSerializerMapping;
    private ExecutionEventListener eventListener;
    private DynamicSAXElementVisitorList dynamicVisitorList;
    private StringBuilder cdataNodeBuilder = new StringBuilder();

    static {
        // Configure the default handler mapping...
        SmooksResourceConfiguration resource = new SmooksResourceConfiguration("*", DefaultSAXElementSerializer.class.getName());
        resource.setDefaultResource(true);
        defaultSerializerMapping = new ContentHandlerConfigMap(new DefaultSAXElementSerializer(), resource);
    }

    @SuppressWarnings("WeakerAccess")
    public SAXHandler(ExecutionContext executionContext, Writer writer) {
        this(executionContext, writer, null);
    }

    public SAXHandler(ExecutionContext executionContext, Writer writer, SmooksContentHandler parentContentHandler) {
        super(executionContext, parentContentHandler);

        this.execContext = executionContext;
        this.writer = writer;
        eventListener = executionContext.getEventListener();

        deliveryConfig = ((SAXContentDeliveryConfig)executionContext.getDeliveryConfig());
        visitorConfigMap = deliveryConfig.getOptimizedVisitorConfig();

        SAXContentDeliveryConfig contentDeliveryConfig = (SAXContentDeliveryConfig) executionContext.getDeliveryConfig();
        SAXElementVisitorMap starVisitorConfigs = visitorConfigMap.get("*");
        SAXElementVisitorMap starStarVisitorConfigs = visitorConfigMap.get("**");

        if(starVisitorConfigs != null) {
            globalVisitorConfig = starVisitorConfigs.merge(starStarVisitorConfigs);
        } else {
            globalVisitorConfig = starStarVisitorConfigs;
        }

        rewriteEntities = contentDeliveryConfig.isRewriteEntities();
        defaultSerializer.setRewriteEntities(rewriteEntities);

        defaultSerializationOn = executionContext.isDefaultSerializationOn();
        if(defaultSerializationOn) {
            // If it's not explicitly configured off, we auto turn it off if the NullWriter is configured...
            defaultSerializationOn = !(writer instanceof NullWriter);
        }
        maintainElementStack = contentDeliveryConfig.isMaintainElementStack();

        reverseVisitOrderOnVisitAfter = contentDeliveryConfig.isReverseVisitOrderOnVisitAfter();
        if(!(executionContext.getEventListener() instanceof AbstractReportGenerator)) {
            terminateOnVisitorException = contentDeliveryConfig.isTerminateOnVisitorException();
        } else {
            terminateOnVisitorException = false;
        }

        dynamicVisitorList = DynamicSAXElementVisitorList.getList(executionContext);
        if(dynamicVisitorList == null) {
            dynamicVisitorList = new DynamicSAXElementVisitorList(executionContext);
        }
    }

    public void cleanup() {
    }

    @SuppressWarnings("RedundantThrows")
    public void startElement(StartElementEvent startEvent) throws SAXException {
        WriterManagedSAXElement element;
        boolean isRoot = (currentProcessor == null);
        SAXElementVisitorMap elementVisitorConfig;
        QName elementQName;
        String elementName;

        elementQName = SAXUtil.toQName(startEvent.uri, startEvent.localName, startEvent.qName);
        elementName = elementQName != null ? elementQName.getLocalPart() : null;

        if(isRoot) {
            elementVisitorConfig = deliveryConfig.getCombinedOptimizedConfig(new String[] {SmooksResourceConfiguration.DOCUMENT_FRAGMENT_SELECTOR, elementName});
        } else {
            elementVisitorConfig = visitorConfigMap.get(elementName);
        }

        if(elementVisitorConfig == null) {
            elementVisitorConfig = globalVisitorConfig;
        }

        if(!maintainElementStack && elementVisitorConfig == null) {
            ElementProcessor processor = new ElementProcessor();

            processor.isNullProcessor = true;
            processor.parentProcessor = currentProcessor;
            currentProcessor = processor;
            // Register the "presence" of the element...
            if(eventListener != null) {
                eventListener.onEvent(new ElementPresentEvent(new WriterManagedSAXElement(elementQName, startEvent.atts, currentProcessor.element)));
            }
        } else {
            if(!isRoot) {
                // Push the existing "current" processor onto the stack and create a new current
                // based on this start event...
                element = new WriterManagedSAXElement(elementQName, startEvent.atts, currentProcessor.element);
                element.setWriter(getWriter());
                onChildElement(element);
            } else {
                element = new WriterManagedSAXElement(elementQName, startEvent.atts, null);
                element.setWriter(writer);
            }

            // Register the "presence" of the element...
            if(eventListener != null) {
                eventListener.onEvent(new ElementPresentEvent(element));
            }

            visitBefore(element, elementVisitorConfig);
        }
    }

    @SuppressWarnings("RedundantThrows")
    public void endElement(EndElementEvent endEvent) throws SAXException {
        boolean flush = false;

        // Apply the dynamic visitors...
        List<SAXVisitAfter> dynamicVisitAfters = dynamicVisitorList.getVisitAfters();
        if(!dynamicVisitAfters.isEmpty()) {
            for (SAXVisitAfter dynamicVisitAfter : dynamicVisitAfters) {
                try {
                    dynamicVisitAfter.visitAfter(currentProcessor.element, execContext);
                } catch(Throwable t) {
                    String errorMsg = "Error in '" + dynamicVisitAfter.getClass().getName() + "' while processing the visitAfter event.";
                    processVisitorException(t, errorMsg);
                }
            }
        }

        if(currentProcessor.elementVisitorConfig != null) {
            List<ContentHandlerConfigMap<SAXVisitAfter>> visitAfterMappings = currentProcessor.elementVisitorConfig.getVisitAfters();

            if(visitAfterMappings != null) {
                if(reverseVisitOrderOnVisitAfter) {
                    // We work through the mappings in reverse order on the end element event...
                    int mappingCount = visitAfterMappings.size();
                    ContentHandlerConfigMap<SAXVisitAfter> mapping;

                    for(int i = mappingCount - 1; i >= 0; i--) {
                        mapping = visitAfterMappings.get(i);
                        visitAfter(mapping);
                    }
                } else {
                    for (final ContentHandlerConfigMap<SAXVisitAfter> visitAfterMapping : visitAfterMappings)
                    {
                        visitAfter(visitAfterMapping);
                    }
                }
            }
            flush = true;
        }

        if(defaultSerializationOn && applyDefaultSerialization()) {
            try {
                defaultSerializer.visitAfter(currentProcessor.element, execContext);
                if(eventListener != null) {
                    eventListener.onEvent(new ElementVisitEvent(currentProcessor.element, defaultSerializerMapping, VisitSequence.AFTER));
                }
            } catch (IOException e) {
                throw new SmooksException("Unexpected exception applying defaultSerializer.", e);
            }
            flush = true;
        }

        if(flush) {
            flushCurrentWriter();
        }

        // Process cleanables after applying all the visit afters...
        if(currentProcessor.elementVisitorConfig != null) {
            List<ContentHandlerConfigMap<VisitLifecycleCleanable>> visitCleanables = currentProcessor.elementVisitorConfig.getVisitCleanables();

            if(visitCleanables != null) {
                for (final ContentHandlerConfigMap<VisitLifecycleCleanable> visitCleanable : visitCleanables)
                {
                    final boolean targetedAtElement
                        = visitCleanable.getResourceConfig().isTargetedAtElement(currentProcessor.element, execContext);

                    if (targetedAtElement)
                    {
                        visitCleanable.getContentHandler().executeVisitLifecycleCleanup(new Fragment(currentProcessor.element), execContext);
                    }
                }
            }
        }

        ElementProcessor parentProcessor = currentProcessor.parentProcessor;
        currentProcessor.element = null;
        currentProcessor.elementVisitorConfig = null;
        currentProcessor.parentProcessor = null;
        currentProcessor = parentProcessor;
    }

    private Writer getWriter() {
        if(!currentProcessor.isNullProcessor) {
            return currentProcessor.element.getWriter();
        } else {
            ElementProcessor processor = currentProcessor;
            while(processor.parentProcessor != null) {
                processor = processor.parentProcessor;
                if(!processor.isNullProcessor) {
                    return processor.element.getWriter();
                }
            }
        }

        return null;
    }

    private void visitBefore(WriterManagedSAXElement element, SAXElementVisitorMap elementVisitorConfig) {

        // Now create the new "current" processor...
        ElementProcessor processor = new ElementProcessor();

        processor.parentProcessor = currentProcessor;
        processor.element = element;
        processor.elementVisitorConfig = elementVisitorConfig;

        currentProcessor = processor;
        if(currentProcessor.elementVisitorConfig != null) {
            // And visit it with the targeted visitor...
            List<ContentHandlerConfigMap<SAXVisitBefore>> visitBeforeMappings = currentProcessor.elementVisitorConfig.getVisitBefores();

            if(elementVisitorConfig.accumulateText()) {
                currentProcessor.element.accumulateText();
            }
            SAXVisitor acquireWriterFor = elementVisitorConfig.acquireWriterFor();
            if(acquireWriterFor != null) {
            	element.getWriter(acquireWriterFor);
            }

            if(visitBeforeMappings != null) {
                for (final ContentHandlerConfigMap<SAXVisitBefore> mapping : visitBeforeMappings)
                {
                    try
                    {
                        if (mapping.getResourceConfig().isTargetedAtElement(currentProcessor.element, execContext))
                        {
                            mapping.getContentHandler().visitBefore(currentProcessor.element, execContext);
                            // Register the targeting event.  No need to register this event again on the visitAfter...
                            if (eventListener != null)
                            {
                                eventListener.onEvent(new ResourceTargetingEvent(element, mapping.getResourceConfig(), VisitSequence.BEFORE));
                                eventListener.onEvent(new ElementVisitEvent(element, mapping, VisitSequence.BEFORE));
                            }
                        }
                    }
                    catch (Throwable t)
                    {
                        String errorMsg = "Error in '" + mapping.getContentHandler().getClass().getName() + "' while processing the visitBefore event.";
                        processVisitorException(currentProcessor.element, t, mapping, VisitSequence.BEFORE, errorMsg);
                    }
                }
            }
        }

        if(defaultSerializationOn && applyDefaultSerialization()) {
            try {
                defaultSerializer.visitBefore(currentProcessor.element, execContext);
                if(eventListener != null) {
                    eventListener.onEvent(new ElementVisitEvent(element, defaultSerializerMapping, VisitSequence.BEFORE));
                }
            } catch (IOException e) {
                throw new SmooksException("Unexpected exception applying defaultSerializer.", e);
            }
        }

        // Apply the dynamic visitors...
        List<SAXVisitBefore> dynamicVisitBefores = dynamicVisitorList.getVisitBefores();
        if(!dynamicVisitBefores.isEmpty()) {
            for (SAXVisitBefore dynamicVisitBefore : dynamicVisitBefores) {
                try {
                    dynamicVisitBefore.visitBefore(currentProcessor.element, execContext);
                } catch(Throwable t) {
                    String errorMsg = "Error in '" + dynamicVisitBefore.getClass().getName() + "' while processing the visitBefore event.";
                    processVisitorException(t, errorMsg);
                }
            }
        }
    }

    private void onChildElement(SAXElement childElement) {
        if(currentProcessor.elementVisitorConfig != null) {
            List<ContentHandlerConfigMap<SAXVisitChildren>> visitChildMappings = currentProcessor.elementVisitorConfig.getChildVisitors();

            if(visitChildMappings != null) {
                for (final ContentHandlerConfigMap<SAXVisitChildren> mapping : visitChildMappings)
                {
                    if (mapping.getResourceConfig().isTargetedAtElement(currentProcessor.element, execContext))
                    {
                        try
                        {
                            mapping.getContentHandler().onChildElement(currentProcessor.element, childElement, execContext);
                        }
                        catch (Throwable t)
                        {
                            String errorMsg = "Error in '" + mapping.getContentHandler().getClass().getName() + "' while processing the onChildElement event.";
                            processVisitorException(currentProcessor.element, t, mapping, VisitSequence.AFTER, errorMsg);
                        }
                    }
                }
            }
        }

        if(defaultSerializationOn && applyDefaultSerialization()) {
            try {
                defaultSerializer.onChildElement(currentProcessor.element, childElement, execContext);
            } catch (IOException e) {
                throw new SmooksException("Unexpected exception applying defaultSerializer.", e);
            }
        }

        // Apply the dynamic visitors...
        List<SAXVisitChildren> dynamicChildVisitors = dynamicVisitorList.getChildVisitors();
        if(!dynamicChildVisitors.isEmpty()) {
            for (SAXVisitChildren dynamicChildVisitor : dynamicChildVisitors) {
                try {
                    dynamicChildVisitor.onChildElement(currentProcessor.element, childElement, execContext);
                } catch(Throwable t) {
                    String errorMsg = "Error in '" + dynamicChildVisitor.getClass().getName() + "' while processing the onChildElement event.";
                    processVisitorException(t, errorMsg);
                }
            }
        }
    }

    private void visitAfter(ContentHandlerConfigMap<SAXVisitAfter> afterMapping) {

        try {
            if(afterMapping.getResourceConfig().isTargetedAtElement(currentProcessor.element, execContext)) {
                afterMapping.getContentHandler().visitAfter(currentProcessor.element, execContext);
                if(eventListener != null) {
                    eventListener.onEvent(new ElementVisitEvent(currentProcessor.element, afterMapping, VisitSequence.AFTER));
                }
            }
        } catch(Throwable t) {
            String errorMsg = "Error in '" + afterMapping.getContentHandler().getClass().getName() + "' while processing the visitAfter event.";
            processVisitorException(currentProcessor.element, t, afterMapping, VisitSequence.AFTER, errorMsg);
        }
    }

    private SAXText textWrapper = new SAXText();
    @SuppressWarnings("RedundantThrows")
    public void characters(char[] ch, int start, int length) throws SAXException {
        if(currentTextType != TextType.CDATA) {
            _characters(ch, start, length);
        } else {
            cdataNodeBuilder.append(ch, start, length);
        }
    }

    private StringBuilder entityBuilder = new StringBuilder(10);
    private void _characters(char[] ch, int start, int length) {

        if(!rewriteEntities && currentTextType == TextType.ENTITY) {
            entityBuilder.setLength(0);

            entityBuilder.append("&#").append((int)ch[start]).append(';');
            char[] newBuf = new char[entityBuilder.length()];
            entityBuilder.getChars(0, newBuf.length, newBuf, 0);

            textWrapper.setText(newBuf, 0, newBuf.length, TextType.TEXT);
        } else {
            textWrapper.setText(ch, start, length, currentTextType);
        }

        if(currentProcessor != null) {
            // Accumulate the text...
            if(currentProcessor.element != null) {
                List<SAXText> saxTextObjects = currentProcessor.element.getText();
                if(saxTextObjects != null) {
                    saxTextObjects.add(textWrapper);
                }
            }

            if(!currentProcessor.isNullProcessor) {
                if(currentProcessor.elementVisitorConfig != null) {
                    List<ContentHandlerConfigMap<SAXVisitChildren>> visitChildMappings = currentProcessor.elementVisitorConfig.getChildVisitors();

                    if(visitChildMappings != null) {
                        for (final ContentHandlerConfigMap<SAXVisitChildren> mapping : visitChildMappings)
                        {
                            try
                            {
                                if (mapping.getResourceConfig().isTargetedAtElement(currentProcessor.element, execContext))
                                {
                                    mapping.getContentHandler().onChildText(currentProcessor.element, textWrapper, execContext);
                                }
                            }
                            catch (Throwable t)
                            {
                                String errorMsg = "Error in '" + mapping.getContentHandler().getClass().getName() + "' while processing the onChildText event.";
                                processVisitorException(currentProcessor.element, t, mapping, VisitSequence.AFTER, errorMsg);
                            }
                        }
                    }
                }

                if(defaultSerializationOn && applyDefaultSerialization()) {
                    try {
                        defaultSerializer.onChildText(currentProcessor.element, textWrapper, execContext);
                    } catch (IOException e) {
                        throw new SmooksException("Unexpected exception applying defaultSerializer.", e);
                    }
                }
            }

            // Apply the dynamic visitors...
            List<SAXVisitChildren> dynamicChildVisitors = dynamicVisitorList.getChildVisitors();
            if(!dynamicChildVisitors.isEmpty()) {
                for (SAXVisitChildren dynamicChildVisitor : dynamicChildVisitors) {
                    try {
                        dynamicChildVisitor.onChildText(currentProcessor.element, textWrapper, execContext);
                    } catch(Throwable t) {
                        String errorMsg = "Error in '" + dynamicChildVisitor.getClass().getName() + "' while processing the onChildText event.";
                        processVisitorException(t, errorMsg);
                    }
                }
            }
        }
    }

    private boolean applyDefaultSerialization() {
        if(currentProcessor.element == null || !defaultSerializationOn) {
            return false;
        }

        return (currentProcessor.element.writerOwner == defaultSerializer || currentProcessor.element.writerOwner == null);
    }

    private void flushCurrentWriter() {
        Writer writer = getWriter();
        if(writer != null) {
            try {
                writer.flush();
            } catch (IOException e) {
                LOGGER.debug("Error flushing writer.", e);
            }
        }
    }

    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        characters(ch, start, length);
    }

    public void comment(char[] ch, int start, int length) throws SAXException {
        currentTextType = TextType.COMMENT;
        characters(ch, start, length);
        currentTextType = TextType.TEXT;
    }

    @SuppressWarnings("RedundantThrows")
    public void startCDATA() throws SAXException {
        currentTextType = TextType.CDATA;
        cdataNodeBuilder.setLength(0);
    }

    @SuppressWarnings("RedundantThrows")
    public void endCDATA() throws SAXException {
        try {
            char[] chars = new char[cdataNodeBuilder.length()];

            cdataNodeBuilder.getChars(0, chars.length, chars, 0);
            _characters(chars, 0, chars.length);
            currentTextType = TextType.TEXT;
        } finally {
            cdataNodeBuilder.setLength(0);
        }
    }

    @SuppressWarnings("RedundantThrows")
    public void startEntity(String name) throws SAXException {
        currentTextType = TextType.ENTITY;
    }

    @SuppressWarnings("RedundantThrows")
    public void endEntity(String name) throws SAXException {
        currentTextType = TextType.TEXT;
    }

    public void startDTD(String name, String publicId, String systemId) throws SAXException {
        DocType.setDocType(name, publicId, systemId, null, execContext);

        if(writer != null) {
            DocType.DocumentTypeData docTypeData = DocType.getDocType(execContext);
            if(docTypeData != null) {
                try {
                    DocType.serializeDoctype(docTypeData, writer);
                } catch (IOException e) {
                    throw new SAXException("Failed to serialize DOCTYPE.");
                }
            }
        }
    }

    private static class ElementProcessor {
        private ElementProcessor parentProcessor;
        private boolean isNullProcessor = false;
        private WriterManagedSAXElement element;
        private SAXElementVisitorMap elementVisitorConfig;
    }

    private void processVisitorException(SAXElement element, Throwable error, ContentHandlerConfigMap configMapping, VisitSequence visitSequence, String errorMsg) throws SmooksException {
        if (eventListener != null) {
            eventListener.onEvent(new ElementVisitEvent(element, configMapping, visitSequence, error));
        }

        processVisitorException(error, errorMsg);
    }

    private void processVisitorException(Throwable error, String errorMsg) {
    	if(error instanceof TerminateException) {
            throw (TerminateException) error;
        }

    	execContext.setTerminationError(error);

        if(terminateOnVisitorException) {
        	if(error instanceof SmooksException) {
                throw (SmooksException) error;
            } else {
                throw new SmooksException(errorMsg, error);
            }
        } else {
            LOGGER.debug(errorMsg, error);
        }
    }

    private class WriterManagedSAXElement extends SAXElement {

        private SAXVisitor writerOwner;

        private WriterManagedSAXElement(QName qName, Attributes attributes, SAXElement parent) {
            super(qName, attributes, parent);
        }

        public Writer getWriter(SAXVisitor visitor) throws SAXWriterAccessException {
            if(writerOwner == null) {
                writerOwner = visitor;
                return super.getWriter(visitor);
            }
            if(visitor == writerOwner) {
                return super.getWriter(visitor);
            }

            throwSAXWriterAccessException(visitor);
            return null;
        }

        public void setWriter(Writer writer, SAXVisitor visitor) throws SAXWriterAccessException {
            if(writerOwner == null) {
                writerOwner = visitor;
                super.setWriter(writer, visitor);
            } else if(visitor == writerOwner) {
                super.setWriter(writer, visitor);
            } else {
                throwSAXWriterAccessException(visitor);
            }
        }

        public boolean isWriterOwner(SAXVisitor visitor) {
            return (visitor != null && visitor == writerOwner);
        }

        public SAXElement getParent() {
            if(!maintainElementStack) {
                throw new SmooksConfigurationException("Invalid Smooks configuration.  Call to 'SAXElement.getParent()' when the '" + Filter.MAINTAIN_ELEMENT_STACK + "' is set to 'false'.  You need to change this configuration, or modify the calling code.");
            }
            return super.getParent();
        }

        private Writer getWriter() {
            return super.getWriter(null);
        }

        private void setWriter(Writer writer) {
            super.setWriter(writer, null);
        }

        private void throwSAXWriterAccessException(SAXVisitor visitor) {
            throw new SAXWriterAccessException("Illegal access to the element writer for element '" + this + "' by SAX visitor '" + visitor.getClass().getName() + "'.  Writer already acquired by SAX visitor '" + writerOwner.getClass().getName() + "'.  See SAXElement javadocs (http://milyn.codehaus.org/Smooks).  Change Smooks visitor resource configuration.");
        }
    }
}
