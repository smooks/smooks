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

package org.milyn.edisax.model.internal;

import org.junit.Test;
import static org.junit.Assert.*;
import org.milyn.edisax.unedifact.UNEdifactInterchangeParser;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class DelimitersTest {

    @Test
    public void test_escape() {
        Delimiters delimiters = UNEdifactInterchangeParser.defaultUNEdifactDelimiters;

        assertEquals("hello world", delimiters.escape("hello world"));
        assertEquals("hello world?'s", delimiters.escape("hello world's"));
        assertEquals("hello world??", delimiters.escape("hello world?"));
        assertEquals("hello ?+ world??", delimiters.escape("hello + world?"));
    }
}
