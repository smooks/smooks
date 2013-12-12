/*
	Milyn - Copyright (C) 2006 - 2010

	This library is free software; you can redistribute it and/or
	modify it under the terms of the GNU Lesser General Public
	License (version 2.1) as published by the Free Software
	Foundation.

	This library is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

	See the GNU Lesser General Public License for more details:
	http://www.gnu.org/licenses/lgpl.txt
*/
package org.milyn.commons.javabean;

import junit.framework.TestCase;
import org.milyn.commons.javabean.decoders.BigDecimalDecoder;

import java.math.BigDecimal;
import java.net.MalformedURLException;

/**
 * @author <a href="mailto:daniel.bevenius@gmail.com">Daniel Bevenius</a>
 */
public class DataDecoderTest extends TestCase {
    private String fileName = "META-INF/classes.inf";

    public void test_factory() throws MalformedURLException {
        DataDecoder decoder = DataDecoder.Factory.create(BigDecimal.class);
        assertNotNull(decoder);
        assertEquals(BigDecimalDecoder.class, decoder.getClass());
    }

}
