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
package org.smooks.api.resource.reader;

import org.smooks.api.ExecutionContext;
import org.smooks.api.delivery.ContentHandler;
import org.xml.sax.XMLReader;

/**
 * Smooks {@link XMLReader}.
 * <p/>
 * {@link SmooksXMLReader} allows you to target a specific SAX Parser at a specific message type.
 * This lets you parse a stream of any type, convert it to a stream of SAX event and so treat the stream
 * as an XML data stream, even when the stream is non-XML.
 * <p/>
 * The parser resource configuration "selector" needs to have a value of "org.xml.sax.driver" i.e.
 * <b>selector="org.xml.sax.driver"</b>.
 *
 * @author tfennelly
 */
public interface SmooksXMLReader extends XMLReader, ContentHandler {

    /**
     * Set the Smooks {@link ExecutionContext} on the implementing class.
     *
     * @param executionContext The Smooks {@link ExecutionContext}.
     */
    void setExecutionContext(ExecutionContext executionContext);
}
