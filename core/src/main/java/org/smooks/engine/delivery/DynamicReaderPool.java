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

import org.smooks.api.delivery.ReaderPool;
import org.xml.sax.XMLReader;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * A pool of readers without an upper bound which means that it grows on-demand.
 */
public class DynamicReaderPool implements ReaderPool {
    private final AtomicReference<AtomicReferenceArray<Optional<XMLReader>>> xmlReaderPoolReference = new AtomicReference<>();

    public DynamicReaderPool() {
        xmlReaderPoolReference.set(new AtomicReferenceArray<>(16));
    }

    @Override
    public XMLReader borrowXMLReader() {
        final AtomicReferenceArray<Optional<XMLReader>> xmlReaderPool = xmlReaderPoolReference.get();
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
     * @param xmlReader The XMLReader instance to be returned. If the pool is full, the pool is re-sized to accommodate
     *                  the new reader.
     */
    @Override
    public void returnXMLReader(final XMLReader xmlReader) {
        final AtomicReferenceArray<Optional<XMLReader>> xmlReaderPool = xmlReaderPoolReference.get();
        for (int i = 0; i < xmlReaderPool.length(); i++) {
            Optional<XMLReader> optionalXMLReader = Optional.of(xmlReader);
            if (xmlReaderPool.compareAndSet(i, Optional.empty(), optionalXMLReader) ||
                    xmlReaderPool.compareAndSet(i, null, optionalXMLReader)) {
                return;
            }
        }

        xmlReaderPoolReference.compareAndSet(xmlReaderPool, new AtomicReferenceArray<>(xmlReaderPool.length() * 2));
        returnXMLReader(xmlReader);
    }

    @Override
    public Map<String, String> getProperties() {
        final AtomicReferenceArray<Optional<XMLReader>> xmlReaderPool = xmlReaderPoolReference.get();
        Map<String, String> properties = new HashMap<>(3);
        properties.put("readerPoolSize", String.valueOf(xmlReaderPool.length()));
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
}
