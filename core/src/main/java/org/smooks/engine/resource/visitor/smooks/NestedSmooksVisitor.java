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
import org.smooks.api.ExecutionContext;
import org.smooks.api.SmooksException;
import org.smooks.api.TypedKey;
import org.smooks.api.bean.repository.BeanId;
import org.smooks.api.delivery.Filter;
import org.smooks.api.delivery.fragment.Fragment;
import org.smooks.api.delivery.ordering.Producer;
import org.smooks.api.lifecycle.ExecutionLifecycleInitializable;
import org.smooks.api.memento.MementoCaretaker;
import org.smooks.api.resource.config.ResourceConfig;
import org.smooks.api.resource.config.ResourceConfigSeq;
import org.smooks.api.resource.visitor.sax.ng.AfterVisitor;
import org.smooks.api.resource.visitor.sax.ng.BeforeVisitor;
import org.smooks.assertion.AssertArgument;
import org.smooks.engine.DefaultApplicationContextBuilder;
import org.smooks.engine.delivery.fragment.NodeFragment;
import org.smooks.engine.delivery.interceptor.InterceptorVisitorChainFactory;
import org.smooks.engine.delivery.interceptor.InterceptorVisitorDefinition;
import org.smooks.engine.delivery.interceptor.StaticProxyInterceptor;
import org.smooks.engine.delivery.sax.ng.session.SessionInterceptor;
import org.smooks.engine.memento.SimpleVisitorMemento;
import org.smooks.engine.memento.VisitorMemento;
import org.smooks.engine.resource.config.ParameterAccessor;
import org.smooks.engine.resource.config.XMLConfigDigester;
import org.smooks.engine.xml.Namespace;
import org.smooks.io.DomToXmlWriter;
import org.smooks.io.FragmentWriter;
import org.smooks.io.ResourceWriter;
import org.smooks.io.Stream;
import org.smooks.support.CollectionsUtil;
import org.smooks.support.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Optional;
import java.util.Set;

public class NestedSmooksVisitor implements BeforeVisitor, AfterVisitor, Producer, ExecutionLifecycleInitializable {
    
    public enum Action {
        REPLACE,
        PREPEND_BEFORE,
        PREPEND_AFTER,
        APPEND_BEFORE,
        APPEND_AFTER,
        BIND_TO,
        OUTPUT_TO
    }

    protected static final TypedKey<Node> SOURCE_SESSION_TYPED_KEY = new TypedKey<>();
    protected static final TypedKey<DocumentBuilder> CACHED_DOCUMENT_BUILDER_TYPED_KEY = new TypedKey<>();
    protected static final TypedKey<ExecutionContext> NESTED_EXECUTION_CONTEXT_MEMENTO_TYPED_KEY = new TypedKey<>();

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
    protected DomToXmlWriter nestedSmooksVisitorWriter;
    
    @PostConstruct
    public void postConstruct() throws SAXException, IOException, URISyntaxException, ClassNotFoundException {
        if (nestedSmooks == null) {
            final ByteArrayInputStream smooksResourceList = new ByteArrayInputStream(resourceConfig.getParameter("smooksResourceList", String.class).getValue().getBytes());
            resourceConfigSeq = XMLConfigDigester.digestConfig(smooksResourceList, "./", new HashMap<>(), applicationContext.getClassLoader());
            nestedSmooks = new Smooks(new DefaultApplicationContextBuilder().setRegisterSystemResources(false).setClassLoader(applicationContext.getClassLoader()).build());

            for (ResourceConfig resourceConfig : resourceConfigSeq) {
                nestedSmooks.addConfiguration(resourceConfig);
            }
        }

        nestedSmooks.getApplicationContext().getRegistry().registerObject(createInterceptorVisitorChainFactory(applicationContext));
        nestedSmooks.setFilterSettings(new FilterSettings(StreamFilterType.SAX_NG).setCloseResult(false).setReaderPoolSize(-1).setMaxNodeDepth(maxNodeDepth == 0 ? Integer.MAX_VALUE : maxNodeDepth));

        action = actionOptional.orElse(null);
        if (action != null) {
            if (action == Action.BIND_TO) {
                AssertArgument.isNotNull(bindIdOptional.orElse(null), "bindId");
                bindBeanId = this.applicationContext.getBeanIdStore().register(bindIdOptional.get());
            } else if (action == Action.OUTPUT_TO) {
                AssertArgument.isNotNull(outputStreamResourceOptional.orElse(null), "outputStreamResource");
            }
        }
        
        nestedSmooksVisitorWriter = new DomToXmlWriter(false, rewriteEntities);
    }

