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

package org.milyn.function;

import static org.milyn.function.StringFunctionDefinitionParser.*;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 */
public class StringFunctionExecutorTest extends TestCase {

    public void test_execute() {

        String def = TRIM_DEFINITION;

        assertEquals("blaat", StringFunctionExecutor.getInstance(def).execute(" blaat   "));
                
        def = TRIM_DEFINITION + SEPARATOR + UPPER_CASE_DEFINITION;

        assertEquals("BLAAT", StringFunctionExecutor.getInstance(def).execute(" blaat   "));

    }

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
