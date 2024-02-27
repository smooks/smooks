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
package org.smooks.engine.delivery.sax.ng;

import org.smooks.api.Registry;
import org.smooks.api.SmooksConfigException;
import org.smooks.api.delivery.ContentHandlerBinding;
import org.smooks.api.delivery.event.ContentDeliveryConfigExecutionEvent;
import org.smooks.api.resource.config.ResourceConfig;
import org.smooks.api.resource.config.xpath.SelectorStep;
import org.smooks.api.resource.visitor.Visitor;
import org.smooks.api.resource.visitor.sax.ng.AfterVisitor;
import org.smooks.api.resource.visitor.sax.ng.BeforeVisitor;
import org.smooks.api.resource.visitor.sax.ng.ChildrenVisitor;
import org.smooks.engine.delivery.AbstractFilterProvider;
import org.smooks.engine.delivery.event.DefaultContentDeliveryConfigExecutionEvent;
import org.smooks.engine.delivery.interceptor.InterceptorVisitorChainFactory;
import org.smooks.engine.lookup.InterceptorVisitorFactoryLookup;
import org.smooks.engine.lookup.NamespaceManagerLookup;
import org.smooks.engine.resource.config.xpath.IndexedSelectorPath;
import org.smooks.engine.resource.config.xpath.step.ElementSelectorStep;

import java.util.List;
import java.util.Map;
import java.util.Properties;

public class SaxNgFilterProvider extends AbstractFilterProvider {

    @Override
    public SaxNgContentDeliveryConfig createContentDeliveryConfig(final List<ContentHandlerBinding<Visitor>> visitorBindings, final Registry registry, Map<String, List<ResourceConfig>> resourceConfigTable, final List<ContentDeliveryConfigExecutionEvent> contentDeliveryConfigExecutionEvents) {
        final SaxNgContentDeliveryConfig saxNgContentDeliveryConfig = new SaxNgContentDeliveryConfig();
        final InterceptorVisitorChainFactory interceptorVisitorChainFactory = registry.lookup(new InterceptorVisitorFactoryLookup());

        for (ContentHandlerBinding<Visitor> visitorBinding : visitorBindings) {
            visitorBinding.getResourceConfig().getSelectorPath().setNamespaces(registry.lookup(new NamespaceManagerLookup()).orElse(new Properties()));

            if (visitorBinding.getContentHandler() instanceof BeforeVisitor || visitorBinding.getContentHandler() instanceof AfterVisitor) {
                if (visitorBinding.getContentHandler() instanceof BeforeVisitor || visitorBinding.getContentHandler() instanceof ChildrenVisitor) {
                    assertSelectorsNotAccessingText(visitorBinding.getResourceConfig());
                }
                final ContentHandlerBinding<Visitor> interceptorChain = interceptorVisitorChainFactory.createInterceptorChain(visitorBinding);
                final Visitor interceptorChainVisitor = interceptorChain.getContentHandler();
                String selector = null;
                if (interceptorChain.getResourceConfig().getSelectorPath() instanceof IndexedSelectorPath) {
                    for (int i = interceptorChain.getResourceConfig().getSelectorPath().size(); i > 0; i--) {
                        final SelectorStep selectorStep = interceptorChain.getResourceConfig().getSelectorPath().get(i - 1);
                        if (selectorStep instanceof ElementSelectorStep) {
                            selector = ((ElementSelectorStep) selectorStep).getQName().getLocalPart();
                            break;
                        }
                    }
                } else {
                    selector = "*";
                }

                if (interceptorChainVisitor instanceof BeforeVisitor && visitBeforeAnnotationsOK(visitorBinding.getContentHandler())) {
                    saxNgContentDeliveryConfig.getBeforeVisitorIndex().put(selector, interceptorChain.getResourceConfig(), (BeforeVisitor) interceptorChainVisitor);
                    if (interceptorChainVisitor instanceof ChildrenVisitor) {
                        saxNgContentDeliveryConfig.getChildVisitorIndex().put(selector, interceptorChain.getResourceConfig(), (ChildrenVisitor) interceptorChainVisitor);
                    }
                }
                if (interceptorChainVisitor instanceof AfterVisitor && visitAfterAnnotationsOK(visitorBinding.getContentHandler())) {
                    saxNgContentDeliveryConfig.getAfterVisitorIndex().put(selector, interceptorChain.getResourceConfig(), (AfterVisitor) interceptorChainVisitor);
                    if (!(interceptorChainVisitor instanceof BeforeVisitor) && interceptorChainVisitor instanceof ChildrenVisitor) {
                        saxNgContentDeliveryConfig.getChildVisitorIndex().put(selector, interceptorChain.getResourceConfig(), (ChildrenVisitor) interceptorChainVisitor);
                    }
                }

                contentDeliveryConfigExecutionEvents.add(new DefaultContentDeliveryConfigExecutionEvent(interceptorChain.getResourceConfig(), "Added as a SAX NG visitor."));
            }
        }
        
        saxNgContentDeliveryConfig.setRegistry(registry);
        saxNgContentDeliveryConfig.setResourceConfigs(resourceConfigTable);
        saxNgContentDeliveryConfig.getContentDeliveryConfigExecutionEvents().addAll(contentDeliveryConfigExecutionEvents);
        saxNgContentDeliveryConfig.addToExecutionLifecycleSets();
        
        return saxNgContentDeliveryConfig;
    }

    protected void assertSelectorsNotAccessingText(ResourceConfig resourceConfig) {
        if (resourceConfig.getSelectorPath() instanceof IndexedSelectorPath &&
                ((IndexedSelectorPath) resourceConfig.getSelectorPath()).getTargetSelectorStep() instanceof ElementSelectorStep &&
                ((ElementSelectorStep) ((IndexedSelectorPath) resourceConfig.getSelectorPath()).getTargetSelectorStep()).accessesText()) {
            throw new SmooksConfigException("Unsupported selector '" + resourceConfig.getSelectorPath().getSelector() + "' on resource '" + resourceConfig + "'.  The 'text()' XPath token is only supported on SAX Visitor implementations that implement the " + AfterVisitor.class.getName() + " interface only.  Class '" + resourceConfig.getResource() + "' implements other SAX Visitor interfaces.");
        }
    }

    @Override
    public Boolean isProvider(List<ContentHandlerBinding<Visitor>> contentHandlerBindings) {
        return contentHandlerBindings.stream().filter(c -> c.getContentHandler() instanceof BeforeVisitor || c.getContentHandler() instanceof AfterVisitor).count() == contentHandlerBindings.size();
    }

    @Override
    public String getName() {
        return "SAX NG";
    }
}