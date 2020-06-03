/*-
 * ========================LICENSE_START=================================
 * Smooks Core
 * %%
 * Copyright (C) 2020 Smooks
 * %%
 * Licensed under the terms of the Apache License Version 2.0 or,
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
package org.smooks.payload;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Unit test for {@link Export}.
 * 
 * @author Daniel Bevenius
 *
 */
public class ExportTest
{
    @Test
    public void equalsIsRelexive()
    {
        Export export = new Export(String.class);
        assertEquals(export, export);
    }
    
    @Test
    public void equalsIsSymetric()
    {
        Export x = new Export(String.class);
        Export y = new Export(String.class);
        assertEquals(x, y);
        assertEquals(y, x);
        assertEquals(x.hashCode(), y.hashCode());
    }
    
    @Test
    public void equalsIsTransitive()
    {
        Export x = new Export(String.class);
        Export y = new Export(String.class);
        Export z = new Export(String.class);
        assertEquals(x, y);
        assertEquals(y, z);
        assertEquals(x, z);
        
        assertEquals(x.hashCode(), y.hashCode());
        assertEquals(y.hashCode(), z.hashCode());
        assertEquals(x.hashCode(), z.hashCode());
    }
    
    @Test
    public void equalsIsConsistent()
    {
        assertFalse(new Export(String.class, "exportNameX").equals(new Export(String.class)));
        assertFalse(new Export(String.class, "exportNameX").equals(new Export(String.class, "exportNameY")));
        assertTrue(new Export(String.class, "exportNameX").equals(new Export(String.class, "exportNameX")));
        
        assertFalse(new Export(String.class, "exportNameX", "ext1").equals(new Export(String.class, "exportNameX")));
        assertTrue(new Export(String.class, "exportNameX", "ext1").equals(new Export(String.class, "exportNameX","ext1")));
    }
    
    @Test
    public void equalsNull()
    {
        Export export = new Export(String.class);
        assertFalse(export.equals(null));
    }

}
