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
package org.smooks.yaml.handler;

import org.xml.sax.SAXException;
import org.yaml.snakeyaml.events.AliasEvent;
import org.yaml.snakeyaml.events.CollectionStartEvent;
import org.yaml.snakeyaml.events.Event;
import org.yaml.snakeyaml.events.ScalarEvent;

/**
 * Adds a 'id' attribute to the element with the anchor and the 'ref' attribute
 * to the elements with the alias. The value of these attributes is the name of
 * the anchor. The reference needs to be handled within the Smooks config. The
 * attribute names can be set via the 'anchorAttributeName' and
 * 'aliasAttributeName' properties.
 *
 * @author maurice_zeijen
 *
 */
public class AliasReferencingEventHandler implements EventHandler {

	private final YamlToSaxHandler contentHandler;

	public AliasReferencingEventHandler(YamlToSaxHandler contentHandler) {
		this.contentHandler = contentHandler;
	}

	public void addValueEvent(ScalarEvent event, String name, String value) throws SAXException {
		contentHandler.addContentElement(name, value, event.getAnchor(), true);
	}

	public void startStructureEvent(CollectionStartEvent event, String name) throws SAXException {
		contentHandler.startElementStructure(name, event.getAnchor(), true);
	}

	public void endStructureEvent(Event event, String name) throws SAXException {
		contentHandler.endElementStructure(name);
	}

	public void addAliasEvent(AliasEvent event, String name) throws SAXException {
		contentHandler.addContentElement(name, null, event.getAnchor(), false);
	}

	public void addNameEvent(ScalarEvent event, String name) throws SAXException {
		// Nothing to do here because we are not interrested in these events. The
		// names are provided to the methods directly and the event object has no use
		// here
	}
}
