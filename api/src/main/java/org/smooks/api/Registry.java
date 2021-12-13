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

import javax.annotation.Resource;
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
     * Registers an object with its name derived from the object's {@link Resource#name()} attribute or the object's class name.
     *
     * @param value object to register
     *
     * @throws SmooksException if the value with the assigned name already exists
     * @throws IllegalArgumentException if the value is null
     */
    void registerObject(Object value);

    /**
     * Registers an object.
     *
     * @param name name under which the object is registered
     * @param value object to register
     *
     * @throws SmooksException if the value with the assigned name already exists
     * @throws IllegalArgumentException if the name or the value is null
     */
    void registerObject(Object name, Object value);

    /**
     * @param name
     */
    void deRegisterObject(Object name);

    /**
     * @param function
     * @param <R>
     * @return
     */
    <R> R lookup(Function<Map<Object, Object>, R> function);

    /**
     * @param name
     * @param <T>
     * @return
     */
    <T> T lookup(Object name);

    /**
     * @param baseURI
     * @param resourceConfigStream
     * @return
     * @throws SAXException
     * @throws IOException
     * @throws URISyntaxException
     */
    ResourceConfigSeq registerResources(String baseURI, InputStream resourceConfigStream) throws SAXException, IOException, URISyntaxException;

    /**
     * @param resourceConfig
     */
    void registerResourceConfig(ResourceConfig resourceConfig);

    /**
     * @param resourceConfigSeq
     */
    void registerResourceConfigSeq(ResourceConfigSeq resourceConfigSeq);

    /**
     *
     */
    void close();

    /**
     * @return
     */
    ClassLoader getClassLoader();
}
