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
package org.smooks.visitors.smooks;

import org.smooks.FilterSettings;
import org.smooks.Smooks;
import org.smooks.SmooksException;
import org.smooks.StreamFilterType;
import org.smooks.assertion.AssertArgument;
import org.smooks.cdr.ResourceConfig;
import org.smooks.cdr.ResourceConfigList;
import org.smooks.cdr.XMLConfigDigester;
import org.smooks.container.ApplicationContext;
import org.smooks.container.ExecutionContext;
import org.smooks.container.standalone.DefaultApplicationContextBuilder;
import org.smooks.delivery.DomToXmlWriter;
import org.smooks.delivery.Filter;
import org.smooks.delivery.Visitor;
import org.smooks.delivery.fragment.Fragment;
import org.smooks.delivery.fragment.NodeFragment;
import org.smooks.delivery.interceptor.InterceptorVisitorChainFactory;
import org.smooks.delivery.interceptor.VisitPhaseInterceptor;
import org.smooks.delivery.memento.AbstractVisitorMemento;
import org.smooks.delivery.memento.VisitorMemento;
import org.smooks.delivery.ordering.Producer;
import org.smooks.delivery.sax.ng.ParameterizedVisitor;
import org.smooks.delivery.sax.ng.event.CharDataFragmentEvent;
import org.smooks.event.ExecutionEvent;
import org.smooks.event.ExecutionEventListener;
import org.smooks.event.types.EndFragmentEvent;
import org.smooks.event.types.StartFragmentEvent;
import org.smooks.io.*;
import org.smooks.javabean.repository.BeanId;
import org.smooks.util.CollectionsUtil;
import org.smooks.xml.XmlUtil;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
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
import java.util.function.Function;

public class NestedSmooksVisitor implements ParameterizedVisitor, Producer {
    
    public enum Action {
        REPLACE,
        PREPEND_BEFORE,
        PREPEND_AFTER,
        APPEND_BEFORE,
        APPEND_AFTER,
        BIND_TO,
        OUTPUT_TO
    }

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
    
    private ResourceConfigList resourceConfigList;
    private Smooks nestedSmooks;
    private DomToXmlWriter nestedSmooksVisitorWriter;
    
    protected static class DescendantEventListenerMemento extends AbstractVisitorMemento {

        protected DescendantEventListener descendantEventListener;

        public DescendantEventListenerMemento(final Fragment fragment, final Visitor visitor, final DescendantEventListener descendantEventListener) {
            super(fragment, visitor);
            this.descendantEventListener = descendantEventListener;
        }

        public DescendantEventListenerMemento(final Fragment fragment, final Visitor visitor) {
            super(fragment, visitor);
        }
        
        @Override
        public VisitorMemento copy() {
            return new DescendantEventListenerMemento(fragment, visitor, descendantEventListener);
        }

        @Override
        public void restore(final VisitorMemento visitorMemento) {
            descendantEventListener = ((DescendantEventListenerMemento) visitorMemento).getDescendantEventListener();
        }

        public DescendantEventListener getDescendantEventListener() {
            return descendantEventListener;
        }
    }

    protected static class ExecutionContextVisitorMemento extends AbstractVisitorMemento {

        protected ExecutionContext executionContext;

        public ExecutionContextVisitorMemento(final Fragment fragment, final Visitor visitor, final ExecutionContext executionContext) {
            super(fragment, visitor);
            this.executionContext = executionContext;
        }
        
        @Override
        public VisitorMemento copy() {
            return new ExecutionContextVisitorMemento(fragment, visitor, executionContext);
        }

        @Override
        public void restore(final VisitorMemento visitorMemento) {
            executionContext = ((ExecutionContextVisitorMemento) visitorMemento).getExecutionContext();
        }

        public ExecutionContext getExecutionContext() {
            return executionContext;
        }
    }

    protected class DescendantEventListener implements ExecutionEventListener {
        private final ExecutionContext executionContext;
        private int currentNodeDepth = 0;
        private final Writer selectorWriter;
        private final Element selectorElement;

        public DescendantEventListener(final Writer selectorWriter, final Element selectorElement, final ExecutionContext executionContext) {
            this.selectorWriter = selectorWriter;
            this.selectorElement = selectorElement;
            this.executionContext = executionContext;
        }

