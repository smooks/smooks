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
package org.smooks.engine.delivery.JIRAs.MILYN_294;

import org.junit.Test;
import org.smooks.FilterSettings;
import org.smooks.Smooks;
import org.smooks.api.SmooksException;
import org.smooks.engine.delivery.dom.ProcessorVisitor1;
import org.smooks.engine.delivery.sax.SAXVisitor01;
import org.smooks.io.payload.StringSource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * http://jira.codehaus.org/browse/MILYN-294
 *
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class MILYN_294_TestCase {

	@Test
    public void test_setting_sax() {
        Smooks smooks = new Smooks();

        // Set the Smooks instance to use the SAX filter...
        smooks.setFilterSettings(FilterSettings.DEFAULT_SAX);

        // Add a DOM-only visitor
        smooks.addVisitor(new ProcessorVisitor1(), "a");

        try {
            smooks.filterSource(new StringSource("<a/>"));
            fail("Expected SmooksException.");
        } catch (SmooksException e) {
            assertEquals("The configured Filter ('SAX') cannot be used: [DOM] filters can be used for the given set of visitors. Turn on debug logging for more information.", e.getMessage());
        }
    }

	@Test
    public void test_setting_dom() {
        Smooks smooks = new Smooks();

        // Set the Smooks instance to use the DOM filter...
        smooks.setFilterSettings(FilterSettings.DEFAULT_DOM);

        // Add a SAX-only visitor
        smooks.addVisitor(new SAXVisitor01(), "a");

        try {
            smooks.filterSource(new StringSource("<a/>"));
            fail("Expected SmooksException.");
        } catch (SmooksException e) {
            assertEquals("The configured Filter ('DOM') cannot be used: [SAX] filters can be used for the given set of visitors. Turn on debug logging for more information.", e.getMessage());
        }
    }
}
