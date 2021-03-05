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
package org.smooks.engine.bean.lifecycle;

import org.smooks.api.ExecutionContext;
import org.smooks.api.delivery.fragment.Fragment;
import org.smooks.api.bean.lifecycle.BeanContextLifecycleEvent;
import org.smooks.api.bean.lifecycle.BeanLifecycle;
import org.smooks.api.bean.repository.BeanId;

public class DefaultBeanContextLifecycleEvent implements BeanContextLifecycleEvent {
    private final ExecutionContext executionContext;

    private final Fragment<?> source;

    private final BeanLifecycle lifecycle;

    private final BeanId beanId;

    private final Object bean;


    /**
     * Public constructor.
     * @param executionContext
     * @param source Source fragment name.
     * @param beanId Source bean.
     * @param lifecycle Lifecycle.
     * @param bean Bean instance.
     */
    public DefaultBeanContextLifecycleEvent(ExecutionContext executionContext, Fragment<?> source, BeanLifecycle lifecycle, BeanId beanId, Object bean) {
        this.executionContext = executionContext;
        this.source = source;
        this.beanId = beanId;
        this.lifecycle = lifecycle;
        this.bean = bean;
    }

    /**
     * @return the executionContext
     */
    @Override
    public ExecutionContext getExecutionContext() {
        return executionContext;
    }

    /**
     * Get the even source fragment.
     * @return Source fragment.
     */
    @Override
    public Fragment<?> getSource() {
        return source;
    }

    /**
     * @return the lifecycle
     */
    @Override
    public BeanLifecycle getLifecycle() {
        return lifecycle;
    }

    /**
     * @return the beanId
     */
    @Override
    public BeanId getBeanId() {
        return beanId;
    }

    /**
     * @return the bean
     */
    @Override
    public Object getBean() {
        return bean;
    }
}