        @Override
        public void onEvent(final ExecutionEvent executionEvent) {
            if (executionEvent instanceof StartFragmentEvent) {
                currentNodeDepth++;
                if (maxNodeDepth != 0 && currentNodeDepth > maxNodeDepth && !selectorElement.equals(((StartFragmentEvent) executionEvent).getFragment().unwrap())) {
                    filterSource(selectorElement, (Element) ((StartFragmentEvent) executionEvent).getFragment().unwrap(), selectorWriter, executionContext, new VisitPhaseInterceptor.VisitBeforePhase());
                }
            } else if (executionEvent instanceof CharDataFragmentEvent) {
                if (maxNodeDepth != 0 && currentNodeDepth + 1 > maxNodeDepth) {
                    filterSource(selectorElement, (Element) ((CharDataFragmentEvent) executionEvent).getFragment().unwrap(), selectorWriter, executionContext, new VisitPhaseInterceptor.VisitChildTextPhase());
                }
            } else if (executionEvent instanceof EndFragmentEvent) {
                if (maxNodeDepth != 0 && currentNodeDepth > maxNodeDepth && !selectorElement.equals(((EndFragmentEvent) executionEvent).getFragment().unwrap())) {
                    filterSource(selectorElement, (Element) ((EndFragmentEvent) executionEvent).getFragment().unwrap(), selectorWriter, executionContext, new VisitPhaseInterceptor.VisitAfterPhase());
                }
                currentNodeDepth--;
            }
        }
    }

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
        interceptorVisitorChainFactory.getInterceptorVisitorClasses().add(VisitPhaseInterceptor.class);
        
        nestedSmooks.getApplicationContext().getRegistry().registerObject(interceptorVisitorChainFactory);
        nestedSmooks.setFilterSettings(new FilterSettings(StreamFilterType.SAX_NG).setCloseResult(false).setMaxNodeDepth(maxNodeDepth == 0 ? Integer.MAX_VALUE : maxNodeDepth));

        action = actionOptional.orElse(null);
        if (action != null && action == Action.BIND_TO) {
            AssertArgument.isNotEmpty(bindIdOptional.orElse(null), "bindId");
            bindBeanId = this.applicationContext.getBeanIdStore().register(bindIdOptional.get()); 
        }
        
