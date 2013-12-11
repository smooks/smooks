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

package org.milyn.delivery.dom;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.milyn.cdr.ParameterAccessor;
import org.milyn.cdr.ResourceConfigurationNotFoundException;
import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.commons.SmooksException;
import org.milyn.commons.xml.DomUtils;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.ContentDeliveryConfig;
import org.milyn.delivery.ContentHandler;
import org.milyn.delivery.ContentHandlerConfigMap;
import org.milyn.delivery.ContentHandlerConfigMapTable;
import org.milyn.delivery.Filter;
import org.milyn.delivery.Fragment;
import org.milyn.delivery.VisitLifecycleCleanable;
import org.milyn.delivery.VisitSequence;
import org.milyn.delivery.dom.serialize.Serializer;
import org.milyn.event.ExecutionEventListener;
import org.milyn.event.report.AbstractReportGenerator;
import org.milyn.event.types.DOMFilterLifecycleEvent;
import org.milyn.event.types.ElementPresentEvent;
import org.milyn.event.types.ElementVisitEvent;
import org.milyn.event.types.ResourceTargetingEvent;
import org.milyn.payload.FilterResult;
import org.milyn.payload.FilterSource;
import org.milyn.payload.JavaSource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Vector;

/**
 * Smooks DOM based content filtering class.
 * <p/>
 * This class is responsible for <b>Filtering</b> XML DOM streams
 * (XML/XHTML/HTML etc) through a process of iterating over the source XML DOM tree
 * and applying the {@link org.milyn.cdr.SmooksResourceConfiguration configured} Content Delivery Units
 * ({@link DOMElementVisitor DOMElementVisitors} and
 * {@link org.milyn.delivery.dom.serialize.SerializationUnit SerializationUnits}).
 * <p/>
 * This class doesn't get used directly.  See the {@link org.milyn.Smooks} class.
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
 * visiting all DOM elements that have {@link org.milyn.delivery.dom.VisitPhase ASSEMBLY} phase
 * {@link org.milyn.delivery.dom.DOMElementVisitor DOMElementVisitors}
 * {@link org.milyn.cdr.SmooksResourceConfiguration targeted} at them for the profile
 * associated with the {@link org.milyn.container.ExecutionContext}.
 * This phase can result in DOM elements being added to, or trimmed from, the DOM.
 * This phase is also very usefull for gathering data from the message in the DOM
 * (and storing it in the {@link org.milyn.container.ExecutionContext}),
 * which can be used during the processing phase (see below).  This phase is only
 * executed if there are
 * {@link DOMElementVisitor DOMElementVisitors} targeted at this phase.
 * </li>
 * <li>
 * <b>Processing</b>: Processing takes the assembled DOM and
 * iterates over it again, so as to perform transformation/analysis.
 * <p/>
 * This sub-phase involves iterating over the source XML DOM again,
 * visiting all DOM elements that have {@link org.milyn.delivery.dom.VisitPhase PROCESSING} phase
 * {@link org.milyn.delivery.dom.DOMElementVisitor DOMElementVisitors}
 * {@link org.milyn.cdr.SmooksResourceConfiguration targeted} at them for the profile
 * associated with the {@link org.milyn.container.ExecutionContext}.
 * This phase will only operate on DOM elements that were present in the assembled
 * document; {@link org.milyn.delivery.dom.DOMElementVisitor DOMElementVisitors} will not be applied
 * to elements that are introduced to the DOM during this phase.
 * </li>
 * </ul>
 * </li>
 * <li>
 * <b><u>Serialisation</u></b>: This phase is executed by the {@link #serialize(Node, Writer)} method (which uses the
 * {@link org.milyn.delivery.dom.serialize.Serializer} class).  The serialisation phase takes the processed DOM and
 * iterates over it to apply all {@link org.milyn.delivery.dom.serialize.SerializationUnit SerializationUnits},
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
 * <li>{@link org.milyn.Smooks}</li>
 * <li>{@link org.milyn.cdr.SmooksResourceConfiguration}</li>
 * <li>{@link org.milyn.cdr.SmooksResourceConfigurationSortComparator}</li>
 * </ul>
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class SmooksDOMFilter extends Filter {

    /**
     * Logger.
     */
    private Log logger = LogFactory.getLog(SmooksDOMFilter.class);
    /**
     * Container request for this Smooks content delivery instance.
     */
    private ExecutionContext executionContext;
    /**
     * Accessor to the ExecutionContext delivery config.
     */
    private DOMContentDeliveryConfig deliveryConfig;
    /**
     * Key under which a non-document content delivery node can be set in the
     * request.  This is needed because Xerces doesn't allow "overwriting" of
     * the document root node.
     */
    public static final String DELIVERY_NODE_REQUEST_KEY = ContentDeliveryConfig.class.getName() + "#DELIVERY_NODE_REQUEST_KEY";
    /**
     * Event Listener.
     */
    private ExecutionEventListener eventListener;
    private boolean closeSource;
    private boolean closeResult;
    private boolean reverseVisitOrderOnVisitAfter;
    private boolean terminateOnVisitorException;

    /**
     * Global assembly befores.
     */
    private List<ContentHandlerConfigMap<DOMVisitBefore>> globalAssemblyBefores;
    /**
     * Global assembly afters.
     */
    private List<ContentHandlerConfigMap<DOMVisitAfter>> globalAssemblyAfters;
    /**
     * Global process befores.
     */
    private List<ContentHandlerConfigMap<DOMVisitBefore>> globalProcessingBefores;
    /**
     * Global process afters.
     */
    private List<ContentHandlerConfigMap<DOMVisitAfter>> globalProcessingAfters;

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
        deliveryConfig = (DOMContentDeliveryConfig) executionContext.getDeliveryConfig();
        eventListener = executionContext.getEventListener();

        closeSource = ParameterAccessor.getBoolParameter(Filter.CLOSE_SOURCE, true, executionContext.getDeliveryConfig());
        closeResult = ParameterAccessor.getBoolParameter(Filter.CLOSE_RESULT, true, executionContext.getDeliveryConfig());
        reverseVisitOrderOnVisitAfter = ParameterAccessor.getBoolParameter(Filter.REVERSE_VISIT_ORDER_ON_VISIT_AFTER, true, executionContext.getDeliveryConfig());
        if (!(executionContext.getEventListener() instanceof AbstractReportGenerator)) {
            terminateOnVisitorException = ParameterAccessor.getBoolParameter(Filter.TERMINATE_ON_VISITOR_EXCEPTION, true, executionContext.getDeliveryConfig());
        } else {
            terminateOnVisitorException = false;
        }
    }

    public void doFilter() throws SmooksException {
        Source source = FilterSource.getSource(executionContext);
        Result result;

        result = FilterResult.getResult(executionContext, StreamResult.class);
        if (result == null) {
            // Maybe there's a DOMResult...
            result = FilterResult.getResult(executionContext, DOMResult.class);
        }

        doFilter(source, result);
    }

    protected void doFilter(Source source, Result result) {
        if (!(source instanceof StreamSource) && !(source instanceof DOMSource) && !(source instanceof JavaSource)) {
            throw new IllegalArgumentException(source.getClass().getName() + " Source types not yet supported by the DOM Filter.");
        }
        if (!(result instanceof FilterResult)) {
            if (result != null && !(result instanceof StreamResult) && !(result instanceof DOMResult)) {
                throw new IllegalArgumentException(result.getClass().getName() + " Result types not yet supported by the DOM Filter.");
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

            // Populate the Result
            if (result instanceof StreamResult) {
                StreamResult streamResult = ((StreamResult) result);
                Writer writer = getWriter(streamResult, executionContext);

                try {
                    serialize(resultNode, writer);
                    writer.flush();
                } catch (IOException e) {
                    logger.debug("Error writing result to output stream.", e);
                }
            } else if (result instanceof DOMResult) {
                ((DOMResult) result).setNode(resultNode);
            }
        } finally {
            if (closeSource) {
                close(source);
            }
            if (closeResult) {
                close(result);
            }
        }
    }

    public void cleanup() {
    }

    /**
     * Phase the supplied input reader.
     * <p/>
     * Simply parses the input reader into a W3C DOM and calls {@link #filter(Document)}.
     *
     * @param source The source of markup to be filtered.
     * @return Node representing filtered document.
     * @throws SmooksException
     */
    public Node filter(Source source) throws SmooksException {
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
            logger.debug("Empty Document [" + executionContext.getDocumentSource() + "].  Not performaing any processing.");
            return doc;
        }

        deliveryNode = filter(doc.getDocumentElement());
        if (deliveryNode == null) {
            deliveryNode = doc;
        }

        return deliveryNode;
    }

    private static String[] GLOBAL_SELECTORS = new String[]{"*", "**"};

    /**
     * Filter the supplied W3C Element.
     * <p/>
     * Executes the <a href="#phases">Assembly &amp Processing phases</a>.
     *
     * @param element The W3C Element to be filtered.
     * @return Node representing filtered Element.
     */
    public Node filter(Element element) {
        ContentHandlerConfigMapTable<DOMVisitBefore> visitBefores = deliveryConfig.getAssemblyVisitBefores();
        ContentHandlerConfigMapTable<DOMVisitAfter> visitAfters = deliveryConfig.getAssemblyVisitAfters();
        globalAssemblyBefores = visitBefores.getMappings(GLOBAL_SELECTORS);
        globalAssemblyAfters = visitAfters.getMappings(GLOBAL_SELECTORS);

        // Register the DOM phase events...
        if (eventListener != null) {
            eventListener.onEvent(new DOMFilterLifecycleEvent(DOMFilterLifecycleEvent.DOMEventType.ASSEMBLY_STARTED));
        }

        // Apply assembly phase, skipping it if there are no configured assembly units...
        if (applyAssembly(visitBefores, visitAfters)) {
            // Assemble
            if (logger.isDebugEnabled()) {
                logger.debug("Starting assembly phase [" + executionContext.getTargetProfiles().getBaseProfile() + "]");
            }
            assemble(element, true);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("No assembly units configured for device [" + executionContext.getTargetProfiles().getBaseProfile() + "]");
            }
        }

        // Register the DOM phase events...
        if (eventListener != null) {
            eventListener.onEvent(new DOMFilterLifecycleEvent(DOMFilterLifecycleEvent.DOMEventType.PROCESSING_STARTED));
        }

        // Apply processing phase...
        if (logger.isDebugEnabled()) {
            logger.debug("Starting processing phase [" + executionContext.getTargetProfiles().getBaseProfile() + "]");
        }

        globalProcessingBefores = deliveryConfig.getProcessingVisitBefores().getMappings(GLOBAL_SELECTORS);
        if (globalProcessingBefores != null && globalProcessingBefores.isEmpty()) {
            globalProcessingBefores = null;
        }
        globalProcessingAfters = deliveryConfig.getProcessingVisitAfters().getMappings(GLOBAL_SELECTORS);
        if (globalProcessingAfters != null && globalProcessingAfters.isEmpty()) {
            globalProcessingAfters = null;
        }

        int transListLength;
        Vector transList = new Vector();

        buildProcessingList(transList, element, true);
        transListLength = transList.size();
        for (int i = 0; i < transListLength; i++) {
            ElementProcessor elementTrans = (ElementProcessor) transList.get(i);
            elementTrans.process(executionContext);
        }

        return (Node) executionContext.getAttribute(DELIVERY_NODE_REQUEST_KEY);
    }

    /**
     * Get the global mappings from the supplied handler table.
     *
     * @param assemblyVisitBefores The handler table.
     * @return A handler config map list containing the merged
     */
    private List<ContentHandlerConfigMap<? extends ContentHandler>> getGlobalConfigs(ContentHandlerConfigMapTable<? extends ContentHandler> assemblyVisitBefores) {
        // TODO Auto-generated method stub
        return null;
    }

    private boolean applyAssembly(ContentHandlerConfigMapTable<DOMVisitBefore> visitBefores, ContentHandlerConfigMapTable<DOMVisitAfter> visitAfters) {
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
        List nodeListCopy = copyList(element.getChildNodes());
        int childCount = nodeListCopy.size();
        ContentHandlerConfigMapTable<DOMVisitBefore> visitBeforeTable = deliveryConfig.getAssemblyVisitBefores();
        ContentHandlerConfigMapTable<DOMVisitAfter> visitAfterTable = deliveryConfig.getAssemblyVisitAfters();
        String elementName = DomUtils.getName(element);

        // Register the "presence" of the element...
        if (eventListener != null) {
            eventListener.onEvent(new ElementPresentEvent(element));
        }

        List<ContentHandlerConfigMap<DOMVisitBefore>> elementVisitBefores;
        List<ContentHandlerConfigMap<DOMVisitAfter>> elementVisitAfters;
        if (isRoot) {
            // The document as a whole (root node) can also be targeted through the "#document" selector.
            elementVisitBefores = visitBeforeTable.getMappings(new String[]{SmooksResourceConfiguration.DOCUMENT_FRAGMENT_SELECTOR, elementName});
            elementVisitAfters = visitAfterTable.getMappings(new String[]{SmooksResourceConfiguration.DOCUMENT_FRAGMENT_SELECTOR, elementName});
        } else {
            elementVisitBefores = visitBeforeTable.getMappings(elementName);
            elementVisitAfters = visitAfterTable.getMappings(elementName);
        }

        // Visit element with its assembly units before visiting its child content.
        if (elementVisitBefores != null && !elementVisitBefores.isEmpty()) {
            applyAssemblyBefores(element, elementVisitBefores);
        }
        if (globalAssemblyBefores != null && !globalAssemblyBefores.isEmpty()) {
            applyAssemblyBefores(element, globalAssemblyBefores);
        }

        // Recursively iterate the elements child content...
        for (int i = 0; i < childCount; i++) {
            Node child = (Node) nodeListCopy.get(i);
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

    private void applyAssemblyBefores(Element element, List<ContentHandlerConfigMap<DOMVisitBefore>> assemblyBefores) {
        for (int i = 0; i < assemblyBefores.size(); i++) {
            ContentHandlerConfigMap<DOMVisitBefore> configMap = assemblyBefores.get(i);
            SmooksResourceConfiguration config = configMap.getResourceConfig();

            // Make sure the assembly unit is targeted at this element...
            if (!config.isTargetedAtElement(element, executionContext)) {
                continue;
            }

            // Register the targeting event.  No need to register it again in the visitAfter loop...
            if (eventListener != null) {
                eventListener.onEvent(new ResourceTargetingEvent(element, config, VisitSequence.BEFORE, VisitPhase.ASSEMBLY));
            }

            DOMVisitBefore assemblyUnit = configMap.getContentHandler();
            try {
                if (logger.isDebugEnabled()) {
                    logger.debug("(Assembly) Calling visitBefore on element [" + DomUtils.getXPath(element) + "]. Config [" + config + "]");
                }
                assemblyUnit.visitBefore(element, executionContext);
                if (eventListener != null) {
                    eventListener.onEvent(new ElementVisitEvent(element, configMap, VisitSequence.BEFORE));
                }
            } catch (Throwable e) {
                String errorMsg = "(Assembly) visitBefore failed [" + assemblyUnit.getClass().getName() + "] on [" + executionContext.getDocumentSource() + ":" + DomUtils.getXPath(element) + "].";
                processVisitorException(element, e, configMap, VisitSequence.BEFORE, errorMsg);
            }
        }
    }

    private void applyAssemblyAfters(Element element, List<ContentHandlerConfigMap<DOMVisitAfter>> elementVisitAfters) {
        if (reverseVisitOrderOnVisitAfter) {
            for (int i = elementVisitAfters.size() - 1; i >= 0; i--) {
                ContentHandlerConfigMap<DOMVisitAfter> configMap = elementVisitAfters.get(i);
                applyAssemblyAfter(element, configMap);
            }
        } else {
            for (int i = 0; i < elementVisitAfters.size(); i++) {
                ContentHandlerConfigMap<DOMVisitAfter> configMap = elementVisitAfters.get(i);
                applyAssemblyAfter(element, configMap);
            }
        }
    }

    private void applyAssemblyAfter(Element element, ContentHandlerConfigMap<DOMVisitAfter> configMap) {
        SmooksResourceConfiguration config = configMap.getResourceConfig();

        // Make sure the assembly unit is targeted at this element...
        if (!config.isTargetedAtElement(element, executionContext)) {
            return;
        }

        DOMVisitAfter visitAfter = configMap.getContentHandler();
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("(Assembly) Calling visitAfter on element [" + DomUtils.getXPath(element) + "]. Config [" + config + "]");
            }
            visitAfter.visitAfter(element, executionContext);
            if (eventListener != null) {
                eventListener.onEvent(new ElementVisitEvent(element, configMap, VisitSequence.AFTER));
            }
        } catch (Throwable e) {
            String errorMsg = "(Assembly) visitAfter failed [" + visitAfter.getClass().getName() + "] on [" + executionContext.getDocumentSource() + ":" + DomUtils.getXPath(element) + "].";
            processVisitorException(element, e, configMap, VisitSequence.AFTER, errorMsg);
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
    private void buildProcessingList(List processingList, Element element, boolean isRoot) {
        String elementName;
        List<ContentHandlerConfigMap<DOMVisitBefore>> processingBefores;
        List<ContentHandlerConfigMap<DOMVisitAfter>> processingAfters;
        List<ContentHandlerConfigMap<VisitLifecycleCleanable>> processingCleanables;

        // Register the "presence" of the element...
        if (eventListener != null) {
            eventListener.onEvent(new ElementPresentEvent(element));
        }

        elementName = DomUtils.getName(element);
        if (isRoot) {
            // The document as a whole (root node) can also be targeted through the "#document" selector.
            processingBefores = deliveryConfig.getProcessingVisitBefores().getMappings(new String[]{SmooksResourceConfiguration.DOCUMENT_FRAGMENT_SELECTOR, elementName});
            processingAfters = deliveryConfig.getProcessingVisitAfters().getMappings(new String[]{SmooksResourceConfiguration.DOCUMENT_FRAGMENT_SELECTOR, elementName});
            processingCleanables = deliveryConfig.getVisitCleanables().getMappings(new String[]{SmooksResourceConfiguration.DOCUMENT_FRAGMENT_SELECTOR, elementName});
        } else {
            processingBefores = deliveryConfig.getProcessingVisitBefores().getMappings(elementName);
            processingAfters = deliveryConfig.getProcessingVisitAfters().getMappings(elementName);
            processingCleanables = deliveryConfig.getVisitCleanables().getMappings(elementName);
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

        // Iterate over the child elements, calling this method recurcively....
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

        if (processingCleanables != null && !processingCleanables.isEmpty()) {
            ElementProcessor processor = new ElementProcessor(element);
            processor.setVisitCleanable(processingCleanables);
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
     * @throws ResourceConfigurationNotFoundException
     *                         DOM Serialiser exception.
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

        if (logger.isDebugEnabled()) {
            logger.debug("Starting serialization phase [" + executionContext.getTargetProfiles().getBaseProfile() + "]");
        }
        serializer = new Serializer(node, executionContext);
        try {
            serializer.serailize(writer);
        } catch (ResourceConfigurationNotFoundException e) {
            throw new SmooksException("Unable to serialize document.", e);
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
    private List copyList(NodeList nodeList) {
        Vector copy = new Vector(nodeList.getLength());
        int nodeCount = nodeList.getLength();

        for (int i = 0; i < nodeCount; i++) {
            copy.add(nodeList.item(i));
        }

        return copy;
    }

    /**
     * Element Prcessor class.
     * <p/>
     * Simple DOM Element to ProcessingUnit[] processing mapping and support functions.
     *
     * @author tfennelly
     */
    private class ElementProcessor {
        /**
         * The Element instance to be processed.
         */
        private Element element;

        private List<ContentHandlerConfigMap<DOMVisitBefore>> visitBefores;
        private List<ContentHandlerConfigMap<DOMVisitAfter>> visitAfters;
        private List<ContentHandlerConfigMap<VisitLifecycleCleanable>> visitCleanable;

        /**
         * Constructor.
         *
         * @param element Element to be processed.
         */
        private ElementProcessor(Element element) {
            this.element = element;
        }

        private void setVisitBefores(List<ContentHandlerConfigMap<DOMVisitBefore>> visitBefores) {
            this.visitBefores = visitBefores;
        }

        private void setVisitAfters(List<ContentHandlerConfigMap<DOMVisitAfter>> visitAfters) {
            this.visitAfters = visitAfters;
        }

        public void setVisitCleanable(List<ContentHandlerConfigMap<VisitLifecycleCleanable>> visitCleanable) {
            this.visitCleanable = visitCleanable;
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
                int loopLength = visitBefores.size();
                for (int i = 0; i < loopLength; i++) {
                    ContentHandlerConfigMap configMap = visitBefores.get(i);
                    processMapping(executionContext, configMap, VisitSequence.BEFORE);
                }
            } else if (visitAfters != null) {
                int loopLength = visitAfters.size();
                if (reverseVisitOrderOnVisitAfter) {
                    for (int i = loopLength - 1; i >= 0; i--) {
                        ContentHandlerConfigMap configMap = visitAfters.get(i);
                        processMapping(executionContext, configMap, VisitSequence.AFTER);
                    }
                } else {
                    for (int i = 0; i < loopLength; i++) {
                        ContentHandlerConfigMap configMap = visitAfters.get(i);
                        processMapping(executionContext, configMap, VisitSequence.AFTER);
                    }
                }
            } else {
                int loopLength = visitCleanable.size();
                for (int i = 0; i < loopLength; i++) {
                    ContentHandlerConfigMap configMap = visitCleanable.get(i);
                    processMapping(executionContext, configMap, VisitSequence.CLEAN);
                }
            }
        }

        private void processMapping(ExecutionContext executionContext, ContentHandlerConfigMap configMap, VisitSequence visitSequence) {
            SmooksResourceConfiguration config = configMap.getResourceConfig();

            // Make sure the processing unit is targeted at this element...
            if (!config.isTargetedAtElement(element, executionContext)) {
                return;
            }

            // Could add an "is-element-in-document-tree" check here
            // but might not be valid.  Also, this check
            // would need to iterate back up to the document root
            // every time. Doing this for every element could be very
            // costly.

            if (visitSequence == VisitSequence.BEFORE) {
                // Register the targeting event...
                if (eventListener != null) {
                    eventListener.onEvent(new ResourceTargetingEvent(element, config, VisitSequence.BEFORE));
                }

                DOMVisitBefore visitor = (DOMVisitBefore) configMap.getContentHandler();
                try {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Applying processing resource [" + config + "] to element [" + DomUtils.getXPath(element) + "] before applying resources to its child elements.");
                    }
                    visitor.visitBefore(element, executionContext);
                    if (eventListener != null) {
                        eventListener.onEvent(new ElementVisitEvent(element, configMap, VisitSequence.BEFORE));
                    }
                } catch (Throwable e) {
                    String errorMsg = "Failed to apply processing unit [" + visitor.getClass().getName() + "] to [" + executionContext.getDocumentSource() + ":" + DomUtils.getXPath(element) + "].";
                    processVisitorException(element, e, configMap, VisitSequence.BEFORE, errorMsg);
                }
            } else if (visitSequence == VisitSequence.AFTER) {
                // Register the targeting event...
                if (eventListener != null) {
                    eventListener.onEvent(new ResourceTargetingEvent(element, config, VisitSequence.AFTER));
                }

                DOMVisitAfter visitor = (DOMVisitAfter) configMap.getContentHandler();
                try {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Applying processing resource [" + config + "] to element [" + DomUtils.getXPath(element) + "] after applying resources to its child elements.");
                    }
                    visitor.visitAfter(element, executionContext);
                    if (eventListener != null) {
                        eventListener.onEvent(new ElementVisitEvent(element, configMap, VisitSequence.AFTER));
                    }
                } catch (Throwable e) {
                    String errorMsg = "Failed to apply processing unit [" + visitor.getClass().getName() + "] to [" + executionContext.getDocumentSource() + ":" + DomUtils.getXPath(element) + "].";
                    processVisitorException(element, e, configMap, VisitSequence.BEFORE, errorMsg);
                }
            } else if (visitSequence == VisitSequence.CLEAN) {
                // Register the targeting event...
                if (eventListener != null) {
                    eventListener.onEvent(new ResourceTargetingEvent(element, config, VisitSequence.CLEAN));
                }

                ContentHandler contentHandler = configMap.getContentHandler();
                if (contentHandler instanceof VisitLifecycleCleanable) {
                    VisitLifecycleCleanable visitor = (VisitLifecycleCleanable) contentHandler;
                    try {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Cleaning up processing resource [" + config + "] that was targeted to element [" + DomUtils.getXPath(element) + "].");
                        }
                        visitor.executeVisitLifecycleCleanup(new Fragment(element), executionContext);
                        if (eventListener != null) {
                            eventListener.onEvent(new ElementVisitEvent(element, configMap, VisitSequence.CLEAN));
                        }
                    } catch (Throwable e) {
                        String errorMsg = "Failed to clean up [" + visitor.getClass().getName() + "]. Targeted at [" + executionContext.getDocumentSource() + ":" + DomUtils.getXPath(element) + "].";
                        processVisitorException(element, e, configMap, VisitSequence.CLEAN, errorMsg);
                    }
                }
            }
        }
    }

    private void processVisitorException(Element element, Throwable error, ContentHandlerConfigMap configMapping, VisitSequence visitSequence, String errorMsg) throws SmooksException {
        if (eventListener != null) {
            eventListener.onEvent(new ElementVisitEvent(element, configMapping, visitSequence, error));
        }

        executionContext.setTerminationError(error);

        if (terminateOnVisitorException) {
            if (error instanceof SmooksException) {
                throw (SmooksException) error;
            } else {
                throw new SmooksException(errorMsg, error);
            }
        } else {
            logger.debug(errorMsg, error);
        }
    }
}
