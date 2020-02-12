/*
	Milyn - Copyright (C) 2006 - 2010

	This library is free software; you can redistribute it and/or
	modify it under the terms of the GNU Lesser General Public
	License (version 2.1) as published by the Free Software
	Foundation.

	This library is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

	See the GNU Lesser General Public License for more details:
	http://www.gnu.org/licenses/lgpl.txt
*/
package org.milyn.xml;

import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.cdr.annotation.AppContext;
import org.milyn.cdr.annotation.Config;
import org.milyn.container.ApplicationContext;
import org.milyn.container.ApplicationContextInitializer;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.annotation.Initialize;
import org.milyn.namespace.NamespaceDeclarationStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	
	@Config
	private SmooksResourceConfiguration config;
	
	@AppContext
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