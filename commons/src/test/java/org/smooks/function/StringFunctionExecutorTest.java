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
package org.smooks.function;

import static org.smooks.function.StringFunctionDefinitionParser.*;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 */
public class StringFunctionExecutorTest {

	@Test
    public void test_execute() {

        String def = TRIM_DEFINITION;

        assertEquals("blaat", StringFunctionExecutor.getInstance(def).execute(" blaat   "));
                
        def = TRIM_DEFINITION + SEPARATOR + UPPER_CASE_DEFINITION;

        assertEquals("BLAAT", StringFunctionExecutor.getInstance(def).execute(" blaat   "));

    }

	@Test
    public void test_caching() {

        String def1 = LOWER_CASE_DEFINITION;
        StringFunctionExecutor executor1 = StringFunctionExecutor.getInstance(def1);
        StringFunctionExecutor executor2 = StringFunctionExecutor.getInstance(def1);

        assertSame(executor1, executor2);

        String def2 = TRIM_DEFINITION + SEPARATOR + UPPER_CASE_DEFINITION;
        StringFunctionExecutor executor3 = StringFunctionExecutor.getInstance(def2);
        StringFunctionExecutor executor4 = StringFunctionExecutor.getInstance(def2);

        assertSame(executor3, executor4);
        assertNotSame(executor1, executor3);

        StringFunctionExecutor executor5 = StringFunctionExecutor.getInstance(def1);

        assertSame(executor1, executor5);

    }
}
