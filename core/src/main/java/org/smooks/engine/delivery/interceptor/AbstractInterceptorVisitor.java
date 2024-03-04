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

import org.smooks.api.ApplicationContext;
import org.smooks.api.ExecutionContext;
import org.smooks.api.resource.visitor.interceptor.InterceptorVisitor;
import org.smooks.api.delivery.ContentHandlerBinding;
import org.smooks.api.resource.visitor.Visitor;
import org.smooks.api.resource.visitor.sax.ng.AfterVisitor;
import org.smooks.api.resource.visitor.sax.ng.BeforeVisitor;
import org.smooks.api.resource.visitor.sax.ng.ChildrenVisitor;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Element;

import javax.inject.Inject;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

public abstract class AbstractInterceptorVisitor implements InterceptorVisitor {

    protected final VisitChildTextInvocation visitChildTextInvocation = new VisitChildTextInvocation();
    protected final VisitChildElementInvocation visitChildElementInvocation = new VisitChildElementInvocation();
    protected final VisitAfterInvocation visitAfterInvocation = new VisitAfterInvocation();
    protected final VisitBeforeInvocation visitBeforeInvocation = new VisitBeforeInvocation();
    protected final Map<Class<?>, Optional<Visitor>> invocationTargetCache = new ConcurrentHashMap<>();
    protected final AtomicReference<ContentHandlerBinding<Visitor>> target = new AtomicReference<>();
    protected ContentHandlerBinding<Visitor> visitorBinding;

    @Inject
    protected ApplicationContext applicationContext;

    @Override
    public void setVisitorBinding(final ContentHandlerBinding<Visitor> visitorBinding) {
        this.visitorBinding = visitorBinding;
    }

    @Override
    public ContentHandlerBinding<Visitor> getVisitorBinding() {
        return visitorBinding;
    }

    @Override
    public ContentHandlerBinding<Visitor> getTarget() {
        if (target.get() == null) {
            ContentHandlerBinding<Visitor> nextVisitorBinding = visitorBinding;
            while (nextVisitorBinding.getContentHandler() instanceof InterceptorVisitor) {
                nextVisitorBinding = ((InterceptorVisitor) visitorBinding.getContentHandler()).getTarget();
            }
            target.compareAndSet(null, nextVisitorBinding);
        }

        return target.get();
    }

    protected <T extends Visitor> Object intercept(final Invocation<T> invocation, Object... args) {
        final Class<?> invocationTargetClass = invocation.getTarget();
        Optional<Visitor> invocationTargetVisitorOptional = invocationTargetCache.get(invocationTargetClass);
        if (invocationTargetVisitorOptional == null) {
            ContentHandlerBinding<Visitor> nextVisitorBinding = visitorBinding;
            Visitor nextVisitor;
            while (nextVisitorBinding != null) {
                nextVisitor = nextVisitorBinding.getContentHandler();
                if (invocationTargetClass.isInstance(nextVisitor)) {
                    invocationTargetVisitorOptional = Optional.of(nextVisitor);
                    invocationTargetCache.put(invocationTargetClass, invocationTargetVisitorOptional);
                    break;
                } else if (nextVisitor instanceof InterceptorVisitor) {
                    nextVisitorBinding = ((InterceptorVisitor) nextVisitor).getVisitorBinding();
                } else {
                    invocationTargetVisitorOptional = Optional.empty();
                    invocationTargetCache.put(invocationTargetClass, invocationTargetVisitorOptional);
                    break;
                }

            }
        }

        if (invocationTargetVisitorOptional.isPresent()) {
            return invocation.invoke((T) invocationTargetVisitorOptional.get(), args);
        } else {
            return null;
        }
    }

    public interface Invocation<T extends Visitor> {
        Object invoke(T visitor, Object... args);

        Class<?> getTarget();
    }

    protected static class VisitChildTextInvocation implements Invocation<ChildrenVisitor> {

        @Override
        public Object invoke(final ChildrenVisitor visitor, final Object... args) {
            visitor.visitChildText((CharacterData) args[0], (ExecutionContext) args[1]);
            return null;
        }

        @Override
        public Class<ChildrenVisitor> getTarget() {
            return ChildrenVisitor.class;
        }
    }

    protected static class VisitChildElementInvocation implements Invocation<ChildrenVisitor> {

        @Override
        public Object invoke(final ChildrenVisitor visitor, final Object... args) {
            visitor.visitChildElement((Element) args[0], (ExecutionContext) args[1]);
            return null;
        }

        @Override
        public Class<ChildrenVisitor> getTarget() {
            return ChildrenVisitor.class;
        }
    }

    protected static class VisitAfterInvocation implements Invocation<AfterVisitor> {
        @Override
        public Object invoke(final AfterVisitor visitor, final Object... args) {
            visitor.visitAfter((Element) args[0], (ExecutionContext) args[1]);
            return null;
        }

        @Override
        public Class<AfterVisitor> getTarget() {
            return AfterVisitor.class;
        }
    }

    protected static class VisitBeforeInvocation implements Invocation<BeforeVisitor> {
        @Override
        public Object invoke(final BeforeVisitor visitor, final Object... args) {
            visitor.visitBefore((Element) args[0], (ExecutionContext) args[1]);
            return null;
        }

        @Override
        public Class<BeforeVisitor> getTarget() {
            return BeforeVisitor.class;
        }
    }
}