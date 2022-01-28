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
import org.smooks.engine.lookup.ExportsLookup;

import javax.xml.transform.Result;
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

        Set<Class<?>> resultTypes = exports.getResultTypes();
        assertTrue(resultTypes.contains(StringResult.class));
        assertTrue(resultTypes.contains(JavaResult.class));
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
        smooks.setExports(new Exports(StringResult.class));

        Exports exports = smooks.getApplicationContext().getRegistry().lookup(new ExportsLookup());

        assertTrue(exports.getResultTypes().contains(StringResult.class));
        assertEquals(1, exports.getResultTypes().size());
    }

    @Test
    public void getResultsFromApplicationContext()
    {
        Smooks smooks = new Smooks();
        smooks.createExecutionContext();
        Exports exports =  new Exports(StringResult.class);
        smooks.setExports(exports);
        Result[] results = exports.createResults();
        assertEquals(1, results.length);
        assertEquals(StringResult.class, results[0].getClass());
    }

}
