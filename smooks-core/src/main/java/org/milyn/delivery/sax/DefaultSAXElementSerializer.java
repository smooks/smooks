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

import org.milyn.container.ExecutionContext;
import org.milyn.SmooksException;
import org.milyn.delivery.Filter;
import org.milyn.cdr.annotation.ConfigParam;

import java.io.IOException;

/**
 * Default Serializer for SAX Filtering.
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class DefaultSAXElementSerializer implements SAXElementVisitor {

    private SAXVisitor writerOwner = this;
    private boolean rewriteEntities = true;

    public void setWriterOwner(SAXVisitor writerOwner) {
        this.writerOwner = writerOwner;
    }

    @ConfigParam(name = Filter.ENTITIES_REWRITE, defaultVal = "true")
    public void setRewriteEntities(boolean rewriteEntities) {
        this.rewriteEntities = rewriteEntities;
    }

    public void visitBefore(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
        // Do nothing here apart from acquiring ownership of the element writer.
        // See is there any child text/elements first...
        element.getWriter(writerOwner);
    }

    public void onChildText(SAXElement element, SAXText text, ExecutionContext executionContext) throws SmooksException, IOException {
        writeStartElement(element);
        if(element.isWriterOwner(writerOwner)) {
            text.toWriter(element.getWriter(writerOwner), rewriteEntities);
        }
    }

    public void onChildElement(SAXElement element, SAXElement childElement, ExecutionContext executionContext) throws SmooksException, IOException {
        writeStartElement(element);
        // The child element is responsible for writing itself...
    }

    public void visitAfter(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
        writeEndElement(element);
    }

    public void writeStartElement(SAXElement element) throws IOException {
        // We set a flag in the cache so as to mark the fact that the start element has been writen
        if(element.isWriterOwner(writerOwner)) {
            if(!isStartWritten(element)) {
                element.setCache(this, true);
                writeStart(element);
            }
        }
    }

    public void writeEndElement(SAXElement element) throws IOException {
        if(element.isWriterOwner(writerOwner)) {
            writeEnd(element);
        }
    }

    protected void writeStart(SAXElement element) throws IOException {
        SAXElementWriterUtil.writeStartElement(element, element.getWriter(writerOwner), rewriteEntities);
    }

    protected void writeEnd(SAXElement element) throws IOException {
        if(!isStartWritten(element)) {
            // It's an empty element...
            SAXElementWriterUtil.writeEmptyElement(element, element.getWriter(writerOwner), rewriteEntities);
        } else {
            SAXElementWriterUtil.writeEndElement(element, element.getWriter(writerOwner));
        }
    }

    public boolean isStartWritten(SAXElement element) {
        return element.getCache(this) != null;
    }
}
