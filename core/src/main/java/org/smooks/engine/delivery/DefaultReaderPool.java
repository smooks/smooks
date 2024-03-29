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
package org.smooks.engine.delivery;

import jakarta.annotation.Resource;
import org.smooks.api.delivery.ReaderPool;
import org.xml.sax.XMLReader;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReferenceArray;

@Resource(name = "DefaultReaderPool")
public class DefaultReaderPool implements ReaderPool {
    private final AtomicReferenceArray<Optional<XMLReader>> xmlReaderPool;
    private final int maxReaderPoolSize;

    public DefaultReaderPool(final int maxReaderPoolSize) {
        this.maxReaderPoolSize = maxReaderPoolSize;
        xmlReaderPool = new AtomicReferenceArray<>(maxReaderPoolSize);
    }

    @Override
    public XMLReader borrowXMLReader() {
        for (int i = 0; i < xmlReaderPool.length(); i++) {
            Optional<XMLReader> xmlReader = xmlReaderPool.get(i);
            if (xmlReader != null) {
                xmlReader = xmlReaderPool.getAndSet(i, Optional.empty());
                if (xmlReader.isPresent()) {
                    return xmlReader.get();
                }
            }
        }

        return null;
    }

    /**
     * Return an {@link XMLReader} instance to the reader pool associated with this ContentDelivery config instance.
     *
     * @param xmlReader The XMLReader instance to be returned. If the pool is full, the instance is left to the GC (i.e. lost).
     */
    @Override
    public void returnXMLReader(XMLReader xmlReader) {
        for (int i = 0; i < xmlReaderPool.length(); i++) {
            Optional<XMLReader> optionalXMLReader = Optional.of(xmlReader);
            if (xmlReaderPool.compareAndSet(i, Optional.empty(), optionalXMLReader) ||
                    xmlReaderPool.compareAndSet(i, null, optionalXMLReader)) {
                return;
            }
        }
    }

    @Override
    public Map<String, String> getProperties() {
        Map<String, String> properties = new HashMap<>(3);
        properties.put("maxReadersSize", String.valueOf(maxReaderPoolSize));
        int unallocatedReaders = 0;
        int activeReaders = 0;
        for (int i = 0; i < xmlReaderPool.length(); i++) {
            Optional<XMLReader> xmlReader = xmlReaderPool.get(i);
            if (xmlReader == null || xmlReader.isPresent()) {
                unallocatedReaders++;
            } else {
                activeReaders++;
            }
        }
        properties.put("unallocatedReaders", String.valueOf(unallocatedReaders));
        properties.put("activeReaders", String.valueOf(activeReaders));

        return properties;
    }

    public int getMaxReaderPoolSize() {
        return maxReaderPoolSize;
    }
}