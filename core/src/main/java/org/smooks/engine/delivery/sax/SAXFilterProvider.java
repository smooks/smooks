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
package org.smooks.engine.delivery.sax;

import org.smooks.api.resource.config.ResourceConfig;
import org.smooks.api.resource.visitor.Visitor;
import org.smooks.api.resource.visitor.sax.SAXVisitAfter;
import org.smooks.api.resource.visitor.sax.SAXVisitBefore;
import org.smooks.engine.resource.config.ParameterAccessor;
import org.smooks.engine.delivery.AbstractFilterProvider;
import org.smooks.api.delivery.ContentDeliveryConfig;
import org.smooks.api.delivery.ContentHandlerBinding;
import org.smooks.api.delivery.event.ConfigBuilderEvent;
import org.smooks.api.lifecycle.VisitLifecycleCleanable;
import org.smooks.api.Registry;
import org.smooks.engine.delivery.event.DefaultConfigBuilderEvent;
import org.smooks.engine.lookup.NamespaceManagerLookup;

import java.util.List;
import java.util.Map;

public class SAXFilterProvider extends AbstractFilterProvider {
    
    @Override
    public ContentDeliveryConfig createContentDeliveryConfig(List<ContentHandlerBinding<Visitor>> visitorBindings, Registry registry, Map<String, List<ResourceConfig>> resourceConfigTable, List<ConfigBuilderEvent> configBuilderEvents) {
        SAXContentDeliveryConfig saxConfig = new SAXContentDeliveryConfig();

        for (ContentHandlerBinding<Visitor> visitorBinding : visitorBindings) {
            String targetElement = visitorBinding.getResourceConfig().getSelectorPath().getTargetElement();
            visitorBinding.getResourceConfig().getSelectorPath().setNamespaces(registry.lookup(new NamespaceManagerLookup()));

            if (isSAXVisitor(visitorBinding.getContentHandler())) {
                if (visitorBinding.getContentHandler() instanceof SAXVisitBefore && visitBeforeAnnotationsOK(visitorBinding.getContentHandler())) {
                    saxConfig.getVisitBeforeSelectorTable().put(targetElement, visitorBinding.getResourceConfig(), (SAXVisitBefore) visitorBinding.getContentHandler());
                }
                if (visitorBinding.getContentHandler() instanceof SAXVisitAfter && visitAfterAnnotationsOK(visitorBinding.getContentHandler())) {
                    saxConfig.getVisitAfterSelectorTable().put(targetElement, visitorBinding.getResourceConfig(), (SAXVisitAfter) visitorBinding.getContentHandler());
                }
                configBuilderEvents.add(new DefaultConfigBuilderEvent(visitorBinding.getResourceConfig(), "Added as a SAX resource."));
            }

            if(visitorBinding.getContentHandler() instanceof VisitLifecycleCleanable) {
                saxConfig.getVisitLifecycleCleanableSelectorTable().put(targetElement, visitorBinding.getResourceConfig(), (VisitLifecycleCleanable) visitorBinding.getContentHandler());
            }
        }
        
        saxConfig.setRegistry(registry);
        saxConfig.setResourceConfigs(resourceConfigTable);
        saxConfig.getConfigBuilderEvents().addAll(configBuilderEvents);

        saxConfig.optimizeConfig();
        saxConfig.assertSelectorsNotAccessingText();

        if (ParameterAccessor.getParameterValue(ContentDeliveryConfig.SMOOKS_VISITORS_SORT, Boolean.class, true, resourceConfigTable)) {
            saxConfig.sort();
        }

        saxConfig.addToExecutionLifecycleSets();
        saxConfig.addIndexCounters();
        
        return saxConfig;
    }

    @Override
    public Boolean isProvider(List<ContentHandlerBinding<Visitor>> visitorBindings) {
        return visitorBindings.stream().filter(c -> isSAXVisitor(c.getContentHandler())).count() == visitorBindings.
                stream().
                filter(v -> isDOMVisitor(v.getContentHandler()) || isSAXVisitor(v.getContentHandler())).
                count();
    }

    @Override
    public String getName() {
        return "SAX";
    }
}