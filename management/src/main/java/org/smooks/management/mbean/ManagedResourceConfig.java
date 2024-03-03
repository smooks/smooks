/*-
 * ========================LICENSE_START=================================
 * Management
 * %%
 * Copyright (C) 2020 - 2024 Smooks
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
package org.smooks.management.mbean;

import org.smooks.api.SmooksException;
import org.smooks.api.management.InstrumentationResource;
import org.smooks.api.management.MBean;
import org.smooks.api.resource.config.Parameter;
import org.smooks.api.resource.config.ResourceConfig;
import org.smooks.api.resource.config.ResourceConfigSeq;
import org.smooks.management.annotation.ManagedAttribute;
import org.smooks.management.annotation.ManagedResource;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@ManagedResource
public class ManagedResourceConfig extends AbstractMBean {

    private final ResourceConfig resourceConfig;
    private final ResourceConfigSeq resourceConfigSeq;

    public ManagedResourceConfig(ResourceConfigSeq resourceConfigSeq, ResourceConfig resourceConfig, InstrumentationResource instrumentationResource) {
        super(instrumentationResource);
        this.resourceConfig = resourceConfig;
        this.resourceConfigSeq = resourceConfigSeq;
    }

    @ManagedAttribute(description = "Parameters")
    public Map<String, Object> getParameters() {
        Map<String, Object> parametersAsMap = new HashMap<>(resourceConfig.getParameterCount());
        List<?> parameters = resourceConfig.getParameterValues();
        for (Object parameter : parameters) {
            if (parameter instanceof Parameter ) {
                parametersAsMap.put(((Parameter<?>) parameter).getName(), ((Parameter<?>) parameter).getValue());
            } else if (parameter instanceof List) {
                List<Object> paramList = new ArrayList<>(((List<?>) parameter).size());
                for (Parameter<?> o : ((List<Parameter<?>>) parameter)) {
                    paramList.add(o.getValue());
                }
                parametersAsMap.put(((List<Parameter>) parameter).get(0).getName(), paramList);
            }
        }

        return parametersAsMap;
    }

    @ManagedAttribute(description = "Resource type")
    public String getResourceType() {
        return resourceConfig.getResourceType();
    }

    @ManagedAttribute(description = "Selector")
    public String getSelector() {
        return resourceConfig.getSelectorPath().getSelector();
    }

    @ManagedAttribute(description = "Selector namespaces")
    public Properties getSelectorNamespaces() {
        return resourceConfig.getSelectorPath().getNamespaces();
    }

    @ManagedAttribute(description = "Resource")
    public String getResource() {
        return resourceConfig.getResource();
    }

    @Override
    protected String getName() {
        return resourceConfig.getClass().getName() + "@" + Integer.toHexString(hashCode());
    }

    @Override
    protected String getType() {
        return "resourceConfig";
    }

    @Override
    public String getContext() {
        return resourceConfigSeq.getName();
    }
}
