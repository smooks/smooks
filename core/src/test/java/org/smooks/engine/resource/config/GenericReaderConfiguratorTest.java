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
package org.smooks.engine.resource.config;

import org.junit.Test;
import org.smooks.api.resource.config.ResourceConfig;
import org.smooks.engine.delivery.AbstractParser;
import org.smooks.api.resource.reader.JavaXMLReader;
import org.xml.sax.SAXException;

import static org.junit.Assert.*;

/**
 * @author
 */
public class GenericReaderConfiguratorTest {

	@Test
    public void test_resource_only() {
        GenericReaderConfigurator configurator = new GenericReaderConfigurator(JavaXMLReader.class);

        ResourceConfig config = configurator.toConfig().get(0);
        assertConfigOK(config, JavaXMLReader.class.getName(), 0, 0, 0);
    }

	@Test
    public void test_resource_and_features_and_params() throws SAXException {
        GenericReaderConfigurator configurator = new GenericReaderConfigurator(JavaXMLReader.class);

        configurator.getParameters().setProperty("a", "1");
        configurator.getParameters().setProperty("b", "2");
        configurator.setFeature("http://a", true);
        configurator.setFeature("http://b", true);
        configurator.setFeature("http://c", false);
        configurator.setFeature("http://d", true);
        configurator.setFeature("http://e", false);
        configurator.setFeature("http://f", false);

        ResourceConfig config = configurator.toConfig().get(0);
        assertConfigOK(config, JavaXMLReader.class.getName(), 8, 3, 3);

        assertEquals("1", config.getParameterValue("a", String.class));
        assertEquals("2", config.getParameterValue("b", String.class));

        assertTrue(AbstractParser.isFeatureOn("http://a", config));
        assertTrue(AbstractParser.isFeatureOn("http://b", config));
        assertTrue(AbstractParser.isFeatureOff("http://c", config));
        assertTrue(AbstractParser.isFeatureOn("http://d", config));
        assertTrue(AbstractParser.isFeatureOff("http://e", config));
        assertTrue(AbstractParser.isFeatureOff("http://f", config));
    }

	@Test
    public void test_features_and_params_only() throws SAXException {
        GenericReaderConfigurator configurator = new GenericReaderConfigurator();

        configurator.getParameters().setProperty("a", "1");
        configurator.setFeature("http://a", true);

        ResourceConfig config = configurator.toConfig().get(0);
        assertConfigOK(config, null, 2, 1, 0);

        assertEquals("1", config.getParameterValue("a", String.class));

        assertTrue(AbstractParser.isFeatureOn("http://a", config));
    }

    private void assertConfigOK(ResourceConfig config, String resource, int numParams, int numFeaturesOn, int numFeaturesOff) {
        assertEquals(resource, config.getResource());
        assertEquals(numParams, config.getParameterCount());
        if(numFeaturesOn != 0) {
            assertEquals(numFeaturesOn, config.getParameters(AbstractParser.FEATURE_ON).size());
        } else {
            assertNull(config.getParameters(AbstractParser.FEATURE_ON));
        }
        if(numFeaturesOff != 0) {
            assertEquals(numFeaturesOff, config.getParameters(AbstractParser.FEATURE_OFF).size());
        } else {
            assertNull(config.getParameters(AbstractParser.FEATURE_OFF));
        }
    }
}
