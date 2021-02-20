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
package org.smooks.engine.resource.visitor.smooks;

import org.smooks.FilterSettings;
import org.smooks.Smooks;
import org.smooks.api.SmooksException;
import org.smooks.StreamFilterType;
import org.smooks.api.resource.config.ResourceConfig;
import org.smooks.api.ApplicationContext;
import org.smooks.api.ExecutionContext;
import org.smooks.api.delivery.Filter;
import org.smooks.api.delivery.fragment.Fragment;
import org.smooks.assertion.AssertArgument;
import org.smooks.engine.resource.config.ParameterAccessor;
import org.smooks.api.resource.config.ResourceConfigSeq;
import org.smooks.engine.resource.config.XMLConfigDigester;
import org.smooks.api.TypedKey;
import org.smooks.engine.DefaultApplicationContextBuilder;
import org.smooks.io.DomToXmlWriter;
import org.smooks.engine.delivery.fragment.NodeFragment;
import org.smooks.engine.delivery.interceptor.InterceptorVisitorChainFactory;
import org.smooks.engine.delivery.interceptor.InterceptorVisitorDefinition;
import org.smooks.engine.memento.SimpleVisitorMemento;
import org.smooks.engine.memento.VisitorMemento;
import org.smooks.api.delivery.ordering.Producer;
import org.smooks.api.resource.visitor.sax.ng.AfterVisitor;
import org.smooks.api.resource.visitor.sax.ng.BeforeVisitor;
import org.smooks.engine.delivery.sax.ng.session.SessionInterceptor;
import org.smooks.api.bean.repository.BeanId;
import org.smooks.engine.xml.Namespace;
import org.smooks.io.FragmentWriter;
import org.smooks.io.ResourceWriter;
import org.smooks.io.Stream;
import org.smooks.support.CollectionsUtil;
import org.smooks.xml.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
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

public class NestedSmooksVisitor implements BeforeVisitor, AfterVisitor, Producer {

    public enum Action {
        REPLACE,
        PREPEND_BEFORE,
        PREPEND_AFTER,
        APPEND_BEFORE,
        APPEND_AFTER,
        BIND_TO,
        OUTPUT_TO
    }

    private static final TypedKey<Node> SOURCE_SESSION_TYPED_KEY = new TypedKey<>();

    private BeanId bindBeanId;

    private Action action;
    
    @Inject
    @Named("action")
    private Optional<Action> actionOptional;

    @Inject
    @Named("bindId")
    private Optional<String> bindIdOptional;

    @Inject
    @Named("outputStreamResource")
    private Optional<String> outputStreamResourceOptional;
    
    @Inject
    private ResourceConfig resourceConfig;
    
    @Inject
    private Integer maxNodeDepth = 1;
    
    @Inject
    private ApplicationContext applicationContext;

    @Inject
    @Named(Filter.ENTITIES_REWRITE)
    private Boolean rewriteEntities = true;
    
    private ResourceConfigSeq resourceConfigList;
    private Smooks nestedSmooks;
    private DomToXmlWriter nestedSmooksVisitorWriter;

