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
package org.smooks.engine.resource.visitor.smooks;

import org.smooks.FilterSettings;
import org.smooks.Smooks;
import org.smooks.StreamFilterType;
import org.smooks.api.ApplicationContext;
import org.smooks.api.ApplicationContextBuilder;
import org.smooks.api.ExecutionContext;
import org.smooks.api.SmooksException;
import org.smooks.api.TypedKey;
import org.smooks.api.bean.repository.BeanId;
import org.smooks.api.delivery.Filter;
import org.smooks.api.delivery.fragment.Fragment;
import org.smooks.api.delivery.ordering.Producer;
import org.smooks.api.lifecycle.PreExecutionLifecycle;
import org.smooks.api.memento.MementoCaretaker;
import org.smooks.api.resource.config.ResourceConfig;
import org.smooks.api.resource.config.ResourceConfigSeq;
import org.smooks.api.resource.visitor.sax.ng.AfterVisitor;
import org.smooks.api.resource.visitor.sax.ng.BeforeVisitor;
import org.smooks.assertion.AssertArgument;
import org.smooks.engine.DefaultApplicationContextBuilder;
import org.smooks.engine.delivery.fragment.NodeFragment;
import org.smooks.engine.memento.SimpleVisitorMemento;
import org.smooks.engine.memento.VisitorMemento;
import org.smooks.engine.resource.config.DefaultResourceConfig;
import org.smooks.engine.resource.config.DefaultResourceConfigSeq;
import org.smooks.engine.resource.config.ParameterAccessor;
import org.smooks.engine.resource.config.SystemResourceConfigSeqFactory;
import org.smooks.engine.resource.visitor.dom.DOMModel;
import org.smooks.engine.xml.Namespace;
import org.smooks.io.DomSerializer;
import org.smooks.io.FragmentWriter;
import org.smooks.io.ResourceWriter;
import org.smooks.io.Stream;
import org.smooks.io.sink.WriterSink;
import org.smooks.io.source.DOMSource;
import org.smooks.support.XmlUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Optional;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;

public class NestedSmooksVisitor implements BeforeVisitor, AfterVisitor, Producer, PreExecutionLifecycle {

    public enum Action {
        REPLACE,
        PREPEND_BEFORE,
        PREPEND_AFTER,
        APPEND_BEFORE,
        APPEND_AFTER,
        BIND_TO,
        OUTPUT_TO
    }

    protected static final TypedKey<Node> SOURCE_BRIDGE_TYPED_KEY = TypedKey.of();
    protected static final TypedKey<DocumentBuilder> CACHED_DOCUMENT_BUILDER_TYPED_KEY = TypedKey.of();
    protected static final TypedKey<ExecutionContext> NESTED_EXECUTION_CONTEXT_MEMENTO_TYPED_KEY = TypedKey.of();

    protected BeanId bindBeanId;

    protected Action action;

    @Inject
    @Named("action")
    protected Optional<Action> actionOptional;

    @Inject
    @Named("bindId")
    protected Optional<String> bindIdOptional;

    @Inject
    @Named("outputStreamResource")
    protected Optional<String> outputStreamResourceOptional;

    @Inject
    protected ResourceConfig resourceConfig;

    @Inject
    protected Integer maxNodeDepth = 1;

    @Inject
    protected ApplicationContext applicationContext;

    @Inject
    @Named(Filter.ENTITIES_REWRITE)
    protected Boolean rewriteEntities = true;

    protected ResourceConfigSeq resourceConfigSeq;
    protected Smooks nestedSmooks;
    protected DomSerializer domSerializer;

