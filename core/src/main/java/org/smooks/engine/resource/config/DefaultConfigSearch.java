/*-
 * ========================LICENSE_START=================================
 * Smooks Core
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

import org.smooks.api.resource.config.ConfigSearch;
import org.smooks.api.resource.config.ResourceConfig;

import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class DefaultConfigSearch implements ConfigSearch {
    private String configNS;
    private String selector;
    private String selectorNS;
    private String resource;
    private final Properties params = new Properties();

    public String getConfigNS() {
        return configNS;
    }

    @Override
    public ConfigSearch configNS(String configNS) {
        this.configNS = configNS;
        return this;
    }

    @Override
    public String getSelector() {
        return selector;
    }

    @Override
    public ConfigSearch selector(String selector) {
        this.selector = selector;
        return this;
    }

    public String getSelectorNS() {
        return selectorNS;
    }

    @Override
    public ConfigSearch selectorNS(String selectorNS) {
        this.selectorNS = selectorNS;
        return this;
    }

    @Override
    public String getResource() {
        return resource;
    }

    @Override
    public ConfigSearch resource(String resource) {
        this.resource = resource;
        return this;
    }

    @Override
    public ConfigSearch param(String name, String value) {
        params.setProperty(name, value);
        return this;
    }

    @Override
    public boolean matches(ResourceConfig resourceConfig) {
        if(configNS != null) {
            if(resourceConfig.getExtendedConfigNS() == null || !resourceConfig.getExtendedConfigNS().startsWith(configNS)) {
                return false;
            }
        }
        if(selector != null) {
            if(resourceConfig.getSelectorPath().getSelector() == null || !resourceConfig.getSelectorPath().getSelector().equalsIgnoreCase(selector)) {
                return false;
            }
        }
        if(selectorNS != null) {
            if(resourceConfig.getSelectorPath().getSelectorNamespaceURI() == null || !resourceConfig.getSelectorPath().getSelectorNamespaceURI().equals(selectorNS)) {
                return false;
            }
        }
        if(resource != null) {
            if(resourceConfig.getResource() == null || !resourceConfig.getResource().equals(resource)) {
                return false;
            }
        }

        if(!params.isEmpty()) {
            Set<Map.Entry<Object, Object>> entries = params.entrySet();
            for(Map.Entry<Object, Object> entry : entries) {
                String expectedValue = (String) entry.getValue();
                String actualValue = resourceConfig.getParameterValue((String) entry.getKey(), String.class);

                if(!expectedValue.equals(actualValue)) {
                    return false;
                }
            }
        }

        return true;
    }
}
