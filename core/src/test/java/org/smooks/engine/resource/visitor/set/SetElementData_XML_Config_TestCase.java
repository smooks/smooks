/*-
 * ========================LICENSE_START=================================
 * Core
 * %%
 * Copyright (C) 2020 Smooks
 * %%
 * Licensed under the terms of the Apache License Version 2.0, or
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
package org.smooks.engine.resource.visitor.set;

import org.junit.Test;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.smooks.FilterSettings;
import org.smooks.Smooks;
import org.smooks.api.ExecutionContext;
import org.smooks.io.payload.StringResult;
import org.smooks.io.payload.StringSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.StringReader;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class SetElementData_XML_Config_TestCase {

	@Test
    public void test_ChangeName_DOM() throws IOException, SAXException {
        test_ChangeName(FilterSettings.DEFAULT_DOM);
    }

    private void test_ChangeName(FilterSettings filterSettings) throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("config-01.xml"));
        StringResult result = new StringResult();

        smooks.setFilterSettings(filterSettings);

        smooks.filterSource(new StringSource("<a><b><d><e>some text</e></d></b></a>"), result);

        XMLUnit.setIgnoreWhitespace( true );
        XMLAssert.assertXMLEqual(new StringReader("<a><c><d><z>some text</z></d></c></a>"), new StringReader(result.getResult()));
    }

    @Test
    public void test_SetNamespace_DOM() throws IOException, SAXException {
        test_SetNamespace(FilterSettings.DEFAULT_DOM);
    }

    private void test_SetNamespace(FilterSettings filterSettings) throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("config-02.xml"));
        StringResult result = new StringResult();

        smooks.setFilterSettings(filterSettings);

        smooks.filterSource(new StringSource("<a><b><d><e>some text</e></d></b></a>"), result);

        XMLUnit.setIgnoreWhitespace( true );
        XMLAssert.assertXMLEqual(new StringReader("<a><c><d><xxx:z xmlns:xxx=\"http://xxx\">some text</xxx:z></d></c></a>"), new StringReader(result.getResult()));
    }

    @Test
    public void test_ChangeNamespace_1_DOM() throws IOException, SAXException {
        test_ChangeNamespace_1(FilterSettings.DEFAULT_DOM);
    }

    public void test_ChangeNamespace_1(FilterSettings filterSettings) throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("config-03.xml"));
        StringResult result = new StringResult();

        smooks.setFilterSettings(filterSettings);

        smooks.filterSource(new StringSource("<a><c><d><xxx:z xmlns:xxx=\"http://xxx\">some text</xxx:z></d></c></a>"), result);

        XMLUnit.setIgnoreWhitespace( true );
        XMLAssert.assertXMLEqual(new StringReader("<a><c><d><yyy:z xmlns:yyy=\"http://yyy\">some text</yyy:z></d></c></a>"), new StringReader(result.getResult()));
    }

    @Test
    public void test_ChangeNamespace_2_DOM() throws IOException, SAXException {
        test_ChangeNamespace_2(FilterSettings.DEFAULT_DOM);
    }

    public void test_ChangeNamespace_2(FilterSettings filterSettings) throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("config-04.xml"));
        StringResult result = new StringResult();

        smooks.setFilterSettings(filterSettings);

        smooks.filterSource(new StringSource("<a><c><d><xxx:z xmlns:xxx=\"http://xxx\">some text</xxx:z></d></c></a>"), result);

        XMLUnit.setIgnoreWhitespace( true );
        XMLAssert.assertXMLEqual(new StringReader("<a><c><d><xxx:z xmlns:xxx=\"http://yyy\">some text</xxx:z></d></c></a>"), new StringReader(result.getResult()));
    }

    @Test
    public void test_SetAttribute_1_DOM() throws IOException, SAXException {
        test_SetAttribute_1(FilterSettings.DEFAULT_DOM);
    }

    public void test_SetAttribute_1(FilterSettings filterSettings) throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("config-05.xml"));
        StringResult result = new StringResult();

        smooks.setFilterSettings(filterSettings);

        ExecutionContext execContext = smooks.createExecutionContext();
        execContext.getBeanContext().addBean("injectedVal", "something");

        smooks.filterSource(execContext, new StringSource("<a/>"), result);

        XMLUnit.setIgnoreWhitespace( true );
        XMLAssert.assertXMLEqual(new StringReader("<a xxx=\"something\" />"), new StringReader(result.getResult()));
    }

    @Test
    public void test_SetAttribute_2_DOM() throws IOException, SAXException {
        test_SetAttribute_2(FilterSettings.DEFAULT_DOM);
    }

    public void test_SetAttribute_2(FilterSettings filterSettings) throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("config-06.xml"));
        StringResult result = new StringResult();

        smooks.setFilterSettings(filterSettings);

        ExecutionContext execContext = smooks.createExecutionContext();
        execContext.getBeanContext().addBean("injectedVal", "something");

        smooks.filterSource(execContext, new StringSource("<a/>"), result);

        XMLUnit.setIgnoreWhitespace( true );
        XMLAssert.assertXMLEqual(new StringReader("<a xmlns:ns1=\"http://ns1\" ns1:xxx=\"something\" />"), new StringReader(result.getResult()));
    }
}
