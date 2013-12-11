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
package org.milyn.commons.net;

import junit.framework.TestCase;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class URIUtilTest extends TestCase {

    public void test_getParent() throws URISyntaxException {
        URI uriIn;
        URI uriOut;

        // without scheme...
        uriIn = new URI("/a/b/s/x.txt");
        uriOut = URIUtil.getParent(uriIn);
        assertEquals(new URI("/a/b/s"), uriOut);

        // with scheme...
        uriIn = new URI("file:/a/b/s/x.txt");
        uriOut = URIUtil.getParent(uriIn);
        assertEquals(new URI("file:/a/b/s"), uriOut);

        // without parent...
        uriIn = new URI("x.txt");
        uriOut = URIUtil.getParent(uriIn);
        assertEquals(new URI("../"), uriOut);
    }
}
