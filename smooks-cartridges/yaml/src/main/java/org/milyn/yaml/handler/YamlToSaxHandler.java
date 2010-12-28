/*
	Milyn - Copyright (C) 2008

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
package org.milyn.yaml.handler;

import javax.xml.XMLConstants;

import org.apache.commons.lang.StringUtils;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Converts yaml events into sax events.
 *
 * @author maurice_zeijen
 *
 */
public class YamlToSaxHandler {


    private static final AttributesImpl EMPTY_ATTRIBS = new AttributesImpl();

	private static final String ATTRIBUTE_IDREF = "IDREF";

	private static final String ATTRIBUTE_ID = "ID";

	private final ContentHandler contentHandler;

	private final String anchorAttributeName;

	private final String aliasAttributeName;

	private final boolean indent;

	private int elementLevel = 0;

    private static char[] INDENT = new String("\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t").toCharArray();

	public YamlToSaxHandler(ContentHandler contentHandler, String anchorAttributeName, String aliasAttributeName, boolean indent) {
		super();
		this.contentHandler = contentHandler;
		this.anchorAttributeName = anchorAttributeName;
		this.aliasAttributeName = aliasAttributeName;
		this.indent = indent;
	}

	public void startElementStructure(String name, String anchorName, boolean addAnchorAttribute) throws SAXException {

		indent();

		startElement(name, anchorName, addAnchorAttribute);

		elementLevel++;

	}

	public void endElementStructure(String name) throws SAXException {
		elementLevel--;

		indent();

		endElement(name);
	}


	public void addContentElement(String name, String  value, String anchorName, boolean addAnchorAttribute) throws SAXException {
		indent();

		startElement(name, anchorName, addAnchorAttribute);

		if(value != null && value.length() > 0) {
			contentHandler.characters(value.toCharArray(), 0, value.length());
		}

		endElement(name);
	}

	private void startElement(String name, String anchorName, boolean addAnchorAttribute) throws SAXException {
		AttributesImpl attributes;
		if (anchorName == null) {
			attributes = EMPTY_ATTRIBS;
		} else {
			attributes = new AttributesImpl();

			String attributeName = addAnchorAttribute ? anchorAttributeName : aliasAttributeName;
			String attributeType = addAnchorAttribute ? ATTRIBUTE_ID : ATTRIBUTE_IDREF;
			if(addAnchorAttribute) {

			}
			attributes.addAttribute(XMLConstants.NULL_NS_URI,
					attributeName, attributeName, attributeType,
					anchorName);
		}
		contentHandler.startElement(XMLConstants.NULL_NS_URI, name, StringUtils.EMPTY, attributes);
	}

	private void endElement(String name) throws SAXException {
		contentHandler.endElement(XMLConstants.NULL_NS_URI, name, StringUtils.EMPTY);
	}

	private void indent() throws SAXException {
		if (indent) {
			contentHandler.characters(INDENT, 0, elementLevel + 1);
		}
	}

}
