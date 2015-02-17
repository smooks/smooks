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

package org.milyn.dtd;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.milyn.profile.DefaultProfileSet;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 
 * @author tfennelly
 */
public class DTDStoreTest {
	
	Log log = LogFactory.getLog(DTDStore.class);

	@Test
	public void testGetEmptyAnyMixedEtcElements() {
        DefaultProfileSet profileSet = new DefaultProfileSet("device3");

        profileSet.addProfiles(new String[] {"prof1", "prof2", "prof3"});
		DTDStore.addDTD(profileSet, getClass().getResourceAsStream("xhtml1-transitional.dtd"));
		DTDStore.DTDObjectContainer dtdContainer = DTDStore.getDTDObject(profileSet);
		
		String[] elements = dtdContainer.getEmptyElements();	
		assertTrue(Arrays.equals(new String[] {"basefont", "area", "link", "isindex", "col", "base", "meta", "img", "br", "hr", "param", "input"}, elements));
		elements = dtdContainer.getAnyElements();	
		assertTrue(Arrays.equals(new String[] {}, elements));
		elements = dtdContainer.getMixedElements();	
		assertTrue(Arrays.equals(new String[] {"button", "textarea", "em", "small", "noframes", "bdo", "form", "label", "dt", "span", "title", "strong", "script", "div", "blockquote", "kbd", "body", "ins", "dd", "fieldset", "big", "code", "option", "u", "s", "q", "p", "i", "pre", "caption", "b", "a", "style", "applet", "tt", "th", "center", "td", "samp", "font", "dfn", "noscript", "object", "sup", "h6", "h5", "h4", "h3", "h2", "h1", "iframe", "strike", "sub", "acronym", "del", "li", "cite", "var", "legend", "abbr", "address"}, elements));
		elements = dtdContainer.getPCDataElements();	
		assertTrue(Arrays.equals(new String[] {}, elements));
	}

	@Test
	public void testChildElements() {
        DefaultProfileSet profileSet = new DefaultProfileSet("device3");

        profileSet.addProfiles(new String[] {"prof1", "prof2", "prof3"});
		DTDStore.addDTD(profileSet, getClass().getResourceAsStream("xhtml1-transitional.dtd"));
		DTDStore.DTDObjectContainer dtdContainer = DTDStore.getDTDObject(profileSet);
		List l1 = dtdContainer.getChildElements("html");
		List l2 = dtdContainer.getChildElements("html");
		
		assertTrue(l1 == l2);
		assertEquals(2, dtdContainer.getChildElements("html").size());
		assertEquals(64, dtdContainer.getChildElements("body").size());
		assertEquals(0, dtdContainer.getChildElements("img").size());
		assertEquals(40, dtdContainer.getChildElements("b").size());
		assertEquals(null, dtdContainer.getChildElements("xxxx"));
	}

	@Test
	public void testElementAttributes() {
        DefaultProfileSet profileSet = new DefaultProfileSet("device3");

        profileSet.addProfiles(new String[] {"prof1", "prof2", "prof3"});
		DTDStore.addDTD(profileSet, getClass().getResourceAsStream("xhtml1-transitional.dtd"));
		DTDStore.DTDObjectContainer dtdContainer = DTDStore.getDTDObject(profileSet);
		List l1 = null;
		List l2 = null;
		try {
			l1 = dtdContainer.getElementAttributes("html");
			l2 = dtdContainer.getElementAttributes("html");
		} catch (ElementNotDefined e) {
			log.debug( "ElementNotDefined exception was throws: ", e);
			fail(e.getMessage());
		}
		
		assertTrue(l1 == l2);
		assertEquals(5, l1.size());
		assertTrue(l1.contains("dir"));
		assertTrue(l1.contains("xml:lang"));
		assertTrue(l1.contains("lang"));
		assertTrue(l1.contains("xmlns"));
		assertTrue(l1.contains("id"));

		try {
			dtdContainer.getElementAttributes("xxxxx");
			fail("Expected ElementNotDefined");
		} catch (ElementNotDefined e) {
			// OK
		}
	}
	
	private void print(String name) {
        DefaultProfileSet profileSet = new DefaultProfileSet("device3");

        profileSet.addProfiles(new String[] {"prof1", "prof2", "prof3"});
		DTDStore.addDTD(profileSet, getClass().getResourceAsStream(name));
		DTDStore.DTDObjectContainer dtdContainer = DTDStore.getDTDObject(profileSet);
		
		log.debug("-------- " + name + " ---------");
		log.debug(Arrays.asList(dtdContainer.getEmptyElements()));
	}
	
	@Test
	public void testPrint() {
		print("xhtml1-transitional.dtd");
		print("html32.xml.dtd");
		print("wml_1_1.dtd");
		print("wml12.dtd");
		print("wml13.dtd");
	}
}
