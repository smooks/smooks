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

package org.milyn.css;

import java.util.Iterator;

import org.milyn.css.StyleSheetStore.StoreEntry;
import org.milyn.container.MockExecutionContext;
import org.milyn.magger.CSSStylesheet;
import org.milyn.xml.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import junit.framework.TestCase;

public class StyleSheetStoreTest extends TestCase {

	private MockExecutionContext request;
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
        request = new MockExecutionContext();
	}

	public void testGetStore() {
		StyleSheetStore store;	
	
		store = StyleSheetStore.getStore(request);
		assertNotNull("Expected to get a store instance from the request.", store);
	}

	public void testAdd() {
		StyleSheetStore store;	
		CSSStylesheet styleSheet1 = CssTestUtil.parseCSS("html40.css");
		CSSStylesheet styleSheet2 = CssTestUtil.parseCSS("html40.css");
		CSSStylesheet styleSheet3 = CssTestUtil.parseCSS("html40.css");
		Iterator iterator;
		Document doc = CssTestUtil.parseXMLString("<html><head><link/></head><body><style/></body></html>");
		Element link = (Element)XmlUtil.getNode(doc, "/html/head/link");
		Element style = (Element)XmlUtil.getNode(doc, "/html/body/style");

		// Nothing in the store
		store = StyleSheetStore.getStore(request);
		iterator = store.iterator();
		assertFalse("Expected empty iterator",  iterator.hasNext());
		
		// 1 Stylesheet in the store
		store.add(styleSheet1, null);
		iterator = store.iterator();
		StoreEntry entry = (StoreEntry)iterator.next();
		assertEntryOK(entry, styleSheet1, null, false);
		
		// 3 Stylesheets in the store - 2 and 3 are associated 
		// with style elements - 2 is in the head and the other 
		// isn't - see the DOM Document above.
		store.add(styleSheet2, link);
		store.add(styleSheet3, style);
		
		// Should iterate in the order they were added - so 1, 2, 3 should 
		// be the order.
		iterator = store.iterator();
		entry = (StoreEntry)iterator.next();
		assertEntryOK(entry, styleSheet1, null, false);
		entry = (StoreEntry)iterator.next();
		assertEntryOK(entry, styleSheet2, link, true);
		entry = (StoreEntry)iterator.next();
		assertEntryOK(entry, styleSheet3, style, false);
	}

	private void assertEntryOK(StoreEntry entry, CSSStylesheet styleSheet, Element element, boolean isLinked) {
		assertEquals(styleSheet, entry.getStylesheet());
		assertEquals(element, entry.getStyleElement());
		assertEquals(isLinked, entry.isLinked());
	}	
}
