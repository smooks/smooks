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
package org.smooks.cdr.injector;

import org.smooks.cdr.SmooksResourceConfiguration;
import org.smooks.cdr.registry.Registry;
import org.smooks.cdr.registry.lookup.GlobalParamsLookup;
import org.smooks.delivery.Filter;
import org.smooks.delivery.sax.SAXToXMLWriter;
import org.smooks.delivery.sax.SAXVisitor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Scope implements Map<Object, Object> {

    private final Map<Object, Object> scope = new HashMap<>();
    
    public Scope(final Registry registry, final SmooksResourceConfiguration smooksResourceConfiguration, final Object instance) {
        this(registry);
        scope.put(SmooksResourceConfiguration.class, smooksResourceConfiguration);
        for (String parameterName : smooksResourceConfiguration.getParameters().keySet()) {
            scope.put(parameterName, smooksResourceConfiguration.getParameterValue(parameterName));
        }
        
        if (instance instanceof SAXVisitor) {
            final boolean encodeSpecialCharacters = Boolean.parseBoolean(smooksResourceConfiguration.getParameterValue(Filter.ENTITIES_REWRITE, String.class, "true"));
            scope.put(SAXToXMLWriter.class, new SAXToXMLWriter((SAXVisitor) instance, encodeSpecialCharacters));
        }
    }
    
    public Scope(final Registry registry) {
        scope.put(Registry.class, registry);
        scope.putAll(registry.lookup(registryEntries -> registryEntries));

        final SmooksResourceConfiguration globalParams = registry.lookup(new GlobalParamsLookup(registry));
        for (String parameterName : globalParams.getParameters().keySet()) {
            scope.put(parameterName, globalParams.getParameterValue(parameterName));
        }
    }

    public Registry getRegistry() {
        return (Registry) scope.get(Registry.class);
    }

    @Override
    public int size() {
        return scope.size();
    }

    @Override
    public boolean isEmpty() {
        return scope.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return scope.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return scope.containsValue(value);
    }

    @Override
    public Object get(Object key) {
        return scope.get(key);
    }

    @Override
    public Object put(Object key, Object value) {
        return scope.put(key, value);
    }

    @Override
    public Object remove(Object key) {
        return scope.remove(key);
    }

    @Override
    public void putAll(Map<?, ?> m) {
        scope.putAll(m);
    }

    @Override
    public void clear() {
        scope.clear();
    }

    @Override
    public Set<Object> keySet() {
        return scope.keySet();
    }

    @Override
    public Collection<Object> values() {
        return scope.values();
    }

    @Override
    public Set<Entry<Object, Object>> entrySet() {
        return scope.entrySet();
    }
}
