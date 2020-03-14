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
package org.smooks.cdr;

import org.junit.Test;
import static org.junit.Assert.*;
import org.smooks.delivery.java.JavaXMLReader;
import org.smooks.delivery.AbstractParser;
import org.smooks.GenericReaderConfigurator;
import org.xml.sax.SAXException;

/**
 * @author
 */
public class GenericReaderConfiguratorTest {

	@Test
    public void test_resource_only() {
        GenericReaderConfigurator configurator = new GenericReaderConfigurator(JavaXMLReader.class);

        SmooksResourceConfiguration config = configurator.toConfig().get(0);
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

        SmooksResourceConfiguration config = configurator.toConfig().get(0);
        assertConfigOK(config, JavaXMLReader.class.getName(), 8, 3, 3);

        assertEquals("1", config.getStringParameter("a"));
        assertEquals("2", config.getStringParameter("b"));

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

        SmooksResourceConfiguration config = configurator.toConfig().get(0);
        assertConfigOK(config, null, 2, 1, 0);

        assertEquals("1", config.getStringParameter("a"));

        assertTrue(AbstractParser.isFeatureOn("http://a", config));
    }

    private void assertConfigOK(SmooksResourceConfiguration config, String resource, int numParams, int numFeaturesOn, int numFeaturesOff) {
        assertEquals(resource, config.getResource());
        assertEquals(numParams, config.getParameterCount());
        if(numFeaturesOn != 0) {
            assertEquals(numFeaturesOn, config.getParameters(AbstractParser.FEATURE_ON).size());
        } else {
            assertEquals(null, config.getParameters(AbstractParser.FEATURE_ON));
        }
        if(numFeaturesOff != 0) {
            assertEquals(numFeaturesOff, config.getParameters(AbstractParser.FEATURE_OFF).size());
        } else {
            assertEquals(null, config.getParameters(AbstractParser.FEATURE_OFF));
        }
    }
}
