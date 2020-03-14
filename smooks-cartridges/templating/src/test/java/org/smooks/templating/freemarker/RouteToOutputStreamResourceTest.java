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
package org.smooks.templating.freemarker;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.smooks.Smooks;
import org.smooks.StreamFilterType;
import org.smooks.FilterSettings;
import org.smooks.payload.StringSource;
import org.smooks.templating.MockOutStreamResource;
import org.xml.sax.SAXException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class RouteToOutputStreamResourceTest {

    @Before
    public void setUp() throws Exception {
        MockOutStreamResource.outputStream = new ByteArrayOutputStream();
    }

    @Test
    public void test_dom_1() throws IOException, SAXException {
        test_1(StreamFilterType.DOM);
    }

    @Test
    public void test_dom_2() throws IOException, SAXException {
        test_2(StreamFilterType.DOM);
    }

    @Test
    public void test_sax_1() throws IOException, SAXException {
        test_1(StreamFilterType.SAX);
    }

    @Test
    public void test_sax_2() throws IOException, SAXException {
        test_2(StreamFilterType.SAX);
    }

    public void test_1(StreamFilterType filterType) throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("route-to-stream-01.cdrl"));

        smooks.setFilterSettings(new FilterSettings(filterType));
        smooks.filterSource(new StringSource("<a><c x='cx' /><d><e x='ex' /></d></a>"), null);
        assertEquals("<mybean>ex</mybean><mybean>cx</mybean>", new String(MockOutStreamResource.outputStream.toByteArray()));
    }

    public void test_2(StreamFilterType filterType) throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("route-to-stream-02.cdrl"));

        smooks.setFilterSettings(new FilterSettings(filterType));
        smooks.filterSource(new StringSource("<a><c x='cx' /><d><e x='ex' /></d></a>"), null);
        assertEquals("<mybean>cx</mybean><mybean>ex</mybean>", new String(MockOutStreamResource.outputStream.toByteArray()));
    }
}