    @PostConstruct
    public void postConstruct() throws SAXException, IOException, URISyntaxException, ClassNotFoundException {
        if (nestedSmooks == null) {
            if (!resourceConfig.getParameters("smooksResourceList").isEmpty()) {
                ByteArrayInputStream smooksResourceList = new ByteArrayInputStream(resourceConfig.getParameter("smooksResourceList", String.class).getValue().getBytes());
                resourceConfigSeq = applicationContext.getResourceConfigLoader().load(smooksResourceList, "./", applicationContext.getClassLoader());
            } else {
                ResourceConfig resourceConfig = new DefaultResourceConfig("*", new Properties());
                resourceConfig.setResource("org.smooks.engine.delivery.sax.ng.SimpleSerializerVisitor");
                resourceConfigSeq = new DefaultResourceConfigSeq("./");
                resourceConfigSeq.add(resourceConfig);
            }
            ApplicationContextBuilder applicationContextBuilder = ServiceLoader.load(ApplicationContextBuilder.class).iterator().next();
            if (applicationContextBuilder instanceof DefaultApplicationContextBuilder) {
                applicationContextBuilder = ((DefaultApplicationContextBuilder) applicationContextBuilder).withSystemResources(false);
            }
            nestedSmooks = new Smooks(applicationContextBuilder.withClassLoader(applicationContext.getClassLoader()).withResourceLocator(applicationContext.getResourceLocator()).build());
            for (ResourceConfig resourceConfig : resourceConfigSeq) {
                nestedSmooks.addResourceConfig(resourceConfig);
            }
        }

        nestedSmooks.getApplicationContext().getRegistry().registerResourceConfigSeq(new SystemResourceConfigSeqFactory("/nested-smooks-interceptors.xml",
                nestedSmooks.getApplicationContext().getClassLoader(), nestedSmooks.getApplicationContext().getResourceLocator(), applicationContext.getResourceConfigLoader()).create());
        nestedSmooks.setFilterSettings(new FilterSettings(StreamFilterType.SAX_NG).setCloseSink(false).setReaderPoolSize(-1).setMaxNodeDepth(maxNodeDepth == 0 ? Integer.MAX_VALUE : maxNodeDepth));

        action = actionOptional.orElse(null);
        if (action != null) {
            if (action == Action.BIND_TO) {
                AssertArgument.isNotNull(bindIdOptional.orElse(null), "bindId");
                bindBeanId = this.applicationContext.getBeanIdStore().register(bindIdOptional.get());
            } else if (action == Action.OUTPUT_TO) {
                AssertArgument.isNotNull(outputStreamResourceOptional.orElse(null), "outputStreamResource");
            }
        }

        domSerializer = new DomSerializer(false, rewriteEntities);
    }

    @Override
    public void onPreExecution(final ExecutionContext executionContext) {
        final DocumentBuilder documentBuilder;
        try {
            documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new SmooksException(e);
        }
        executionContext.put(CACHED_DOCUMENT_BUILDER_TYPED_KEY, documentBuilder);
    }

    protected Node deAttach(final Node node, ExecutionContext executionContext) {
        final Document document = executionContext.get(CACHED_DOCUMENT_BUILDER_TYPED_KEY).newDocument();
        document.setStrictErrorChecking(false);
        final Node copyNode = document.importNode(node, true);
        document.appendChild(copyNode);

        return copyNode;
    }

    @Override
    public void visitBefore(final Element element, final ExecutionContext executionContext) {
        final Node rootNode = deAttach(element, executionContext);
        final NodeFragment visitedFragment = new NodeFragment(element);
        executionContext.getMementoCaretaker().capture(new SimpleVisitorMemento<>(visitedFragment, this, rootNode));

        final NodeFragment rootNodeFragment = new NodeFragment(rootNode);
        final Writer nodeWriter;
        if (action == null) {
            filterSource(visitedFragment, rootNodeFragment, Stream.out(executionContext), executionContext, "visitBefore");
            nodeWriter = Stream.out(executionContext);
        } else {
            if (action == Action.OUTPUT_TO) {
                final ResourceWriter resourceWriter = new ResourceWriter(executionContext, outputStreamResourceOptional.get());
                if (resourceWriter.getDelegateWriter() == null) {
                    filterSource(visitedFragment, rootNodeFragment, null, executionContext, "visitBefore");
                    nodeWriter = null;
                } else {
                    executionContext.getMementoCaretaker().capture(new SimpleVisitorMemento<>(rootNodeFragment, this, resourceWriter));
                    filterSource(visitedFragment, rootNodeFragment, resourceWriter, executionContext, "visitBefore");
                    nodeWriter = resourceWriter.getDelegateWriter();
                }
            } else {
                if (action == Action.PREPEND_BEFORE || action == Action.PREPEND_AFTER) {
                    nodeWriter = prependBefore(visitedFragment, action, (Element) rootNodeFragment.unwrap(), executionContext);
                } else if (action == Action.APPEND_BEFORE || action == Action.APPEND_AFTER) {
                    final FragmentWriter fragmentWriter = new FragmentWriter(executionContext, rootNodeFragment);
                    if (executionContext.getContentDeliveryRuntime().getContentDeliveryConfig().isDefaultSerializationOn()) {
                        try {
                            domSerializer.writeStartElement(element, fragmentWriter);
                        } catch (IOException e) {
                            throw new SmooksException(e);
                        }
                    }
                    executionContext.getMementoCaretaker().capture(new SimpleVisitorMemento<>(rootNodeFragment, this, fragmentWriter));
                    filterSource(visitedFragment, rootNodeFragment, fragmentWriter, executionContext, "visitBefore");
                    nodeWriter = fragmentWriter;
                } else if (action == Action.REPLACE) {
                    nodeWriter = replaceBefore(visitedFragment, rootNodeFragment.unwrap(), executionContext);
                } else if (action == Action.BIND_TO) {
                    nodeWriter = new StringWriter();
                    executionContext.getMementoCaretaker().capture(new SimpleVisitorMemento<>(rootNodeFragment, this, nodeWriter));
                    filterSource(visitedFragment, rootNodeFragment, nodeWriter, executionContext, "visitBefore");
                } else {
                    throw new UnsupportedOperationException();
                }
            }
        }

        final ChildEventListener childEventListener = new ChildEventListener(this, nodeWriter, visitedFragment, executionContext);
        executionContext.getContentDeliveryRuntime().addExecutionEventListener(childEventListener);
        executionContext.getMementoCaretaker().capture(new SimpleVisitorMemento<>(visitedFragment, this, childEventListener));
    }

