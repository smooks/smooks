/*
	Milyn - Copyright (C) 2006

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
package org.milyn.distro.html.visitors;

import junit.framework.TestCase;
import org.milyn.Smooks;
import org.milyn.container.ExecutionContext;
import org.milyn.payload.StringResult;
import org.milyn.payload.StringSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.URI;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class URLRewriterTest extends TestCase {

    private Smooks smooks;
    private ExecutionContext execContext;

    protected void setUp() throws Exception {
        smooks = new Smooks(getClass().getResourceAsStream("url-rewriting.xml"));
        execContext = smooks.createExecutionContext();
        execContext.setDocumentSource(new URI("http://x/"));
    }

    public void test_internal_object_01() throws IOException, SAXException {
        StringResult result = new StringResult();

        smooks.filterSource(execContext, new StringSource("<x><a href='a/b.html#internal_objectx'>Internal Ojbect</a><p id='internal_objectx' /></x>"), result);
        assertEquals("<x><a href=\"#internal_objectx\">Internal Ojbect</a><p id=\"internal_objectx\"></p></x>", result.getResult());
    }

    public void test_internal_object_02() throws IOException, SAXException {
        StringResult result = new StringResult();

        smooks.filterSource(execContext, new StringSource("<x><a href='#internal_objectx'>Internal Ojbect</a><p id='internal_objectx' /></x>"), result);
        assertEquals("<x><a href=\"#internal_objectx\">Internal Ojbect</a><p id=\"internal_objectx\"></p></x>", result.getResult());
    }

    public void test_relative_path() throws IOException, SAXException {
        StringResult result = new StringResult();

        smooks.filterSource(execContext, new StringSource("<a href='a/b.html'>Internal Ojbect</a>"), result);
        assertEquals("<a href=\"http://x/a/b.html\">Internal Ojbect</a>", result.getResult());
    }
}
