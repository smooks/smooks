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

import org.smooks.SmooksException;
import org.smooks.container.ExecutionContext;
import org.smooks.delivery.Fragment;
import org.smooks.delivery.dom.DOMElementVisitor;
import org.smooks.delivery.ordering.Consumer;
import org.smooks.delivery.ordering.Producer;
import org.smooks.delivery.sax.*;
import org.smooks.delivery.sax.ng.*;
import org.smooks.lifecycle.ExecutionLifecycleCleanable;
import org.smooks.lifecycle.ExecutionLifecycleInitializable;
import org.smooks.lifecycle.VisitLifecycleCleanable;
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

public class StaticProxyInterceptor extends AbstractInterceptorVisitor implements SAXElementVisitor, ElementVisitor, DOMElementVisitor, VisitLifecycleCleanable, Producer, Consumer, ParameterizedVisitor, ExecutionLifecycleInitializable, ExecutionLifecycleCleanable {
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
        });
    }

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
        });
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
        });
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
        });
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
        });
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
        });
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
        });
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
        });
    }

    @Override
    public void executeVisitLifecycleCleanup(Fragment fragment, ExecutionContext executionContext) {
        intercept(new Invocation<VisitLifecycleCleanable>() {
            @Override
            public Object invoke(VisitLifecycleCleanable visitor) {
                visitor.executeVisitLifecycleCleanup(fragment, executionContext);
                return null;
            }

            @Override
            public Class<VisitLifecycleCleanable> getTarget() {
                return VisitLifecycleCleanable.class;
            }
        });
    }

    @Override
    public boolean consumes(Object object) {
        final Object result = intercept(new Invocation<Consumer>() {
            @Override
            public Object invoke(Consumer visitor) {
                return visitor.consumes(object);
            }

            @Override
            public Class<Consumer> getTarget() {
                return Consumer.class;
            }
        });
        
        if (result != null) {
            return (boolean) result;
        } else {
            return false;
        }
    }

    @Override
    public Set<?> getProducts() {
        Object result = intercept(new Invocation<Producer>() {
            @Override
            public Object invoke(Producer visitor) {
                return visitor.getProducts();
            }

            @Override
            public Class<Producer> getTarget() {
                return Producer.class;
            }
        });

        if (result == null) {
            return Collections.EMPTY_SET;
        } else {
            return (Set<?>) result;
        }
    }

    @Override
    public int getMaxNodeDepth() {
        Object depth = intercept(new Invocation<ParameterizedVisitor>() {
            @Override
            public Object invoke(ParameterizedVisitor visitor) {
                return visitor.getMaxNodeDepth();
            }

            @Override
            public Class<ParameterizedVisitor> getTarget() {
                return ParameterizedVisitor.class;
            }
        });
        
        return depth == null ? 1 : (int) depth;
    }

    @Override
    public void executeExecutionLifecycleInitialize(ExecutionContext executionContext) {
        intercept(new Invocation<ExecutionLifecycleInitializable>() {
            @Override
            public Object invoke(ExecutionLifecycleInitializable visitor) {
                visitor.executeExecutionLifecycleInitialize(executionContext);
                return null;
            }

            @Override
            public Class<ExecutionLifecycleInitializable> getTarget() {
                return ExecutionLifecycleInitializable.class;
            }
        });
    }

    @Override
    public void executeExecutionLifecycleCleanup(ExecutionContext executionContext) {
        intercept(new Invocation<ExecutionLifecycleCleanable>() {
            @Override
            public Object invoke(ExecutionLifecycleCleanable visitor) {
                visitor.executeExecutionLifecycleCleanup(executionContext);
                return null;
            }

            @Override
            public Class<ExecutionLifecycleCleanable> getTarget() {
                return ExecutionLifecycleCleanable.class;
            }
        });
    }
}