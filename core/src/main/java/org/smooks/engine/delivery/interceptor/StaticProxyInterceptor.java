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

import org.smooks.api.ExecutionContext;
import org.smooks.api.SmooksException;
import org.smooks.api.delivery.FilterBypass;
import org.smooks.api.delivery.fragment.Fragment;
import org.smooks.api.delivery.ordering.Consumer;
import org.smooks.api.delivery.ordering.Producer;
import org.smooks.api.io.Sink;
import org.smooks.api.io.Source;
import org.smooks.api.lifecycle.PostExecutionLifecycle;
import org.smooks.api.lifecycle.PreExecutionLifecycle;
import org.smooks.api.lifecycle.PostFragmentLifecycle;
import org.smooks.api.resource.visitor.Visitor;
import org.smooks.api.resource.visitor.dom.DOMElementVisitor;
import org.smooks.api.resource.visitor.sax.ng.ElementVisitor;
import org.smooks.api.resource.visitor.sax.ng.ParameterizedVisitor;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Element;

import java.util.Collections;
import java.util.Set;

public class StaticProxyInterceptor extends AbstractInterceptorVisitor implements ElementVisitor, DOMElementVisitor, PostFragmentLifecycle, Producer, Consumer, ParameterizedVisitor, PreExecutionLifecycle, PostExecutionLifecycle, FilterBypass {

    protected Invocation<PostFragmentLifecycle> postFragmentLifecycleInvocation = new Invocation<PostFragmentLifecycle>() {
        @Override
        public Object invoke(PostFragmentLifecycle visitor, Object... args) {
            visitor.onPostFragment((Fragment<?>) args[0], (ExecutionContext) args[1]);
            return null;
        }

        @Override
        public Class<PostFragmentLifecycle> getTarget() {
            return PostFragmentLifecycle.class;
        }
    };

    protected Invocation<Visitor> preExecutionLifecyleInvocation = new Invocation<Visitor>() {
        @Override
        public Object invoke(Visitor visitor, Object... args) {
            if (visitor instanceof PreExecutionLifecycle) {
                ((PreExecutionLifecycle) visitor).onPreExecution((ExecutionContext) args[0]);
            }
            return null;
        }

        @Override
        public Class<PreExecutionLifecycle> getTarget() {
            return PreExecutionLifecycle.class;
        }
    };

    protected Invocation<Visitor> postExecutionLifecycleInvocation = new Invocation<Visitor>() {
        @Override
        public Object invoke(Visitor visitor, Object... args) {
            if (visitor instanceof PostExecutionLifecycle) {
                ((PostExecutionLifecycle) visitor).onPostExecution((ExecutionContext) args[0]);
            }
            return null;
        }

        @Override
        public Class<PostExecutionLifecycle> getTarget() {
            return PostExecutionLifecycle.class;
        }
    };

    protected Invocation<ParameterizedVisitor> getMaxNodeInvocation = new Invocation<ParameterizedVisitor>() {
        @Override
        public Object invoke(ParameterizedVisitor visitor, Object... args) {
            return visitor.getMaxNodeDepth();
        }

        @Override
        public Class<ParameterizedVisitor> getTarget() {
            return ParameterizedVisitor.class;
        }
    };

    @Override
    public void visitBefore(Element element, ExecutionContext executionContext) {
        intercept(visitBeforeInvocation, element, executionContext);
    }

    @Override
    public void visitAfter(Element element, ExecutionContext executionContext) {
        intercept(visitAfterInvocation, element, executionContext);
    }

    @Override
    public void visitChildText(CharacterData characterData, ExecutionContext executionContext) {
        intercept(visitChildTextInvocation, characterData, executionContext);
    }

    @Override
    public void visitChildElement(Element childElement, ExecutionContext executionContext) {
        intercept(visitChildElementInvocation, childElement, executionContext);
    }

    @Override
    public void onPostFragment(Fragment<?> fragment, ExecutionContext executionContext) {
        intercept(postFragmentLifecycleInvocation, fragment, executionContext);
    }

    @Override
    public boolean consumes(Object object) {
        final Object result = intercept(new Invocation<Consumer>() {
            @Override
            public Object invoke(Consumer visitor, Object... args) {
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
        final Object result = intercept(new Invocation<Producer>() {
            @Override
            public Object invoke(Producer visitor, Object... args) {
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
        final Object depth = intercept(getMaxNodeInvocation);
        return depth == null ? 1 : (int) depth;
    }

    @Override
    public void onPreExecution(final ExecutionContext executionContext) {
        intercept(preExecutionLifecyleInvocation, executionContext);
    }

    @Override
    public void onPostExecution(final ExecutionContext executionContext) {
        intercept(postExecutionLifecycleInvocation, executionContext);
    }

    @Override
    public boolean bypass(ExecutionContext executionContext, Source source, Sink sink) throws SmooksException {
        final Object interceptResult = intercept(new Invocation<FilterBypass>() {
            @Override
            public Object invoke(FilterBypass visitor, Object... args) {
                return visitor.bypass(executionContext, source, sink);
            }

            @Override
            public Class<FilterBypass> getTarget() {
                return FilterBypass.class;
            }
        });

        if (interceptResult != null) {
            return (boolean) interceptResult;
        } else {
            return false;
        }
    }
}