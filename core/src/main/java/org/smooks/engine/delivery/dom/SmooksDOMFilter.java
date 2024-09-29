/*-
 * ========================LICENSE_START=================================
 * Core
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
package org.smooks.engine.delivery.dom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smooks.api.ExecutionContext;
import org.smooks.api.Registry;
import org.smooks.api.SmooksException;
import org.smooks.api.TypedKey;
import org.smooks.api.delivery.ContentDeliveryRuntime;
import org.smooks.api.delivery.ContentHandler;
import org.smooks.api.delivery.ContentHandlerBinding;
import org.smooks.api.delivery.Filter;
import org.smooks.api.delivery.event.ExecutionEventListener;
import org.smooks.api.delivery.fragment.Fragment;
import org.smooks.api.io.Sink;
import org.smooks.api.io.Source;
import org.smooks.api.lifecycle.DOMFilterLifecycle;
import org.smooks.api.lifecycle.LifecycleManager;
import org.smooks.api.lifecycle.PostFragmentLifecycle;
import org.smooks.api.resource.config.ResourceConfig;
import org.smooks.api.resource.config.ResourceConfigSortComparator;
import org.smooks.api.resource.visitor.SerializerVisitor;
import org.smooks.api.resource.visitor.Visitor;
import org.smooks.api.resource.visitor.dom.DOMElementVisitor;
import org.smooks.api.resource.visitor.dom.DOMVisitAfter;
import org.smooks.api.resource.visitor.dom.DOMVisitBefore;
import org.smooks.api.resource.visitor.dom.VisitPhase;
import org.smooks.engine.delivery.AbstractFilter;
import org.smooks.engine.delivery.ContentHandlerBindingIndex;
import org.smooks.engine.delivery.dom.serialize.Serializer;
import org.smooks.engine.delivery.dom.serialize.TextSerializerVisitor;
import org.smooks.engine.delivery.event.*;
import org.smooks.engine.delivery.fragment.NodeFragment;
import org.smooks.engine.lifecycle.AssemblyStartedDOMFilterLifecyclePhase;
import org.smooks.engine.lifecycle.PostFragmentPhase;
import org.smooks.engine.lifecycle.ProcessingStartedDOMFilterLifecyclePhase;
import org.smooks.engine.lookup.InstanceLookup;
import org.smooks.engine.lookup.LifecycleManagerLookup;
import org.smooks.engine.report.AbstractReportGenerator;
import org.smooks.engine.resource.config.DefaultResourceConfig;
import org.smooks.engine.resource.config.ParameterAccessor;
import org.smooks.io.Stream;
import org.smooks.io.sink.DOMSink;
import org.smooks.io.sink.FilterSink;
import org.smooks.io.sink.StreamSink;
import org.smooks.io.sink.WriterSink;
import org.smooks.io.source.DOMSource;
import org.smooks.io.source.FilterSource;
import org.smooks.io.source.JavaSource;
import org.smooks.io.source.ReaderSource;
import org.smooks.io.source.StreamSource;
import org.smooks.io.source.URLSource;
import org.smooks.support.DomUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * Smooks DOM based content filtering class.
 * <p/>
 * This class is responsible for <b>Filtering</b> XML DOM streams
 * (XML/XHTML/HTML etc) through a process of iterating over the source XML DOM tree
 * and applying the {@link ResourceConfig configured} Content Delivery Units
 * ({@link DOMElementVisitor DOMElementVisitors} and
 * {@link SerializerVisitor SerializationUnits}).
 * <p/>
 * This class doesn't get used directly.  See the {@link org.smooks.Smooks} class.
 * <p/>
 * <h3 id="phases">XML/XHTML/HTML Filtering Process</h3>
 * SmooksDOMFilter markup processing (XML/XHTML/HTML) is a 2 phase filter, depending on what
 * needs to be done.  The first phase is called the "Visit Phase", and the second
 * phase is called the "Serialisation Phase".  SmooksDOMFilter can be used to execute either or both of these
 * phases (depending on what needs to be done!).
 * <p/>
 * Through this filter, Smooks can be used to analyse and/or transform markup, and then
 * serialise it.
 * <p/>
 * <p/>
 * So, in a little more detail, the 2 phases are:
 * <ol>
 * <li>
 * <b><u>Visit</u></b>: This phase is executed via either of the
 * {@link #filter(Document)} or {@link #filter(Source)} methods.
 * This phase is really 2 "sub" phases.
 * <ul>
 * <li>
 * <b>Assembly</b>: This is effectively a pre-processing phase.
 * <p/>
 * This sub-phase involves iterating over the source XML DOM,
 * visiting all DOM elements that have {@link VisitPhase ASSEMBLY} phase
 * {@link DOMElementVisitor DOMElementVisitors}
 * {@link ResourceConfig targeted} at them for the profile
 * associated with the {@link ExecutionContext}.
 * This phase can result in DOM elements being added to, or trimmed from, the DOM.
 * This phase is also very usefull for gathering data from the message in the DOM
 * (and storing it in the {@link ExecutionContext}),
 * which can be used during the processing phase (see below).  This phase is only
 * executed if there are
 * {@link DOMElementVisitor DOMElementVisitors} targeted at this phase.
 * </li>
 * <li>
 * <b>Processing</b>: Processing takes the assembled DOM and
 * iterates over it again, so as to perform transformation/analysis.
 * <p/>
 * This sub-phase involves iterating over the source XML DOM again,
 * visiting all DOM elements that have {@link VisitPhase PROCESSING} phase
 * {@link DOMElementVisitor DOMElementVisitors}
 * {@link ResourceConfig targeted} at them for the profile
 * associated with the {@link ExecutionContext}.
 * This phase will only operate on DOM elements that were present in the assembled
 * document; {@link DOMElementVisitor DOMElementVisitors} will not be applied
 * to elements that are introduced to the DOM during this phase.
 * </li>
 * </ul>
 * </li>
 * <li>
 * <b><u>Serialisation</u></b>: This phase is executed by the {@link #serialize(Node, Writer)} method (which uses the
 * {@link org.smooks.engine.delivery.dom.serialize.Serializer} class).  The serialisation phase takes the processed DOM and
 * iterates over it to apply all {@link SerializerVisitor SerializationUnits},
 * which write the document to the target output stream.
 * <p/>
 * Instead of using this serialisation mechanism, you may wish to perform
 * DOM Serialisation via some other mechanism e.g. XSL-FO via something like Apache FOP.
 * </li>
 * </ol>
 * <p/>
 * See the <a href="http://milyn.codehaus.org/flash/DOMProcess.html" target="DOMProcess">online flash demo</a> demonstrating this process.
 * <p/>
 * <h3>Other Documents</h3>
 * <ul>
 * <li>{@link org.smooks.Smooks}</li>
 * <li>{@link ResourceConfig}</li>
 * <li>{@link ResourceConfigSortComparator}</li>
 * </ul>
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
@SuppressWarnings("WeakerAccess")
public class SmooksDOMFilter extends AbstractFilter {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(SmooksDOMFilter.class);
    /**
     * Container request for this Smooks content delivery instance.
     */
    private final ExecutionContext executionContext;
    /**
     * Accessor to the ExecutionContext delivery config.
     */
    private final DOMContentDeliveryConfig deliveryConfig;
    /**
     * Key under which a non-document content delivery node can be set in the
     * request.  This is needed because Xerces doesn't allow "overwriting" of
     * the document root node.
     */
    private static final TypedKey<Node> DELIVERY_NODE_REQUEST_KEY = TypedKey.of();

    private final Boolean closeSource;
    private final Boolean closeSink;
    private final Boolean reverseVisitOrderOnVisitAfter;
    private final ContentDeliveryRuntime contentDeliveryRuntime;
    private final LifecycleManager lifecycleManager;
    private Boolean terminateOnVisitorException;

    /**
     * Global assembly befores.
     */
    private List<ContentHandlerBinding<DOMVisitBefore>> globalAssemblyBefores;
    /**
     * Global assembly afters.
     */
    private List<ContentHandlerBinding<DOMVisitAfter>> globalAssemblyAfters;
    /**
     * Global process befores.
     */
    private List<ContentHandlerBinding<DOMVisitBefore>> globalProcessingBefores;
    /**
     * Global process afters.
     */
    private List<ContentHandlerBinding<DOMVisitAfter>> globalProcessingAfters;

    /**
     * Public constructor.
     * <p/>
     * Constructs a SmooksDOMFilter instance for delivering content for the supplied execution context.
     *
     * @param executionContext Execution context.  This instance
     *                         is bound to the current Thread of execution.  See <a href="#threading">Threading Issues</a>.
     */
    public SmooksDOMFilter(ExecutionContext executionContext) {
        if (executionContext == null) {
            throw new IllegalArgumentException("null 'executionContext' arg passed in constructor call.");
        }
        this.executionContext = executionContext;
        this.contentDeliveryRuntime = executionContext.getContentDeliveryRuntime();
        deliveryConfig = (DOMContentDeliveryConfig) contentDeliveryRuntime.getContentDeliveryConfig();
        lifecycleManager = executionContext.getApplicationContext().getRegistry().lookup(new LifecycleManagerLookup());

        closeSource = Boolean.parseBoolean(ParameterAccessor.getParameterValue(Filter.CLOSE_SOURCE, String.class, "true", deliveryConfig));
        closeSink = Boolean.parseBoolean(ParameterAccessor.getParameterValue(Filter.CLOSE_SINK, String.class, "true", deliveryConfig));
        reverseVisitOrderOnVisitAfter = Boolean.parseBoolean(ParameterAccessor.getParameterValue(Filter.REVERSE_VISIT_ORDER_ON_VISIT_AFTER, String.class, "true", deliveryConfig));

        for (ExecutionEventListener executionEventListener : contentDeliveryRuntime.getExecutionEventListeners()) {
            if (executionEventListener instanceof AbstractReportGenerator) {
                terminateOnVisitorException = false;
            }
        }

        if (terminateOnVisitorException == null) {
            terminateOnVisitorException = Boolean.parseBoolean(ParameterAccessor.getParameterValue(Filter.TERMINATE_ON_VISITOR_EXCEPTION, String.class, "true", deliveryConfig));
        }
    }

    @Override
    public void doFilter() throws SmooksException {
        Source source = FilterSource.getSource(executionContext);
        Sink sink = FilterSink.getSink(executionContext, StreamSink.class);
        if (sink == null) {
            sink = FilterSink.getSink(executionContext, WriterSink.class);
            if (sink == null) {
                // Maybe there's a DOMSink...
                sink = FilterSink.getSink(executionContext, DOMSink.class);
            }
        }

        doFilter(source, sink);
    }

    protected void doFilter(Source source, Sink sink) {
        if (!(source instanceof StreamSource) && !(source instanceof ReaderSource)  && !(source instanceof DOMSource) && !(source instanceof JavaSource) && !(source instanceof URLSource)) {
            throw new IllegalArgumentException(source.getClass().getName() + " Source types not yet supported by the DOM Filter.");
        }
        if (!(sink instanceof FilterSink)) {
            if (sink != null && !(sink instanceof StreamSink) && !(sink instanceof WriterSink) && !(sink instanceof DOMSink)) {
                throw new IllegalArgumentException(sink.getClass().getName() + " Sinks types not yet supported by the DOM Filter.");
            }
        }

        try {
            Node resultNode;

            // Filter the Source....
            if (source instanceof DOMSource) {
                Node node = ((DOMSource) source).getNode();
                if ((node instanceof Document)) {
                    resultNode = filter((Document) node);
                } else if ((node instanceof Element)) {
                    resultNode = filter((Element) node);
                } else {
                    throw new IllegalArgumentException("DOMSource Source types must contain a Document or Element node.");
                }
            } else {
                resultNode = filter(source);
            }

            // Populate the Sink
            if (sink instanceof WriterSink || sink instanceof StreamSink) {
                Writer writer = getWriter(sink, executionContext);

                try {
                    serialize(resultNode, writer);
                    writer.flush();
                } catch (IOException e) {
                    LOGGER.debug("Error writing result to output stream.", e);
                }
            } else if (sink instanceof DOMSink) {
                ((DOMSink) sink).setNode(resultNode);
            }
        } finally {
            if (closeSource) {
                close(source);
            }
            if (closeSink) {
                close(sink);
            }
        }
    }

    @Override
    public void close() {
    }

    /**
     * Phase the supplied input reader.
     * <p/>
     * Simply parses the input reader into a W3C DOM and calls {@link #filter(Document)}.
     *
     * @param source The source of markup to be filtered.
     * @return Node representing filtered document.
     */
    public Node filter(Source source) {
        Node deliveryNode;

        if (source == null) {
            throw new IllegalArgumentException("null 'source' arg passed in method call.");
        }
        try {
            DOMParser parser = new DOMParser(executionContext);
            Document document = parser.parse(source);

            deliveryNode = filter(document);
        } catch (Exception cause) {
            throw new SmooksException("Unable to filter InputStream for target profile [" + executionContext.getTargetProfiles().getBaseProfile() + "].", cause);
        }

        return deliveryNode;
    }

    /**
     * Filter the supplied W3C Document.
     * <p/>
     * Executes the <a href="#phases">Assembly &amp Processing phases</a>.
     *
     * @param doc The W3C Document to be filtered.
     * @return Node representing filtered document.
     */
    public Node filter(Document doc) {
        Node deliveryNode;

        // Apply assembly phase...
        if (doc.getDocumentElement() == null) {
            LOGGER.debug("Empty Document [" + executionContext.getDocumentSource() + "].  Not performaing any processing.");
            return doc;
        }

        deliveryNode = filter(doc.getDocumentElement());
        if (deliveryNode == null) {
            deliveryNode = doc;
        }

        return deliveryNode;
    }

    private static final String[] GLOBAL_SELECTORS = new String[]{"*", "//"};

    /**
     * Filter the supplied W3C Element.
     * <p/>
     * Executes the <a href="#phases">Assembly &amp Processing phases</a>.
     *
     * @param element The W3C Element to be filtered.
     * @return Node representing filtered Element.
     */
    public Node filter(Element element) {
        executionContext.put(Stream.STREAM_WRITER_TYPED_KEY, new Writer() {
            @Override
            public void write(char[] cbuf, int off, int len) {
                StringWriter stringWriter = new StringWriter();
                stringWriter.write(cbuf, off, len);
                final Node resultNode = TextSerializerVisitor.createTextElement(element, stringWriter.toString());
                DomUtils.replaceNode(resultNode, element);
            }

            @Override
            public void flush() throws IOException {

            }

            @Override
            public void close() throws IOException {

            }
        });
        ContentHandlerBindingIndex<DOMVisitBefore> visitBeforeContentHandlerBindingIndex = deliveryConfig.getAssemblyVisitBeforeIndex();
        ContentHandlerBindingIndex<DOMVisitAfter> visitAfterContentHandlerBindingIndex = deliveryConfig.getAssemblyVisitAfterIndex();
        globalAssemblyBefores = visitBeforeContentHandlerBindingIndex.get(GLOBAL_SELECTORS);
        globalAssemblyAfters = visitAfterContentHandlerBindingIndex.get(GLOBAL_SELECTORS);

        // Register the DOM phase events...
        Registry registry = executionContext.getApplicationContext().getRegistry();
        LifecycleManager lifecycleManager = registry.lookup(new LifecycleManagerLookup());
        AssemblyStartedDOMFilterLifecyclePhase assemblyStartedDOMFilterLifecyclePhase = new AssemblyStartedDOMFilterLifecyclePhase(executionContext);
        for (DOMFilterLifecycle domFilterLifecycle : registry.lookup(new InstanceLookup<>(DOMFilterLifecycle.class)).values()) {
            lifecycleManager.applyPhase(domFilterLifecycle, assemblyStartedDOMFilterLifecyclePhase);
        }

        // Apply assembly phase, skipping it if there are no configured assembly units...
        if (applyAssembly(visitBeforeContentHandlerBindingIndex, visitAfterContentHandlerBindingIndex)) {
            // Assemble
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Starting assembly phase [" + executionContext.getTargetProfiles().getBaseProfile() + "]");
            }
            assemble(element, true);
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("No assembly units configured for device [" + executionContext.getTargetProfiles().getBaseProfile() + "]");
            }
        }

        // Register the DOM phase events...
        ProcessingStartedDOMFilterLifecyclePhase processingStartedDOMFilterLifecyclePhase = new ProcessingStartedDOMFilterLifecyclePhase(executionContext);
        for (DOMFilterLifecycle domFilterLifecycle : registry.lookup(new InstanceLookup<>(DOMFilterLifecycle.class)).values()) {
            lifecycleManager.applyPhase(domFilterLifecycle, processingStartedDOMFilterLifecyclePhase);
        }

        // Apply processing phase...
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Starting processing phase [" + executionContext.getTargetProfiles().getBaseProfile() + "]");
        }

        globalProcessingBefores = deliveryConfig.getProcessingVisitBeforeIndex().get(GLOBAL_SELECTORS);
        if (globalProcessingBefores != null && globalProcessingBefores.isEmpty()) {
            globalProcessingBefores = null;
        }
        globalProcessingAfters = deliveryConfig.getProcessingVisitAfterIndex().get(GLOBAL_SELECTORS);
        if (globalProcessingAfters != null && globalProcessingAfters.isEmpty()) {
            globalProcessingAfters = null;
        }

        int transListLength;
        List<ElementProcessor> transList = new ArrayList<>();

        buildProcessingList(transList, element, true);
        transListLength = transList.size();
        for (int i = 0; i < transListLength; i++) {
            ElementProcessor elementTrans = transList.get(i);
            elementTrans.process(executionContext);
        }

        return executionContext.get(DELIVERY_NODE_REQUEST_KEY);
    }

    private boolean applyAssembly(ContentHandlerBindingIndex<DOMVisitBefore> visitBefores, ContentHandlerBindingIndex<DOMVisitAfter> visitAfters) {
        return !visitBefores.isEmpty() || !visitAfters.isEmpty() ||
                (globalAssemblyBefores != null && !globalAssemblyBefores.isEmpty()) ||
                (globalAssemblyAfters != null && !globalAssemblyAfters.isEmpty());
    }

    /**
     * Assemble the supplied element.
     * <p/>
     * Recursively iterate down into the elements children.
     *
     * @param element Next element to operate on and iterate over.
     * @param isRoot  Is the supplied element the document root element.
     */
    private void assemble(Element element, boolean isRoot) {
        List<Node> nodeListCopy = copyList(element.getChildNodes());

        ContentHandlerBindingIndex<DOMVisitBefore> visitBeforeTable = deliveryConfig.getAssemblyVisitBeforeIndex();
        ContentHandlerBindingIndex<DOMVisitAfter> visitAfterTable = deliveryConfig.getAssemblyVisitAfterIndex();
        String elementName = DomUtils.getName(element);

        // Register the "presence" of the element...
        for (ExecutionEventListener executionEventListener : contentDeliveryRuntime.getExecutionEventListeners()) {
            executionEventListener.onEvent(new StartFragmentExecutionEvent<>(new NodeFragment(element)));
        }

        List<ContentHandlerBinding<DOMVisitBefore>> elementVisitBefores;
        List<ContentHandlerBinding<DOMVisitAfter>> elementVisitAfters;
        if (isRoot) {
            // The document as a whole (root node) can also be targeted through the "#document" selector.
            elementVisitBefores = visitBeforeTable.get(new String[]{ResourceConfig.DOCUMENT_FRAGMENT_SELECTOR, elementName});
            elementVisitAfters = visitAfterTable.get(new String[]{ResourceConfig.DOCUMENT_FRAGMENT_SELECTOR, elementName});
        } else {
            elementVisitBefores = visitBeforeTable.get(elementName);
            elementVisitAfters = visitAfterTable.get(elementName);
        }

        // Visit element with its assembly units before visiting its child content.
        if (elementVisitBefores != null && !elementVisitBefores.isEmpty()) {
            applyAssemblyBefores(element, elementVisitBefores);
        }
        if (globalAssemblyBefores != null && !globalAssemblyBefores.isEmpty()) {
            applyAssemblyBefores(element, globalAssemblyBefores);
        }

        // Recursively iterate the elements child content...
        for (final Object aNodeListCopy : nodeListCopy) {
            Node child = (Node) aNodeListCopy;
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                assemble((Element) child, false);
            }
        }

        // Revisit the element with its assembly units after visiting its child content.
        if (elementVisitAfters != null && !elementVisitAfters.isEmpty()) {
            applyAssemblyAfters(element, elementVisitAfters);
        }
        if (globalAssemblyAfters != null && !globalAssemblyAfters.isEmpty()) {
            applyAssemblyAfters(element, globalAssemblyAfters);
        }
    }

    private void applyAssemblyBefores(Element element, List<ContentHandlerBinding<DOMVisitBefore>> assemblyBefores) {
        final Fragment<Node> nodeFragment = new NodeFragment(element);
        for (final ContentHandlerBinding<DOMVisitBefore> configMap : assemblyBefores) {
            ResourceConfig resourceConfig = configMap.getResourceConfig();

            // Make sure the assembly unit is targeted at this element...
            if (!nodeFragment.isMatch(resourceConfig.getSelectorPath(), executionContext)) {
                continue;
            }

            // Register the targeting event.  No need to register it again in the visitAfter loop...
            for (ExecutionEventListener executionEventListener : contentDeliveryRuntime.getExecutionEventListeners()) {
                executionEventListener.onEvent(new ResourceTargetingExecutionEvent(nodeFragment, resourceConfig, VisitSequence.BEFORE, VisitPhase.ASSEMBLY));
            }

            DOMVisitBefore assemblyUnit = configMap.getContentHandler();
            try {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("(Assembly) Calling visitBefore on element [" + DomUtils.getXPath(element) + "]. Config [" + resourceConfig + "]");
                }
                assemblyUnit.visitBefore(element, executionContext);
                for (ExecutionEventListener executionEventListener : contentDeliveryRuntime.getExecutionEventListeners()) {
                    executionEventListener.onEvent(new VisitExecutionEvent<>(nodeFragment, configMap, VisitSequence.BEFORE, executionContext));
                }
            } catch (Throwable e) {
                String errorMsg =
                        "(Assembly) visitBefore failed [" + assemblyUnit.getClass().getName() + "] on [" + executionContext.getDocumentSource() + ":" + DomUtils.getXPath(element)
                                + "].";
                processVisitorException(nodeFragment, e, configMap, VisitSequence.BEFORE, errorMsg);
            }
        }
    }

    private void applyAssemblyAfters(Element element, List<ContentHandlerBinding<DOMVisitAfter>> elementVisitAfters) {
        if (reverseVisitOrderOnVisitAfter) {
            for (int i = elementVisitAfters.size() - 1; i >= 0; i--) {
                ContentHandlerBinding<DOMVisitAfter> configMap = elementVisitAfters.get(i);
                applyAssemblyAfter(element, configMap);
            }
        } else {
            for (final ContentHandlerBinding<DOMVisitAfter> configMap : elementVisitAfters) {
                applyAssemblyAfter(element, configMap);
            }
        }
    }

    private void applyAssemblyAfter(final Element element, final ContentHandlerBinding<DOMVisitAfter> visitAfterBinding) {
        final ResourceConfig resourceConfig = visitAfterBinding.getResourceConfig();
        final Fragment<Node> nodeFragment = new NodeFragment(element);
        // Make sure the assembly unit is targeted at this element...
        if (!nodeFragment.isMatch(resourceConfig.getSelectorPath(), executionContext)) {
            return;
        }

        DOMVisitAfter visitAfter = visitAfterBinding.getContentHandler();
        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("(Assembly) Calling visitAfter on element [" + DomUtils.getXPath(element) + "]. Config [" + resourceConfig + "]");
            }
            visitAfter.visitAfter(element, executionContext);
            for (ExecutionEventListener executionEventListener : contentDeliveryRuntime.getExecutionEventListeners()) {
                executionEventListener.onEvent(new VisitExecutionEvent<>(nodeFragment, visitAfterBinding, VisitSequence.AFTER, executionContext));
            }
        } catch (Throwable e) {
            String errorMsg = "(Assembly) visitAfter failed [" + visitAfter.getClass().getName() + "] on [" + executionContext.getDocumentSource() + ":" + DomUtils.getXPath(element) + "].";
            processVisitorException(nodeFragment, e, visitAfterBinding, VisitSequence.AFTER, errorMsg);
        }
    }

    /**
     * Recurcively build the processing list for the supplied element, iterating over the elements
     * child content.
     *
     * @param processingList List under construction.  List of ElementProcessor instances.
     * @param element        Current element being tested.  Starts at the document root element.
     * @param isRoot         Is the supplied element the document root element.
     */
    private void buildProcessingList(List<ElementProcessor> processingList, Element element, boolean isRoot) {
        String elementName;
        List<ContentHandlerBinding<DOMVisitBefore>> processingBefores;
        List<ContentHandlerBinding<DOMVisitAfter>> processingAfters;
        List<ContentHandlerBinding<PostFragmentLifecycle>> processingPostFragmentLifecycles;

        // Register the "presence" of the element...
        for (ExecutionEventListener executionEventListener : contentDeliveryRuntime.getExecutionEventListeners()) {
            executionEventListener.onEvent(new StartFragmentExecutionEvent<>(new NodeFragment(element)));
        }

        elementName = DomUtils.getName(element);
        if (isRoot) {
            // The document as a whole (root node) can also be targeted through the "#document" selector.
            processingBefores = deliveryConfig.getProcessingVisitBeforeIndex().get(new String[]{DefaultResourceConfig.DOCUMENT_FRAGMENT_SELECTOR, elementName});
            processingAfters = deliveryConfig.getProcessingVisitAfterIndex().get(new String[]{DefaultResourceConfig.DOCUMENT_FRAGMENT_SELECTOR, elementName});
            processingPostFragmentLifecycles = deliveryConfig.getPostFragmentLifecycleIndex().get(new String[]{DefaultResourceConfig.DOCUMENT_FRAGMENT_SELECTOR, elementName});
        } else {
            processingBefores = deliveryConfig.getProcessingVisitBeforeIndex().get(elementName);
            processingAfters = deliveryConfig.getProcessingVisitAfterIndex().get(elementName);
            processingPostFragmentLifecycles = deliveryConfig.getPostFragmentLifecycleIndex().get(elementName);
        }

        if (processingBefores != null && !processingBefores.isEmpty()) {
            ElementProcessor processor = new ElementProcessor(element);
            processor.setVisitBefores(processingBefores);
            processingList.add(processor);
        }
        if (globalProcessingBefores != null) {
            // TODO: Inefficient. Find a better way!
            ElementProcessor processor = new ElementProcessor(element);
            processor.setVisitBefores(globalProcessingBefores);
            processingList.add(processor);
        }

        // Iterate over the child elements, calling this method recursively....
        NodeList children = element.getChildNodes();
        int childCount = children.getLength();
        for (int i = 0; i < childCount; i++) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                buildProcessingList(processingList, (Element) child, false);
            }
        }

        if (processingAfters != null && !processingAfters.isEmpty()) {
            ElementProcessor processor = new ElementProcessor(element);
            processor.setVisitAfters(processingAfters);
            processingList.add(processor);
        }
        if (globalProcessingAfters != null) {
            // TODO: Inefficient. Find a better way!
            ElementProcessor processor = new ElementProcessor(element);
            processor.setVisitAfters(globalProcessingAfters);
            processingList.add(processor);
        }

        if (processingPostFragmentLifecycles != null && !processingPostFragmentLifecycles.isEmpty()) {
            ElementProcessor processor = new ElementProcessor(element);
            processor.setPostFragmentLifecycles(processingPostFragmentLifecycles);
            processingList.add(processor);
        }
    }

    /**
     * Serialise the node to the supplied output writer instance.
     * <p/>
     * Executes the <a href="#phases">Serialisation phase</a>,
     * using the {@link Serializer} class to perform the serialization.
     *
     * @param node   Document to be serialised.
     * @param writer Output writer.
     * @throws IOException     Unable to write to output writer.
     * @throws SmooksException Unable to serialise due to bad Smooks environment.  Check cause.
     */
    public void serialize(Node node, Writer writer) throws IOException, SmooksException {
        Serializer serializer;

        if (node == null) {
            throw new IllegalArgumentException("null 'doc' arg passed in method call.");
        } else if (writer == null) {
            throw new IllegalArgumentException("null 'writer' arg passed in method call.");
        }

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Starting serialization phase [" + executionContext.getTargetProfiles().getBaseProfile() + "]");
        }
        serializer = new Serializer(node, executionContext);
        try {
            serializer.serialize(writer);
        } catch (Exception e) {
            throw new SmooksException("Unable to serialize document", e);
        }
    }

    /**
     * Copy the nodes of a NodeList into the supplied list.
     * <p/>
     * This is not a clone.  It's just a copy of the node references.
     * <p/>
     * Allows iteration over the Nodelist using the copy in the knowledge that
     * the list will remain the same length.  Using the NodeList can result in problems
     * because elements can get removed from the list while we're iterating over it.
     *
     * @param nodeList Nodelist to copy.
     * @return List copy.
     */
    private List<Node> copyList(NodeList nodeList) {
        List<Node> copy = new ArrayList<>(nodeList.getLength());
        int nodeCount = nodeList.getLength();

        for (int i = 0; i < nodeCount; i++) {
            copy.add(nodeList.item(i));
        }

        return copy;
    }

    /**
     * Element Processor class.
     * <p/>
     * Simple DOM Element to ProcessingUnit[] processing mapping and support functions.
     *
     * @author tfennelly
     */
    private class ElementProcessor {
        /**
         * The Element instance to be processed.
         */
        private final Element element;

        private List<ContentHandlerBinding<DOMVisitBefore>> visitBefores;
        private List<ContentHandlerBinding<DOMVisitAfter>> visitAfters;
        private List<ContentHandlerBinding<PostFragmentLifecycle>> postFragmentLifecycles;

        /**
         * Constructor.
         *
         * @param element Element to be processed.
         */
        private ElementProcessor(Element element) {
            this.element = element;
        }

        private void setVisitBefores(List<ContentHandlerBinding<DOMVisitBefore>> visitBefores) {
            this.visitBefores = visitBefores;
        }

        private void setVisitAfters(List<ContentHandlerBinding<DOMVisitAfter>> visitAfters) {
            this.visitAfters = visitAfters;
        }

        public void setPostFragmentLifecycles(List<ContentHandlerBinding<PostFragmentLifecycle>> postFragmentLifecycles) {
            this.postFragmentLifecycles = postFragmentLifecycles;
        }

        /**
         * Apply the ProcessingUnits.
         * <p/>
         * Iterate over the ProcessingUnit instances calling the visitAfter method.
         *
         * @param executionContext Container request instance.
         */
        private void process(ExecutionContext executionContext) {

            if (visitBefores != null) {
                for (final ContentHandlerBinding<DOMVisitBefore> visitBefore : visitBefores) {
                    processMapping(executionContext, visitBefore, VisitSequence.BEFORE);
                }
            } else if (visitAfters != null) {
                int loopLength = visitAfters.size();
                if (reverseVisitOrderOnVisitAfter) {
                    for (int i = loopLength - 1; i >= 0; i--) {
                        ContentHandlerBinding<? extends Visitor> configMap = visitAfters.get(i);
                        processMapping(executionContext, configMap, VisitSequence.AFTER);
                    }
                } else {
                    for (final ContentHandlerBinding<DOMVisitAfter> visitAfter : visitAfters) {
                        processMapping(executionContext, visitAfter, VisitSequence.AFTER);
                    }
                }
            } else {
                for (final ContentHandlerBinding<PostFragmentLifecycle> postFragmentLifecycle : postFragmentLifecycles) {
                    processMapping(executionContext, postFragmentLifecycle, VisitSequence.CLEAN);
                }
            }
        }

        private void processMapping(ExecutionContext executionContext, ContentHandlerBinding<? extends Visitor> visitorBinding, VisitSequence visitSequence) {
            ResourceConfig resourceConfig = visitorBinding.getResourceConfig();

            // Make sure the processing unit is targeted at this element...
            final Fragment<Node> nodeFragment = new NodeFragment(element);
            if (!nodeFragment.isMatch(resourceConfig.getSelectorPath(), executionContext)) {
                return;
            }

            // Could add an "is-element-in-document-tree" check here
            // but might not be valid.  Also, this check
            // would need to iterate back up to the document root
            // every time. Doing this for every element could be very
            // costly.

            if (visitSequence == VisitSequence.BEFORE) {
                // Register the targeting event...
                for (ExecutionEventListener executionEventListener : contentDeliveryRuntime.getExecutionEventListeners()) {
                    executionEventListener.onEvent(new ResourceTargetingExecutionEvent(nodeFragment, resourceConfig, VisitSequence.BEFORE));
                }

                DOMVisitBefore visitor = (DOMVisitBefore) visitorBinding.getContentHandler();
                try {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Applying processing resource [" + resourceConfig + "] to element [" + DomUtils.getXPath(element) + "] before applying resources to its child elements.");
                    }
                    visitor.visitBefore(element, executionContext);
                    for (ExecutionEventListener executionEventListener : contentDeliveryRuntime.getExecutionEventListeners()) {
                        executionEventListener.onEvent(new VisitExecutionEvent<>(nodeFragment, visitorBinding, VisitSequence.BEFORE, executionContext));
                    }
                } catch (Throwable e) {
                    String errorMsg = "Failed to apply processing unit [" + visitor.getClass().getName() + "] to [" + executionContext.getDocumentSource() + ":" + DomUtils.getXPath(element) + "].";
                    processVisitorException(nodeFragment, e, visitorBinding, VisitSequence.BEFORE, errorMsg);
                }
            } else if (visitSequence == VisitSequence.AFTER) {
                // Register the targeting event...
                for (ExecutionEventListener executionEventListener : contentDeliveryRuntime.getExecutionEventListeners()) {
                    executionEventListener.onEvent(new ResourceTargetingExecutionEvent(nodeFragment, resourceConfig, VisitSequence.AFTER));
                }

                DOMVisitAfter visitor = (DOMVisitAfter) visitorBinding.getContentHandler();
                try {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Applying processing resource [" + resourceConfig + "] to element [" + DomUtils.getXPath(element) + "] after applying resources to its child elements.");
                    }
                    visitor.visitAfter(element, executionContext);
                    for (ExecutionEventListener executionEventListener : contentDeliveryRuntime.getExecutionEventListeners()) {
                        executionEventListener.onEvent(new VisitExecutionEvent<>(nodeFragment, visitorBinding, VisitSequence.AFTER, executionContext));
                    }
                } catch (Throwable e) {
                    String errorMsg = "Failed to apply processing unit [" + visitor.getClass().getName() + "] to [" + executionContext.getDocumentSource() + ":" + DomUtils.getXPath(element) + "].";
                    processVisitorException(nodeFragment, e, visitorBinding, VisitSequence.BEFORE, errorMsg);
                }
            } else if (visitSequence == VisitSequence.CLEAN) {
                // Register the targeting event...
                for (ExecutionEventListener executionEventListener : contentDeliveryRuntime.getExecutionEventListeners()) {
                    executionEventListener.onEvent(new ResourceTargetingExecutionEvent(nodeFragment, resourceConfig, VisitSequence.CLEAN));
                }

                ContentHandler contentHandler = visitorBinding.getContentHandler();
                if (contentHandler instanceof PostFragmentLifecycle) {
                    PostFragmentLifecycle visitor = (PostFragmentLifecycle) contentHandler;
                    try {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("Cleaning up processing resource [" + resourceConfig + "] that was targeted to element [" + DomUtils.getXPath(element) + "].");
                        }
                        lifecycleManager.applyPhase(visitor, new PostFragmentPhase(nodeFragment, executionContext));
                        for (ExecutionEventListener executionEventListener : contentDeliveryRuntime.getExecutionEventListeners()) {
                            executionEventListener.onEvent(new VisitExecutionEvent<>(nodeFragment, visitorBinding, VisitSequence.CLEAN, executionContext));
                        }
                    } catch (Throwable e) {
                        String errorMsg = "Failed to clean up [" + visitor.getClass().getName() + "]. Targeted at [" + executionContext.getDocumentSource() + ":" + DomUtils.getXPath(element) + "].";
                        processVisitorException(nodeFragment, e, visitorBinding, VisitSequence.CLEAN, errorMsg);
                    }
                }
            }
        }
    }

    private void processVisitorException(Fragment<?> fragment, Throwable error, ContentHandlerBinding<? extends Visitor> configMapping, VisitSequence visitSequence, String errorMsg) throws SmooksException {
        for (ExecutionEventListener executionEventListener : executionContext.getContentDeliveryRuntime().getExecutionEventListeners()) {
            executionEventListener.onEvent(new VisitExecutionEvent<>(fragment, configMapping, visitSequence, executionContext, error));
        }

        executionContext.setTerminationError(error);

        if (terminateOnVisitorException) {
            if (error instanceof SmooksException) {
                throw (SmooksException) error;
            } else {
                throw new SmooksException(errorMsg, error);
            }
        } else {
            LOGGER.debug(errorMsg, error);
        }
    }
}
