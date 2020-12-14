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
package org.smooks.delivery.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smooks.SmooksException;
import org.smooks.container.ApplicationContext;
import org.smooks.container.ExecutionContext;
import org.smooks.delivery.ContentHandlerBinding;
import org.smooks.delivery.Filter;
import org.smooks.delivery.VisitSequence;
import org.smooks.delivery.Visitor;
import org.smooks.delivery.dom.DOMElementVisitor;
import org.smooks.delivery.sax.*;
import org.smooks.delivery.sax.ng.AfterVisitor;
import org.smooks.delivery.sax.ng.BeforeVisitor;
import org.smooks.delivery.sax.ng.ChildrenVisitor;
import org.smooks.delivery.sax.ng.ElementVisitor;
import org.smooks.delivery.sax.ng.terminate.TerminateException;
import org.smooks.event.report.AbstractReportGenerator;
import org.smooks.event.types.ElementVisitEvent;
import org.smooks.registry.lookup.GlobalParamsLookup;
import org.w3c.dom.Element;

import java.io.IOException;

public class ExceptionInterceptor extends AbstractInterceptorVisitor implements ElementVisitor, DOMElementVisitor, SAXElementVisitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExceptionInterceptor.class);

    private boolean terminateOnVisitorException;

    @Override
    public void visitBefore(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
        intercept(new Invocation<SAXVisitBefore>() {
            @Override
            public Object invoke(SAXVisitBefore visitor) {
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
        }, executionContext, String.format("Error in %s while processing visitBefore SAX event", visitorBinding.getContentHandler().getClass().getName()), element, VisitSequence.BEFORE);
    }

    @Override
    public void visitAfter(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
        intercept(new Invocation<SAXVisitAfter>() {
            @Override
            public Object invoke(SAXVisitAfter visitor) {
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
        }, executionContext, String.format("Error in %s while processing visitAfter SAX event", visitorBinding.getContentHandler().getClass().getName()), element, VisitSequence.AFTER);
    }

    @Override
    public void onChildText(SAXElement element, SAXText childText, ExecutionContext executionContext) throws SmooksException {
        intercept(new Invocation<SAXVisitChildren>() {
            @Override
            public Object invoke(SAXVisitChildren visitor) {
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
        }, executionContext, String.format("Error in %s while processing onChildText SAX event", visitorBinding.getContentHandler().getClass().getName()), element, VisitSequence.AFTER);
    }

    @Override
    public void onChildElement(SAXElement element, SAXElement childElement, ExecutionContext executionContext) throws SmooksException, IOException {
        intercept(new Invocation<SAXVisitChildren>() {
            @Override
            public Object invoke(SAXVisitChildren visitor) {
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
        }, executionContext, String.format("Error in %s while processing onChildElement SAX event", visitorBinding.getContentHandler().getClass().getName()), element, VisitSequence.AFTER);
    }

    @Override
    public void visitBefore(Element element, ExecutionContext executionContext) {
        intercept(new Invocation<BeforeVisitor>() {
            @Override
            public Object invoke(BeforeVisitor visitor) {
                visitor.visitBefore(element, executionContext);
                return null;
            }

            @Override
            public Class<BeforeVisitor> getTarget() {
                return BeforeVisitor.class;
            }
        }, executionContext, String.format("Error in %s while processing visitBefore SAX-NG event", visitorBinding.getContentHandler().getClass().getName()), element, VisitSequence.BEFORE);
    }

    @Override
    public void visitAfter(Element element, ExecutionContext executionContext) {
        intercept(new Invocation<AfterVisitor>() {
            @Override
            public Object invoke(AfterVisitor visitor) {
                visitor.visitAfter(element, executionContext);
                return null;
            }

            @Override
            public Class<AfterVisitor> getTarget() {
                return AfterVisitor.class;
            }
        }, executionContext, String.format("Error in %s while processing visitAfter SAX-NG event", visitorBinding.getContentHandler().getClass().getName()), element, VisitSequence.AFTER);
    }

    @Override
    public void visitChildText(Element element, ExecutionContext executionContext) {
        intercept(new Invocation<ChildrenVisitor>() {
            @Override
            public Object invoke(ChildrenVisitor visitor) {
                visitor.visitChildText(element, executionContext);
                return null;
            }

            @Override
            public Class<ChildrenVisitor> getTarget() {
                return ChildrenVisitor.class;
            }
        }, executionContext, String.format("Error in %s while processing visitChildText SAX-NG event", visitorBinding.getContentHandler().getClass().getName()), element, VisitSequence.AFTER);
    }

    @Override
    public void visitChildElement(Element childElement, ExecutionContext executionContext) {
        intercept(new Invocation<ChildrenVisitor>() {
            @Override
            public Object invoke(ChildrenVisitor visitor) {
                visitor.visitChildElement(childElement, executionContext);
                return null;
            }

            @Override
            public Class<ChildrenVisitor> getTarget() {
                return ChildrenVisitor.class;
            }
        }, executionContext, String.format("Error in %s while processing visitChildElement SAX-NG event", visitorBinding.getContentHandler().getClass().getName()), childElement.getParentNode(), VisitSequence.AFTER);
    }

    @Override
    public void setApplicationContext(final ApplicationContext applicationContext) {
        super.setApplicationContext(applicationContext);
        terminateOnVisitorException = Boolean.parseBoolean(applicationContext.getRegistry().lookup(new GlobalParamsLookup(applicationContext.getRegistry())).getParameterValue(Filter.TERMINATE_ON_VISITOR_EXCEPTION, String.class, "true"));
    }
    
    private <T extends Visitor> void intercept(final Invocation<T> invocation, final ExecutionContext executionContext, final String exceptionMessage, final Object visitable, final VisitSequence visitSequence) {
        try {
            intercept(invocation);
        } catch (Throwable t) {
            processVisitorException(t, exceptionMessage, executionContext, visitable, visitSequence, visitorBinding);
        }
    }
    
    private void processVisitorException(final Throwable t, final String exceptionMessage, final ExecutionContext executionContext, final Object visitable, final VisitSequence visitSequence, final ContentHandlerBinding<Visitor> visitorBinding) {
        if (executionContext.getEventListener() != null) {
            executionContext.getEventListener().onEvent(new ElementVisitEvent<>(visitable, visitorBinding, visitSequence, executionContext, t));
        }
        
        if (t instanceof TerminateException) {
            throw (TerminateException) t;
        }

        executionContext.setTerminationError(t);
        
        if (!(executionContext.getEventListener() instanceof AbstractReportGenerator) && terminateOnVisitorException) {
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