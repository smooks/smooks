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

import org.junit.Test;
import static org.junit.Assert.*;
import static org.smooks.function.StringFunctionDefinitionParser.*;

import java.util.List;

/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 */
public class StringFunctionDefinitionParserTest {

	@Test
    public void test_parse() {

        assertSame(TRIM_FUNCTION, parse(TRIM_DEFINITION).get(0));
        assertSame(LEFT_TRIM_FUNCTION, parse(LEFT_TRIM_DEFINITION).get(0));
        assertSame(RIGHT_TRIM_FUNCTION, parse(RIGHT_TRIM_DEFINITION).get(0));
        assertSame(UPPER_CASE_FUNCTION, parse(UPPER_CASE_DEFINITION).get(0));
        assertSame(LOWER_CASE_FUNCTION, parse(LOWER_CASE_DEFINITION).get(0));
        assertSame(CAPITALIZE_FUNCTION, parse(CAPITALIZE_DEFINITION).get(0));
        assertSame(CAPITALIZE_FIRST_FUNCTION, parse(CAPITALIZE_FIRST_DEFINITION).get(0));
        assertSame(UNCAPITALIZE_FIRST_FUNCTION, parse(UNCAPITALIZE_FIRST_DEFINITION).get(0));

        String def = TRIM_DEFINITION + SEPARATOR + UPPER_CASE_DEFINITION + SEPARATOR + CAPITALIZE_DEFINITION;

        List<StringFunction> functions = parse(def);

        assertSame(TRIM_FUNCTION, functions.get(0));
        assertSame(UPPER_CASE_FUNCTION, functions.get(1));
        assertSame(CAPITALIZE_FUNCTION, functions.get(2));

    }

	@Test
    public void test_unknown_function() {
        try {
            parse("fewfewfwe");
        } catch (UnknownStringFunctionException e) {
            
             return;
        }
        fail("UnknownStringFunctionException not thrown");

    }

}
