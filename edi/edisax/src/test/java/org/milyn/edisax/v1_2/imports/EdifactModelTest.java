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

package org.milyn.edisax.v1_2.imports;

import junit.framework.TestCase;
import org.milyn.edisax.model.EdifactModel;
import org.milyn.edisax.model.internal.Segment;
import org.milyn.edisax.EDIConfigurationException;
import org.milyn.commons.io.StreamUtils;
import org.xml.sax.SAXException;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;

/**
 * Validates that the EdifactModel's logic works as expected.
 * @author bardl
 */
public class EdifactModelTest extends TestCase {
	
	private String packageName = "/" + EdifactModelTest.class.getPackage().getName().replace('.', '/');

    public void testImport_truncatableSegmentsExists() throws IOException, EDIConfigurationException, SAXException {
        InputStream input = new ByteArrayInputStream(StreamUtils.readStream(getClass().getResourceAsStream("edi-config-truncatableSegmentsExists.xml")));
        EdifactModel ediModel = new EdifactModel(input);

        assertTrue("The truncatable attribute should have value [true] in Segment.", ((Segment)ediModel.getEdimap().getSegments().getSegments().get(0).getSegments().get(0)).isTruncatable());
    }

    public void testImport_truncatableSegmentsExists_relative() throws IOException, EDIConfigurationException, SAXException {
        InputStream input = new ByteArrayInputStream(StreamUtils.readStream(getClass().getResourceAsStream("edi-config-truncatableSegmentsExists-relativepath.xml")));
        EdifactModel ediModel = new EdifactModel(URI.create(packageName + "/" + "edi-config-truncatableSegmentsExists-relativepath.xml"), URI.create(packageName), input);

        assertTrue("The truncatable attribute should have value [true] in Segment.", ((Segment)ediModel.getEdimap().getSegments().getSegments().get(0).getSegments().get(0)).isTruncatable());
    }

    public void testImport_truncatableSegmentsNotExists() throws IOException, EDIConfigurationException, SAXException {
        InputStream input = new ByteArrayInputStream(StreamUtils.readStream(getClass().getResourceAsStream("edi-config-truncatableSegmentsNotExists.xml")));
        EdifactModel ediModel = new EdifactModel(input);

        assertTrue("The truncatable attribute should have value [true] in Segment.", !((Segment)ediModel.getEdimap().getSegments().getSegments().get(0).getSegments().get(0)).isTruncatable());
    }        
}