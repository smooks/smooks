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
package org.smooks.cdr.xsd11.readertests;

import org.junit.Test;
import org.smooks.Smooks;
import org.smooks.cdr.SmooksResourceConfiguration;
import org.smooks.delivery.AbstractParser;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class ReaderConfigTest {

	@Test
    public void test_01() throws IOException, SAXException {
        // Should allow reader config without a defined reader class i.e. config the default... 
        new Smooks(getClass().getResourceAsStream("config_01.xml"));
    }

	@Test
    public void test_02() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("config_02.xml"));
        SmooksResourceConfiguration readerConfig = AbstractParser.getSAXParserConfiguration(smooks.createExecutionContext("A").getDeliveryConfig());

        assertEquals("com.ZZZZReader", readerConfig.getResource());

        assertEquals("A", readerConfig.getTargetProfile());

        List handlers = readerConfig.getParameters("sax-handler");
        assertEquals("[com.X, com.Y]", handlers.toString());

        List featuresOn = readerConfig.getParameters("feature-on");
        assertEquals("[http://a, http://b]", featuresOn.toString());

        List featuresOff = readerConfig.getParameters("feature-off");
        assertEquals("[http://c, http://d]", featuresOff.toString());

        assertEquals("val1", readerConfig.getParameterValue("param1", String.class));
        assertEquals("val2", readerConfig.getParameterValue("param2", String.class));
    }
}
