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
package org.smooks.engine.bean.context;

import org.smooks.api.ExecutionContext;
import org.smooks.api.bean.context.BeanContext;
import org.smooks.api.bean.context.BeanIdStore;
import org.smooks.api.bean.repository.BeanId;
import org.smooks.api.io.Sink;
import org.smooks.api.io.Source;
import org.smooks.io.sink.FilterSink;
import org.smooks.io.source.FilterSource;
import org.smooks.io.sink.JavaSink;
import org.smooks.io.source.JavaSource;

import java.util.HashMap;
import java.util.Map;

/**
 * The Bean Context Manager
 * <p/>
 * Creates {@link StandaloneBeanContext} that share the same {@link BeanIdStore}.
 *
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 */
public class StandaloneBeanContextFactory {


    /* (non-Javadoc)
     * @see org.smooks.engine.bean.context.BeanContextFactory#createBeanRepository(org.smooks.engine.container.ExecutionContext)
     */
    public static StandaloneBeanContext create(ExecutionContext executionContext) {
        StandaloneBeanContext beanContext;

        BeanIdStore beanIdStore = executionContext.getApplicationContext().getBeanIdStore();
        Map<String, Object> beanMap = createBeanMap(executionContext, beanIdStore);

        beanContext = new StandaloneBeanContext(executionContext, beanIdStore, beanMap);

        return beanContext;
    }


    /**
     * Returns the BeanMap which must be used by the {@link BeanContext}. If
     * a JavaSink or a JavaSource is used with the {@link ExecutionContext} then
     * those are used in the creation of the Bean map.
     * <p>
     * Bean's that are already in the JavaSink or JavaSource map are given
     * a {@link BeanId} in the {@link BeanIdStore}.
     *
     * @param executionContext
     * @param beanIdStore
     * @return
     */
    private static Map<String, Object> createBeanMap(ExecutionContext executionContext, BeanIdStore beanIdStore) {
        Sink sink = FilterSink.getSink(executionContext, JavaSink.class);
        Source source = FilterSource.getSource(executionContext);
        Map<String, Object> beanMap = null;

        if (sink != null) {
            JavaSink javaSink = (JavaSink) sink;
            beanMap = javaSink.getResultMap();
        }

        if (source instanceof JavaSource) {
            JavaSource javaSource = (JavaSource) source;
            Map<String, Object> sourceBeans = javaSource.getBeans();

            if (sourceBeans != null) {
                if (beanMap != null) {
                    beanMap.putAll(sourceBeans);
                } else {
                    beanMap = sourceBeans;
                }
            }
        }

        if (beanMap == null) {
            beanMap = new HashMap<String, Object>();
        } else {

            for (String beanId : beanMap.keySet()) {

                if (!beanIdStore.containsBeanId(beanId)) {
                    beanIdStore.register(beanId);
                }

            }

        }
        return beanMap;
    }

}