    protected InterceptorVisitorChainFactory createInterceptorVisitorChainFactory(final ApplicationContext applicationContext) {
        final InterceptorVisitorChainFactory interceptorVisitorChainFactory = new InterceptorVisitorChainFactory();
        interceptorVisitorChainFactory.setApplicationContext(applicationContext);

        InterceptorVisitorDefinition sessionInterceptorVisitorDefinition = new InterceptorVisitorDefinition();
        sessionInterceptorVisitorDefinition.setSelector(Optional.of("*"));
        sessionInterceptorVisitorDefinition.setClass(SessionInterceptor.class);
        interceptorVisitorChainFactory.getInterceptorVisitorDefinitions().add(sessionInterceptorVisitorDefinition);

        InterceptorVisitorDefinition staticProxyInterceptorVisitorDefinition = new InterceptorVisitorDefinition();
        staticProxyInterceptorVisitorDefinition.setSelector(Optional.of("*"));
        staticProxyInterceptorVisitorDefinition.setClass(StaticProxyInterceptor.class);
        interceptorVisitorChainFactory.getInterceptorVisitorDefinitions().add(staticProxyInterceptorVisitorDefinition);

        return interceptorVisitorChainFactory;
    }

    @Override
    public void executeExecutionLifecycleInitialize(final ExecutionContext executionContext) {
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
                            nestedSmooksVisitorWriter.writeStartElement(element, fragmentWriter);
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
                nestedSmooksVisitorWriter.writeStartElement(rootElement, fragmentWriter);
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
                nestedSmooksVisitorWriter.writeStartElement(rootElement, fragmentWriterMemento.getState());
            }
            if (executionContext.getContentDeliveryRuntime().getContentDeliveryConfig().isDefaultSerializationOn()) {
                fragmentWriterMemento.getState().write(XmlUtil.serialize(rootElement.getChildNodes(), Boolean.parseBoolean(ParameterAccessor.getParameterValue(Filter.CLOSE_EMPTY_ELEMENTS, String.class, "false", executionContext.getContentDeliveryRuntime().getContentDeliveryConfig()))));
                nestedSmooksVisitorWriter.writeEndElement(rootElement, fragmentWriterMemento.getState());
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
                nestedSmooksVisitorWriter.writeEndElement(rootElement, fragmentWriterMemento.getState());
            }
            filterSource(visitedNodeFragment, rootNodeFragment, fragmentWriterMemento.getState(), executionContext, "visitAfter");
            if (executionContext.getContentDeliveryRuntime().getContentDeliveryConfig().isDefaultSerializationOn() && action == Action.APPEND_BEFORE) {
                nestedSmooksVisitorWriter.writeEndElement(rootElement, fragmentWriterMemento.getState());
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
            nestedExecutionContext.setBeanContext(executionContext.getBeanContext().newSubContext(nestedExecutionContext));
            nestedExecutionContextMemento = new VisitorMemento<>(visitedNodeFragment, this, NESTED_EXECUTION_CONTEXT_MEMENTO_TYPED_KEY, nestedExecutionContext);
            mementoCaretaker.capture(nestedExecutionContextMemento);
        }
        
        final Document document = executionContext.get(CACHED_DOCUMENT_BUILDER_TYPED_KEY).newDocument();
        document.setStrictErrorChecking(false);
        final Element smooksSessionElement = document.createElementNS(Namespace.SMOOKS_URI, "session");
        smooksSessionElement.setAttribute("visit", visit);
        smooksSessionElement.setAttribute("source", SOURCE_SESSION_TYPED_KEY.getName());
        document.appendChild(smooksSessionElement);

        nestedExecutionContextMemento.getState().put(SOURCE_SESSION_TYPED_KEY, rootNodeFragment.unwrap());
        if (writer == null) {
            nestedSmooks.filterSource(nestedExecutionContextMemento.getState(), new DOMSource(document));
        } else {
            nestedSmooks.filterSource(nestedExecutionContextMemento.getState(), new DOMSource(document), new StreamResult(writer));
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
        return outputStreamResourceOptional.map(CollectionsUtil::toSet).orElseGet(CollectionsUtil::toSet);
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