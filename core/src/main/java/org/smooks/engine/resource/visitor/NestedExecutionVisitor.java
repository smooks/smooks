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
package org.smooks.engine.resource.visitor;

import org.smooks.Smooks;
import org.smooks.api.SmooksException;
import org.smooks.api.ApplicationContext;
import org.smooks.api.ExecutionContext;
import org.smooks.api.TypedKey;
import org.smooks.api.delivery.sax.SAXElement;
import org.smooks.engine.delivery.AbstractParser;
import org.smooks.engine.delivery.SmooksContentHandler;
import org.smooks.api.delivery.fragment.Fragment;
import org.smooks.api.delivery.ordering.Producer;
import org.smooks.engine.delivery.sax.DynamicSAXElementVisitorList;
import org.smooks.engine.delivery.sax.SAXHandler;
import org.smooks.api.resource.visitor.sax.SAXVisitBefore;
import org.smooks.api.delivery.event.ExecutionEventListener;
import org.smooks.engine.bean.lifecycle.DefaultBeanContextLifecycleEvent;
import org.smooks.io.Stream;
import org.smooks.api.bean.context.BeanContext;
import org.smooks.api.bean.lifecycle.BeanLifecycle;
import org.smooks.api.bean.repository.BeanId;
import org.smooks.api.lifecycle.VisitLifecycleCleanable;
import org.smooks.namespace.NamespaceDeclarationStack;
import org.smooks.support.CollectionsUtil;
import org.smooks.engine.xml.NamespaceManager;
import org.xml.sax.XMLReader;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Nested Smooks execution visitor.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
@Deprecated
public class NestedExecutionVisitor implements SAXVisitBefore, VisitLifecycleCleanable, Producer {

    private static final TypedKey<ExecutionContext> NESTED_EXECUTION_CONTEXT_TYPED_KEY = new TypedKey<>();
    
    @Inject
    private String smooksConfig;

    @Inject
    private String[] mapBeans;
    private final List<BeanId> mapBeanIds = new ArrayList<BeanId>();

    @Inject
    private boolean inheritBeanContext = true;

    @Inject
    private ApplicationContext applicationContext;

    private volatile Smooks smooksInstance;

    public void setSmooksConfig(String smooksConfig) {
        this.smooksConfig = smooksConfig;
    }

    public void setSmooksInstance(Smooks smooksInstance) {
        this.smooksInstance = smooksInstance;
    }

    @PostConstruct
    public void preRegBeanIds() {
        for(String preRegBeanId : mapBeans) {
            mapBeanIds.add(applicationContext.getBeanIdStore().register(preRegBeanId));
        }
    }

    @PreDestroy
    public void closeSmooksInstance() {
        if (smooksInstance != null) {
            smooksInstance.close();
        }
    }

    public void visitBefore(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
        Smooks smooks = getSmooksInstance();
        ExecutionContext nestedExecutionContext = smooks.createExecutionContext();

        // In case there's an attached event listener...
        for (ExecutionEventListener executionEventListener : executionContext.getContentDeliveryRuntime().getExecutionEventListeners()) {
            nestedExecutionContext.getContentDeliveryRuntime().addExecutionEventListener(executionEventListener);
        }
        
        // Copy over the XMLReader stack...
        AbstractParser.setReaders(AbstractParser.getReaders(executionContext), nestedExecutionContext);

        // Attach the NamespaceDeclarationStack to the nested execution context...
        NamespaceDeclarationStack nsStack = executionContext.get(NamespaceManager.NAMESPACE_DECLARATION_STACK_TYPED_KEY);
        nestedExecutionContext.put(NamespaceManager.NAMESPACE_DECLARATION_STACK_TYPED_KEY, nsStack);

        SmooksContentHandler parentContentHandler = SmooksContentHandler.getHandler(executionContext);

        if(parentContentHandler.getNestedContentHandler() != null) {
            throw new SmooksException("Illegal use of more than one nested content handler fired on the same element.");
        }

        nestedExecutionContext.put(Stream.STREAM_WRITER_TYPED_KEY, element.getWriter(this));
        SmooksContentHandler nestedContentHandler = new SAXHandler(nestedExecutionContext, parentContentHandler);

        DynamicSAXElementVisitorList.propogateDynamicVisitors(executionContext, nestedExecutionContext);

        // Attach the XMLReader instance to the nested ExecutionContext and then swap the content handler on
        // the XMLReader to be the nested handler created here.  All events wll be forwarded to the ..
        XMLReader xmlReader = AbstractParser.getXMLReader(executionContext);
        AbstractParser.attachXMLReader(xmlReader, nestedExecutionContext);
        xmlReader.setContentHandler(nestedContentHandler);

        executionContext.put(NESTED_EXECUTION_CONTEXT_TYPED_KEY, nestedExecutionContext);

        // Note we do not execute the Smooks filterSource methods for a nested instance... we just install
        // the content handler and redirect the reader events to it...
    }

    public Set<?> getProducts() {
        return CollectionsUtil.toSet(mapBeans);
    }

    private Smooks getSmooksInstance() {
        // Lazily create the Smooks instance...
        if(smooksInstance == null) {
            synchronized (this) {
                if(smooksInstance == null) {
                    try {
                        smooksInstance = new Smooks(smooksConfig);
                    } catch (Exception e) {
                        throw new SmooksException("Error creating nested Smooks instance for Smooks configuration '" + smooksConfig + "'.", e);
                    }
                }
            }
        }
        return smooksInstance;
    }

    public void executeVisitLifecycleCleanup(Fragment fragment, ExecutionContext executionContext) {
        try {
            ExecutionContext nestedExecutionContext = executionContext.get(NESTED_EXECUTION_CONTEXT_TYPED_KEY);

            try {
                if(nestedExecutionContext != null) {
                    BeanContext parentBeanContext = executionContext.getBeanContext();
                    BeanContext nestedBeanContext = nestedExecutionContext.getBeanContext();

                    for(BeanId beanId : mapBeanIds) {
                        Object bean = nestedBeanContext.getBean(beanId.getName());

                        // Add the bean from the nested context onto the parent context and then remove
                        // it again.  This is enough to fire the wiring and end events...
                        parentBeanContext.notifyObservers(new DefaultBeanContextLifecycleEvent(executionContext, null, BeanLifecycle.START_FRAGMENT, beanId, bean));
                        parentBeanContext.addBean(beanId, bean);
                        parentBeanContext.notifyObservers(new DefaultBeanContextLifecycleEvent(executionContext, null, BeanLifecycle.END_FRAGMENT, beanId, bean));
                        parentBeanContext.removeBean(beanId, null);
                    }
                }
            } finally {
                executionContext.remove(NESTED_EXECUTION_CONTEXT_TYPED_KEY);
            }
        } finally {
            AbstractParser.detachXMLReader(executionContext);
        }
    }
}
