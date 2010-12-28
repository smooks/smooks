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

import java.net.URI;

import junit.framework.TestCase;

import org.milyn.Smooks;
import org.milyn.SmooksUtil;
import org.milyn.profile.DefaultProfileSet;
import org.milyn.container.ExecutionContext;
import org.milyn.magger.CSSProperty;
import org.milyn.magger.CSSStylesheet;
import org.milyn.xml.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class CssAccessorTest extends TestCase {

    private Smooks smooks;
    private ExecutionContext execContext;
	private StyleSheetStore store;
	
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
        smooks = new Smooks();
        SmooksUtil.registerProfileSet(DefaultProfileSet.create("device1", new String[] {"screen", "audio"}), smooks);

        execContext = smooks.createExecutionContext("device1");
        execContext.setDocumentSource(URI.create("http://x.com"));
        store = StyleSheetStore.getStore(execContext);
	}

	public void test_getProperty_1() {
		Document doc = CssTestUtil.parseCPResource("testpage1.html");
		Element style = (Element)XmlUtil.getNode(doc, "/html/head/style");
		Element p = (Element)XmlUtil.getNode(doc, "/html/body/p");
		Element h1 = (Element)XmlUtil.getNode(doc, "/html/body/h1");
		Element h1p = (Element)XmlUtil.getNode(doc, "/html/body/h1/p");
		Element h1h1 = (Element)XmlUtil.getNode(doc, "/html/body/h1/h1");
		CSSStylesheet styleSheet1;
		CSSStylesheet styleSheet2;
		
		styleSheet1 = CssTestUtil.parseCSS("style1.css");
		store.add(styleSheet1, style);
		
		CSSAccessor cssAccessor = CSSAccessor.getInstance(execContext);
		
		CSSProperty propVal_p = cssAccessor.getProperty(p, "font-size");
		assertNotNull(propVal_p);
		assertEquals("4em", propVal_p.getValue().toString());
		
		CSSProperty propVal_h1p = cssAccessor.getProperty(h1p, "font-size");
		assertNotNull(propVal_h1p);
		assertEquals("2em", propVal_h1p.getValue().toString());
		
		CSSProperty propVal_background_color = cssAccessor.getProperty(h1, "background-color");
		assertNull(propVal_background_color);
		
		// Add the second StyleSheet
		styleSheet2 = CssTestUtil.parseCSS("style2.css");
		store.add(styleSheet2, style);
		
		propVal_p = cssAccessor.getProperty(p, "font-size");
		assertNotNull(propVal_p);
		assertEquals("8em", propVal_p.getValue().toString());
		
		propVal_h1p = cssAccessor.getProperty(h1p, "font-size");
		assertNotNull(propVal_h1p);
		assertEquals("9em", propVal_h1p.getValue().toString());
		
		CSSProperty propVal_h1h1 = cssAccessor.getProperty(h1h1, "font-size");
		assertNotNull(propVal_h1h1);
		assertEquals("1em", propVal_h1h1.getValue().toString());
		
		propVal_background_color = cssAccessor.getProperty(h1, "background-color");
		assertNotNull(propVal_background_color);
		assertEquals("attr(blue)", propVal_background_color.getValue().toString());
	}
}
