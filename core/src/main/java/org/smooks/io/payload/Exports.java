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
package org.smooks.io.payload;

import org.smooks.api.SmooksException;
import org.smooks.assertion.AssertArgument;
import org.smooks.api.ApplicationContext;
import org.smooks.api.delivery.ContentHandler;
import org.smooks.support.ClassUtil;

import javax.inject.Inject;
import javax.xml.transform.Result;
import java.util.*;

/**
 * An Exports instance holds a Map of {@link Export}s that Smooks
 * produces/exports.
 * </p>
 * The map uses the type of of result as its key and the {@link Export}
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

    public Exports(final Class<?> resultType) {
        AssertArgument.isNotNull(resultType, "resultType");
        addExport(new Export(resultType));
    }

    public Exports(final String resultType) {
        AssertArgument.isNotNull(resultType, "resultType");
        addExport(new Export(getClassForType(resultType)));
    }

    private Class<?> getClassForType(final String type) {
        try {
            return ClassUtil.forName(type, Exports.class);
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

    public Set<Class<?>> getResultTypes() {
        return Collections.unmodifiableSet(exportsMap.keySet());
    }

    public boolean hasExports() {
        return !exportsMap.isEmpty();
    }

    public Export getExport(Class<?> type) {
        return exportsMap.get(type);
    }

    public Result[] createResults() {
        Set<Result> results = new HashSet<Result>();
        for (Class<?> resultTypeClass : exportsMap.keySet()) {
            results.add(createResultInstance(resultTypeClass));
        }
        return results.toArray(new Result[]{});
    }

    public Collection<Export> getProducts() {
        return getExports();
    }
    
    /**
     * Will return the Objects contained in the results array. If the corresponding
     * {@link Export} for that result type was configured with an extract property
     * only that portion of the result will be returned.
     *
     * @param results The results produced by a Smooks filtering operation.
     * @param exports The exports.
     * @return List<Object> Either the results unchanged if no 'extract' was configured
     * or if an 'extract' was configured in the corresponding Export then only the
     * object identified will be returned in the list of objects.
     */
    public static List<Object> extractResults(final Result[] results, final Exports exports) {
        final List<Object> objects = new ArrayList<Object>();
        for (Result result : results) {
            if (result instanceof ResultExtractor) {
                @SuppressWarnings("unchecked") final ResultExtractor<Result> e = (ResultExtractor<Result>) result;
                objects.add(e.extractFromResult(result, exports.getExport(result.getClass())));
            } else {
                objects.add(result);
            }
        }

        return objects;
    }

    private static Result createResultInstance(final Class<?> resultTypeClass) {
        try {
            return (Result) resultTypeClass.newInstance();
        } catch (InstantiationException e) {
            throw new SmooksException("Could not instantiate instance for result type ["
                    + resultTypeClass.getName() + "]", e);
        } catch (IllegalAccessException e) {
            throw new SmooksException("Could not create instance for result type ["
                    + resultTypeClass.getName() + "]", e);
        }
    }

}
