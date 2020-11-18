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
package org.smooks.delivery.sax.ng;

import org.smooks.cdr.SmooksResourceConfiguration;
import org.smooks.container.ApplicationContext;
import org.smooks.delivery.AbstractFilterProvider;
import org.smooks.delivery.ContentDeliveryConfig;
import org.smooks.delivery.ContentHandlerBinding;
import org.smooks.delivery.Visitor;
import org.smooks.delivery.interceptor.InterceptorVisitorChainFactory;
import org.smooks.dtd.DTDStore;
import org.smooks.event.types.ConfigBuilderEvent;
import org.smooks.registry.lookup.InterceptorVisitorFactoryLookup;
import org.smooks.registry.lookup.NamespaceManagerLookup;

import java.util.List;
import java.util.Map;

public class SaxNgFilterProvider extends AbstractFilterProvider {

    @Override
    public ContentDeliveryConfig createContentDeliveryConfig(List<ContentHandlerBinding<Visitor>> visitorBindings, ApplicationContext applicationContext, Map<String, List<SmooksResourceConfiguration>> resourceConfigTable, List<ConfigBuilderEvent> configBuilderEvents, DTDStore.DTDObjectContainer dtdObjectContainer, Boolean sortVisitors) {
        SaxNgContentDeliveryConfig saxNgContentDeliveryConfig = new SaxNgContentDeliveryConfig();
        InterceptorVisitorChainFactory interceptorVisitorChainFactory = applicationContext.getRegistry().lookup(new InterceptorVisitorFactoryLookup());

        for (ContentHandlerBinding<Visitor> visitorBinding : visitorBindings) {
            String targetElement = visitorBinding.getSmooksResourceConfiguration().getSelectorPath().getTargetElement();
            visitorBinding.getSmooksResourceConfiguration().getSelectorPath().setNamespaces(applicationContext.getRegistry().lookup(new NamespaceManagerLookup()));

            if (visitorBinding.getContentHandler() instanceof BeforeVisitor || visitorBinding.getContentHandler() instanceof AfterVisitor) {
                final Visitor interceptorChain = interceptorVisitorChainFactory.createInterceptorChain(visitorBinding);
                if (interceptorChain instanceof BeforeVisitor && visitBeforeAnnotationsOK(visitorBinding.getContentHandler())) {
                    saxNgContentDeliveryConfig.getBeforeVisitors().addBinding(targetElement, visitorBinding.getSmooksResourceConfiguration(), (BeforeVisitor) interceptorChain);
                    if (interceptorChain instanceof ChildrenVisitor) {
                        saxNgContentDeliveryConfig.getChildVisitors().addBinding(targetElement, visitorBinding.getSmooksResourceConfiguration(), (ChildrenVisitor) interceptorChain);
                    }
                }
                if (interceptorChain instanceof AfterVisitor && visitAfterAnnotationsOK(visitorBinding.getContentHandler())) {
                    saxNgContentDeliveryConfig.getAfterVisitors().addBinding(targetElement, visitorBinding.getSmooksResourceConfiguration(), (AfterVisitor) interceptorChain);
                    if (!(interceptorChain instanceof BeforeVisitor) && interceptorChain instanceof ChildrenVisitor) {
                        saxNgContentDeliveryConfig.getChildVisitors().addBinding(targetElement, visitorBinding.getSmooksResourceConfiguration(), (ChildrenVisitor) interceptorChain);
                    }
                }
                configBuilderEvents.add(new ConfigBuilderEvent(visitorBinding.getSmooksResourceConfiguration(), "Added as a SAX-NG resource."));
            }
        }
        
        saxNgContentDeliveryConfig.setApplicationContext(applicationContext);
        saxNgContentDeliveryConfig.setSmooksResourceConfigurations(resourceConfigTable);
        saxNgContentDeliveryConfig.setDtd(dtdObjectContainer);
        saxNgContentDeliveryConfig.getConfigBuilderEvents().addAll(configBuilderEvents);

        saxNgContentDeliveryConfig.optimizeConfig();
        saxNgContentDeliveryConfig.assertSelectorsNotAccessingText();

        if(sortVisitors) {
            saxNgContentDeliveryConfig.sort();
        }

        saxNgContentDeliveryConfig.addToExecutionLifecycleSets();
        saxNgContentDeliveryConfig.initializeXMLReaderPool();

        saxNgContentDeliveryConfig.addIndexCounters();

        return saxNgContentDeliveryConfig;
    }

    @Override
    public Boolean isProvider(List<ContentHandlerBinding<Visitor>> contentHandlerBindings) {
        return contentHandlerBindings.stream().filter(c -> c.getContentHandler() instanceof BeforeVisitor || c.getContentHandler() instanceof AfterVisitor).count() == contentHandlerBindings.size();
    }

    @Override
    public String getName() {
        return "SAX-NG";
    }
}