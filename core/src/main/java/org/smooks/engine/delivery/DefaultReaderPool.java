/*-
 * ========================LICENSE_START=================================
 * Smooks Core
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

import org.smooks.api.delivery.ContentDeliveryConfig;
import org.smooks.api.delivery.Filter;
import org.smooks.api.delivery.ReaderPool;
import org.smooks.engine.resource.config.ParameterAccessor;
import org.xml.sax.XMLReader;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class DefaultReaderPool implements ReaderPool {
    private final List<XMLReader> readerPool = new CopyOnWriteArrayList<>();
    private final ContentDeliveryConfig contentDeliveryConfig;
    private final int readerPoolSize;

    public DefaultReaderPool(final ContentDeliveryConfig contentDeliveryConfig) {
        this.contentDeliveryConfig = contentDeliveryConfig;
        this.readerPoolSize = Integer.parseInt(ParameterAccessor.getParameterValue(Filter.READER_POOL_SIZE, String.class, "0", contentDeliveryConfig));
    }

    /**
     * Get an {@link XMLReader} instance from the 
     * reader pool associated with this ContentDelivery config instance.
     * @return An XMLReader instance if the pool is not empty, otherwise null.
     */
    @Override
    public XMLReader borrowXMLReader() {
        synchronized (readerPool) {
            if (!readerPool.isEmpty()) {
                return readerPool.remove(0);
            } else {
                return null;
            }
        }
    }

    /**
     * Return an {@link XMLReader} instance to the
     * reader pool associated with this ContentDelivery config instance.
     * @param reader The XMLReader instance to be returned.  If the pool is full, the instance
     * is left to the GC (i.e. lost).
     */
    @Override
    public void returnXMLReader(XMLReader reader) {
        synchronized(readerPool) {
            if(readerPool.size() < readerPoolSize) {
                readerPool.add(reader);
            }
        }
    }

    public ContentDeliveryConfig getContentDeliveryConfig() {
        return contentDeliveryConfig;
    }
}
