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
package org.smooks.engine.delivery.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smooks.api.ExecutionContext;
import org.smooks.api.SmooksException;
import org.smooks.api.delivery.ContentHandlerBinding;
import org.smooks.api.delivery.Filter;
import org.smooks.api.delivery.event.ExecutionEventListener;
import org.smooks.api.delivery.fragment.Fragment;
import org.smooks.api.delivery.sax.SAXElement;
import org.smooks.api.delivery.sax.SAXText;
import org.smooks.api.resource.visitor.Visitor;
import org.smooks.api.resource.visitor.dom.DOMElementVisitor;
import org.smooks.api.resource.visitor.sax.SAXElementVisitor;
import org.smooks.api.resource.visitor.sax.SAXVisitAfter;
import org.smooks.api.resource.visitor.sax.SAXVisitBefore;
import org.smooks.api.resource.visitor.sax.SAXVisitChildren;
import org.smooks.api.resource.visitor.sax.ng.AfterVisitor;
import org.smooks.api.resource.visitor.sax.ng.BeforeVisitor;
import org.smooks.api.resource.visitor.sax.ng.ChildrenVisitor;
import org.smooks.api.resource.visitor.sax.ng.ElementVisitor;
import org.smooks.engine.delivery.event.VisitEvent;
import org.smooks.engine.delivery.event.VisitSequence;
import org.smooks.engine.delivery.fragment.NodeFragment;
import org.smooks.engine.delivery.fragment.SAXElementFragment;
import org.smooks.engine.delivery.sax.ng.terminate.TerminateException;
import org.smooks.engine.lookup.GlobalParamsLookup;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Element;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.function.Supplier;

