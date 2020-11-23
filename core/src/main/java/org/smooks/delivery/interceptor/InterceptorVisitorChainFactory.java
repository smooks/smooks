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
package org.smooks.delivery.interceptor;

import org.smooks.SmooksException;
import org.smooks.cdr.Parameter;
import org.smooks.cdr.ResourceConfig;
import org.smooks.container.ApplicationContext;
import org.smooks.delivery.ContentHandlerBinding;
import org.smooks.delivery.Visitor;
import org.smooks.util.ClassUtil;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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

	private final List<Class<InterceptorVisitor>> interceptorVisitorClasses = new ArrayList<>();

	@PostConstruct
	public void postConstruct() throws ClassNotFoundException {
		List<Parameter> interceptorClasses = resourceConfig.getParameters("class");
		if (interceptorClasses != null) {
			for (String interceptorClassName : interceptorClasses.stream().map(Parameter::toString).collect(Collectors.toList())) {
				interceptorVisitorClasses.add(ClassUtil.forName(interceptorClassName, this.getClass()));
			}
		}
	}

	public Visitor createInterceptorChain(final ContentHandlerBinding<Visitor> visitorBinding) {
		if (interceptorVisitorClasses.isEmpty()) {
			return visitorBinding.getContentHandler();
		} else {
			InterceptorVisitor interceptorVisitor = null;
			ContentHandlerBinding<Visitor> interceptedVisitorBinding = visitorBinding;
			for (Class<InterceptorVisitor> interceptorVisitorClass : interceptorVisitorClasses) {
				try {
					interceptorVisitor = interceptorVisitorClass.newInstance();
				} catch (InstantiationException | IllegalAccessException e) {
					throw new SmooksException(e.getMessage(), e);
				}
				interceptorVisitor.setVisitorBinding(interceptedVisitorBinding);
				interceptorVisitor.setApplicationContext(applicationContext);
				interceptedVisitorBinding = new ContentHandlerBinding<>(interceptorVisitor, visitorBinding.getResourceConfig());
			}
			
			return interceptorVisitor;
		}

	}
}