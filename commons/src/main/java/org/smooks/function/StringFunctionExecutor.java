/*
 * Milyn - Copyright (C) 2006 - 2010
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License (version 2.1) as published by the Free Software
 * Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU Lesser General Public License for more details:
 * http://www.gnu.org/licenses/lgpl.txt
 */

package org.smooks.function;

import org.smooks.assertion.AssertArgument;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Takes a StringFunction definition and executes it on a string
 *
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 */
public class StringFunctionExecutor {

    private static ConcurrentMap<String, StringFunctionExecutor> cache = new ConcurrentHashMap<String, StringFunctionExecutor>();

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

    private List<StringFunction> functions;

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
