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
package org.milyn.edisax.v1_5.namespaces;

import java.io.IOException;
import java.util.List;

import junit.framework.TestCase;

import org.milyn.edisax.EDIConfigurationException;
import org.milyn.edisax.EDIParser;
import org.milyn.edisax.model.EdifactModel;
import org.milyn.edisax.model.internal.Component;
import org.milyn.edisax.model.internal.Edimap;
import org.milyn.edisax.model.internal.Field;
import org.milyn.edisax.model.internal.Segment;
import org.milyn.edisax.model.internal.SegmentGroup;
import org.xml.sax.SAXException;

/**
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class NamespacesTest extends TestCase {

	private static final String NS = "http://smooks.org/edi/un/test.xsd";

	public void test() throws IOException, SAXException,
			EDIConfigurationException {
		EdifactModel msg1 = EDIParser.parseMappingModel(getClass()
				.getResourceAsStream("edi-to-xml-mapping.xml"));
		assertNotNull(msg1);
		Edimap edimap = msg1.getEdimap();
		assertEquals(NS,edimap.getNamespace());
		SegmentGroup group = edimap.getSegments();
		assertEquals(NS,group.getNamespace());
		List<SegmentGroup> segments = group.getSegments();
		for (SegmentGroup segment : segments) {
			assertEquals(NS,segment.getNamespace());
			if (segment instanceof Segment) {
				List<Field> fields = ((Segment)segment).getFields();
				for (Field field : fields) {
					assertEquals(NS,field.getNamespace());
					for (Component comp : field.getComponents()) {
						assertEquals(NS,comp.getNamespace());
					}
				}
			}
		}
	}
}
