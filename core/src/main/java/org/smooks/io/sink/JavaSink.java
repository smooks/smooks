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
package org.smooks.io.sink;

import com.thoughtworks.xstream.XStream;

import org.smooks.api.ExecutionContext;
import org.smooks.assertion.AssertArgument;
import org.smooks.engine.bean.context.StandaloneBeanContext;
import org.smooks.io.payload.Export;
import org.smooks.io.payload.SinkExtractor;

import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Java filtration/transformation result.
 * <p/>
 * Used to extract a Java "{@link org.smooks.api.io.Sink sink}" Map from the transformation.
 * Simply set an instance of this class as the {@link org.smooks.api.io.Sink} arg in the call
 * to {@link org.smooks.Smooks#filterSource(ExecutionContext, org.smooks.api.io.Source, org.smooks.api.io.Sink...)} .
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class JavaSink extends FilterSink implements SinkExtractor<JavaSink> {

    private Map<String, Object> resultMap;

    /**
     * Public default constructor.
     */
    public JavaSink() {
        this(true);
    }

    /**
     * Public default constructor.
     */
    public JavaSink(boolean preserveOrder) {
        if (preserveOrder) {
            resultMap = new LinkedHashMap<>();
        } else {
            resultMap = new HashMap<>();
        }
    }

    /**
     * Public constructor.
     * <p/>
     * See {@link #setResultMap(java.util.Map)}.
     *
     * @param resultMap Result Map. This is the map onto which Java "result" objects will be set.
     */
    public JavaSink(Map<String, Object> resultMap) {
        AssertArgument.isNotNull(resultMap, "resultMap");
        this.resultMap = resultMap;
    }

    /**
     * Get the named bean from the Java Result Map.
     *
     * @param name the name of the bean.
     * @return The bean Object, or null if the bean is not in the bean Result Map.
     * @see #getResultMap()
     */
    public Object getBean(String name) {
        return resultMap.get(name);
    }

    /**
     * Get the first instance of the specified bean type
     * from this JavaSink instance.
     *
     * @param beanType The bean runtime class type.
     * @return The bean instance, otherwise null.
     */
    public <T> T getBean(Class<T> beanType) {
        return StandaloneBeanContext.getBean(beanType, resultMap);
    }

    /**
     * Get the Java result map.
     *
     * @return The Java result map.
     * @see #getBean(String)
     */
    public Map<String, Object> getResultMap() {
        return resultMap;
    }

    /**
     * Set the Java result map.
     *
     * @param resultMap The Java result map.
     */
    public void setResultMap(Map<String, Object> resultMap) {
        this.resultMap = resultMap;
    }

    /**
     * XML Serialized form of the bean Map associate with the
     * result instance.
     *
     * @return XML Serialized form of the bean Map associate with the
     * result instance.
     */
    @Override
    public String toString() {
        StringWriter stringBuilder = new StringWriter();
        XStream xstream = new XStream();

        if (resultMap != null && !resultMap.isEmpty()) {
            Set<Map.Entry<String, Object>> entries = resultMap.entrySet();

            for (Map.Entry<String, Object> entry : entries) {
                stringBuilder.write(entry.getKey() + ":\n");
                stringBuilder.write(xstream.toXML(entry.getValue()) + "\n\n");
            }
        }

        return stringBuilder.toString();
    }

    @Override
    public Object extractFromSink(JavaSink sink, Export export) {
        Set<String> extractSet = export.getExtractSet();

        if (extractSet == null) {
            return extractBeans(sink, sink.getResultMap().keySet());
        }

        if (extractSet.size() == 1) {
            return sink.getBean(extractSet.iterator().next());
        } else {
            return extractBeans(sink, extractSet);
        }
    }

    private Object extractBeans(JavaSink result, Collection<String> extractSet) {
        Map<String, Object> extractedObjects = new ResultMap<String, Object>();

        for (String extract : extractSet) {
            Object bean = result.getBean(extract);
            if (bean != null) {
                extractedObjects.put(extract, bean);
            }
        }

        return extractedObjects;
    }

    public static class ResultMap<K, V> extends HashMap<K, V> {
    }
}
