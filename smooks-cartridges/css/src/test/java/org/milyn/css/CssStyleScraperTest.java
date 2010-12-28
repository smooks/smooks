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

import junit.framework.TestCase;
import org.milyn.Smooks;
import org.milyn.SmooksUtil;
import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.container.ApplicationContext;
import org.milyn.container.ExecutionContext;
import org.milyn.container.MockContainerResourceLocator;
import org.milyn.container.standalone.StandaloneApplicationContext;
import org.milyn.magger.CSSProperty;
import org.milyn.profile.DefaultProfileSet;
import org.milyn.xml.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

public class CssStyleScraperTest extends TestCase {

    private Smooks smooks;
    private ExecutionContext execContext;
    private ApplicationContext appContext;
    
    /* (non-Javadoc)
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        smooks = new Smooks();
        SmooksUtil.registerProfileSet(DefaultProfileSet.create("device1", new String[] {"blah"}), smooks);
        execContext = smooks.createExecutionContext("device1");
        execContext.setDocumentSource(URI.create("http://milyn.codehaus.org/myapp/aaa/mypage.html"));
        appContext = smooks.getApplicationContext();
    }
		
	public void testProcessPageCSS() {
		assertTrue("Expected CSS to be processed - href only.", 
				isCSSProcessed("href='mycss.css'"));
		assertTrue("Expected CSS to be processed - href + type.", 
				isCSSProcessed("href='mycss.css' type='text/css'"));
		assertTrue("Expected CSS to be processed - href + rel.", 
				isCSSProcessed("href='mycss.css' rel='stylesheet'"));
		assertTrue("Expected CSS to be processed - href + rel.", 
				isCSSProcessed("href='mycss.css' rel='xxx stylesheet'"));

        // register a new profile set and recreate the request.
        SmooksUtil.registerProfileSet(DefaultProfileSet.create("device2", new String[] {"screen"}), smooks);
        execContext = smooks.createExecutionContext("device2");
        execContext.setDocumentSource(URI.create("http://x.com"));

        assertTrue("Expected CSS to be processed - href + media.", 
				isCSSProcessed("href='mycss.css' media='screen'"));
		
		assertFalse("Expected CSS not to be processed - href + invalid media.", 
				isCSSProcessed("href='mycss.css' media='audio'"));
		assertFalse("Expected CSS not to be processed - href + invalid type.", 
				isCSSProcessed("href='mycss.css' type='xxx'"));
		assertFalse("Expected CSS not to be processed - href + alternate stylesheet rel.", 
				isCSSProcessed("href='mycss.css' rel='alternate stylesheet'"));
	}
	
	public void test_link_href_resolution() {
		String requestUri = "http://milyn.codehaus.org/myapp/aaa/mypage.html";
		
		assertEquals("http://milyn.codehaus.org/xxx/yyy/mycss.css", 
				getResolvedUri("/xxx/yyy/mycss.css", requestUri));

		assertEquals("http://milyn.codehaus.org/myapp/aaa/mycss.css", 
				getResolvedUri("mycss.css", requestUri));

		assertEquals("http://milyn.codehaus.org/myapp/mycss.css", 
				getResolvedUri("../mycss.css", requestUri));

		assertEquals("http://milyn.codehaus.org/mycss.css", 
				getResolvedUri("../../mycss.css", requestUri));

		assertEquals("http://milyn.codehaus.org/../mycss.css", 
				getResolvedUri("../../../mycss.css", requestUri));
	}
	
	public void testInlineStyle() {
		Document doc = CssTestUtil.parseXMLString("<x><style>p {background-color: white}</style><p/></x>"); 
		Element style = (Element)XmlUtil.getNode(doc, "/x/style");
		Element paragraph = (Element)XmlUtil.getNode(doc, "/x/p");
		SmooksResourceConfiguration cdrDef = new SmooksResourceConfiguration("link", "device", "xxx");
		CSSStyleScraper delivUnit = new CSSStyleScraper();
        delivUnit.setConfiguration(cdrDef);
        CssMockResLocator mockrl = new CssMockResLocator();
		
        appContext.setResourceLocator(mockrl);
		delivUnit.visitAfter(style, execContext);
		CSSAccessor accessor = CSSAccessor.getInstance(execContext);
		
		CSSProperty property = accessor.getProperty(paragraph, "background-color");
		assertNotNull("Expected CSS property.", property);
	}
	
	public String getResolvedUri(String href, String requestUri) {
		Document doc = CssTestUtil.parseXMLString("<x><link href='" + href + "' /></x>"); 
		Element link = (Element)XmlUtil.getNode(doc, "/x/link");
		SmooksResourceConfiguration cdrDef = new SmooksResourceConfiguration("link", "device", "xxx");
		CSSStyleScraper delivUnit = new CSSStyleScraper();
        delivUnit.setConfiguration(cdrDef);
        CssMockResLocator mockrl = new CssMockResLocator();
		
        appContext.setResourceLocator(mockrl);
		delivUnit.visitAfter(link, execContext);
		
		return mockrl.uri;
	}

	
	public boolean isCSSProcessed(String attribs) {
		Document doc = CssTestUtil.parseXMLString("<x><link " + attribs + " /></x>"); 
		Element link = (Element)XmlUtil.getNode(doc, "/x/link");
		SmooksResourceConfiguration cdrDef = new SmooksResourceConfiguration("link", "device", "xxx");
        CSSStyleScraper delivUnit = new CSSStyleScraper();
        delivUnit.setConfiguration(cdrDef);
		CssMockResLocator mockrl = new CssMockResLocator();
		
        appContext.setResourceLocator(mockrl);
		delivUnit.visitAfter(link, execContext);
		
		return (mockrl.uri != null);
	}
	
	private class CssMockResLocator extends MockContainerResourceLocator {

		private InputStream stream = CssStyleScraperTest.class.getResourceAsStream("style1.css");
		private String uri;

		/* (non-Javadoc)
		 * @see org.milyn.container.MockContainerResourceLocator#getResource(java.lang.String)
		 */
		public InputStream getResource(String uri) throws IllegalArgumentException, IOException {
			this.uri = uri;
			return stream;
		}
		
	}
}
