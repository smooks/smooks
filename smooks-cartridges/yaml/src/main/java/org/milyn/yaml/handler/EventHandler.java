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

import org.xml.sax.SAXException;
import org.yaml.snakeyaml.events.AliasEvent;
import org.yaml.snakeyaml.events.CollectionStartEvent;
import org.yaml.snakeyaml.events.Event;
import org.yaml.snakeyaml.events.NodeEvent;
import org.yaml.snakeyaml.events.ScalarEvent;

/**
 * Responsible for handling YAML events and converting them to SAX events.
 *
 * @author maurice_zeijen
 *
 */
public interface EventHandler {

	void addNameEvent(ScalarEvent event, String name) throws SAXException;

	void addValueEvent(ScalarEvent event, String name, String value) throws SAXException;

	void startStructureEvent(CollectionStartEvent event, String name) throws SAXException;

	void endStructureEvent(Event event, String name) throws SAXException;

	void addAliasEvent(AliasEvent event, String name) throws SAXException;

}
