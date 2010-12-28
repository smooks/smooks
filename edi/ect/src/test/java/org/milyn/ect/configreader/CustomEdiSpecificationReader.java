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
package org.milyn.ect.configreader;

import org.milyn.ect.EdiSpecificationReader;
import org.milyn.ect.EdiParseException;
import org.milyn.edisax.model.internal.Edimap;
import org.milyn.edisax.model.internal.Description;
import org.milyn.edisax.model.internal.Delimiters;
import org.milyn.edisax.model.internal.SegmentGroup;

import java.io.InputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Set;
import java.util.HashSet;

public class CustomEdiSpecificationReader implements EdiSpecificationReader {
    public void initialize(InputStream inputStream, boolean useImport) throws IOException, EdiParseException {
    }

    public Set<String> getMessageNames() {
        return new HashSet<String>();
    }

    public Edimap getMappingModel(String messageName) throws IOException {
        return createEdimap();
    }

    public Properties getInterchangeProperties() {
        return new Properties();
    }

    public Edimap getDefinitionModel() throws IOException {
        return createEdimap();
    }

    private Edimap createEdimap() {
        Edimap edimap = new Edimap();

        Description description = new Description();
        description.setName("Custom Config Reader");
        description.setVersion("1.0");
        edimap.setDescription(description);

        Delimiters delimiters = new Delimiters();
        delimiters.setSegment("'");
        delimiters.setField("+");
        delimiters.setComponent(":");
        delimiters.setSubComponent("^");
        delimiters.setEscape("?");
        edimap.setDelimiters(delimiters);

        SegmentGroup root = new SegmentGroup();
        root.setXmltag("Root");
        edimap.setSegments(root);

        return edimap;
    }
}
