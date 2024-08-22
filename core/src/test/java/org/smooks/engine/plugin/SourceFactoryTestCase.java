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
package org.smooks.engine.plugin;

import org.junit.jupiter.api.Test;
import org.smooks.api.io.Source;
import org.smooks.io.source.ReaderSource;
import org.smooks.io.source.StreamSource;
import org.smooks.io.source.StringSource;

import java.io.ByteArrayInputStream;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author <a href="mailto:daniel.bevenius@gmail.com">Daniel Bevenius</a>
 */
public class SourceFactoryTestCase {
    private final SourceFactory factory = SourceFactory.getInstance();

    @Test
    public void createSourceFromString() {
        Source source = factory.createSource("testing");
        assertNotNull(source);
        assertInstanceOf(StringSource.class, source);
    }

    @Test
    public void getSourceByteArray() {
        Source source = factory.createSource("test".getBytes());
        assertNotNull(source);
        assertInstanceOf(StreamSource.class, source);
    }

    @Test
    public void getSourceReader() {
        Source source = factory.createSource(new StringReader("testing"));
        assertNotNull(source);
        assertInstanceOf(ReaderSource.class, source);
    }

    @Test
    public void getSourceInputStream() {
        Source source = factory.createSource(new ByteArrayInputStream("testing".getBytes()));
        assertNotNull(source);
        assertInstanceOf(StreamSource.class, source);
    }

}
