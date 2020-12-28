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
package org.smooks.cdr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smooks.assertion.AssertArgument;
import org.smooks.profile.ProfileSet;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * {@link ResourceConfig} list.
 * @author tfennelly
 */
public class ResourceConfigList implements Iterable<ResourceConfig> {

	/**
	 * Logger.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(ResourceConfigList.class);
    /**
     * List name.
     */
    private final String name;
    /**
     * Is the config list one of the "system" installed config lists.
     */
    private boolean isSystemConfigList = false;
    /**
     * {@link ProfileSet} list.
     */
    private final List<ProfileSet> profiles = new Vector<ProfileSet>();
    /**
     * {@link ResourceConfig} list.
     */
    private final List<ResourceConfig> resourceConfigs = new ArrayList<ResourceConfig>();
    /**
     * List of loaded resource URIs.
     */
    private final List<URI> loadedResources = new ArrayList<URI>();

    /**
     * Public constructor.
     * @param name The name of this instance.
     */
    public ResourceConfigList(String name) {
        if(name == null || (name = name.trim()).equals("")) {
            throw new IllegalArgumentException("null or empty 'name' arg in constructor call.");
        }
        this.name = name;
        LOGGER.debug("Smooks ResourceConfiguration List [" + name + "] created.");
    }
    
    /**
     * Add a {@link ResourceConfig} instance to this list.
     * @param resourceConfig {@link ResourceConfig} instance to add.
     */
    public void add(ResourceConfig resourceConfig) {
        AssertArgument.isNotNull(resourceConfig, "resourceConfig");
        String[] selectors = resourceConfig.getSelectorPath().getSelector().split(",");

        for(String selector : selectors) {
            ResourceConfig clone = (ResourceConfig) resourceConfig.clone();

            clone.setSelector(selector.trim());
            resourceConfigs.add(clone);
            LOGGER.debug("Smooks ResourceConfiguration [" + clone + "] added to list [" + name + "].");
        }
    }


    /**
     * Add all the {@link ResourceConfig} instances in the specified
     * {@link ResourceConfigList} to this list.
     * @param configList {@link ResourceConfigList} instance to add.
     */
    public void addAll(ResourceConfigList configList) {
        resourceConfigs.addAll(configList.resourceConfigs);
    }

    /**
     * Add a {@link ProfileSet} instance to this list.
     * @param profileSet {@link ProfileSet} instance to add.
     */
    public void add(ProfileSet profileSet) {
        AssertArgument.isNotNull(profileSet, "profileSet");
        profiles.add(profileSet);
        LOGGER.debug("ProfileSet [" + profileSet.getBaseProfile() + "] added to list Smooks configuration [" + name + "].");
    }

    /**
     * Get the name of this list instance.
     * @return List name.
     */
    public String getName() {
        return name;
    }

    /**
     * Is this configuration list instance one of the system installed config lists.
     * @return True if this configuration list instance one of the system installed config lists, otherwise false.
     */
    public boolean isSystemConfigList() {
        return isSystemConfigList;
    }

    /**
     * Set whether or not this configuration list instance is one of the system installed config lists.
     * @param systemConfigList True if this configuration list instance one of the system installed config lists, otherwise false.
     */
    public void setSystemConfigList(boolean systemConfigList) {
        isSystemConfigList = systemConfigList;
    }

    /**
     * Is this list instance empty.
     * @return True if this list instance is empty, otherwise false.
     */
    public boolean isEmpty() {
        return resourceConfigs.isEmpty();
    }

    /**
     * Get the size of this list.
     * @return The size of te list i.e. number of entries.
     */
    public int size() {
        return resourceConfigs.size();
    }

    /**
     * Get the {@link ResourceConfig} instance at the specified index.
     * @param index Resource index.
     * @return The {@link ResourceConfig} instance at the specified index.
     * @throws ArrayIndexOutOfBoundsException The specified index is out of bounds.
     */
    public ResourceConfig get(int index) throws ArrayIndexOutOfBoundsException {
        return resourceConfigs.get(index);
    }
    
    /**
     * Get all ResourceConfig entries targeted at the specified profile set. 
     * @param profileSet The profile set to searh against.
     * @return All ResourceConfig entries targeted at the specified profile set.
     */
    public ResourceConfig[] getTargetConfigurations(ProfileSet profileSet) {
        Vector<ResourceConfig> matchingResourceConfigsColl = new Vector<>();
        ResourceConfig[] matchingResourceConfigs;
        
        // Iterate over the ResourceConfigs defined on this list.
        for(int i = 0; i < size(); i++) {
            ResourceConfig resourceConfig = get(i);
            ProfileTargetingExpression[] profileTargetingExpressions = resourceConfig.getProfileTargetingExpressions();
            
            for(int expIndex = 0; expIndex < profileTargetingExpressions.length; expIndex++) {
                ProfileTargetingExpression expression = profileTargetingExpressions[expIndex];

                if(expression.isMatch(profileSet)) {
                    matchingResourceConfigsColl.addElement(resourceConfig);
                    break;
                } else {
            		LOGGER.debug("Resource [" + resourceConfig + "] not targeted at profile [" + profileSet.getBaseProfile() + "].  Sub Profiles: [" + profileSet + "]");
                }
            }
        }

        matchingResourceConfigs = new ResourceConfig[matchingResourceConfigsColl.size()];
        matchingResourceConfigsColl.toArray(matchingResourceConfigs);
        
        return matchingResourceConfigs;
    }

    /**
     * Get the list of profiles configured on this resource configuration list.
     * @return List of profiles.
     */
    public List<ProfileSet> getProfiles() {
        return profiles;
    }

	/**
	 * Lookup a resource configuration from this config list.
	 * <p/>
	 * Note that this is resource config order-dependent.  It will not locate configs that
	 * have not yet been loaded.
	 *
	 * @param searchCriteria The resource lookup criteria.
	 * @return List of matches resources, or an empty List if no matches are found.
	 */
	public List<ResourceConfig> lookupResource(ConfigSearch searchCriteria) {
		List<ResourceConfig> results = new ArrayList<ResourceConfig>();
		
		for(ResourceConfig config : resourceConfigs) {
			if(searchCriteria.matches(config)) {
				results.add(config);
			}
		}
		
		return results;
	}

    @Override
    public Iterator<ResourceConfig> iterator() {
        return resourceConfigs.iterator();
    }
}
