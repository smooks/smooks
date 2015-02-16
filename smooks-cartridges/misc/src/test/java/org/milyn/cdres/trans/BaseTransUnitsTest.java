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

package org.milyn.cdres.trans;

import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.cdr.annotation.Configurator;
import org.milyn.xml.XmlUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.junit.Test;
import static org.junit.Assert.*;

public class BaseTransUnitsTest {

        @Test
	public void test_RenameAttributeTU() {
		Document doc = parseCPResource("testpage1.html");
		SmooksResourceConfiguration resourceConfiguration = new SmooksResourceConfiguration("body", "device", "xxx");
		Element body = (Element)XmlUtil.getNode(doc, "/html/body");
		RenameAttributeTU tu;

		resourceConfiguration.setParameter("attributeName", "attrib1");
		resourceConfiguration.setParameter("attributeNewName", "attrib2");
		tu = Configurator.configure(new RenameAttributeTU(), resourceConfiguration);
        tu.visitAfter(body, null);
		assertEquals("Default overwrite protection failed.", "value2", body.getAttribute("attrib2"));

		resourceConfiguration.setParameter("overwrite", "true");
        tu = Configurator.configure(new RenameAttributeTU(), resourceConfiguration);
		tu.visitAfter(body, null);
		assertFalse("Rename failed to remove target attribute.", body.hasAttribute("attrib1"));
		assertEquals("Overwrite failed.", "value1", body.getAttribute("attrib2"));
	}

        @Test
	public void test_RemoveAttributeTU() {
		Document doc = parseCPResource("testpage1.html");
		SmooksResourceConfiguration resourceConfiguration = new SmooksResourceConfiguration("body", "device", "xxx");
		Element body = (Element)XmlUtil.getNode(doc, "/html/body");
		RemoveAttributeTU tu;

		resourceConfiguration.setParameter("attributeName", "attrib1");
        tu = Configurator.configure(new RemoveAttributeTU(), resourceConfiguration);

		assertTrue("XPath failed - test corrupted.", body.hasAttribute("attrib1"));
		tu.visitAfter(body, null);
		assertFalse("Failed to remove target attribute.", body.hasAttribute("attrib1"));
	}

        @Test
	public void test_RenameElementTU() {
		Document doc = parseCPResource("testpage1.html");
		SmooksResourceConfiguration resourceConfiguration = new SmooksResourceConfiguration("body", "device", "xxx");
		Element body = (Element)XmlUtil.getNode(doc, "/html/body");
		RenameElementTU tu;

		resourceConfiguration.setParameter("replacementElement", "head");
        tu = Configurator.configure(new RenameElementTU(), resourceConfiguration);

		tu.visitAfter(body, null);
		assertNull("Failed to rename target element.", XmlUtil.getNode(doc, "/html/body"));
		assertNotNull("Failed to rename target element.", XmlUtil.getNode(doc, "/html/head"));
	}

        @Test
	public void test_RenameElementTU_root_element() {
		Document doc = parseCPResource("testpage1.html");
		SmooksResourceConfiguration resourceConfiguration = new SmooksResourceConfiguration("body", "device", "xxx");
		Element body = (Element)XmlUtil.getNode(doc, "/html/body");
		RenameElementTU tu;

		resourceConfiguration.setParameter("replacementElement", "head");
        tu = Configurator.configure(new RenameElementTU(), resourceConfiguration);

		tu.visitAfter(body, null);
		assertNull("Failed to rename target element.", XmlUtil.getNode(doc, "/html/body"));
		assertNotNull("Failed to rename target element.", XmlUtil.getNode(doc, "/html/head"));
	}

        @Test
	public void test_RemoveElementTU() {
		Document doc = parseCPResource("testpage1.html");
		SmooksResourceConfiguration resourceConfiguration = new SmooksResourceConfiguration("body", "device", "xxx");
		Element body = (Element)XmlUtil.getNode(doc, "/html/body");
		RemoveElementTU tu;

        tu = Configurator.configure(new RemoveElementTU(), resourceConfiguration);

		tu.visitAfter(body, null);
		assertNull("Failed to remove target element.", XmlUtil.getNode(doc, "/html/body"));
	}

        @Test
	public void test_RemoveElementTU_root_element() {
		Document doc = parseCPResource("testpage1.html");
		SmooksResourceConfiguration resourceConfiguration = new SmooksResourceConfiguration("html", "xxx");
		Element body = (Element)XmlUtil.getNode(doc, "/html/body");
		RemoveElementTU tu;

        tu = Configurator.configure(new RemoveElementTU(), resourceConfiguration);

		// So remove the root element...
		tu.visitAfter(doc.getDocumentElement(), null);
		assertEquals("Failed to remove root element.", body, doc.getDocumentElement());

		// Try removing the new root element - should fail because the body element has no child elements...
		tu.visitAfter(doc.getDocumentElement(), null);
		assertEquals("Remove root element but shouldn't have.", body, doc.getDocumentElement());
	}

        @Test	
	public void test_SetAttributeTU() {
		Document doc = parseCPResource("testpage1.html");
		SmooksResourceConfiguration resourceConfiguration = new SmooksResourceConfiguration("body", "device", "xxx");
		Element body = (Element)XmlUtil.getNode(doc, "/html/body");
		SetAttributeTU tu;

		resourceConfiguration.setParameter("attributeName", "attrib1");
		resourceConfiguration.setParameter("attributeValue", "value3");
        tu = Configurator.configure(new SetAttributeTU(), resourceConfiguration);
		tu.visitAfter(body, null);
		assertEquals("Default overwrite protection failed.", "value1", body.getAttribute("attrib1"));

		resourceConfiguration.setParameter("overwrite", "true");
        tu = Configurator.configure(new SetAttributeTU(), resourceConfiguration);
		tu.visitAfter(body, null);
		assertEquals("Overwrite failed.", "value3", body.getAttribute("attrib1"));
	}
	
	
	public Document parseCPResource(String classpath) {
		try {
			return XmlUtil.parseStream(getClass().getResourceAsStream(classpath), XmlUtil.VALIDATION_TYPE.NONE, true);
		} catch (Exception e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		return null;
	}
}
