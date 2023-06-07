/*-
 * ========================LICENSE_START=================================
 * API
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
package org.smooks.api;

import org.smooks.api.resource.config.ResourceConfig;
import org.smooks.api.delivery.ContentHandler;
import org.smooks.api.resource.config.ResourceConfigSeq;
import org.xml.sax.SAXException;

import jakarta.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.function.Function;

/**
 * Holds system and user objects such as {@link ResourceConfig} and {@link ContentHandler}.
 * <p>
 * A registry is bound to the application context. It allows Smooks to discover and reference registered objects during a
 * filter execution. Clients should call {@link #deRegisterObject(Object)} to remove a registered object once it is no longer
 * needed.
 */
public interface Registry {

    /**
     * Registers an object with its key derived from the object's {@link Resource#name()} attribute or the object's class name.
     *
     * @param value object to register
     * @throws SmooksException if the value with the assigned name already exists
     * @throws IllegalArgumentException if the value is null
     */
    void registerObject(Object value);

    /**
     * Adds an object with the given key to this <code>Registry</code>.
     *
     * @param key object that maps to the <code>value</code> to register
     * @param value object to register which can be retrieved by its <code>key</code>
     * @throws SmooksException if the value with the assigned name already exists
     * @throws IllegalArgumentException if the name or the value is null
     */
    void registerObject(Object key, Object value);

    /**
     * Removes a registered object from this <code>Registry</code>.
     *
     * @param key key of the registered object to remove
     */
    void deRegisterObject(Object key);

    /**
     * Looks up a registered object by function.
     *
     * @param function function to apply for looking up an object
     * @param <R> type of object to be returned
     * @return registered object if it exists or null
     */
    <R> R lookup(Function<Map<Object, Object>, R> function);

    /**
     * Looks up a registered object by its key.
     *
     * @param key key of registered object
     * @param <T> type of object to be returned
     * @return the registered object if it exists or null
     */
    <T> T lookup(Object key);

    /**
     * Registers the set of resources specified in the supplied XML configuration stream.
     *
     * @param baseURI base URI to be associated with the configuration stream
     * @param resourceConfigStream XML resource configuration stream
     * @return {@link ResourceConfigSeq} created from the added resource configuration
     * @throws SAXException if error happens while parsing the resource stream
     * @throws IOException  if error happens while reading resource stream
     */
    ResourceConfigSeq registerResources(String baseURI, InputStream resourceConfigStream) throws SAXException, IOException, URISyntaxException;

    /**
     * Registers and initialises a {@link ResourceConfig}.
     *
     * @param resourceConfig {@link ResourceConfig} to register
     */
    void registerResourceConfig(ResourceConfig resourceConfig);

    /**
     * Registers and initialises a {@link ResourceConfigSeq}.
     *
     * @param resourceConfigSeq {@link ResourceConfigSeq} to register
     */
    void registerResourceConfigSeq(ResourceConfigSeq resourceConfigSeq);

    /**
     * Cleans up the resources of this <code>Registry</code> and calls the {@link jakarta.annotation.PreDestroy} method of
     * each registered object.
     */
    void close();

    /**
     * @return the class loader of this <code>Registry</code>
     */
    ClassLoader getClassLoader();
}
