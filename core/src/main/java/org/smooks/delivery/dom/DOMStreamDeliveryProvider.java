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
package org.smooks.delivery.dom;

import org.jaxen.saxpath.SAXPathException;
import org.smooks.cdr.SmooksConfigurationException;
import org.smooks.cdr.SmooksResourceConfiguration;
import org.smooks.cdr.registry.lookup.NamespaceManagerLookup;
import org.smooks.container.ApplicationContext;
import org.smooks.delivery.*;
import org.smooks.delivery.dom.serialize.SerializationUnit;
import org.smooks.dtd.DTDStore;
import org.smooks.event.types.ConfigBuilderEvent;

import java.util.List;
import java.util.Map;

public class DOMStreamDeliveryProvider extends AbstractStreamDeliveryProvider {
    @Override
    public ContentDeliveryConfig createContentDeliveryConfig(final List<ContentHandlerBinding<Visitor>> visitorBindings, final ApplicationContext applicationContext, Map<String, List<SmooksResourceConfiguration>> resourceConfigTable, final List<ConfigBuilderEvent> configBuilderEvents, DTDStore.DTDObjectContainer dtdObjectContainer, final Boolean sortVisitors) {
        DOMContentDeliveryConfig domConfig = new DOMContentDeliveryConfig();

        for (ContentHandlerBinding<Visitor> contentHandlerBinding : visitorBindings) {
            final String targetElement = contentHandlerBinding.getSmooksResourceConfiguration().getSelectorPath().getTargetElement();
            final Visitor visitor = contentHandlerBinding.getContentHandler();
            final SmooksResourceConfiguration smooksResourceConfiguration = contentHandlerBinding.getSmooksResourceConfiguration();

            try {
                smooksResourceConfiguration.getSelectorPath().setNamespaces(applicationContext.getRegistry().lookup(new NamespaceManagerLookup()));
            } catch (SAXPathException e) {
                throw new SmooksConfigurationException("Error configuring resource selector.", e);
            }

            if (isDOMVisitor(visitor)) {
                if (visitor instanceof SerializationUnit) {
                    domConfig.getSerializationVisitors().addBinding(targetElement, smooksResourceConfiguration, (SerializationUnit) visitor);
                    configBuilderEvents.add(new ConfigBuilderEvent(smooksResourceConfiguration, "Added as a DOM " + SerializationUnit.class.getSimpleName() + " resource."));
                } else {
                    Phase phaseAnnotation = contentHandlerBinding.getContentHandler().getClass().getAnnotation(Phase.class);
                    String visitPhase = smooksResourceConfiguration.getParameterValue("VisitPhase", String.class, VisitPhase.PROCESSING.toString());

                    if (phaseAnnotation != null && phaseAnnotation.value() == VisitPhase.ASSEMBLY) {
                        // It's an assembly unit...
                        if (visitor instanceof DOMVisitBefore && visitBeforeAnnotationsOK(visitor)) {
                            domConfig.getAssemblyVisitBefores().addBinding(targetElement, smooksResourceConfiguration, (DOMVisitBefore) visitor);
                        }
                        if (visitor instanceof DOMVisitAfter && visitAfterAnnotationsOK(visitor)) {
                            domConfig.getAssemblyVisitAfters().addBinding(targetElement, smooksResourceConfiguration, (DOMVisitAfter) visitor);
                        }
                    } else if (visitPhase.equalsIgnoreCase(VisitPhase.ASSEMBLY.toString())) {
                        // It's an assembly unit...
                        if (visitor instanceof DOMVisitBefore && visitBeforeAnnotationsOK(visitor)) {
                            domConfig.getAssemblyVisitBefores().addBinding(targetElement, smooksResourceConfiguration, (DOMVisitBefore) visitor);
                        }
                        if (visitor instanceof DOMVisitAfter && visitAfterAnnotationsOK(visitor)) {
                            domConfig.getAssemblyVisitAfters().addBinding(targetElement, smooksResourceConfiguration, (DOMVisitAfter) visitor);
                        }
                    } else {
                        // It's a processing unit...
                        if (visitor instanceof DOMVisitBefore && visitBeforeAnnotationsOK(visitor)) {
                            domConfig.getProcessingVisitBefores().addBinding(targetElement, smooksResourceConfiguration, (DOMVisitBefore) visitor);
                        }
                        if (visitor instanceof DOMVisitAfter && visitAfterAnnotationsOK(visitor)) {
                            domConfig.getProcessingVisitAfters().addBinding(targetElement, smooksResourceConfiguration, (DOMVisitAfter) visitor);
                        }
                    }

                    configBuilderEvents.add(new ConfigBuilderEvent(smooksResourceConfiguration, "Added as a DOM " + visitPhase + " Phase resource."));
                }
            }

            if (visitor instanceof VisitLifecycleCleanable) {
                domConfig.getVisitCleanables().addBinding(targetElement, smooksResourceConfiguration, (VisitLifecycleCleanable) visitor);
            }
        }

        domConfig.setApplicationContext(applicationContext);
        domConfig.setSmooksResourceConfigurations(resourceConfigTable);
        domConfig.setDtd(dtdObjectContainer);
        domConfig.getConfigBuilderEvents().addAll(configBuilderEvents);

        if (sortVisitors) {
            domConfig.sort();
        }

        domConfig.addToExecutionLifecycleSets();
        domConfig.initializeXMLReaderPool();
        domConfig.configureFilterBypass();

        return domConfig;
    }

    @Override
    public Boolean isProvider(List<ContentHandlerBinding<Visitor>> visitorBindings) {
        return visitorBindings.stream().filter(c -> isDOMVisitor(c.getContentHandler())).count() == visitorBindings.
                stream().
                filter(v -> isDOMVisitor(v.getContentHandler()) || isSAXVisitor(v.getContentHandler())).
                count();
    }

    @Override
    public String getName() {
        return "DOM";
    }
}
