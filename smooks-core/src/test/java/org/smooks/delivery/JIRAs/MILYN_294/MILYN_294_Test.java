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
package org.smooks.delivery.JIRAs.MILYN_294;

import org.junit.Test;
import static org.junit.Assert.*;
import org.smooks.Smooks;
import org.smooks.FilterSettings;
import org.smooks.SmooksException;
import org.smooks.payload.StringSource;
import org.smooks.delivery.dom.ProcessorVisitor1;
import org.smooks.delivery.sax.SAXVisitor01;

/**
 * http://jira.codehaus.org/browse/MILYN-294
 *
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class MILYN_294_Test {

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
            assertEquals("The configured Filter ('SAX') cannot be used with the specified set of Smooks visitors.  The 'DOM' Filter is the only filter that can be used for this set of Visitors.  Turn on Debug logging for more information.", e.getMessage());
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
            assertEquals("The configured Filter ('DOM') cannot be used with the specified set of Smooks visitors.  The 'SAX' Filter is the only filter that can be used for this set of Visitors.  Turn on Debug logging for more information.", e.getMessage());
        }
    }
}
