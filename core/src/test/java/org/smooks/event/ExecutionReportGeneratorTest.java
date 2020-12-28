/*-
 * ========================LICENSE_START=================================
 * Smooks Core
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
package org.smooks.event;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smooks.Smooks;
import org.smooks.container.ExecutionContext;
import org.smooks.event.report.FlatReportGenerator;
import org.smooks.event.report.ReportConfiguration;
import org.smooks.io.StreamUtils;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.StringWriter;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class ExecutionReportGeneratorTest {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ExecutionReportGeneratorTest.class);

	@Test
    public void test_basic_dom() throws IOException, SAXException {
        Smooks smooks = new Smooks(); // Nothing targeted
        ExecutionContext execContext;
        String expected = new String(StreamUtils.readStream(getClass().getResourceAsStream("expected_dom.txt")));

        // DOM...
        execContext = smooks.createExecutionContext();
        String actual = runBasicTest(smooks, execContext);
        //assertTrue("Report output not as expected.", CharUtils.compareStrings(expected, actual));
    }

	@Test
    public void test_basic_sax() throws IOException, SAXException {
        Smooks smooks = new Smooks(); // Nothing targeted
        ExecutionContext execContext;
        String expected = new String(StreamUtils.readStream(getClass().getResourceAsStream("expected_sax.txt")));

        // SAX...
        execContext = smooks.createExecutionContext("null-sax");
        String actual = runBasicTest(smooks, execContext);
        //assertTrue("Report output not as expected.", CharUtils.compareStrings(expected, actual));
    }

    private String runBasicTest(Smooks smooks, ExecutionContext execContext) {
        StringWriter reportWriter = new StringWriter();

        execContext.getContentDeliveryRuntime().addExecutionEventListener(new FlatReportGenerator(new ReportConfiguration(reportWriter)));
        smooks.filterSource(execContext, new StreamSource(getClass().getResourceAsStream("test-data-01.xml")), new StreamResult(new StringWriter()));
        LOGGER.debug(reportWriter.toString());
        return reportWriter.toString();
    }
}
