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

package org.milyn.smooks.scripting.groovy;

import junit.framework.TestCase;
import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.commons.io.StreamUtils;
import org.milyn.commons.xml.XmlUtil;
import org.milyn.delivery.dom.DOMElementVisitor;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * @author tfennelly
 */
public class GroovyContentHandlerFactoryTest extends TestCase {

    public void test_goodscript_by_URI() throws InstantiationException, IllegalArgumentException, IOException, SAXException {
        test_goodscript_by_URI("classpath:/org/milyn/smooks/scripting/groovy/MyGroovyScript.groovy");
        test_goodscript_by_URI("/org/milyn/smooks/scripting/groovy/MyGroovyScript.groovy");
    }

    public void test_goodscript_by_Inlining() throws InstantiationException, IllegalArgumentException, IOException, SAXException {
        String script = new String(StreamUtils.readStream(getClass().getResourceAsStream("MyGroovyScript.groovy")));
        SmooksResourceConfiguration config = new SmooksResourceConfiguration("x", null);

        config.setParameter("resdata", script);
        test_goodscript(config);
    }

    private void test_goodscript_by_URI(String path) throws InstantiationException, IllegalArgumentException, IOException, SAXException {
        test_goodscript(new SmooksResourceConfiguration("x", path));
    }

    private void test_goodscript(SmooksResourceConfiguration config) throws InstantiationException, IllegalArgumentException, IOException, SAXException {
        GroovyContentHandlerFactory creator = new GroovyContentHandlerFactory();

        config.setParameter("new-name", "yyy");
        DOMElementVisitor resource = (DOMElementVisitor) creator.create(config);

        Document doc = XmlUtil.parseStream(new ByteArrayInputStream("<xxx/>".getBytes()), XmlUtil.VALIDATION_TYPE.NONE, false);
        assertEquals("xxx", doc.getDocumentElement().getTagName());
        resource.visitAfter(doc.getDocumentElement(), null);
        assertEquals("yyy", doc.getDocumentElement().getTagName());
    }
}
