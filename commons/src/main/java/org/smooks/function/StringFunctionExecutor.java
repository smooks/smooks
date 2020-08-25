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

import org.apache.commons.lang.builder.ToStringBuilder;
import org.smooks.assertion.AssertArgument;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Takes a StringFunction definition and executes it on a string
 *
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 */
public class StringFunctionExecutor {

    private static final ConcurrentMap<String, StringFunctionExecutor> cache = new ConcurrentHashMap<String, StringFunctionExecutor>();

    public static StringFunctionExecutor getInstance(String functionDefinition) {
        StringFunctionExecutor executor = cache.get(functionDefinition);
        if(executor == null) {
            executor = new StringFunctionExecutor(functionDefinition, StringFunctionDefinitionParser.parse(functionDefinition));

            StringFunctionExecutor existing = cache.putIfAbsent(functionDefinition, executor);

            if(existing != null) {
                executor = existing;
            }
        }

        return executor;
    }

    private final List<StringFunction> functions;

    private final String functionDefinition;

    private StringFunctionExecutor(String functionDefinition, List<StringFunction> functions) {
        this.functionDefinition = functionDefinition;
        this.functions = functions;
    }

    /**
     * Takes a StringFunction definition and executes it on a string
     *
     * @param input The input string
     * @return The result string
     */
    public String execute(String input) {
        AssertArgument.isNotNull(input, "input");

        for(StringFunction function : functions) {
            input = function.execute(input);
        }

        return input;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                    .append("functionDefinition", functionDefinition)
                    .toString();
    }
}
