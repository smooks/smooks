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
package org.smooks.delivery.sax;

import org.jaxen.saxpath.SAXPathException;
import org.smooks.cdr.SmooksConfigurationException;
import org.smooks.cdr.SmooksResourceConfiguration;
import org.smooks.cdr.registry.lookup.NamespaceMappingsLookup;
import org.smooks.cdr.xpath.SelectorStep;
import org.smooks.container.ApplicationContext;
import org.smooks.delivery.*;
import org.smooks.dtd.DTDStore;
import org.smooks.event.types.ConfigBuilderEvent;

import java.util.List;
import java.util.Map;

public class SAXStreamDeliveryProvider extends AbstractStreamDeliveryProvider {
    
    @Override
    public ContentDeliveryConfig createContentDeliveryConfig(List<ContentHandlerBinding<Visitor>> contentHandlerBindings, ApplicationContext applicationContext, Map<String, List<SmooksResourceConfiguration>> resourceConfigTable, List<ConfigBuilderEvent> configBuilderEvents, DTDStore.DTDObjectContainer dtdObjectContainer, Boolean sortVisitors) {
        SAXContentDeliveryConfig saxConfig = new SAXContentDeliveryConfig();

        for (ContentHandlerBinding<Visitor> contentHandlerBinding : contentHandlerBindings) {
            String targetElement = contentHandlerBinding.getResourceConfig().getTargetElement();
            try {
                SelectorStep.setNamespaces(contentHandlerBinding.getResourceConfig().getSelectorSteps(), applicationContext.getRegistry().lookup(new NamespaceMappingsLookup()));
            } catch (SAXPathException e) {
                throw new SmooksConfigurationException("Error configuring resource selector.", e);
            }
            
            if (isSAXVisitor(contentHandlerBinding.getContentHandler())) {
                if (contentHandlerBinding.getContentHandler() instanceof SAXVisitBefore && visitBeforeAnnotationsOK(contentHandlerBinding.getResourceConfig(), contentHandlerBinding.getContentHandler())) {
                    saxConfig.getVisitBefores().addBinding(targetElement, contentHandlerBinding.getResourceConfig(), (SAXVisitBefore) contentHandlerBinding.getContentHandler());
                }
                if (contentHandlerBinding.getContentHandler() instanceof SAXVisitAfter && visitAfterAnnotationsOK(contentHandlerBinding.getResourceConfig(), contentHandlerBinding.getContentHandler())) {
                    saxConfig.getVisitAfters().addBinding(targetElement, contentHandlerBinding.getResourceConfig(), (SAXVisitAfter) contentHandlerBinding.getContentHandler());
                }
                configBuilderEvents.add(new ConfigBuilderEvent(contentHandlerBinding.getResourceConfig(), "Added as a SAX resource."));
            }

            if(contentHandlerBinding.getContentHandler() instanceof VisitLifecycleCleanable) {
                saxConfig.getVisitCleanables().addBinding(targetElement, contentHandlerBinding.getResourceConfig(), (VisitLifecycleCleanable) contentHandlerBinding.getContentHandler());
            }
        }
        
        saxConfig.setApplicationContext(applicationContext);
        saxConfig.setSmooksResourceConfigurations(resourceConfigTable);
        saxConfig.setDtd(dtdObjectContainer);
        saxConfig.getConfigBuilderEvents().addAll(configBuilderEvents);

        saxConfig.optimizeConfig();
        saxConfig.assertSelectorsNotAccessingText();

        if(sortVisitors) {
            saxConfig.sort();
        }

        saxConfig.addToExecutionLifecycleSets();
        saxConfig.initializeXMLReaderPool();

        saxConfig.addIndexCounters();
        
        return saxConfig;
    }

    @Override
    public Boolean isProvider(List<ContentHandlerBinding<Visitor>> contentHandlerBindings) {
        return contentHandlerBindings.stream().filter(c -> isSAXVisitor(c.getContentHandler())).count() == contentHandlerBindings.size();
    }

    @Override
    public String getName() {
        return "SAX";
    }

    protected boolean isSAXVisitor(ContentHandler contentHandler) {
        // Intentionally not checking for SAXVisitChildren.  Must be incorporated into a visit before or after...
        return (contentHandler instanceof SAXVisitBefore || contentHandler instanceof SAXVisitAfter);
    }
}