    @Override
    public void visitAfter(final Element element, final ExecutionContext executionContext) {
        final NodeFragment visitedFragment = new NodeFragment(element);
        final VisitorMemento<Node> rootNodeMemento = new SimpleVisitorMemento<>(visitedFragment, this, element);
        executionContext.getMementoCaretaker().restore(rootNodeMemento);
        final NodeFragment rootNodeFragment = new NodeFragment(rootNodeMemento.getState());

        if (action == null) {
            filterSource(visitedFragment, rootNodeFragment, Stream.out(executionContext), executionContext, "visitAfter");
        } else {
            if (action == Action.OUTPUT_TO) {
                final ResourceWriter resourceWriter = executionContext.getMementoCaretaker().stash(new SimpleVisitorMemento<>(rootNodeFragment, this, new ResourceWriter(executionContext, outputStreamResourceOptional.get())), resourceWriterMemento -> resourceWriterMemento).getState();
                filterSource(visitedFragment, rootNodeFragment, resourceWriter, executionContext, "visitAfter");
            } else {
                if (action == Action.APPEND_BEFORE || action == Action.APPEND_AFTER) {
                    append(visitedFragment, (Element) rootNodeFragment.unwrap(), action, executionContext);
                } else if (action == Action.PREPEND_BEFORE || action == Action.PREPEND_AFTER) {
                    prependAfter(visitedFragment, (Element) rootNodeFragment.unwrap(), executionContext);
                } else if (action == Action.REPLACE) {
                    replaceAfter(visitedFragment, rootNodeFragment.unwrap(), executionContext);
                } else if (action == Action.BIND_TO) {
                    final VisitorMemento<StringWriter> memento = new SimpleVisitorMemento<>(rootNodeFragment, this, new StringWriter());
                    executionContext.getMementoCaretaker().restore(memento);
                    filterSource(visitedFragment, rootNodeFragment, memento.getState(), executionContext, "visitAfter");
                    executionContext.getBeanContext().addBean(bindBeanId, memento.getState().toString(), rootNodeFragment);
                } else {
                    throw new UnsupportedOperationException();
                }
            }
        }

        final VisitorMemento<ChildEventListener> childEventListenerMemento = new SimpleVisitorMemento<>(visitedFragment, this, new ChildEventListener(this, null, null, null));
        executionContext.getMementoCaretaker().restore(childEventListenerMemento);
        executionContext.getContentDeliveryRuntime().removeExecutionEventListener(childEventListenerMemento.getState());
    }

    protected Writer replaceBefore(final Fragment<Node> visitedNodeFragment, final Node rootNode, final ExecutionContext executionContext) {
        final NodeFragment rootNodeFragment = new NodeFragment(rootNode, true);
        final FragmentWriter fragmentWriter = new FragmentWriter(executionContext, rootNodeFragment, false);
        try {
            fragmentWriter.park();
        } catch (IOException e) {
            throw new SmooksException(e);
        }
        executionContext.getMementoCaretaker().capture(new SimpleVisitorMemento<>(rootNodeFragment, this, fragmentWriter));
        filterSource(visitedNodeFragment, rootNodeFragment, fragmentWriter, executionContext, "visitBefore");

        return fragmentWriter;
    }

