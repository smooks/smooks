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
package org.smooks.xml;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smooks.cdr.SmooksResourceConfiguration;
import org.smooks.container.ApplicationContext;
import org.smooks.container.ApplicationContextInitializer;
import org.smooks.container.ExecutionContext;
import org.smooks.delivery.annotation.Initialize;
import org.smooks.namespace.NamespaceDeclarationStack;

import javax.inject.Inject;
import java.util.Properties;

/**
 * Namespace Mappings.
 * <p/>
 * This handler loads namespace prefix-to-uri mappings into the {@link ApplicationContext}.
 * 
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class NamespaceMappings implements ApplicationContextInitializer {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(NamespaceMappings.class);
	
	@Inject
	private SmooksResourceConfiguration config;
	
	@Inject
	private ApplicationContext appContext;
	
	/**
	 * Load the namespace prefix-to-uri mappings into the {@link ApplicationContext}.
	 */
	@Initialize
	public void loadNamespaces() {
		Properties namespaces = getMappings(appContext);
		Properties namespacesToAdd = config.toProperties();
		
		if(LOGGER.isDebugEnabled()) {
			LOGGER.debug("Adding namespace prefix-to-uri mappings: " + namespacesToAdd);
		}
		namespaces.putAll(namespacesToAdd);

        setMappings(namespaces, appContext);
    }

    /**
     * Set the namespace prefix-to-uri mappings.
     * @param namespaces The namespace mappings.
     * @param appContext The application context.
     */
    public static void setMappings(Properties namespaces, ApplicationContext appContext) {
        appContext.setAttribute(NamespaceMappings.class, namespaces);
    }

    /**
	 * Get the prefix-to-namespace mannings from the {@link ApplicationContext}.
	 * @param appContext The {@link ApplicationContext}.
	 * @return The prefix-to-namespace mannings.
	 */
	public static Properties getMappings(ApplicationContext appContext) {
		Properties properties = (Properties) appContext.getAttribute(NamespaceMappings.class);
		if(properties == null) {
			return new Properties();
		}
		return properties;
	}

    /**
     * Set the {@link NamespaceDeclarationStack} for the current message on the current {@link ExecutionContext}.
     * @param namespaceDeclarationStack The {@link NamespaceDeclarationStack} instance.
     * @param executionContext The execution context.
     */
    public static void setNamespaceDeclarationStack(NamespaceDeclarationStack namespaceDeclarationStack, ExecutionContext executionContext) {
        executionContext.setAttribute(NamespaceDeclarationStack.class, namespaceDeclarationStack);
    }

    /**
     * Get the {@link NamespaceDeclarationStack} for the current message from the current {@link ExecutionContext}.
     * @param executionContext The execution context.
     */
    public static NamespaceDeclarationStack getNamespaceDeclarationStack(ExecutionContext executionContext) {
        return (NamespaceDeclarationStack) executionContext.getAttribute(NamespaceDeclarationStack.class);
    }
}
