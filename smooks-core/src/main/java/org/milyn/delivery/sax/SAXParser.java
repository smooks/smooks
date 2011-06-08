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
package org.milyn.delivery.sax;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.Charset;

import javax.xml.transform.Result;
import javax.xml.transform.Source;

import org.milyn.container.ExecutionContext;
import org.milyn.delivery.AbstractParser;
import org.milyn.delivery.ContentDeliveryConfig;
import org.milyn.delivery.XMLReaderHierarchyChangeListener;
import org.milyn.xml.hierarchy.HierarchyChangeReader;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.DefaultHandler2;

/**
 * Smooks SAX data stream parser.
 * <p/>
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class SAXParser extends AbstractParser {

    private SAXHandler saxHandler;

    public SAXParser(ExecutionContext execContext) {
        super(execContext);
    }

    protected Writer parse(Source source, Result result, ExecutionContext executionContext) throws SAXException, IOException {

        Writer writer = getWriter(result, executionContext);
        ContentDeliveryConfig deliveryConfig = executionContext.getDeliveryConfig();
        XMLReader saxReader = getXMLReader(executionContext);

        saxHandler = new SAXHandler(getExecContext(), writer);

        try {
            if(saxReader == null) {
                saxReader = deliveryConfig.getXMLReader();
            }
            if(saxReader == null) {
                saxReader = createXMLReader();
            }
            attachXMLReader(saxReader, executionContext);

            configureReader(saxReader, saxHandler, executionContext, source);
            if(executionContext != null) {
                if(saxReader instanceof HierarchyChangeReader) {
                    ((HierarchyChangeReader)saxReader).setHierarchyChangeListener(new XMLReaderHierarchyChangeListener(executionContext));
                }
	            saxReader.parse(createInputSource(source, executionContext.getContentEncoding()));
            } else {
                saxReader.parse(createInputSource(source, Charset.defaultCharset().name()));
            }
        } finally {
            try {
                if(executionContext != null && saxReader instanceof HierarchyChangeReader) {
                    ((HierarchyChangeReader)saxReader).setHierarchyChangeListener(null);
                }
            } finally {
                try {
                    if(saxReader != null) {
                        try {
                            detachXMLReader(executionContext);
                        } finally {
                            deliveryConfig.returnXMLReader(saxReader);
                        }
                    }
                } finally {
                    saxHandler.detachHandler();
                }
            }
        }
        
        return writer;
    }

    public void cleanup() {
        if(saxHandler != null) {
            saxHandler.cleanup();
        }
    }
}
