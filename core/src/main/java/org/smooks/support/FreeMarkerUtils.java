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
package org.smooks.support;

import freemarker.ext.dom.NodeModel;
import org.smooks.api.ExecutionContext;
import org.smooks.api.TypedKey;
import org.smooks.engine.resource.visitor.dom.DOMModel;
import org.smooks.api.bean.context.BeanContext;
import org.w3c.dom.Element;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * FreeMarker utility class.
 *
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public abstract class FreeMarkerUtils {

    private static final TypedKey<Map<String, ElementToNodeModel>> ELEMENT_TO_NODE_MODEL_TYPED_KEY = TypedKey.of();

    /**
     * Get a "merged" model for FreeMarker templating.
     * <p/>
     * This utility merges the current set of beans being managed by the
     * {@link BeanContext} associated with the
     * current {@link ExecutionContext}, with the contents of the {@link DOMModel}
     * associated with the current {@link ExecutionContext}.  This is very useful
     * for templating with FreeMarker.
     *
     * @param executionContext The current execution context.
     * @return A merged templating model.
     */
    public static Map<String, Object> getMergedModel(ExecutionContext executionContext) {
        Map<String, Object> beans = executionContext.getBeanContext().getBeanMap();
        Map<String, Object> model = beans;
        DOMModel domModel = DOMModel.getModel(executionContext);

        if (!domModel.getModels().isEmpty()) {
            Map<String, ElementToNodeModel> elementToNodeModelMap = getElementToNodeModelMap(executionContext);

            model = new HashMap<>(beans);

            Set<Map.Entry<String, Element>> models = domModel.getModels().entrySet();
            for (Map.Entry<String, Element> entry : models) {
                NodeModel nodeModel = getNodeModel(entry.getKey(), entry.getValue(), elementToNodeModelMap);
                model.put(entry.getKey(), nodeModel);
            }
        }

        return model;
    }

    private static NodeModel getNodeModel(String key, Element element, Map<String, ElementToNodeModel> elementToNodeModelMap) {
        ElementToNodeModel elementToNodeModel = elementToNodeModelMap.get(key);

        if (elementToNodeModel == null) {
            elementToNodeModel = new ElementToNodeModel();
            elementToNodeModelMap.put(key, elementToNodeModel);
            elementToNodeModel.element = element;
            elementToNodeModel.nodeModel = NodeModel.wrap(element);
        } else if (elementToNodeModel.element != element) {
            // Must be a new element with the same name... update the map...
            elementToNodeModel.element = element;
            elementToNodeModel.nodeModel = NodeModel.wrap(element);
        }

        return elementToNodeModel.nodeModel;
    }


    private static Map<String, ElementToNodeModel> getElementToNodeModelMap(ExecutionContext executionContext) {
        @SuppressWarnings("unchecked")
        Map<String, ElementToNodeModel> map = executionContext.get(ELEMENT_TO_NODE_MODEL_TYPED_KEY);

        if (map == null) {
            map = new HashMap<>();
            executionContext.put(ELEMENT_TO_NODE_MODEL_TYPED_KEY, map);
        }

        return map;
    }

    private static class ElementToNodeModel {
        private Element element;
        private NodeModel nodeModel;
    }
}
