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
package org.smooks.engine.resource.config;

import org.smooks.api.resource.config.ReaderConfigurator;
import org.smooks.api.resource.config.ResourceConfig;
import org.smooks.engine.delivery.AbstractParser;
import org.xml.sax.XMLReader;

import java.util.*;

/**
 * Generic reader configurator.
 * <p/>
 * Specific reader implementations can define specialized configurators.
 * 
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class GenericReaderConfigurator implements ReaderConfigurator {

    private Class<?> readerClass;
    private Properties parameters = new Properties();
    private final List<String> featuresOn = new ArrayList<>();
    private final List<String> featuresOff = new ArrayList<>();
    private String targetProfile;

    public GenericReaderConfigurator() {
    }

    public GenericReaderConfigurator(Class<? extends XMLReader> readerClass) {
        this.readerClass = readerClass;
    }

    public Properties getParameters() {
        return parameters;
    }

    public GenericReaderConfigurator setParameters(Properties parameters) {
        this.parameters = parameters;
        return this;
    }

    public GenericReaderConfigurator setFeature(String feature, boolean on) {
        if(on) {
            featuresOn.add(feature);
        } else {
            featuresOff.add(feature);
        }
        return this;
    }

    public GenericReaderConfigurator setTargetProfile(String targetProfile) {
        this.targetProfile = targetProfile;
        return this;
    }

    @Override
    public List<ResourceConfig> toConfig() {
        ResourceConfig resourceConfig = new DefaultResourceConfig();
        resourceConfig.setSelector(AbstractParser.ORG_XML_SAX_DRIVER, new Properties());

        if(readerClass != null) {
            resourceConfig.setResource(readerClass.getName());
        }

        if(targetProfile != null) {
            resourceConfig.setTargetProfile(targetProfile);
        }

        // Add the parameters...
        Set<Map.Entry<Object, Object>> entries = parameters.entrySet();
        for (Map.Entry<Object, Object> entry : entries) {
            resourceConfig.setParameter((String)entry.getKey(), (String)entry.getValue());
        }

        // Add the "on" features...
        for(String featureOn : featuresOn) {
            resourceConfig.setParameter(AbstractParser.FEATURE_ON, featureOn);
        }

        // Add the "off" features...
        for(String featureOff : featuresOff) {
            resourceConfig.setParameter(AbstractParser.FEATURE_OFF, featureOff);
        }

        List<ResourceConfig> resourceConfigs = new ArrayList<>();
        resourceConfigs.add(resourceConfig);

        return resourceConfigs;
    }
}