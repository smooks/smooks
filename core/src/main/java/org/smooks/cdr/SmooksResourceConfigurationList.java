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
import java.util.List;
import java.util.Vector;

/**
 * {@link org.smooks.cdr.SmooksResourceConfiguration} list.
 * @author tfennelly
 */
public class SmooksResourceConfigurationList {

	/**
	 * Logger.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(SmooksResourceConfigurationList.class);
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
     * {@link org.smooks.cdr.SmooksResourceConfiguration} list.
     */
    private final List<SmooksResourceConfiguration> smooksResourceConfigurations = new ArrayList<SmooksResourceConfiguration>();
    /**
     * List of loaded resource URIs.
     */
    private final List<URI> loadedResources = new ArrayList<URI>();

    /**
     * Public constructor.
     * @param name The name of this instance.
     */
    public SmooksResourceConfigurationList(String name) {
        if(name == null || (name = name.trim()).equals("")) {
            throw new IllegalArgumentException("null or empty 'name' arg in constructor call.");
        }
        this.name = name;
        LOGGER.debug("Smooks ResourceConfiguration List [" + name + "] created.");
    }
    
    /**
     * Add a {@link SmooksResourceConfiguration} instance to this list.
     * @param config {@link SmooksResourceConfiguration} instance to add.
     */
    public void add(SmooksResourceConfiguration config) {
        AssertArgument.isNotNull(config, "config");
        String[] selectors = config.getSelectorPath().getSelector().split(",");

        for(String selector : selectors) {
            SmooksResourceConfiguration clone = (SmooksResourceConfiguration) config.clone();

            clone.setSelector(selector.trim());
            smooksResourceConfigurations.add(clone);
            LOGGER.debug("Smooks ResourceConfiguration [" + clone + "] added to list [" + name + "].");
        }
    }


    /**
     * Add all the {@link SmooksResourceConfiguration} instances in the specified
     * {@link SmooksResourceConfigurationList} to this list.
     * @param configList {@link SmooksResourceConfigurationList} instance to add.
     */
    public void addAll(SmooksResourceConfigurationList configList) {
        smooksResourceConfigurations.addAll(configList.smooksResourceConfigurations);
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
        return smooksResourceConfigurations.isEmpty();
    }

    /**
     * Get the size of this list.
     * @return The size of te list i.e. number of entries.
     */
    public int size() {
        return smooksResourceConfigurations.size();
    }

    /**
     * Get the {@link SmooksResourceConfiguration} instance at the specified index.
     * @param index Resource index.
     * @return The {@link SmooksResourceConfiguration} instance at the specified index.
     * @throws ArrayIndexOutOfBoundsException The specified index is out of bounds.
     */
    public SmooksResourceConfiguration get(int index) throws ArrayIndexOutOfBoundsException {
        return smooksResourceConfigurations.get(index);
    }
    
    /**
     * Get all SmooksResourceConfiguration entries targeted at the specified profile set. 
     * @param profileSet The profile set to searh against.
     * @return All SmooksResourceConfiguration entries targeted at the specified profile set.
     */
    public SmooksResourceConfiguration[] getTargetConfigurations(ProfileSet profileSet) {
        Vector<SmooksResourceConfiguration> matchingSmooksResourceConfigurationsColl = new Vector<SmooksResourceConfiguration>();
        SmooksResourceConfiguration[] matchingSmooksResourceConfigurations;
        
        // Iterate over the SmooksResourceConfigurations defined on this list.
        for(int i = 0; i < size(); i++) {
            SmooksResourceConfiguration resourceConfig = get(i);
            ProfileTargetingExpression[] profileTargetingExpressions = resourceConfig.getProfileTargetingExpressions();
            
            for(int expIndex = 0; expIndex < profileTargetingExpressions.length; expIndex++) {
                ProfileTargetingExpression expression = profileTargetingExpressions[expIndex];

                if(expression.isMatch(profileSet)) {
                    matchingSmooksResourceConfigurationsColl.addElement(resourceConfig);
                    break;
                } else {
            		LOGGER.debug("Resource [" + resourceConfig + "] not targeted at profile [" + profileSet.getBaseProfile() + "].  Sub Profiles: [" + profileSet + "]");
                }
            }
        }

        matchingSmooksResourceConfigurations = new SmooksResourceConfiguration[matchingSmooksResourceConfigurationsColl.size()];
        matchingSmooksResourceConfigurationsColl.toArray(matchingSmooksResourceConfigurations);
        
        return matchingSmooksResourceConfigurations;
    }

    /**
     * Get the list of profiles configured on this resource configuration list.
     * @return List of profiles.
     */
    public List<ProfileSet> getProfiles() {
        return profiles;
    }

    protected boolean addSourceResourceURI(URI resource) {
        AssertArgument.isNotNull(resource, "resource");

        if(loadedResources.contains(resource)) {
            URI lastLoaded = loadedResources.get(loadedResources.size() - 1);

            LOGGER.info("Not adding resource config import '" + resource + "'.  This resource is already loaded on this list.");

            return false;
        }
        
        loadedResources.add(resource);
        return true;
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
	public List<SmooksResourceConfiguration> lookupResource(ConfigSearch searchCriteria) {
		List<SmooksResourceConfiguration> results = new ArrayList<SmooksResourceConfiguration>();
		
		for(SmooksResourceConfiguration config : smooksResourceConfigurations) {
			if(searchCriteria.matches(config)) {
				results.add(config);
			}
		}
		
		return results;
	}
}