    protected void replaceAfter(final Fragment<Node> visitedNodeFragment, final Node rootNode, final ExecutionContext executionContext) {
        final NodeFragment rootNodeFragment = new NodeFragment(rootNode);
        final VisitorMemento<FragmentWriter> fragmentWriterVisitorMemento = new SimpleVisitorMemento<>(rootNodeFragment, this, new FragmentWriter(executionContext, new NodeFragment(rootNode)));
        executionContext.getMementoCaretaker().restore(fragmentWriterVisitorMemento);

        filterSource(visitedNodeFragment, rootNodeFragment, fragmentWriterVisitorMemento.getState(), executionContext, "visitAfter");
    }

    protected Writer prependBefore(final Fragment<Node> visitedNodeFragment, final Action action, final Element rootElement, final ExecutionContext executionContext) {
        final NodeFragment rootNodeFragment = new NodeFragment(rootElement, true);
        final FragmentWriter fragmentWriter = new FragmentWriter(executionContext, rootNodeFragment, false);
        try {
            fragmentWriter.park();
            if (executionContext.getContentDeliveryRuntime().getContentDeliveryConfig().isDefaultSerializationOn() && action == Action.PREPEND_AFTER) {
                domSerializer.writeStartElement(rootElement, fragmentWriter);
            }
            executionContext.getMementoCaretaker().capture(new SimpleVisitorMemento<>(rootNodeFragment, this, fragmentWriter));
            filterSource(visitedNodeFragment, rootNodeFragment, fragmentWriter, executionContext, "visitBefore");
        } catch (IOException e) {
            throw new SmooksException(e);
        }

        return fragmentWriter;
    }

    protected void prependAfter(final Fragment<Node> visitedNodeFragment, final Element rootElement, final ExecutionContext executionContext) {
        final NodeFragment rootNodeFragment = new NodeFragment(rootElement);
        final VisitorMemento<FragmentWriter> fragmentWriterMemento = new SimpleVisitorMemento<>(rootNodeFragment, this, new FragmentWriter(executionContext, rootNodeFragment));
        executionContext.getMementoCaretaker().restore(fragmentWriterMemento);
        filterSource(visitedNodeFragment, rootNodeFragment, fragmentWriterMemento.getState(), executionContext, "visitAfter");
        try {
            if (executionContext.getContentDeliveryRuntime().getContentDeliveryConfig().isDefaultSerializationOn() && action == Action.PREPEND_BEFORE) {
                domSerializer.writeStartElement(rootElement, fragmentWriterMemento.getState());
            }
            if (executionContext.getContentDeliveryRuntime().getContentDeliveryConfig().isDefaultSerializationOn()) {
                fragmentWriterMemento.getState().write(XmlUtils.serialize(rootElement.getChildNodes(), Boolean.parseBoolean(ParameterAccessor.getParameterValue(Filter.CLOSE_EMPTY_ELEMENTS, String.class, "false", executionContext.getContentDeliveryRuntime().getContentDeliveryConfig()))));
                domSerializer.writeEndElement(rootElement, fragmentWriterMemento.getState());
            }
        } catch (IOException e) {
            throw new SmooksException(e);
        }
    }

    protected void append(final Fragment<Node> visitedNodeFragment, final Element rootElement, final Action action, final ExecutionContext executionContext) {
        final NodeFragment rootNodeFragment = new NodeFragment(rootElement);
        final VisitorMemento<FragmentWriter> fragmentWriterMemento = new SimpleVisitorMemento<>(rootNodeFragment, this, new FragmentWriter(executionContext, rootNodeFragment));
        executionContext.getMementoCaretaker().restore(fragmentWriterMemento);

        try {
            if (executionContext.getContentDeliveryRuntime().getContentDeliveryConfig().isDefaultSerializationOn() && action == Action.APPEND_AFTER) {
                domSerializer.writeEndElement(rootElement, fragmentWriterMemento.getState());
            }
            filterSource(visitedNodeFragment, rootNodeFragment, fragmentWriterMemento.getState(), executionContext, "visitAfter");
            if (executionContext.getContentDeliveryRuntime().getContentDeliveryConfig().isDefaultSerializationOn() && action == Action.APPEND_BEFORE) {
                domSerializer.writeEndElement(rootElement, fragmentWriterMemento.getState());
            }
        } catch (IOException e) {
            throw new SmooksException(e);
        }
    }

