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
import java.util.List;

import javax.xml.transform.stream.StreamResult;

import org.milyn.Smooks;
import org.milyn.assertion.AssertArgument;
import org.milyn.delivery.sax.annotation.TextConsumer;

/**
 * {@link SAXElement} to XML writer class.
 * 
 * <p/>
 * This class encapsulates the writing of {@link SAXElement} content as XML.  It allows
 * writing of start, end and text parts of an element to a supplied {@link Writer}, or to the
 * {@link Writer} associated with any {@link StreamResult} that may have been supplied in
 * one of the {@link Smooks#filterSource(javax.xml.transform.Source, javax.xml.transform.Result) Smooks.filterSource()} method.
 * 
 * <p id="writing-text"/>
 * If you want to write text events with any of the <code>writeText</code> methods, you need to annotate the {@link SAXVisitor} 
 * instance provided in the {@link #SAXToXMLWriter(SAXVisitor, boolean) constructor} with the {@link TextConsumer @TextConsumer}
 * Annotation.
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class SAXToXMLWriter {

	private SAXVisitor owner;
	private boolean encodeSpecialChars;

	/**
	 * Public constructor.
	 * @param owner The owning {@link SAXVisitor} instance.
	 * @param encodeSpecialChars Encode special XML characters.
	 */
	public SAXToXMLWriter(SAXVisitor owner, boolean encodeSpecialChars) {
		AssertArgument.isNotNull(owner, "owner");
		this.owner = owner;
		this.encodeSpecialChars = encodeSpecialChars;
	}

	/**
	 * Write the element start to the supplied writer instance.
	 * @param element The element.
	 * @param writer The writer.
	 * @throws IOException Exception writing.
	 */
    public void writeStartElement(SAXElement element, Writer writer) throws IOException {
    	SAXElementWriterUtil.writeStartElement(element, writer, encodeSpecialChars);
    }

	/**
	 * Write the element start to any {@link StreamResult} instance that may have been supplied to the
     * {@link Smooks#filterSource(javax.xml.transform.Source, javax.xml.transform.Result) Smooks.filterSource()} 
     * method.
	 * @param element The element.
	 * @throws IOException Exception writing.
	 */
    public void writeStartElement(SAXElement element) throws IOException {
    	SAXElementWriterUtil.writeStartElement(element, element.getWriter(owner), encodeSpecialChars);
    }

    
	/**
	 * Write the element end to the supplied {@link Writer}.
	 * 
	 * @param element The element.
	 * @param writer The Writer.
	 * @throws IOException Exception writing.
	 */
    public void writeEndElement(SAXElement element, Writer writer) throws IOException {
    	SAXElementWriterUtil.writeEndElement(element, writer);
    }

	/**
	 * Write the element end to any {@link StreamResult} instance that may have been supplied to the
     * {@link Smooks#filterSource(javax.xml.transform.Source, javax.xml.transform.Result) Smooks.filterSource()} 
     * method.
	 * @param element The element.
	 * @throws IOException Exception writing.
	 */
    public void writeEndElement(SAXElement element) throws IOException {
    	SAXElementWriterUtil.writeEndElement(element, element.getWriter(owner));
    }

	/**
	 * Write the element text to the supplied {@link Writer} instance.
     * <p/>
     * <a href="#writing-text">See about writing text</a>.
     * 
	 * @param element The element.
	 * @param writer The Writer.
	 * @throws IOException Exception writing.
	 */
    public void writeText(SAXElement element, Writer writer) throws IOException {
    	List<SAXText> textList = element.getText();
    	
    	if(textList == null) {
    		return;
    	}
    	
		for(SAXText text : textList) {
        	SAXElementWriterUtil.writeText(text, writer);
    	}
    }	

	/**
	 * Write the element text to any {@link StreamResult} instance that may have been supplied to the
     * {@link Smooks#filterSource(javax.xml.transform.Source, javax.xml.transform.Result) Smooks.filterSource()} 
     * method.
     * <p/>
     * <a href="#writing-text">See about writing text</a>.
     * 
	 * @param element The element.
	 * @throws IOException Exception writing.
	 */
    public void writeText(SAXElement element) throws IOException {
    	writeText(element, element.getWriter(owner));
    }	

	/**
	 * Write the text event content to the supplied {@link Writer} instance.
     * 
	 * @param text The SAXText event.
	 * @param writer The Writer.
	 * @throws IOException Exception writing.
	 */
    public void writeText(SAXText text, Writer writer) throws IOException {
    	SAXElementWriterUtil.writeText(text, writer);
    }	

	/**
	 * Write the text event content to any {@link StreamResult} instance that may have been supplied to the
     * {@link Smooks#filterSource(javax.xml.transform.Source, javax.xml.transform.Result) Smooks.filterSource()} 
     * method.
     * 
	 * @param text The SAXText event.
	 * @param associatedElement The associated element (fragment) of the SAXText event.
	 * @throws IOException Exception writing.
	 */
    public void writeText(SAXText text, SAXElement associatedElement) throws IOException {
    	SAXElementWriterUtil.writeText(text, associatedElement.getWriter(owner));
    }	
    
	/**
	 * Write the text content to the supplied {@link Writer}.
     * 
	 * @param text The text.
	 * @param writer The Writer.
	 * @throws IOException Exception writing.
	 */
    public void writeText(String text, Writer writer) throws IOException {
    	SAXElementWriterUtil.writeText(text, TextType.TEXT, writer);
    }
    
	/**
	 * Write the text content to any {@link StreamResult} instance that may have been supplied to the
     * {@link Smooks#filterSource(javax.xml.transform.Source, javax.xml.transform.Result) Smooks.filterSource()} 
     * method.
     * 
	 * @param text The text.
	 * @param associatedElement The associated element (fragment) of the text.
	 * @throws IOException Exception writing.
	 */
    public void writeText(String text, SAXElement associatedElement) throws IOException {
    	SAXElementWriterUtil.writeText(text, TextType.TEXT, associatedElement.getWriter(owner));
    }
	
	/**
	 * Write the element as an empty (closed) element to the supplied {@link Writer}.
	 * @param element The element.
	 * @param writer The Writer.
	 * @throws IOException Exception writing.
	 */
    public void writeEmptyElement(SAXElement element, Writer writer) throws IOException {
    	SAXElementWriterUtil.writeEmptyElement(element, writer, encodeSpecialChars);
    }
	
	/**
	 * Write the element as an empty (closed) element to any {@link StreamResult} instance that may have been supplied to the
     * {@link Smooks#filterSource(javax.xml.transform.Source, javax.xml.transform.Result) Smooks.filterSource()} 
     * method.
	 * @param element The element.
	 * @throws IOException Exception writing.
	 */
    public void writeEmptyElement(SAXElement element) throws IOException {
    	SAXElementWriterUtil.writeEmptyElement(element, element.getWriter(owner), encodeSpecialChars);
    }
}
