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
package org.smooks.io.payload;

import org.smooks.api.SmooksException;
import org.smooks.api.io.Sink;
import org.smooks.assertion.AssertArgument;
import org.smooks.api.ApplicationContext;
import org.smooks.api.delivery.ContentHandler;
import org.smooks.support.ClassUtils;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * An Exports instance holds a Map of {@link Export}s that Smooks
 * produces/exports.
 * </p>
 * The map uses the type of sink as its key and the {@link Export}
 * as its value.
 *
 * @author Daniel Bevenius
 * @since 1.4
 */
public class Exports implements ContentHandler {

    private final Map<Class<?>, Export> exportsMap = new HashMap<>();

    @Inject
    private ApplicationContext applicationContext;

    public Exports() {
    }

    public Exports(final Export export) {
        addExport(export);
    }

    public Exports(final Set<Export> exportTypes) {
        for (Export export : exportTypes) {
            addExport(export);
        }
    }

    public Exports(final Class<?> sinkType) {
        AssertArgument.isNotNull(sinkType, "sinkType");
        addExport(new Export(sinkType));
    }

    public Exports(final String sinkType) {
        AssertArgument.isNotNull(sinkType, "sinkType");
        addExport(new Export(getClassForType(sinkType)));
    }

    private Class<?> getClassForType(final String type) {
        try {
            return ClassUtils.forName(type, Exports.class);
        } catch (ClassNotFoundException e) {
            throw new SmooksException("Could not load class for type [" + type + "].");
        }
    }

    public void addExport(Export export) {
        exportsMap.put(export.getType(), export);
    }

    public Collection<Export> getExports() {
        return Collections.unmodifiableCollection(exportsMap.values());
    }

    public Set<Class<?>> getSinkTypes() {
        return Collections.unmodifiableSet(exportsMap.keySet());
    }

    public boolean hasExports() {
        return !exportsMap.isEmpty();
    }

    public Export getExport(Class<?> type) {
        return exportsMap.get(type);
    }

    public Sink[] createSinks() {
        Set<Sink> sinks = new HashSet<>();
        for (Class<?> sinkTypeClass : exportsMap.keySet()) {
            sinks.add(createSinkInstance(sinkTypeClass));
        }
        return sinks.toArray(new Sink[]{});
    }

    public Collection<Export> getProducts() {
        return getExports();
    }

    /**
     * Will return the Objects contained in the sinks array. If the corresponding
     * {@link Export} for that sink type was configured with an extract property
     * only that portion of the sink will be returned.
     *
     * @param sinks   The sinks produced by a Smooks filtering operation.
     * @param exports The exports.
     * @return List<Object> Either the sinks unchanged if no 'extract' was configured
     * or if an 'extract' was configured in the corresponding Export then only the
     * object identified will be returned in the list of objects.
     */
    public static List<Object> extractSinks(final Sink[] sinks, final Exports exports) {
        final List<Object> objects = new ArrayList<>();
        for (Sink sink : sinks) {
            if (sink instanceof SinkExtractor) {
                @SuppressWarnings("unchecked") final SinkExtractor<Sink> e = (SinkExtractor<Sink>) sink;
                objects.add(e.extractFromSink(sink, exports.getExport(sink.getClass())));
            } else {
                objects.add(sink);
            }
        }

        return objects;
    }

    private static Sink createSinkInstance(final Class<?> sinkTypeClass) {
        try {
            return (Sink) sinkTypeClass.newInstance();
        } catch (InstantiationException e) {
            throw new SmooksException("Could not instantiate instance for sink type ["
                    + sinkTypeClass.getName() + "]", e);
        } catch (IllegalAccessException e) {
            throw new SmooksException("Could not create instance for sink type ["
                    + sinkTypeClass.getName() + "]", e);
        }
    }

}
