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
package org.smooks.engine.delivery.dom;

import org.smooks.api.Registry;
import org.smooks.api.delivery.ContentDeliveryConfig;
import org.smooks.api.delivery.ContentHandler;
import org.smooks.api.delivery.ContentHandlerBinding;
import org.smooks.api.delivery.event.ConfigBuilderEvent;
import org.smooks.api.lifecycle.PostFragmentLifecycle;
import org.smooks.api.resource.config.ResourceConfig;
import org.smooks.api.resource.config.xpath.SelectorStep;
import org.smooks.api.resource.visitor.SerializerVisitor;
import org.smooks.api.resource.visitor.Visitor;
import org.smooks.api.resource.visitor.dom.DOMVisitAfter;
import org.smooks.api.resource.visitor.dom.DOMVisitBefore;
import org.smooks.api.resource.visitor.dom.Phase;
import org.smooks.api.resource.visitor.dom.VisitPhase;
import org.smooks.engine.delivery.AbstractFilterProvider;
import org.smooks.engine.delivery.dom.serialize.DOMSerializerVisitor;
import org.smooks.engine.delivery.event.DefaultConfigBuilderEvent;
import org.smooks.engine.lookup.NamespaceManagerLookup;
import org.smooks.engine.resource.config.ParameterAccessor;
import org.smooks.engine.resource.config.xpath.step.ElementSelectorStep;

import java.util.List;
import java.util.Map;

public class DOMFilterProvider extends AbstractFilterProvider {
    @Override
    public DOMContentDeliveryConfig createContentDeliveryConfig(final List<ContentHandlerBinding<Visitor>> visitorBindings, final Registry registry, Map<String, List<ResourceConfig>> resourceConfigTable, final List<ConfigBuilderEvent> configBuilderEvents) {
        DOMContentDeliveryConfig domConfig = new DOMContentDeliveryConfig();

        for (ContentHandlerBinding<Visitor> contentHandlerBinding : visitorBindings) {
            String targetElement = null;
            for (int i = contentHandlerBinding.getResourceConfig().getSelectorPath().size(); i > 0; i--) {
                final SelectorStep selectorStep = contentHandlerBinding.getResourceConfig().getSelectorPath().get(i - 1);
                if (selectorStep instanceof ElementSelectorStep) {
                    targetElement = ((ElementSelectorStep) selectorStep).getQName().getLocalPart();
                    break;
                }
            }

            final Visitor visitor = contentHandlerBinding.getContentHandler();
            final ResourceConfig resourceConfig = contentHandlerBinding.getResourceConfig();
            resourceConfig.getSelectorPath().setNamespaces(registry.lookup(new NamespaceManagerLookup()));

            if (isDOMVisitor(visitor)) {
                if (visitor instanceof DOMSerializerVisitor) {
                    domConfig.getSerializerVisitorIndex().put(targetElement, resourceConfig, (SerializerVisitor) visitor);
                    configBuilderEvents.add(new DefaultConfigBuilderEvent(resourceConfig, "Added as a DOM " + SerializerVisitor.class.getSimpleName() + " resource."));
                } else {
                    Phase phaseAnnotation = contentHandlerBinding.getContentHandler().getClass().getAnnotation(Phase.class);
                    String visitPhase = resourceConfig.getParameterValue("VisitPhase", String.class, VisitPhase.PROCESSING.toString());

                    if (phaseAnnotation != null && phaseAnnotation.value() == VisitPhase.ASSEMBLY) {
                        // It's an assembly unit...
                        if (visitor instanceof DOMVisitBefore && visitBeforeAnnotationsOK(visitor)) {
                            domConfig.getAssemblyVisitBeforeIndex().put(targetElement, resourceConfig, (DOMVisitBefore) visitor);
                        }
                        if (visitor instanceof DOMVisitAfter && visitAfterAnnotationsOK(visitor)) {
                            domConfig.getAssemblyVisitAfterIndex().put(targetElement, resourceConfig, (DOMVisitAfter) visitor);
                        }
                    } else if (visitPhase.equalsIgnoreCase(VisitPhase.ASSEMBLY.toString())) {
                        // It's an assembly unit...
                        if (visitor instanceof DOMVisitBefore && visitBeforeAnnotationsOK(visitor)) {
                            domConfig.getAssemblyVisitBeforeIndex().put(targetElement, resourceConfig, (DOMVisitBefore) visitor);
                        }
                        if (visitor instanceof DOMVisitAfter && visitAfterAnnotationsOK(visitor)) {
                            domConfig.getAssemblyVisitAfterIndex().put(targetElement, resourceConfig, (DOMVisitAfter) visitor);
                        }
                    } else {
                        // It's a processing unit...
                        if (visitor instanceof DOMVisitBefore && visitBeforeAnnotationsOK(visitor)) {
                            domConfig.getProcessingVisitBeforeIndex().put(targetElement, resourceConfig, (DOMVisitBefore) visitor);
                        }
                        if (visitor instanceof DOMVisitAfter && visitAfterAnnotationsOK(visitor)) {
                            domConfig.getProcessingVisitAfterIndex().put(targetElement, resourceConfig, (DOMVisitAfter) visitor);
                        }
                    }

                    configBuilderEvents.add(new DefaultConfigBuilderEvent(resourceConfig, "Added as a DOM " + visitPhase + " Phase resource."));
                }
            }

            if (visitor instanceof PostFragmentLifecycle) {
                domConfig.getPostFragmentLifecycleIndex().put(targetElement, resourceConfig, (PostFragmentLifecycle) visitor);
            }
        }

        domConfig.setRegistry(registry);
        domConfig.setResourceConfigs(resourceConfigTable);
        domConfig.getConfigBuilderEvents().addAll(configBuilderEvents);

        if (ParameterAccessor.getParameterValue(ContentDeliveryConfig.SMOOKS_VISITORS_SORT, Boolean.class, true, resourceConfigTable)) {
            domConfig.sort();
        }

        domConfig.addToExecutionLifecycleSets();
        domConfig.configureFilterBypass();

        return domConfig;
    }

    @Override
    public Boolean isProvider(List<ContentHandlerBinding<Visitor>> visitorBindings) {
        return visitorBindings.stream().filter(c -> isDOMVisitor(c.getContentHandler())).count() == visitorBindings.
                stream().count();
    }

    @Override
    public String getName() {
        return "DOM";
    }

    protected boolean isDOMVisitor(ContentHandler contentHandler) {
        return (contentHandler instanceof DOMVisitBefore || contentHandler instanceof DOMVisitAfter || contentHandler instanceof DOMSerializerVisitor);
    }
}