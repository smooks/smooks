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
package org.smooks.engine.delivery.sax;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smooks.api.SmooksException;
import org.smooks.api.delivery.sax.SAXElement;
import org.smooks.api.delivery.sax.SAXText;
import org.smooks.api.delivery.sax.SAXWriterAccessException;
import org.smooks.api.delivery.sax.TextType;
import org.smooks.api.resource.config.ResourceConfig;
import org.smooks.api.delivery.ContentDeliveryRuntime;
import org.smooks.api.delivery.ContentHandlerBinding;
import org.smooks.api.delivery.Filter;
import org.smooks.api.resource.visitor.Visitor;
import org.smooks.api.resource.visitor.sax.*;
import org.smooks.engine.delivery.event.VisitSequence;
import org.smooks.engine.resource.config.DefaultResourceConfig;
import org.smooks.api.SmooksConfigException;
import org.smooks.api.ExecutionContext;
import org.smooks.engine.delivery.*;
import org.smooks.api.delivery.fragment.Fragment;
import org.smooks.engine.delivery.fragment.SAXElementFragment;
import org.smooks.engine.delivery.replay.EndElementEvent;
import org.smooks.engine.delivery.replay.StartElementEvent;
import org.smooks.engine.delivery.sax.terminate.TerminateException;
import org.smooks.api.delivery.event.ExecutionEventListener;
import org.smooks.engine.report.AbstractReportGenerator;
import org.smooks.engine.delivery.event.ResourceTargetingEvent;
import org.smooks.engine.delivery.event.StartFragmentEvent;
import org.smooks.engine.delivery.event.VisitEvent;
import org.smooks.io.FragmentWriter;
import org.smooks.io.NullWriter;
import org.smooks.io.Stream;
import org.smooks.api.lifecycle.LifecycleManager;
import org.smooks.api.lifecycle.VisitLifecycleCleanable;
import org.smooks.engine.lifecycle.VisitCleanupPhase;
import org.smooks.engine.lookup.LifecycleManagerLookup;
import org.smooks.engine.xml.DocType;
import org.smooks.support.SAXUtil;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * SAX Handler.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
@SuppressWarnings("unchecked")
public class SAXHandler extends SmooksContentHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SAXHandler.class);
    private static final ContentHandlerBinding<SAXElementVisitor> DEFAULT_SERIALIZER_MAPPING;

    private final ExecutionContext executionContext;
    private final SAXContentDeliveryConfig deliveryConfig;
    private final Map<String, SAXElementVisitorMap> visitorConfigMap;
    private final SAXElementVisitorMap globalVisitorConfig;
    private final Boolean rewriteEntities;
    private final Boolean maintainElementStack;
    private final Boolean reverseVisitOrderOnVisitAfter;
    private final DefaultSAXElementSerializer defaultSerializer = new DefaultSAXElementSerializer();
    private final ContentDeliveryRuntime contentDeliveryRuntime;
    private final StringBuilder cdataNodeBuilder = new StringBuilder();
    private final LifecycleManager lifecycleManager;

    private boolean defaultSerializationOn;
    private boolean terminateOnVisitorException;
    private ElementProcessor currentProcessor = null;
    private TextType currentTextType = TextType.TEXT;
    private DynamicSAXElementVisitorList dynamicVisitorList;

    static {
        // Configure the default handler mapping...
        ResourceConfig resource = new DefaultResourceConfig("*", DefaultSAXElementSerializer.class.getName());
        resource.setDefaultResource(true);
        DEFAULT_SERIALIZER_MAPPING = new DefaultContentHandlerBinding<>(new DefaultSAXElementSerializer(), resource);
    }

    @SuppressWarnings("WeakerAccess")
    public SAXHandler(ExecutionContext executionContext) {
        this(executionContext, null);
    }

    public SAXHandler(ExecutionContext executionContext, SmooksContentHandler parentContentHandler) {
        super(executionContext, parentContentHandler);

        this.executionContext = executionContext;
        contentDeliveryRuntime = executionContext.getContentDeliveryRuntime();

        lifecycleManager = executionContext.getApplicationContext().getRegistry().lookup(new LifecycleManagerLookup());
        deliveryConfig = ((SAXContentDeliveryConfig) executionContext.getContentDeliveryRuntime().getContentDeliveryConfig());
        visitorConfigMap = deliveryConfig.getOptimizedVisitorConfig();

        SAXElementVisitorMap starVisitorConfigs = visitorConfigMap.get("*");
        SAXElementVisitorMap starStarVisitorConfigs = visitorConfigMap.get("**");

        if(starVisitorConfigs != null) {
            globalVisitorConfig = starVisitorConfigs.merge(starStarVisitorConfigs);
        } else {
            globalVisitorConfig = starStarVisitorConfigs;
        }

        rewriteEntities = deliveryConfig.isRewriteEntities();
        defaultSerializer.setRewriteEntities(Optional.of(rewriteEntities));

        defaultSerializationOn = deliveryConfig.isDefaultSerializationOn();
        if(defaultSerializationOn) {
            // If it's not explicitly configured off, we auto turn it off if the NullWriter is configured...
            defaultSerializationOn = !(Stream.out(executionContext) instanceof NullWriter);
        }
        maintainElementStack = deliveryConfig.isMaintainElementStack();

        reverseVisitOrderOnVisitAfter = deliveryConfig.isReverseVisitOrderOnVisitAfter();
        for (ExecutionEventListener executionEventListener : contentDeliveryRuntime.getExecutionEventListeners()) {
            if (executionEventListener instanceof AbstractReportGenerator) {
                terminateOnVisitorException = false;
                break;
            }
        }

        terminateOnVisitorException = deliveryConfig.isTerminateOnVisitorException();

        dynamicVisitorList = DynamicSAXElementVisitorList.getList(executionContext);
        if(dynamicVisitorList == null) {
            dynamicVisitorList = new DynamicSAXElementVisitorList(executionContext);
        }
    }

    public void cleanup() {
    }

    public void startElement(StartElementEvent startEvent) throws SAXException {
        WriterManagedSAXElement element;
        boolean isRoot = (currentProcessor == null);
        SAXElementVisitorMap elementVisitorConfig;
        QName elementQName;
        String elementName;

        elementQName = SAXUtil.toQName(startEvent.uri, startEvent.localName, startEvent.qName);
        elementName = elementQName != null ? elementQName.getLocalPart() : null;

        if(isRoot) {
            elementVisitorConfig = deliveryConfig.getCombinedOptimizedConfig(new String[] {ResourceConfig.DOCUMENT_FRAGMENT_SELECTOR, elementName});
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
            for (ExecutionEventListener executionEventListener : contentDeliveryRuntime.getExecutionEventListeners()) {
                executionEventListener.onEvent(new StartFragmentEvent<>(new SAXElementFragment(new WriterManagedSAXElement(elementQName, startEvent.attributes, currentProcessor.element))));
            }
        } else {
            if(!isRoot) {
                // Push the existing "current" processor onto the stack and create a new current
                // based on this start event...
                element = new WriterManagedSAXElement(elementQName, startEvent.attributes, currentProcessor.element);
                element.setWriter(getWriter());
                onChildElement(element);
            } else {
                element = new WriterManagedSAXElement(elementQName, startEvent.attributes, null);
                element.setWriter(new FragmentWriter(executionContext, new SAXElementFragment(element)));
            }

            // Register the "presence" of the element...
            final StartFragmentEvent<SAXElement> startFragmentEvent = new StartFragmentEvent<>(new SAXElementFragment(element));
            for (ExecutionEventListener executionEventListener : contentDeliveryRuntime.getExecutionEventListeners()) {
                executionEventListener.onEvent(startFragmentEvent);
            }

            visitBefore(element, elementVisitorConfig);
        }
    }

    @Override
    public void endElement(EndElementEvent endEvent) {
        boolean flush = false;

        // Apply the dynamic visitors...
        List<SAXVisitAfter> dynamicVisitAfters = dynamicVisitorList.getVisitAfters();
        if(!dynamicVisitAfters.isEmpty()) {
            for (SAXVisitAfter dynamicVisitAfter : dynamicVisitAfters) {
                try {
                    dynamicVisitAfter.visitAfter(currentProcessor.element, executionContext);
                } catch(Throwable t) {
                    String errorMsg = "Error in '" + dynamicVisitAfter.getClass().getName() + "' while processing the visitAfter event.";
                    processVisitorException(t, errorMsg);
                }
            }
        }

        if(currentProcessor.elementVisitorConfig != null) {
            List<ContentHandlerBinding<SAXVisitAfter>> visitAfterMappings = currentProcessor.elementVisitorConfig.getVisitAfters();

            if(visitAfterMappings != null) {
                if(reverseVisitOrderOnVisitAfter) {
                    // We work through the mappings in reverse order on the end element event...
                    int mappingCount = visitAfterMappings.size();
                    ContentHandlerBinding<SAXVisitAfter> mapping;

                    for(int i = mappingCount - 1; i >= 0; i--) {
                        mapping = visitAfterMappings.get(i);
                        visitAfter(mapping);
                    }
                } else {
                    for (final ContentHandlerBinding<SAXVisitAfter> visitAfterMapping : visitAfterMappings)
                    {
                        visitAfter(visitAfterMapping);
                    }
                }
            }
            flush = true;
        }

        final Fragment<SAXElement> saxElementFragment = new SAXElementFragment(currentProcessor.element);
        if (defaultSerializationOn && applyDefaultSerialization()) {
            try {
                defaultSerializer.visitAfter(currentProcessor.element, executionContext);
                for (ExecutionEventListener executionEventListener : contentDeliveryRuntime.getExecutionEventListeners()) {
                    executionEventListener.onEvent(new VisitEvent<>(saxElementFragment, DEFAULT_SERIALIZER_MAPPING, VisitSequence.AFTER, executionContext));
                }
            } catch (IOException e) {
                throw new SmooksException("Unexpected exception applying defaultSerializer.", e);
            }
            flush = true;
        }

        if (flush) {
            flushCurrentWriter();
        }

        // Process cleanables after applying all the visit afters...
        if (currentProcessor.elementVisitorConfig != null) {
            List<ContentHandlerBinding<VisitLifecycleCleanable>> visitCleanables = currentProcessor.elementVisitorConfig.getVisitCleanables();

            if (visitCleanables != null) {
                final VisitCleanupPhase visitCleanupPhase = new VisitCleanupPhase(saxElementFragment, executionContext);
                for (final ContentHandlerBinding<VisitLifecycleCleanable> visitCleanable : visitCleanables) {
                    final boolean targetedAtElement = saxElementFragment.isMatch(visitCleanable.getResourceConfig().getSelectorPath(), executionContext);

                    if (targetedAtElement) {
                        lifecycleManager.applyPhase(visitCleanable.getContentHandler(), visitCleanupPhase);
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
        final SAXElementFragment saxElementFragment = new SAXElementFragment(element);
        if (currentProcessor.elementVisitorConfig != null) {
            // And visit it with the targeted visitor...
            List<ContentHandlerBinding<SAXVisitBefore>> visitBeforeMappings = currentProcessor.elementVisitorConfig.getVisitBefores();

            if (elementVisitorConfig.accumulateText()) {
                element.accumulateText();
            }
            SAXVisitor acquireWriterFor = elementVisitorConfig.acquireWriterFor();
            if (acquireWriterFor != null) {
                element.getWriter(acquireWriterFor);
            }

            if (visitBeforeMappings != null) {
                for (final ContentHandlerBinding<SAXVisitBefore> mapping : visitBeforeMappings) {
                    try {
                        if (saxElementFragment.isMatch(mapping.getResourceConfig().getSelectorPath(), executionContext)) {
                            mapping.getContentHandler().visitBefore(element, executionContext);
                            // Register the targeting event.  No need to register this event again on the visitAfter...
                            for (ExecutionEventListener executionEventListener : contentDeliveryRuntime.getExecutionEventListeners()) {
                                executionEventListener.onEvent(new ResourceTargetingEvent(saxElementFragment, mapping.getResourceConfig(), VisitSequence.BEFORE));
                                executionEventListener.onEvent(new VisitEvent<>(saxElementFragment, mapping, VisitSequence.BEFORE, executionContext));
                            }
                        }
                    } catch (Throwable t) {
                        String errorMsg = "Error in '" + mapping.getContentHandler().getClass().getName() + "' while processing the visitBefore event.";
                        processVisitorException(element, t, mapping, VisitSequence.BEFORE, errorMsg);
                    }
                }
            }
        }

        if (defaultSerializationOn && applyDefaultSerialization()) {
            try {
                defaultSerializer.visitBefore(element, executionContext);
                for (ExecutionEventListener executionEventListener : contentDeliveryRuntime.getExecutionEventListeners()) {
                    executionEventListener.onEvent(new VisitEvent<>(saxElementFragment, DEFAULT_SERIALIZER_MAPPING, VisitSequence.BEFORE, executionContext));
                }

            } catch (IOException e) {
                throw new SmooksException("Unexpected exception applying defaultSerializer.", e);
            }
        }

        // Apply the dynamic visitors...
        List<SAXVisitBefore> dynamicVisitBefores = dynamicVisitorList.getVisitBefores();
        if (!dynamicVisitBefores.isEmpty()) {
            for (SAXVisitBefore dynamicVisitBefore : dynamicVisitBefores) {
                try {
                    dynamicVisitBefore.visitBefore(element, executionContext);
                } catch (Throwable t) {
                    String errorMsg = "Error in '" + dynamicVisitBefore.getClass().getName() + "' while processing the visitBefore event.";
                    processVisitorException(t, errorMsg);
                }
            }
        }
    }

    private void onChildElement(SAXElement childElement) {
        if (currentProcessor.elementVisitorConfig != null) {
            List<ContentHandlerBinding<SAXVisitChildren>> visitChildMappings = currentProcessor.elementVisitorConfig.getChildVisitors();

            if (visitChildMappings != null) {
                final Fragment<SAXElement> saxElementFragment = new SAXElementFragment(childElement);
                for (final ContentHandlerBinding<SAXVisitChildren> mapping : visitChildMappings) {
                    if (saxElementFragment.isMatch(mapping.getResourceConfig().getSelectorPath(), executionContext)) {
                        try {
                            mapping.getContentHandler().onChildElement(currentProcessor.element, childElement, executionContext);
                        } catch (Throwable t) {
                            String errorMsg = "Error in '" + mapping.getContentHandler().getClass().getName() + "' while processing the onChildElement event.";
                            processVisitorException(currentProcessor.element, t, mapping, VisitSequence.AFTER, errorMsg);
                        }
                    }
                }
            }
        }

        if (defaultSerializationOn && applyDefaultSerialization()) {
            try {
                defaultSerializer.onChildElement(currentProcessor.element, childElement, executionContext);
            } catch (IOException e) {
                throw new SmooksException("Unexpected exception applying defaultSerializer.", e);
            }
        }

        // Apply the dynamic visitors...
        List<SAXVisitChildren> dynamicChildVisitors = dynamicVisitorList.getChildVisitors();
        if (!dynamicChildVisitors.isEmpty()) {
            for (SAXVisitChildren dynamicChildVisitor : dynamicChildVisitors) {
                try {
                    dynamicChildVisitor.onChildElement(currentProcessor.element, childElement, executionContext);
                } catch (Throwable t) {
                    String errorMsg = "Error in '" + dynamicChildVisitor.getClass().getName() + "' while processing the onChildElement event.";
                    processVisitorException(t, errorMsg);
                }
            }
        }
    }

    private void visitAfter(ContentHandlerBinding<SAXVisitAfter> afterMapping) {
        try {
            final Fragment<SAXElement> saxElementFragment = new SAXElementFragment(currentProcessor.element);
            if (saxElementFragment.isMatch(afterMapping.getResourceConfig().getSelectorPath(), executionContext)) {
                afterMapping.getContentHandler().visitAfter(currentProcessor.element, executionContext);
                for (ExecutionEventListener executionEventListener : contentDeliveryRuntime.getExecutionEventListeners()) {
                    executionEventListener.onEvent(new VisitEvent<>(saxElementFragment, afterMapping, VisitSequence.AFTER, executionContext));
                }
            }
        } catch (Throwable t) {
            String errorMsg = "Error in '" + afterMapping.getContentHandler().getClass().getName() + "' while processing the visitAfter event.";
            processVisitorException(currentProcessor.element, t, afterMapping, VisitSequence.AFTER, errorMsg);
        }
    }

    private final SAXText textWrapper = new DefaultSAXText();

    public void characters(char[] ch, int start, int length) throws SAXException {
        if(currentTextType != TextType.CDATA) {
            _characters(ch, start, length);
        } else {
            cdataNodeBuilder.append(ch, start, length);
        }
    }

    private final StringBuilder entityBuilder = new StringBuilder(10);

    private void _characters(char[] ch, int start, int length) {
        if (!rewriteEntities && currentTextType == TextType.ENTITY) {
            entityBuilder.setLength(0);

            entityBuilder.append("&#").append((int) ch[start]).append(';');
            char[] newBuf = new char[entityBuilder.length()];
            entityBuilder.getChars(0, newBuf.length, newBuf, 0);

            textWrapper.setText(newBuf, 0, newBuf.length, TextType.TEXT);
        } else {
            textWrapper.setText(ch, start, length, currentTextType);
        }

        if (currentProcessor != null) {
            // Accumulate the text...
            if (currentProcessor.element != null) {
                List<SAXText> saxTextObjects = currentProcessor.element.getText();
                if (saxTextObjects != null) {
                    saxTextObjects.add(textWrapper);
                }
            }

            if (!currentProcessor.isNullProcessor) {
                if (currentProcessor.elementVisitorConfig != null) {
                    List<ContentHandlerBinding<SAXVisitChildren>> visitChildMappings = currentProcessor.elementVisitorConfig.getChildVisitors();

                    if (visitChildMappings != null) {
                        final Fragment<SAXElement> saxElementFragment = new SAXElementFragment(currentProcessor.element);
                        for (final ContentHandlerBinding<SAXVisitChildren> mapping : visitChildMappings) {
                            try {
                                if (saxElementFragment.isMatch(mapping.getResourceConfig().getSelectorPath(), executionContext)) {
                                    mapping.getContentHandler().onChildText(currentProcessor.element, textWrapper, executionContext);
                                }
                            } catch (Throwable t) {
                                String errorMsg = "Error in '" + mapping.getContentHandler().getClass().getName() + "' while processing the onChildText event.";
                                processVisitorException(currentProcessor.element, t, mapping, VisitSequence.AFTER, errorMsg);
                            }
                        }
                    }
                }

                if (defaultSerializationOn && applyDefaultSerialization()) {
                    try {
                        defaultSerializer.onChildText(currentProcessor.element, textWrapper, executionContext);
                    } catch (IOException e) {
                        throw new SmooksException("Unexpected exception applying defaultSerializer.", e);
                    }
                }
            }

            // Apply the dynamic visitors...
            List<SAXVisitChildren> dynamicChildVisitors = dynamicVisitorList.getChildVisitors();
            if (!dynamicChildVisitors.isEmpty()) {
                for (SAXVisitChildren dynamicChildVisitor : dynamicChildVisitors) {
                    try {
                        dynamicChildVisitor.onChildText(currentProcessor.element, textWrapper, executionContext);
                    } catch (Throwable t) {
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

    public void startEntity(String name) throws SAXException {
        currentTextType = TextType.ENTITY;
    }

    public void endEntity(String name) {
        currentTextType = TextType.TEXT;
    }

    public void startDTD(String name, String publicId, String systemId) throws SAXException {
        DocType.setDocType(name, publicId, systemId, null, executionContext);

        final Writer writer = Stream.out(executionContext);
        if (writer != null) {
            DocType.DocumentTypeData docTypeData = DocType.getDocType(executionContext);
            if (docTypeData != null) {
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

    private void processVisitorException(SAXElement element, Throwable error, ContentHandlerBinding<? extends Visitor> visitorBinding, VisitSequence visitSequence, String errorMsg) throws SmooksException {
        VisitEvent<SAXElement, ? extends Visitor> visitEvent = new VisitEvent<>(new SAXElementFragment(element), visitorBinding, visitSequence, executionContext, error);
        for (ExecutionEventListener executionEventListener : contentDeliveryRuntime.getExecutionEventListeners()) {
            executionEventListener.onEvent(visitEvent);
        }
        
        processVisitorException(error, errorMsg);
    }

    private void processVisitorException(Throwable error, String errorMsg) {
    	if(error instanceof TerminateException) {
            throw (TerminateException) error;
        }

    	executionContext.setTerminationError(error);

        if(terminateOnVisitorException) {
        	if(error instanceof SmooksException) {
                throw (SmooksException) error;
            } else {
                throw new SmooksException(errorMsg, error);
            }
        } else {
            LOGGER.error(errorMsg, error);
        }
    }

    private class WriterManagedSAXElement extends DefaultSAXElement {

        private SAXVisitor writerOwner;

        private WriterManagedSAXElement(QName qName, Attributes attributes, SAXElement parent) {
            super(qName, attributes, parent);
        }

        @Override
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

        @Override
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

        @Override
        public boolean isWriterOwner(SAXVisitor visitor) {
            return (visitor != null && visitor == writerOwner);
        }

        @Override
        public SAXElement getParent() {
            if(!maintainElementStack) {
                throw new SmooksConfigException("Invalid Smooks configuration.  Call to 'SAXElement.getParent()' when the '" + Filter.MAINTAIN_ELEMENT_STACK + "' is set to 'false'.  You need to change this configuration, or modify the calling code.");
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
            throw new SAXWriterAccessException("Illegal access to the element writer for element '" + this + "' by SAX visitor '" + visitor.getClass().getName() + "'.  Writer already acquired by SAX visitor '" + writerOwner.getClass().getName() + "'.  See SAXElement javadocs (https://www.smooks.org).  Change Smooks visitor resource configuration.");
        }
    }
}
