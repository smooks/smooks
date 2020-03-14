/*
 * Milyn - Copyright (C) 2006 - 2011
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License (version 2.1) as published by the Free Software
 * Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU Lesser General Public License for more details:
 * http://www.gnu.org/licenses/lgpl.txt
 */
package org.smooks.edit.utils.test;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Error handler that also shows the XML fragment
 * with the error in it
 * 
 * @author zubairov
 *
 */
public class ExtensiveErrorHandler implements ErrorHandler {

	private boolean errors = false;
	
	private final String xml;
	
	public ExtensiveErrorHandler(String xml) {
		this.xml = xml;
	}
	
	public void warning(SAXParseException exception) throws SAXException {
		System.err.println("Warning: " + exception.getMessage());
		errors = true;
		printExcept(exception.getLineNumber());
	}

	public void error(SAXParseException exception) throws SAXException {
		System.err.println("Error: " + exception.getMessage());
		errors = true;
		printExcept(exception.getLineNumber());
	}

	public void fatalError(SAXParseException exception) throws SAXException {
		throw new SAXException("Fatal parsing error", exception);
	}
	
	public boolean hasErrors() {
		return errors;
	}
	
	private void printExcept(int lineNumber) {
		String[] split = xml.split("\n");
		int start = lineNumber - 10 > 0? lineNumber - 10: 0;
		int end = lineNumber + 4 > split.length?split.length : lineNumber + 4;
		for(int i= start; i <= end; i++) {
			if (i == (lineNumber - 1)) {
				System.err.println((i + 1) + ">>>\t" + split[i]);
			} else {
				System.err.println((i + 1) +"   \t" + split[i]);
			}
		}
	}
	
}
