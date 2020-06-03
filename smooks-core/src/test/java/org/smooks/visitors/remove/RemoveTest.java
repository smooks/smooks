/*-
 * ========================LICENSE_START=================================
 * Smooks Core
 * %%
 * Copyright (C) 2020 Smooks
 * %%
 * Licensed under the terms of the Apache License Version 2.0 or,
 * the GNU Lesser General Public License version 3.0 or later.
 * 
 * SPDX-License-Identifier: Apache-2.0 OR LGPL-3.0-or-later
 * 
 * ======================================================================
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * ======================================================================
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * =========================LICENSE_END==================================
 */
package org.smooks.visitors.remove;

import org.junit.Test;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.smooks.FilterSettings;
import org.smooks.Smooks;
import org.smooks.payload.StringResult;
import org.smooks.payload.StringSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class RemoveTest {

	@Test
    public void test_no_children_SAX() throws IOException, SAXException {
        test(FilterSettings.DEFAULT_SAX, false, "<a><something /></a>");
    }

	@Test
    public void test_no_children_DOM() throws IOException, SAXException {
        test(FilterSettings.DEFAULT_DOM, false, "<a><something></something></a>");
    }

	@Test
    public void test_children_SAX() throws IOException, SAXException {
        test(FilterSettings.DEFAULT_SAX, true, "<a><d><e>some text</e></d><something /></a>");
    }

	@Test
    public void test_children_DOM() throws IOException, SAXException {
        test(FilterSettings.DEFAULT_DOM, true, "<a><d><e>some text</e></d><something></something></a>");
    }

    public void test(FilterSettings filterSettings, boolean keepChildren, String expected) throws IOException, SAXException {
        Smooks smooks = new Smooks();
        StringResult result = new StringResult();

        smooks.setFilterSettings(filterSettings);
        smooks.addVisitor(new RemoveElement().setKeepChildren(keepChildren), "b");

        smooks.filterSource(new StringSource("<a><b><d><e>some text</e></d></b><something/></a>"), result);

        XMLUnit.setIgnoreWhitespace( true );
        XMLAssert.assertXMLEqual(new StringReader(expected), new StringReader(result.getResult()));
    }

	@Test
    public void test_XML_config_SAX() throws IOException, SAXException {
        test_XML_config(FilterSettings.DEFAULT_SAX);
    }

	@Test
    public void test_XML_config_DOM() throws IOException, SAXException {
        test_XML_config(FilterSettings.DEFAULT_DOM);
    }

    public void test_XML_config(FilterSettings filterSettings) throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("config-01.xml"));
        StringResult result = new StringResult();

        smooks.setFilterSettings(filterSettings);

        smooks.filterSource(new StringSource("<a attrib1='1' ns1:attrib2='2' xmlns:ns1='http://ns1'><b xmlns:ns2='http://ns2'><d><e>some text</e></d></b><something/></a>"), result);

        XMLUnit.setIgnoreWhitespace( true );
        XMLAssert.assertXMLEqual(new StringReader("<a><b><e>some text</e></b><something /></a>"), new StringReader(result.getResult()));
    }
}
