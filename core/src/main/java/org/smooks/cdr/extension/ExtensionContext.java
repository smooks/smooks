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
package org.smooks.cdr.extension;

import org.smooks.cdr.*;
import org.smooks.container.ExecutionContext;
import org.smooks.expression.ExpressionEvaluator;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Context object used by Smooks configuration extension visitors.
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class ExtensionContext {

    private static final String EXEC_CONTEXT_KEY = ExtensionContext.class.getName() + "#EXEC_CONTEXT_KEY";

    private final XMLConfigDigester xmlConfigDigester;
    private final String defaultSelector;
    private final String defaultNamespace;
    private final String defaultProfile;
    private final ExpressionEvaluator defaultConditionEvaluator;

    private final Stack<SmooksResourceConfiguration> resourceStack = new Stack<SmooksResourceConfiguration>() {
        @Override
        public SmooksResourceConfiguration push(SmooksResourceConfiguration smooksResourceConfiguration) {
            if(!isEmpty()) {
                smooksResourceConfiguration.addChangeListener(resChangeListener);
            }
            return super.push(smooksResourceConfiguration);
        }

        @Override
        public SmooksResourceConfiguration pop() {
            SmooksResourceConfiguration smooksResourceConfiguration = super.pop();
            if(!isEmpty()) {
                smooksResourceConfiguration.removeChangeListener(resChangeListener);
            }
            return smooksResourceConfiguration;
        }
    };
    private final SmooksResourceConfigurationChangeListener resChangeListener = new SmooksResourceConfigurationChangeListener() {
        public void changed(SmooksResourceConfiguration configuration) {
            String selector = configuration.getSelector();
            if(selector != null && selector.startsWith("#/")) {
                SmooksResourceConfiguration parentResource = resourceStack.get(resourceStack.size() - 2);
                configuration.setSelector(parentResource.getSelector() + selector.substring(1));
            }
        }
    };
    private final List<SmooksResourceConfiguration> resources = new ArrayList<SmooksResourceConfiguration>();

    /**
     * Public constructor.
     * @param xmlConfigDigester The base XMLConfigDigester.
     * @param defaultSelector The default selector.
     * @param defaultNamespace The default namespace.
     * @param defaultProfile The default profile.
     * @param defaultConditionEvaluator The default condition evaluator.
     */
    public ExtensionContext(XMLConfigDigester xmlConfigDigester, String defaultSelector, String defaultNamespace, String defaultProfile, ExpressionEvaluator defaultConditionEvaluator) {
        this.xmlConfigDigester = xmlConfigDigester;
        this.defaultSelector = defaultSelector;
        this.defaultNamespace = defaultNamespace;
        this.defaultProfile = defaultProfile;
        this.defaultConditionEvaluator = defaultConditionEvaluator;
    }

    /**
     * Set the {@link ExtensionContext} on the {@link org.smooks.container.ExecutionContext}.
     * @param extensionContext Extension Context.
     * @param executionContext Execution Context.
     */
    public static void setExtensionContext(ExtensionContext extensionContext, ExecutionContext executionContext) {
        executionContext.setAttribute(EXEC_CONTEXT_KEY, extensionContext);
    }

    /**
     * Get the {@link ExtensionContext} from the {@link org.smooks.container.ExecutionContext}.
     * @param executionContext Execution Context.
     * @return Extension Context.
     */
    public static ExtensionContext getExtensionContext(ExecutionContext executionContext) {
        return (ExtensionContext) executionContext.getAttribute(EXEC_CONTEXT_KEY);
    }

    /**
     * Add a resource configuration to the list of resources for this Extension Context.
     * <p/>
     * The resource gets added to the {@link #getResourceStack() resourceStack} and the
     * basic list of {@link #getResources() resources}.
     *
     * @param resource The resource to be added.
     */
    public void addResource(SmooksResourceConfiguration resource) {
        resourceStack.push(resource);
        resources.add(resource);
    }

    /**
     * Add a resource configuration template to the resources stack for this Extension Context.
     * <p/>
     * This resource is not added as a resource on the Smooks instance, but is instead available
     * for cloning.
     *
     * @param resource The resource to be added.
     */
    public void addResourceTemplate(SmooksResourceConfiguration resource) {
        resourceStack.push(resource);
    }

    /**
     * Get the resource stack.
     * @return The resource stack.
     * @see #addResource(org.smooks.cdr.SmooksResourceConfiguration)
     */
    public Stack<SmooksResourceConfiguration> getResourceStack() {
        return resourceStack;
    }

    /**
     * Get the resource list.
     * @return The resource list.
     * @see #addResource(org.smooks.cdr.SmooksResourceConfiguration)
     */
    public List<SmooksResourceConfiguration> getResources() {
        return resources;
    }
    
    /**
     * Get the active resource configuration list.
     * <p/>
     * This is the global config list i.e. not just the config list for the config
     * being processed.
     * 
     * @return The active resource configuration list.
     */
    public SmooksResourceConfigurationList getResourceList() {
    	return xmlConfigDigester.getResourceList();
    }

    public SmooksResourceConfiguration getCurrentConfig() {
        if(resourceStack.isEmpty()) {
            return null;
        } else {
            return resourceStack.peek();
        }
    }

    public XMLConfigDigester getXmlConfigDigester() {
        return xmlConfigDigester;
    }

    public String getDefaultSelector() {
        return defaultSelector;
    }

    public String getDefaultNamespace() {
        return defaultNamespace;
    }

    public String getDefaultProfile() {
        return defaultProfile;
    }

    public ExpressionEvaluator getDefaultConditionEvaluator() {
        return defaultConditionEvaluator;
    }

	public SmooksResourceConfiguration getResourceByName(String name) {
		for(int i = resourceStack.size() - 1; i >= 0; i--) {
			SmooksResourceConfiguration config = resourceStack.get(i);
			String resourceName = config.getResource();
			if(name.equals(resourceName)) {
				return config;
			}
		}

		return null;
	}

	/**
	 * Lookup an existing resource configuration from the global config list.
	 * <p/>
	 * Note that this is resource config order-dependent.  It will not locate configs that
	 * have not yet been loaded.
	 *
	 * @param searchCriteria The resource lookup criteria.
	 * @return List of matches resources, or an empty List if no matches are found.
	 */
	public List<SmooksResourceConfiguration> lookupResource(ConfigSearch searchCriteria) {
		return xmlConfigDigester.getResourceList().lookupResource(searchCriteria);
	}
}