public class ExceptionInterceptor extends AbstractInterceptorVisitor implements ElementVisitor, DOMElementVisitor, SAXElementVisitor {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionInterceptor.class);
    
    protected boolean terminateOnVisitorException;
    protected String visitBeforeExceptionMessage;
    protected String visitAfterExceptionMessage;
    protected String visitChildTextExceptionMessage;
    private String visitChildElementExceptionMessage;

    @PostConstruct
    public void postConstructor() {
        terminateOnVisitorException = Boolean.parseBoolean(applicationContext.getRegistry().lookup(new GlobalParamsLookup(applicationContext.getRegistry())).getParameterValue(Filter.TERMINATE_ON_VISITOR_EXCEPTION, String.class, "true"));
        visitBeforeExceptionMessage = String.format("Error in %s while processing visitBefore SAX NG event", visitorBinding.getContentHandler().getClass().getName());
        visitAfterExceptionMessage = String.format("Error in %s while processing visitAfter SAX NG event", visitorBinding.getContentHandler().getClass().getName());
        visitChildTextExceptionMessage = String.format("Error in %s while processing visitChildText SAX NG event", visitorBinding.getContentHandler().getClass().getName());
        visitChildElementExceptionMessage = String.format("Error in %s while processing visitChildElement SAX NG event", visitorBinding.getContentHandler().getClass().getName());
    }
    
    @Override
    public void visitBefore(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
        intercept(new Invocation<SAXVisitBefore>() {
            @Override
            public Object invoke(SAXVisitBefore visitor, Object... args) {
                try {
                    visitor.visitBefore(element, executionContext);
                } catch (IOException e) {
                    throw new SmooksException(e.getMessage(), e);
                }
                return null;
            }

            @Override
            public Class<SAXVisitBefore> getTarget() {
                return SAXVisitBefore.class;
            }
        }, executionContext, String.format("Error in %s while processing visitBefore SAX event", visitorBinding.getContentHandler().getClass().getName()), new SAXElementFragment(element), VisitSequence.BEFORE);
    }

    @Override
    public void visitAfter(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
        intercept(new Invocation<SAXVisitAfter>() {
            @Override
            public Object invoke(SAXVisitAfter visitor, Object... args) {
                try {
                    visitor.visitAfter(element, executionContext);
                } catch (IOException e) {
                    throw new SmooksException(e.getMessage(), e);
                }
                return null;
            }

            @Override
            public Class<SAXVisitAfter> getTarget() {
                return SAXVisitAfter.class;
            }
        }, executionContext, String.format("Error in %s while processing visitAfter SAX event", visitorBinding.getContentHandler().getClass().getName()), new SAXElementFragment(element), VisitSequence.AFTER);
    }

    @Override
    public void onChildText(SAXElement element, SAXText childText, ExecutionContext executionContext) throws SmooksException {
        intercept(new Invocation<SAXVisitChildren>() {
            @Override
            public Object invoke(SAXVisitChildren visitor, Object... args) {
                try {
                    visitor.onChildText(element, childText, executionContext);
                } catch (IOException e) {
                    throw new SmooksException(e.getMessage(), e);
                }
                return null;
            }

            @Override
            public Class<SAXVisitChildren> getTarget() {
                return SAXVisitChildren.class;
            }
        }, executionContext, String.format("Error in %s while processing onChildText SAX event", visitorBinding.getContentHandler().getClass().getName()), new SAXElementFragment(element), VisitSequence.AFTER);
    }

    @Override
    public void onChildElement(SAXElement element, SAXElement childElement, ExecutionContext executionContext) throws SmooksException, IOException {
        intercept(new Invocation<SAXVisitChildren>() {
            @Override
            public Object invoke(SAXVisitChildren visitor, Object... args) {
                try {
                    visitor.onChildElement(element, childElement, executionContext);
                } catch (IOException e) {
                    throw new SmooksException(e.getMessage(), e);
                }
                return null;
            }

            @Override
            public Class<SAXVisitChildren> getTarget() {
                return SAXVisitChildren.class;
            }
        }, executionContext, String.format("Error in %s while processing onChildElement SAX event", visitorBinding.getContentHandler().getClass().getName()), new SAXElementFragment(element), VisitSequence.AFTER);
    }

    @Override
    public void visitBefore(Element element, ExecutionContext executionContext) {
        intercept(visitBeforeInvocation, executionContext, visitBeforeExceptionMessage, new NodeFragment(element), VisitSequence.BEFORE, element, executionContext);
    }

    @Override
    public void visitAfter(Element element, ExecutionContext executionContext) {
        intercept(visitAfterInvocation, executionContext, visitAfterExceptionMessage, new NodeFragment(element), VisitSequence.AFTER, element, executionContext);
    }

    @Override
    public void visitChildText(CharacterData characterData, ExecutionContext executionContext) {
        intercept(visitChildTextInvocation, executionContext, visitChildTextExceptionMessage, new NodeFragment(characterData), VisitSequence.AFTER, characterData, executionContext);
    }

    @Override
    public void visitChildElement(Element childElement, ExecutionContext executionContext) {
        intercept(visitChildElementInvocation, executionContext, visitChildElementExceptionMessage, new NodeFragment(childElement.getParentNode()), VisitSequence.AFTER, childElement, executionContext);
    }
    
    private <T extends Visitor> void intercept(final Invocation<T> invocation, final ExecutionContext executionContext, final String exceptionMessage, final Fragment<?> fragment, final VisitSequence visitSequence, final Object... invocationArgs) {
        try {
            intercept(invocation, invocationArgs);
        } catch (Throwable t) {
            processVisitorException(t, exceptionMessage, executionContext, fragment, visitSequence, visitorBinding);
        }
    }
    
    private void processVisitorException(final Throwable t, final String exceptionMessage, final ExecutionContext executionContext, final Fragment<?> fragment, final VisitSequence visitSequence, final ContentHandlerBinding<Visitor> visitorBinding) {
        for (ExecutionEventListener executionEventListener : executionContext.getContentDeliveryRuntime().getExecutionEventListeners()) {
            executionEventListener.onEvent(new VisitEvent<>(fragment, visitorBinding, visitSequence, executionContext, t));
        }
        
        if (t instanceof TerminateException) {
            throw (TerminateException) t;
        }

        executionContext.setTerminationError(t);

        if (terminateOnVisitorException) {
            if (t instanceof SmooksException) {
                throw (SmooksException) t;
            } else {
                throw new SmooksException(exceptionMessage, t);
            }
        } else {
            LOGGER.error(exceptionMessage, t);
        }
    }
}