    @PostConstruct
    public void postConstruct() throws SAXException, IOException, URISyntaxException, ClassNotFoundException {
        if (nestedSmooks == null) {
            final ByteArrayInputStream smooksResourceList = new ByteArrayInputStream(resourceConfig.getParameter("smooksResourceList", String.class).getValue().getBytes());
            resourceConfigList = XMLConfigDigester.digestConfig(smooksResourceList, "./", new HashMap<>(), applicationContext.getClassLoader());
            nestedSmooks = new Smooks(new DefaultApplicationContextBuilder().setRegisterSystemResources(false).setClassLoader(applicationContext.getClassLoader()).build());

            for (ResourceConfig resourceConfig : resourceConfigList) {
                nestedSmooks.addConfiguration(resourceConfig);
            }
        }
        
        final InterceptorVisitorChainFactory interceptorVisitorChainFactory = new InterceptorVisitorChainFactory();
        InterceptorVisitorDefinition interceptorVisitorDefinition = new InterceptorVisitorDefinition();
        interceptorVisitorDefinition.setSelector(Optional.of("*"));
        interceptorVisitorDefinition.setClass(SessionInterceptor.class);
        interceptorVisitorChainFactory.getInterceptorVisitorDefinitions().add(interceptorVisitorDefinition);

        nestedSmooks.getApplicationContext().getRegistry().registerObject(interceptorVisitorChainFactory);
        nestedSmooks.setFilterSettings(new FilterSettings(StreamFilterType.SAX_NG).setCloseResult(false).setReaderPoolSize(1).setMaxNodeDepth(maxNodeDepth == 0 ? Integer.MAX_VALUE : maxNodeDepth));

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
    
    protected Node deattach(final Node node) {
        final Document document;
        try {
            document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) {
            throw new SmooksException(e);
        }
        final Node copyNode = document.importNode(node, true);
        document.appendChild(copyNode);
        
        return copyNode;
    }
    
    @Override
    public void visitBefore(final Element element, final ExecutionContext executionContext) {
        final Node sourceTreeNode = deattach(element);
        final NodeFragment visitedFragment = new NodeFragment(element);
        executionContext.getMementoCaretaker().capture(new SimpleVisitorMemento<>(visitedFragment, this, sourceTreeNode));

        final NodeFragment sourceTreeFragment = new NodeFragment(sourceTreeNode);
        final Writer nodeWriter;
        if (action == null) {
            filterSource(visitedFragment, sourceTreeFragment, Stream.out(executionContext), executionContext, "visitBefore");
            nodeWriter = Stream.out(executionContext);
        } else {
            if (action == Action.OUTPUT_TO) {
                final ResourceWriter resourceWriter = new ResourceWriter(executionContext, outputStreamResourceOptional.get());
                if (resourceWriter.getDelegateWriter() == null) {
                    filterSource(visitedFragment, sourceTreeFragment, null, executionContext, "visitBefore");
                    nodeWriter = null;
                } else {
                    executionContext.getMementoCaretaker().capture(new SimpleVisitorMemento<>(sourceTreeFragment, this, resourceWriter));
                    filterSource(visitedFragment, sourceTreeFragment, resourceWriter, executionContext, "visitBefore");
                    nodeWriter = resourceWriter.getDelegateWriter();
                }
            } else {
                if (action == Action.PREPEND_BEFORE || action == Action.PREPEND_AFTER) {
                    nodeWriter = prependBefore(visitedFragment, action, (Element) sourceTreeFragment.unwrap(), executionContext);
                } else if (action == Action.APPEND_BEFORE || action == Action.APPEND_AFTER) {
                    final FragmentWriter fragmentWriter = new FragmentWriter(executionContext, sourceTreeFragment);
                    if (executionContext.getContentDeliveryRuntime().getContentDeliveryConfig().isDefaultSerializationOn()) {
                        try {
                            nestedSmooksVisitorWriter.writeStartElement(element, fragmentWriter);
                        } catch (IOException e) {
                            throw new SmooksException(e);
                        }
                    }
                    executionContext.getMementoCaretaker().capture(new SimpleVisitorMemento<>(sourceTreeFragment, this, fragmentWriter));
                    filterSource(visitedFragment, sourceTreeFragment, fragmentWriter, executionContext, "visitBefore");
                    nodeWriter = fragmentWriter;
                } else if (action == Action.REPLACE) {
                    nodeWriter = replaceBefore(visitedFragment, sourceTreeFragment.unwrap(), executionContext);
                } else if (action == Action.BIND_TO) {
                    nodeWriter = new StringWriter();
                    executionContext.getMementoCaretaker().capture(new SimpleVisitorMemento<>(sourceTreeFragment, this, nodeWriter));
                    filterSource(visitedFragment, sourceTreeFragment, nodeWriter, executionContext, "visitBefore");
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
        final VisitorMemento<Node> sourceTreeMemento = new SimpleVisitorMemento<>(visitedFragment, this, element);
        executionContext.getMementoCaretaker().restore(sourceTreeMemento);
        final NodeFragment sourceTreeFragment = new NodeFragment(sourceTreeMemento.getState());

        if (action == null) {
            filterSource(visitedFragment, sourceTreeFragment, Stream.out(executionContext), executionContext, "visitAfter");
        } else {
            if (action == Action.OUTPUT_TO) {
                final ResourceWriter resourceWriter = executionContext.getMementoCaretaker().stash(new SimpleVisitorMemento<>(sourceTreeFragment, this, new ResourceWriter(executionContext, outputStreamResourceOptional.get())), resourceWriterMemento -> resourceWriterMemento).getState();
                filterSource(visitedFragment, sourceTreeFragment, resourceWriter, executionContext, "visitAfter");
            } else {
                if (action == Action.APPEND_BEFORE || action == Action.APPEND_AFTER) {
                    append(action, (Element) sourceTreeFragment.unwrap(), executionContext);
                } else if (action == Action.PREPEND_BEFORE || action == Action.PREPEND_AFTER) {
                    prependAfter(visitedFragment, (Element) sourceTreeFragment.unwrap(), executionContext);
                } else if (action == Action.REPLACE) {
                    replaceAfter(visitedFragment, sourceTreeFragment.unwrap(), executionContext);
                } else if (action == Action.BIND_TO) {
                    final VisitorMemento<StringWriter> memento = new SimpleVisitorMemento<>(sourceTreeFragment, this, new StringWriter());
                    executionContext.getMementoCaretaker().restore(memento);
                    filterSource(visitedFragment, sourceTreeFragment, memento.getState(), executionContext, "visitAfter");
                    executionContext.getBeanContext().addBean(bindBeanId, memento.getState().toString(), sourceTreeFragment);
                } else {
                    throw new UnsupportedOperationException();
                }
            }
        }

        final VisitorMemento<ChildEventListener> childEventListenerMemento = new SimpleVisitorMemento<>(visitedFragment, this, new ChildEventListener(this, null, null, null));
        executionContext.getMementoCaretaker().restore(childEventListenerMemento);
        executionContext.getContentDeliveryRuntime().removeExecutionEventListener(childEventListenerMemento.getState());
    }

    protected Writer replaceBefore(final Fragment<Node> visitedFragment, final Node sourceTreeNode, final ExecutionContext executionContext) {
        final NodeFragment nodeFragment = new NodeFragment(sourceTreeNode, true);
        final FragmentWriter fragmentWriter = new FragmentWriter(executionContext, nodeFragment, false);
        try {
            fragmentWriter.park();
        } catch (IOException e) {
            throw new SmooksException(e);
        }
        executionContext.getMementoCaretaker().capture(new SimpleVisitorMemento<>(nodeFragment, this, fragmentWriter));

        filterSource(visitedFragment, nodeFragment, fragmentWriter, executionContext, "visitBefore");

        return fragmentWriter;
    }

    protected void replaceAfter(final Fragment<Node> visitedFragment, final Node sourceTreeNode, final ExecutionContext executionContext) {
        final NodeFragment nodeFragment = new NodeFragment(sourceTreeNode);
        final VisitorMemento<FragmentWriter> fragmentWriterVisitorMemento = new SimpleVisitorMemento<>(nodeFragment, this, new FragmentWriter(executionContext, new NodeFragment(sourceTreeNode)));
        executionContext.getMementoCaretaker().restore(fragmentWriterVisitorMemento);

        filterSource(visitedFragment, nodeFragment, fragmentWriterVisitorMemento.getState(), executionContext, "visitAfter");
    }
    
    protected Writer prependBefore(final Fragment<Node> visitedFragment, final Action action, final Element sourceTreeNode, final ExecutionContext executionContext) {
        final NodeFragment nodeFragment = new NodeFragment(sourceTreeNode, true);
        final FragmentWriter fragmentWriter = new FragmentWriter(executionContext, nodeFragment, false);
        try {
            fragmentWriter.park();
            if (executionContext.getContentDeliveryRuntime().getContentDeliveryConfig().isDefaultSerializationOn() && action == Action.PREPEND_AFTER) {
                nestedSmooksVisitorWriter.writeStartElement(sourceTreeNode, fragmentWriter);
            }
            executionContext.getMementoCaretaker().capture(new SimpleVisitorMemento<>(nodeFragment, this, fragmentWriter));
            filterSource(visitedFragment, nodeFragment, fragmentWriter, executionContext, "visitBefore");
        } catch (IOException e) {
            throw new SmooksException(e);
        }

        return fragmentWriter;
    }
    
    protected void prependAfter(final Fragment<Node> visitedFragment, final Element element, final ExecutionContext executionContext) {
        final NodeFragment nodeFragment = new NodeFragment(element);
        final VisitorMemento<FragmentWriter> fragmentWriterMemento = new SimpleVisitorMemento<>(nodeFragment, this, new FragmentWriter(executionContext, nodeFragment));
        executionContext.getMementoCaretaker().restore(fragmentWriterMemento);
        filterSource(visitedFragment, nodeFragment, fragmentWriterMemento.getState(), executionContext, "visitAfter");
        try {
            if (executionContext.getContentDeliveryRuntime().getContentDeliveryConfig().isDefaultSerializationOn() && action == Action.PREPEND_BEFORE) {
                nestedSmooksVisitorWriter.writeStartElement(element, fragmentWriterMemento.getState());
            }
            if (executionContext.getContentDeliveryRuntime().getContentDeliveryConfig().isDefaultSerializationOn()) {
                fragmentWriterMemento.getState().write(XmlUtil.serialize(element.getChildNodes(), Boolean.parseBoolean(ParameterAccessor.getParameterValue(Filter.CLOSE_EMPTY_ELEMENTS, String.class, "false", executionContext.getContentDeliveryRuntime().getContentDeliveryConfig()))));
                nestedSmooksVisitorWriter.writeEndElement(element, fragmentWriterMemento.getState());
            }
        } catch (IOException e) {
            throw new SmooksException(e);
        }
    }

    protected void append(final Action action, final Element element, final ExecutionContext executionContext) {
        final NodeFragment nodeFragment = new NodeFragment(element);
        final VisitorMemento<FragmentWriter> fragmentWriterMemento = new SimpleVisitorMemento<>(nodeFragment, this, new FragmentWriter(executionContext, nodeFragment));
        executionContext.getMementoCaretaker().restore(fragmentWriterMemento);

        try {
            if (executionContext.getContentDeliveryRuntime().getContentDeliveryConfig().isDefaultSerializationOn() && action == Action.APPEND_AFTER) {
                nestedSmooksVisitorWriter.writeEndElement(element, fragmentWriterMemento.getState());
            }
            filterSource(nodeFragment, nodeFragment, fragmentWriterMemento.getState(), executionContext, "visitAfter");
            if (executionContext.getContentDeliveryRuntime().getContentDeliveryConfig().isDefaultSerializationOn() && action == Action.APPEND_BEFORE) {
                nestedSmooksVisitorWriter.writeEndElement(element, fragmentWriterMemento.getState());
            }
        } catch (IOException e) {
            throw new SmooksException(e);
        }
    }

    protected void filterSource(final Fragment<Node> visitedFragment, final Fragment<Node> sourceFragment, final Writer writer, final ExecutionContext executionContext, final String visit) {
        final ExecutionContext nestedExecutionContext = nestedSmooks.createExecutionContext();
        nestedExecutionContext.setBeanContext(executionContext.getBeanContext().newSubContext(nestedExecutionContext));

        executionContext.getMementoCaretaker().stash(new SimpleVisitorMemento<>(visitedFragment, this, nestedExecutionContext), nestedExecutionContextMemento -> {
            final Element smooksSessionElement;
            try {
                smooksSessionElement = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().createElementNS(Namespace.SMOOKS_URI, "session");
            } catch (ParserConfigurationException e) {
                throw new SmooksException(e);
            }
            smooksSessionElement.setAttribute("visit", visit);
            smooksSessionElement.setAttribute("source", SOURCE_SESSION_TYPED_KEY.getName());

            nestedExecutionContextMemento.getState().put(SOURCE_SESSION_TYPED_KEY, sourceFragment.unwrap());
            if (writer == null) {
                nestedSmooks.filterSource(nestedExecutionContextMemento.getState(), new DOMSource(smooksSessionElement));
            } else {
                nestedSmooks.filterSource(nestedExecutionContextMemento.getState(), new DOMSource(smooksSessionElement), new StreamResult(writer));
            }
            
            return nestedExecutionContextMemento;
        });
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

    public void setResourceConfigList(ResourceConfigSeq resourceConfigList) {
        this.resourceConfigList = resourceConfigList;
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