    protected void filterSource(final Fragment<Node> visitedNodeFragment, final Fragment<Node> rootNodeFragment, final Writer writer, final ExecutionContext executionContext, final String visit) {
        final VisitorMemento<ExecutionContext> nestedExecutionContextMemento;
        final MementoCaretaker mementoCaretaker = executionContext.getMementoCaretaker();
        if (mementoCaretaker.exists(new VisitorMemento<>(visitedNodeFragment, this, NESTED_EXECUTION_CONTEXT_MEMENTO_TYPED_KEY))) {
            nestedExecutionContextMemento = new VisitorMemento<>(visitedNodeFragment, this, NESTED_EXECUTION_CONTEXT_MEMENTO_TYPED_KEY);
            mementoCaretaker.restore(nestedExecutionContextMemento);
        } else {
            final ExecutionContext nestedExecutionContext = nestedSmooks.createExecutionContext();
            nestedExecutionContext.setContentEncoding(executionContext.getContentEncoding());
            nestedExecutionContext.setBeanContext(executionContext.getBeanContext().newSubContext(nestedExecutionContext));
            nestedExecutionContext.put(DOMModel.DOM_MODEL_TYPED_KEY, DOMModel.getModel(executionContext));
            nestedExecutionContextMemento = new VisitorMemento<>(visitedNodeFragment, this, NESTED_EXECUTION_CONTEXT_MEMENTO_TYPED_KEY, nestedExecutionContext);
            mementoCaretaker.capture(nestedExecutionContextMemento);
        }

        final Document document = executionContext.get(CACHED_DOCUMENT_BUILDER_TYPED_KEY).newDocument();
        document.setStrictErrorChecking(false);
        final Element smooksBridgeElement = document.createElementNS(Namespace.SMOOKS_URI, "bridge");
        smooksBridgeElement.setAttribute("visit", visit);
        smooksBridgeElement.setAttribute("source", SOURCE_BRIDGE_TYPED_KEY.getName());
        document.appendChild(smooksBridgeElement);

        nestedExecutionContextMemento.getState().put(SOURCE_BRIDGE_TYPED_KEY, rootNodeFragment.unwrap());
        if (writer == null) {
            nestedSmooks.filterSource(nestedExecutionContextMemento.getState(), new DOMSource(document));
        } else {
            nestedSmooks.filterSource(nestedExecutionContextMemento.getState(), new DOMSource(document), new WriterSink(writer));
        }
    }

    public int getMaxNodeDepth() {
        if (action != null && (action.equals(Action.PREPEND_BEFORE) || action.equals(Action.PREPEND_AFTER))) {
            return Integer.MAX_VALUE;
        } else {
            return maxNodeDepth == 0 ? Integer.MAX_VALUE : maxNodeDepth;
        }
    }

    @Override
    public Set<String> getProducts() {
        return outputStreamResourceOptional.map(os -> java.util.stream.Stream.of(os).collect(Collectors.toSet())).orElse(Collections.EMPTY_SET);
    }

    public void setMaxNodeDepth(Integer maxNodeDepth) {
        this.maxNodeDepth = maxNodeDepth;
    }

    public Smooks getNestedSmooks() {
        return nestedSmooks;
    }

    public void setNestedSmooks(Smooks nestedSmooks) {
        this.nestedSmooks = nestedSmooks;
    }

    public void setResourceConfig(ResourceConfig resourceConfig) {
        this.resourceConfig = resourceConfig;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void setResourceConfigSeq(ResourceConfigSeq resourceConfigSeq) {
        this.resourceConfigSeq = resourceConfigSeq;
    }

    public void setAction(Optional<Action> actionOptional) {
        this.actionOptional = actionOptional;
    }

    public void setBindIdOptional(Optional<String> bindIdOptional) {
        this.bindIdOptional = bindIdOptional;
    }

    public Optional<String> getOutputStreamResourceOptional() {
        return outputStreamResourceOptional;
    }

    public void setOutputStreamResourceOptional(Optional<String> outputStreamResourceOptional) {
        this.outputStreamResourceOptional = outputStreamResourceOptional;
    }

    @PreDestroy
    public void preDestroy() {
        if (nestedSmooks != null) {
            nestedSmooks.close();
        }
    }
}
