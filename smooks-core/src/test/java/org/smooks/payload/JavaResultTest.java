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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Unit test for {@link JavaResult}.
 * </p>
 *
 * @author Daniel Bevenius
 */
@SuppressWarnings("unchecked")
public class JavaResultTest
{
    private HashMap<String, Object> beans;

    @Before
    public void createBeanMap()
    {
        beans = new HashMap<String, Object>();
        beans.put("first", "bean1");
        beans.put("second", "bean2");
        beans.put("third", "bean3");
    }

    @Test
    public void extractSpecificBean()
    {
        JavaResult javaResult = new JavaResult(beans);
        Object result = javaResult.extractFromResult(javaResult, new Export(JavaResult.class, null, "second"));
        assertEquals("bean2", result);
    }

    @Test
    public void extractSpecificBeans()
    {
        JavaResult javaResult = new JavaResult(beans);
        Map<String, Object> result = (Map<String, Object>) javaResult.extractFromResult(javaResult, new Export(JavaResult.class, null, "second,first"));
        Assert.assertTrue(result.containsKey("first"));
        Assert.assertTrue(result.containsKey("second"));
        Assert.assertFalse(result.containsKey("third"));
    }
}