        nestedSmooksVisitorWriter = new DomToXmlWriter(false, rewriteEntities);
    }
    
    @Override
    public void visitBefore(final Element element, final ExecutionContext executionContext) {
        final Writer nodeWriter;
        if (action == null) {
            filterSource(element, element, null, executionContext, new VisitPhaseInterceptor.VisitBeforePhase());
            nodeWriter = null;
        } else {
            if (action == Action.OUTPUT_TO) {
                final ResourceWriter resourceWriter = new ResourceWriter(executionContext, outputStreamResourceOptional.get());
                if (resourceWriter.getDelegateWriter() == null) {
                    filterSource(element, element, null, executionContext, new VisitPhaseInterceptor.VisitBeforePhase());
                    nodeWriter = null;
                } else {
                    filterSource(element, element, resourceWriter, executionContext, new VisitPhaseInterceptor.VisitBeforePhase());
                    nodeWriter = resourceWriter;
                }
            } else {
                if (action == Action.PREPEND_BEFORE || action == Action.PREPEND_AFTER) {
                    nodeWriter = prependOnVisitBefore(action, element, executionContext);
                } else if (action == Action.APPEND_BEFORE || action == Action.APPEND_AFTER) {
                    final FragmentWriter fragmentWriter = new FragmentWriter(executionContext, new NodeFragment(element));
                    if (executionContext.getContentDeliveryRuntime().getContentDeliveryConfig().isDefaultSerializationOn()) {
                        try {
                            nestedSmooksVisitorWriter.writeStartElement(element, fragmentWriter);
                        } catch (IOException e) {
                            throw new SmooksException(e);
                        }
                    }
                    executionContext.getMementoCaretaker().save(new FragmentWriterMemento(this, fragmentWriter));
                    nodeWriter = fragmentWriter;
                } else if (action == Action.REPLACE) {
                    nodeWriter = replaceBefore(element, executionContext);
                } else if (action == Action.BIND_TO) {
                    nodeWriter = new StringWriter();
                    filterSource(element, element, nodeWriter, executionContext, new VisitPhaseInterceptor.VisitBeforePhase());
                    executionContext.getBeanContext().addBean(bindBeanId, nodeWriter.toString(), new NodeFragment(element));
                } else {
                    throw new UnsupportedOperationException();
                }
            }
        }

        if (action != Action.APPEND_BEFORE && action != Action.APPEND_AFTER && action != Action.PREPEND_BEFORE && action != Action.PREPEND_AFTER) {
            final DescendantEventListener descendantEventListener = new DescendantEventListener(nodeWriter, element, executionContext);
            executionContext.getMementoCaretaker().save(new DescendantEventListenerMemento(new NodeFragment(element), this, descendantEventListener));
            executionContext.getContentDeliveryRuntime().addExecutionEventListener(descendantEventListener);
        }
    }
    
    @Override
    public void visitAfter(final Element element, final ExecutionContext executionContext) {
        if (action == null) {
            filterSource(element, element, null, executionContext, new VisitPhaseInterceptor.VisitAfterPhase());
        } else {
            if (action == Action.OUTPUT_TO) {
                filterSource(element, element, new ResourceWriter(executionContext, outputStreamResourceOptional.get()), executionContext, new VisitPhaseInterceptor.VisitAfterPhase());
            } else {
                if (action == Action.APPEND_BEFORE || action == Action.APPEND_AFTER) {
                    append(action, element, executionContext);
                } else if (action == Action.PREPEND_BEFORE || action == Action.PREPEND_AFTER) {
                    prependOnVisitAfter(element, executionContext);
                } else if (action == Action.REPLACE) {
                    replaceAfter(element, executionContext);
                } else if (action == Action.BIND_TO) {
                    final Writer writer = new StringWriter();
                    filterSource(element, element, writer, executionContext, new VisitPhaseInterceptor.VisitAfterPhase());
                    executionContext.getBeanContext().addBean(bindBeanId, executionContext.getBeanContext().getBean(bindBeanId).toString() + writer.toString());
                } else {
                    throw new UnsupportedOperationException();
                }
            }
        }

        final DescendantEventListenerMemento descendantEventListenerMemento = new DescendantEventListenerMemento(new NodeFragment(element), this);
        executionContext.getMementoCaretaker().restore(descendantEventListenerMemento);
        executionContext.getContentDeliveryRuntime().removeExecutionEventListener(descendantEventListenerMemento.getDescendantEventListener());
    }

    protected Writer replaceBefore(final Element element, final ExecutionContext executionContext) {
        final FragmentWriter fragmentWriter = new FragmentWriter(executionContext, new NodeFragment(element, true), false);
        try {
            fragmentWriter.capture();
        } catch (IOException e) {
            throw new SmooksException(e);
        }
        executionContext.getMementoCaretaker().save(new FragmentWriterMemento(this, fragmentWriter));

        filterSource(element, element, fragmentWriter.getDelegateWriter(), executionContext, new VisitPhaseInterceptor.VisitBeforePhase());

        return fragmentWriter;
    }

    protected void replaceAfter(final Element element, final ExecutionContext executionContext) {
        final FragmentWriterMemento fragmentWriterVisitorMemento = new FragmentWriterMemento(this, new FragmentWriter(executionContext, new NodeFragment(element)));
        executionContext.getMementoCaretaker().restore(fragmentWriterVisitorMemento);

        filterSource(element, element, fragmentWriterVisitorMemento.getFragmentWriter(), executionContext, new VisitPhaseInterceptor.VisitAfterPhase());
    }
    
    protected Writer prependOnVisitBefore(final Action action, final Element element, final ExecutionContext executionContext) {
        final FragmentWriter fragmentWriter = new FragmentWriter(executionContext, new NodeFragment(element, true), false);
        try {
            fragmentWriter.capture();
            if (executionContext.getContentDeliveryRuntime().getContentDeliveryConfig().isDefaultSerializationOn() && action == Action.PREPEND_AFTER) {
                nestedSmooksVisitorWriter.writeStartElement(element, fragmentWriter);
            }
            executionContext.getMementoCaretaker().save(new FragmentWriterMemento(this, fragmentWriter));
        } catch (IOException e) {
            throw new SmooksException(e);
        }

        return fragmentWriter;
    }
    
    protected void prependOnVisitAfter(final Element element, final ExecutionContext executionContext) {
        final FragmentWriterMemento fragmentWriterMemento = new FragmentWriterMemento(this, new FragmentWriter(executionContext, new NodeFragment(element)));
        executionContext.getMementoCaretaker().restore(fragmentWriterMemento);
        filterSource(element, element, fragmentWriterMemento.getFragmentWriter(), executionContext, null);
        try {
            if (executionContext.getContentDeliveryRuntime().getContentDeliveryConfig().isDefaultSerializationOn() && action == Action.PREPEND_BEFORE) {
                nestedSmooksVisitorWriter.writeStartElement(element, fragmentWriterMemento.getFragmentWriter());
            }
            if (executionContext.getContentDeliveryRuntime().getContentDeliveryConfig().isDefaultSerializationOn()) {
                fragmentWriterMemento.getFragmentWriter().write(XmlUtil.serialize(element.getChildNodes(), false));
                nestedSmooksVisitorWriter.writeEndElement(element, fragmentWriterMemento.getFragmentWriter());
            }
        } catch (IOException e) {
            throw new SmooksException(e);
        }
    }

    protected void append(final Action action, final Element element, final ExecutionContext executionContext) {
        final FragmentWriterMemento fragmentWriterMemento = new FragmentWriterMemento(this, new FragmentWriter(executionContext, new NodeFragment(element)));
        executionContext.getMementoCaretaker().restore(fragmentWriterMemento);

        try {
            if (executionContext.getContentDeliveryRuntime().getContentDeliveryConfig().isDefaultSerializationOn() && action == Action.APPEND_AFTER) {
                nestedSmooksVisitorWriter.writeEndElement(element, fragmentWriterMemento.getFragmentWriter());
            }
            filterSource(element, element, fragmentWriterMemento.getFragmentWriter(), executionContext, new VisitPhaseInterceptor.VisitAfterPhase());
            if (executionContext.getContentDeliveryRuntime().getContentDeliveryConfig().isDefaultSerializationOn() && action == Action.APPEND_BEFORE) {
                nestedSmooksVisitorWriter.writeEndElement(element, fragmentWriterMemento.getFragmentWriter());
            }
        } catch (IOException e) {
            throw new SmooksException(e);
        }
    }

    protected void filterSource(final Element selectorElement, final Element visitedElement, final Writer writer, final ExecutionContext executionContext, final VisitPhaseInterceptor.VisitPhase visitPhase) {
        final ExecutionContext nestedExecutionContext = nestedSmooks.createExecutionContext();
        for (ExecutionEventListener executionEventListener : executionContext.getContentDeliveryRuntime().getExecutionEventListeners()) {
            nestedExecutionContext.getContentDeliveryRuntime().addExecutionEventListener(executionEventListener);
        }
        nestedExecutionContext.setBeanContext(executionContext.getBeanContext().newSubContext(nestedExecutionContext));
        
        final ExecutionContextVisitorMemento executionContextVisitorMemento = executionContext.getMementoCaretaker().stash(new ExecutionContextVisitorMemento(new NodeFragment(selectorElement), this, nestedExecutionContext), new Function<ExecutionContextVisitorMemento, ExecutionContextVisitorMemento>() {
            @Override
            public ExecutionContextVisitorMemento apply(ExecutionContextVisitorMemento executionContextVisitorMemento) {
                if (visitPhase != null) {
                    executionContextVisitorMemento.getExecutionContext().put(VisitPhaseInterceptor.VISIT_PHASE_TYPED_KEY, visitPhase);
                }
                return executionContextVisitorMemento;
            }
        });

        if (writer == null) {
            nestedSmooks.filterSource(executionContextVisitorMemento.getExecutionContext(), new DOMSource(visitedElement));
        } else {
            nestedSmooks.filterSource(executionContextVisitorMemento.getExecutionContext(), new DOMSource(visitedElement), new StreamResult(writer));
        }
    }
    
    @Override
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

    public void setResourceConfigList(ResourceConfigList resourceConfigList) {
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
