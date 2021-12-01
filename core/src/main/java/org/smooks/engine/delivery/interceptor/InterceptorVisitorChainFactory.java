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
import org.smooks.api.SmooksException;
import org.smooks.api.delivery.ContentHandlerBinding;
import org.smooks.api.resource.config.ResourceConfig;
import org.smooks.api.resource.config.xpath.SelectorStep;
import org.smooks.api.resource.visitor.Visitor;
import org.smooks.api.resource.visitor.interceptor.InterceptorVisitor;
import org.smooks.engine.delivery.DefaultContentHandlerBinding;
import org.smooks.engine.injector.Scope;
import org.smooks.engine.lifecycle.PostConstructLifecyclePhase;
import org.smooks.engine.lookup.LifecycleManagerLookup;
import org.smooks.engine.resource.config.DefaultResourceConfig;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * Namespace Mappings.
 * <p/>
 * This handler loads namespace prefix-to-uri mappings into the {@link ApplicationContext}.
 * 
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class InterceptorVisitorChainFactory {

	@Inject
	private ResourceConfig resourceConfig;

	@Inject
	private ApplicationContext applicationContext;

	protected final List<InterceptorVisitorDefinition> interceptorVisitorDefinitions = new ArrayList<>();
	
	public ContentHandlerBinding<Visitor> createInterceptorChain(final ContentHandlerBinding<Visitor> visitorBinding) {
		if (interceptorVisitorDefinitions.isEmpty()) {
			return visitorBinding;
		} else {
			ContentHandlerBinding<Visitor> interceptedVisitorBinding = visitorBinding;
			for (InterceptorVisitorDefinition interceptorVisitorDefinition : interceptorVisitorDefinitions) {
				InterceptorVisitor interceptorVisitor;
				try {
					interceptorVisitor = interceptorVisitorDefinition.getInterceptorVisitorClass().newInstance();
				} catch (InstantiationException | IllegalAccessException e) {
					throw new SmooksException(e.getMessage(), e);
				}
				interceptorVisitor.setVisitorBinding(interceptedVisitorBinding);
				
				final ResourceConfig interceptorResourceConfig = new DefaultResourceConfig(visitorBinding.getResourceConfig());
				if (interceptorVisitorDefinition.getSelector().isPresent()) {
					interceptorResourceConfig.setSelector(interceptorVisitorDefinition.getSelector().get(), interceptorResourceConfig.getSelectorPath().getNamespaces());
				}
				applicationContext.getRegistry().lookup(new LifecycleManagerLookup()).applyPhase(interceptorVisitor, new PostConstructLifecyclePhase(new Scope(applicationContext.getRegistry(), interceptorResourceConfig, interceptorVisitor)));
				interceptedVisitorBinding = new DefaultContentHandlerBinding<>(interceptorVisitor, interceptorResourceConfig);
			}
			
			return interceptedVisitorBinding;
		}
	}

	public List<InterceptorVisitorDefinition> getInterceptorVisitorDefinitions() {
		return interceptorVisitorDefinitions;
	}

	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}
}