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

import org.junit.jupiter.api.Test;
import org.smooks.Smooks;
import org.smooks.api.io.Sink;
import org.smooks.engine.lookup.ExportsLookup;
import org.smooks.io.sink.JavaSink;
import org.smooks.io.sink.StringSink;

import java.util.Collection;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Functional test for {@link Exports}.
 * 
 * @author Daniel Bevenius
 * 
 */
public class ExportsFunctionalTestCase
{
    @Test
    public void multipleExportTypes() throws Exception
    {
        Smooks smooks = new Smooks("/org/smooks/io/payload/exports-01.xml");
        smooks.createExecutionContext();
        
        Exports exports = smooks.getApplicationContext().getRegistry().lookup(new ExportsLookup());

        Set<Class<?>> sinkTypes = exports.getSinkTypes();
        assertTrue(sinkTypes.contains(StringSink.class));
        assertTrue(sinkTypes.contains(JavaSink.class));
    }
    
    @Test
    public void multipleNamedExportTypes() throws Exception
    {
        Smooks smooks = new Smooks("/org/smooks/io/payload/exports-named.xml");
        smooks.createExecutionContext();

        Exports exports = smooks.getApplicationContext().getRegistry().lookup(new ExportsLookup());
        Collection<Export> exportTypes = exports.getExports();
        assertEquals(2, exportTypes.size());
    }

    @Test
    public void programmaticConfiguration()
    {
        Smooks smooks = new Smooks();
        smooks.createExecutionContext();
        smooks.setExports(new Exports(StringSink.class));

        Exports exports = smooks.getApplicationContext().getRegistry().lookup(new ExportsLookup());

        assertTrue(exports.getSinkTypes().contains(StringSink.class));
        assertEquals(1, exports.getSinkTypes().size());
    }

    @Test
    public void getSinksFromApplicationContext()
    {
        Smooks smooks = new Smooks();
        smooks.createExecutionContext();
        Exports exports =  new Exports(StringSink.class);
        smooks.setExports(exports);
        Sink[] sinks = exports.createSinks();
        assertEquals(1, sinks.length);
        assertEquals(StringSink.class, sinks[0].getClass());
    }

}
