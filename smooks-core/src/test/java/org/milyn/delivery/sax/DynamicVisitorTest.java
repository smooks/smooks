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
package org.milyn.delivery.sax;

import junit.framework.TestCase;
import org.milyn.Smooks;
import org.milyn.SmooksUtil;
import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.payload.StringSource;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class DynamicVisitorTest extends TestCase {
    
    public void test() {
        Smooks smooks = new Smooks();
        StringSource source = new StringSource("<a><b><c>c1</c><d>c2</d><e>c3</e></b></a>");

        SmooksUtil.registerResource(new SmooksResourceConfiguration("b", DynamicVisitorLoader.class.getName()), smooks);
        smooks.filterSource(source);

        assertEquals("<b><c>c1</c><d>c2</d><e>c3</e></b>", DynamicVisitorLoader.visitor.stuff.toString());
    }
}
