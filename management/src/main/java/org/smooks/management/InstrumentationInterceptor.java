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
package org.smooks.management;

import jakarta.annotation.PostConstruct;
import org.smooks.api.ExecutionContext;
import org.smooks.api.SmooksConfigException;
import org.smooks.api.SmooksException;
import org.smooks.api.management.InstrumentationResource;
import org.smooks.api.resource.visitor.Visitor;
import org.smooks.api.resource.visitor.sax.ng.AfterVisitor;
import org.smooks.api.resource.visitor.sax.ng.BeforeVisitor;
import org.smooks.api.resource.visitor.sax.ng.ChildrenVisitor;
import org.smooks.api.resource.visitor.sax.ng.ElementVisitor;
import org.smooks.engine.delivery.interceptor.AbstractInterceptorVisitor;
import org.smooks.management.mbean.ManagedVisitor;
import org.w3c.dom.CharacterData;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.Date;

public class InstrumentationInterceptor extends AbstractInterceptorVisitor implements ElementVisitor {

    private ManagedVisitor managedVisitor;

    @PostConstruct
    public void postConstruct() {
        InstrumentationResource instrumentationResource = applicationContext.getRegistry().lookup(InstrumentationResource.INSTRUMENTATION_RESOURCE_TYPED_KEY);
        if (instrumentationResource == null) {
            throw new SmooksConfigException("Instrumentation resource not found. Hint: have you declared the instrumentation resource => <management:instrumentationResource/>");
        }

        managedVisitor = new ManagedVisitor(instrumentationResource, getTarget().getResourceConfig(), getTarget().getContentHandler());
    }

    @Override
    public void visitBefore(final Element element, final ExecutionContext executionContext) throws SmooksException {
        if (visitorBinding.getContentHandler() instanceof BeforeVisitor) {
            managedVisitor.incrementVisitBeforeCounter();
            manageVisit(visitBeforeInvocation, element, executionContext);
        }
    }

    @Override
    public void visitAfter(final Element element, final ExecutionContext executionContext) throws SmooksException {
        if (visitorBinding.getContentHandler() instanceof AfterVisitor) {
            managedVisitor.incrementVisitAfterCounter();
            manageVisit(visitAfterInvocation, element, executionContext);
        }
    }

    @Override
    public void visitChildText(final CharacterData characterData, final ExecutionContext executionContext) throws SmooksException {
        if (visitorBinding.getContentHandler() instanceof ChildrenVisitor) {
            managedVisitor.incrementVisitChildTextCounter();
            manageVisit(visitChildTextInvocation, characterData, executionContext);
        }
    }

    @Override
    public void visitChildElement(Element childElement, ExecutionContext executionContext) {
        if (visitorBinding.getContentHandler() instanceof ChildrenVisitor) {
            managedVisitor.incrementVisitChildTextCounter();
            manageVisit( visitChildElementInvocation, childElement, executionContext);
        }
    }

    protected <T extends Visitor> void manageVisit(Invocation<T> invocation, Node node, ExecutionContext executionContext) {
        long startTime = new Date().getTime();
        try {
            intercept(invocation, node, executionContext);
        } catch (RuntimeException e) {
            managedVisitor.incrementFailedVisitCounter();
            throw e;
        } finally {
            long visitProcessingTime = new Date().getTime() - startTime;
            managedVisitor.addTotalProcessingTime(visitProcessingTime);
            managedVisitor.sendNotification(node, visitProcessingTime);
        }
    }
}
