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
package org.smooks.engine.bean.context;

import org.smooks.api.bean.context.BeanIdStore;
import org.smooks.assertion.AssertArgument;
import org.smooks.api.bean.repository.BeanId;
import org.smooks.engine.bean.repository.DefaultBeanId;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DefaultBeanIdStore implements BeanIdStore {
    private volatile HashMap<String, BeanId> beanIdMap = new HashMap<>();

    /**
     * registers a beanId name and returns the {@link BeanId} object.
     * If the beanId name is already registered then belonging BeanId
     * is returned.
     * <p>
     * This method doesn't have a performance penalty anymore when then BeanId
     * already exists.
     *
     * @return A new or existing BeanId.
     */
    @Override
    public BeanId register(String beanIdName) {
        AssertArgument.isNotEmpty(beanIdName, "beanIdName");

        BeanId beanId = beanIdMap.get(beanIdName);
        if (beanId == null) {

            synchronized (this) {

                beanId = beanIdMap.get(beanIdName);
                if (beanId == null) {
                    @SuppressWarnings("unchecked")
                    HashMap<String, BeanId> newBeanIdMap = (HashMap<String, BeanId>) beanIdMap.clone();

                    beanId = new DefaultBeanId(this, newBeanIdMap.size(), beanIdName);

                    newBeanIdMap.put(beanIdName, beanId);

                    beanIdMap = newBeanIdMap;
                }

            }
        }

        return beanId;
    }

    /**
     * @return The BeanId or <code>null</code> if it is not registered;
     */
    @Override
    public BeanId getBeanId(String beanId) {
        return beanIdMap.get(beanId);
    }

    /**
     * @return if the bean Id name is already registered.
     */
    @Override
    public boolean containsBeanId(String beanId) {
        return beanIdMap.containsKey(beanId);
    }

    /**
     * Returns a copy of the internal bean id map
     *
     * @return An map where the key is the string based beanId and the value is the BeanId.
     */
    @Override
    public Map<String, BeanId> getBeanIdMap() {
        return Collections.unmodifiableMap(beanIdMap);
    }

    /**
     * @return the current index size.
     */
    @Override
    public int size() {
        return beanIdMap.size();
    }
}
