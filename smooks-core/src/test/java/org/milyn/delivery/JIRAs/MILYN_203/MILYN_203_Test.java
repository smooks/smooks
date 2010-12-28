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
package org.milyn.delivery.JIRAs.MILYN_203;

import junit.framework.TestCase;
import org.milyn.Smooks;
import org.milyn.io.StreamUtils;
import org.milyn.payload.StringResult;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;

/**
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class MILYN_203_Test extends TestCase {

    public void test_DOM() throws IOException, SAXException {
        test_CDATA("dom.xml");
    }

    public void test_SAX() throws IOException, SAXException {
        test_CDATA("sax.xml");
    }

    public void test_CDATA(String config) throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream(config));
        StringResult result = new StringResult();

        smooks.filterSource(new StreamSource(getClass().getResourceAsStream("in-message.xml")), result);
        assertTrue(StreamUtils.compareCharStreams(new InputStreamReader(getClass().getResourceAsStream("in-message.xml")), new StringReader(result.getResult())));
    }

}
