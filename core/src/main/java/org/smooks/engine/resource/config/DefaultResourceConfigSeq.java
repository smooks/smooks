/*-
 * ========================LICENSE_START=================================
 * Core
 * %%
 * Copyright (C) 2020 - 2021 Smooks
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
package org.smooks.engine.resource.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smooks.api.profile.ProfileSet;
import org.smooks.api.resource.config.ConfigSearch;
import org.smooks.api.resource.config.ProfileTargetingExpression;
import org.smooks.api.resource.config.ResourceConfig;
import org.smooks.api.resource.config.ResourceConfigSeq;
import org.smooks.assertion.AssertArgument;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 * {@link ResourceConfig} list.
 * @author tfennelly
 */
public class DefaultResourceConfigSeq implements ResourceConfigSeq {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceConfigSeq.class);
    /**
     * List name.
     */
    private final String name;
    /**
     * Is the config list one of the "system" installed config lists.
     */
    private boolean isSystemConfigList;
    /**
     * {@link ProfileSet} list.
     */
    private final List<ProfileSet> profiles = new ArrayList<>();
    /**
     * {@link ResourceConfig} list.
     */
    private final List<ResourceConfig> resourceConfigs = new ArrayList<>();

    /**
     * Public constructor.
     * @param name The name of this instance.
     */
    public DefaultResourceConfigSeq(String name) {
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
    @Override
    public void add(ResourceConfig resourceConfig) {
        AssertArgument.isNotNull(resourceConfig, "resourceConfig");
        String[] selectors = resourceConfig.getSelectorPath().getSelector().split(",");

        for(String selector : selectors) {
            ResourceConfig clone = resourceConfig.copy();

            clone.setSelector(selector.trim());
            resourceConfigs.add(clone);
            LOGGER.debug("Smooks ResourceConfiguration [" + clone + "] added to list [" + name + "].");
        }
    }


    /**
     * Add all the {@link ResourceConfig} instances in the specified
     * {@link ResourceConfigSeq} to this list.
     * @param resourceConfigSeq {@link ResourceConfigSeq} instance to add.
     */
    @Override
    public void addAll(ResourceConfigSeq resourceConfigSeq) {
        resourceConfigs.addAll(resourceConfigSeq.getAll());
    }

    /**
     * Add a {@link ProfileSet} instance to this list.
     * @param profileSet {@link ProfileSet} instance to add.
     */
    @Override
    public void add(ProfileSet profileSet) {
        AssertArgument.isNotNull(profileSet, "profileSet");
        profiles.add(profileSet);
        LOGGER.debug("ProfileSet [" + profileSet.getBaseProfile() + "] added to list Smooks configuration [" + name + "].");
    }

    /**
     * Get the name of this list instance.
     * @return List name.
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Is this configuration list instance one of the system installed config lists.
     * @return True if this configuration list instance one of the system installed config lists, otherwise false.
     */
    @Override
    public boolean isSystemConfigList() {
        return isSystemConfigList;
    }

    /**
     * Set whether or not this configuration list instance is one of the system installed config lists.
     * @param systemConfigList True if this configuration list instance one of the system installed config lists, otherwise false.
     */
    @Override
    public void setSystemConfigList(boolean systemConfigList) {
        isSystemConfigList = systemConfigList;
    }

    /**
     * Is this list instance empty.
     * @return True if this list instance is empty, otherwise false.
     */
    @Override
    public boolean isEmpty() {
        return resourceConfigs.isEmpty();
    }

    /**
     * Get the size of this list.
     * @return The size of te list i.e. number of entries.
     */
    @Override
    public int size() {
        return resourceConfigs.size();
    }

    /**
     * Get the {@link ResourceConfig} instance at the specified index.
     * @param index Resource index.
     * @return The {@link ResourceConfig} instance at the specified index.
     * @throws ArrayIndexOutOfBoundsException The specified index is out of bounds.
     */
    @Override
    public ResourceConfig get(int index) throws ArrayIndexOutOfBoundsException {
        return resourceConfigs.get(index);
    }

    @Override
    public List<ResourceConfig> getAll() throws ArrayIndexOutOfBoundsException {
        return resourceConfigs;
    }

    /**
     * Get all ResourceConfig entries targeted at the specified profile set. 
     * @param profileSet The profile set to searh against.
     * @return All ResourceConfig entries targeted at the specified profile set.
     */
    @Override
    public List<ResourceConfig> getAll(ProfileSet profileSet) {
        List<ResourceConfig> matchingResourceConfigsColl = new ArrayList<>();

        // Iterate over the ResourceConfigs defined on this list.
        for(int i = 0; i < size(); i++) {
            ResourceConfig resourceConfig = get(i);
            ProfileTargetingExpression[] profileTargetingExpressions = resourceConfig.getProfileTargetingExpressions();

            for (ProfileTargetingExpression expression : profileTargetingExpressions) {
                if (expression.isMatch(profileSet)) {
                    matchingResourceConfigsColl.add(resourceConfig);
                    break;
                } else {
                    LOGGER.debug("Resource [" + resourceConfig + "] not targeted at profile [" + profileSet.getBaseProfile() + "].  Sub Profiles: [" + profileSet + "]");
                }
            }
        }
        
        return matchingResourceConfigsColl;
    }

    /**
     * Get the list of profiles configured on this resource configuration list.
     * @return List of profiles.
     */
    @Override
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
    @Override
    public List<ResourceConfig> lookupResource(ConfigSearch searchCriteria) {
        List<ResourceConfig> results = new ArrayList<>();

        for(ResourceConfig resourceConfig : resourceConfigs) {
            if(searchCriteria.matches(resourceConfig)) {
                results.add(resourceConfig);
            }
        }

        return results;
    }

    @Override
    public Iterator<ResourceConfig> iterator() {
        return resourceConfigs.iterator();
    }
}
