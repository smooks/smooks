/*-
 * ========================LICENSE_START=================================
 * Smooks Commons
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
package org.smooks.support;

import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class CollectionsUtilTest {

	@Test
    public void test_Set_01() {
        Set<String> strings = CollectionsUtil.toSet("1", "2", "3");
        assertTrue(strings.contains("1"));
        assertTrue(strings.contains("2"));
        assertTrue(strings.contains("3"));
    }

	@Test
    public void test_Set_02() {
        Set<Integer> integers = CollectionsUtil.toSet(1, 2, 3);
        assertTrue(integers.contains(1));
        assertTrue(integers.contains(2));
        assertTrue(integers.contains(3));
    }

	@Test
    public void test_Set_03() {
        Set<Integer> integers = Collections.emptySet();
        assertEquals(0, integers.size());
        Set<String> strings = Collections.emptySet();
        assertEquals(0, strings.size());
    }

	@Test
    public void test_List_01() {
        List<String> strings = CollectionsUtil.toList("1", "2", "3");
        assertTrue(strings.contains("1"));
        assertTrue(strings.contains("2"));
        assertTrue(strings.contains("3"));
    }

	@Test
    public void test_List_02() {
        List<Integer> integers = CollectionsUtil.toList(1, 2, 3);
        assertTrue(integers.contains(1));
        assertTrue(integers.contains(2));
        assertTrue(integers.contains(3));
    }
}
