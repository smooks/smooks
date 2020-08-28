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
package org.smooks.delivery.dom;

import org.smooks.cdr.SmooksResourceConfiguration;
import org.smooks.container.ApplicationContext;
import org.smooks.delivery.ContentDeliveryConfig;
import org.smooks.delivery.StreamDeliveryProvider;
import org.smooks.delivery.VisitorConfigMap;
import org.smooks.dtd.DTDStore;
import org.smooks.event.types.ConfigBuilderEvent;

import java.util.List;
import java.util.Map;

public class DOMStreamDeliveryProvider implements StreamDeliveryProvider {
    @Override
    public ContentDeliveryConfig createContentDeliveryConfig(VisitorConfigMap visitorConfigMap, ApplicationContext applicationContext, Map<String, List<SmooksResourceConfiguration>> resourceConfigTable, List<ConfigBuilderEvent> configBuilderEvents, DTDStore.DTDObjectContainer dtdObjectContainer, Boolean sortVisitors) {
        DOMContentDeliveryConfig domConfig = new DOMContentDeliveryConfig();

        domConfig.setAssemblyVisitBefores(visitorConfigMap.getDomAssemblyVisitBefores());
        domConfig.setAssemblyVisitAfters(visitorConfigMap.getDomAssemblyVisitAfters());
        domConfig.setProcessingVisitBefores(visitorConfigMap.getDomProcessingVisitBefores());
        domConfig.setProcessingVisitAfters(visitorConfigMap.getDomProcessingVisitAfters());
        domConfig.setSerializationVisitors(visitorConfigMap.getDomSerializationVisitors());
        domConfig.setVisitCleanables(visitorConfigMap.getVisitCleanables());

        domConfig.setApplicationContext(applicationContext);
        domConfig.setSmooksResourceConfigurations(resourceConfigTable);
        domConfig.setDtd(dtdObjectContainer);
        domConfig.getConfigBuilderEvents().addAll(configBuilderEvents);

        if(sortVisitors) {
            domConfig.sort();
        }

        domConfig.addToExecutionLifecycleSets();
        domConfig.initializeXMLReaderPool();
        domConfig.configureFilterBypass();

        return domConfig;
    }

    @Override
    public Boolean isProvider(VisitorConfigMap visitorConfigMap) {
        return visitorConfigMap.getDomVisitorCount() == visitorConfigMap.getVisitorCount();
    }

    @Override
    public String getName() {
        return "DOM";
    }
}
