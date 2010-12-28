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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.xerces.impl.xs.identity.ValueStore;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.yaml.snakeyaml.events.AliasEvent;
import org.yaml.snakeyaml.events.CollectionStartEvent;
import org.yaml.snakeyaml.events.Event;
import org.yaml.snakeyaml.events.NodeEvent;
import org.yaml.snakeyaml.events.ScalarEvent;

/**
 * The elements or value from the anchor are resolved (copied) under the element
 * with the alias.
 *
 * When addReferenceAttributes is true then attributes are set on the elements with
 * the anchors and alias containing the anchor name.
 *
 * @author maurice_zeijen
 *
 */
public class AliasResolvingEventHandler implements EventHandler {

	private final YamlToSaxHandler contentHandler;

	private final YamlEventStreamHandler eventStreamParser;

	private final boolean addReferenceAttributes;

	private int level = 0;

	private Map<String, Anchor> anchorMap = new HashMap<String, Anchor>();

	private Map<Integer, Anchor> activeAnchorMap = new HashMap<Integer, Anchor>();

	public AliasResolvingEventHandler(YamlEventStreamHandler eventStreamParser, YamlToSaxHandler contentHandler,
			boolean addReferenceAttributes) {
		this.eventStreamParser = eventStreamParser;
		this.contentHandler = contentHandler;
		this.addReferenceAttributes = addReferenceAttributes;
	}

	public void addValueEvent(ScalarEvent event, String name, String value) throws SAXException {
		addValueEvent(event, name, value, true);
	}

	private void addValueEvent(ScalarEvent event, String name, String value, boolean addAnchorAttribute) throws SAXException {
		if(event.getAnchor() != null) {
			addValueAnchor(event);
		}
		addToActiveAnchors(event);
		contentHandler.addContentElement(name, value, getAnchorName(event), addAnchorAttribute);
	}

	public void startStructureEvent(CollectionStartEvent event, String name) throws SAXException {
		level++;

		if(event.getAnchor() != null) {
			addStructureAnchor(event);
		}
		addToActiveAnchors(event);

		contentHandler.startElementStructure(name, getAnchorName(event), true);
	}

	public void endStructureEvent(Event event, String name) throws SAXException {
		addToActiveAnchors(event);
		removeActiveAnchors();

		level--;

		contentHandler.endElementStructure(name);
	}

	public void addAliasEvent(AliasEvent event, String name) throws SAXException {
		String anchorName = event.getAnchor();

		Anchor anchor = anchorMap.get(anchorName);
		if(anchor == null) {
			throw new SAXParseException(
					"A non existing anchor with the name '" + anchorName +
					"' is referenced by the alias of the element '" + name +
					"'. The anchor must be declared before it can be referenced by an alias.",
					null,
					null,
					event.getStartMark().getLine(),
					event.getStartMark().getColumn());
		}
		if(activeAnchorMap.values().contains(anchor)) {
			throw new SAXParseException(
					"The alias to anchor '" + anchorName +
					"' is declared within the element structure in which on of the parent elements declares the anchor. " +
					"This is not allowed because it leads to infinite loops.",
					null,
					null,
					event.getStartMark().getLine(),
					event.getStartMark().getColumn());
		}


		if(anchor.isValueAnchor()) {
			ScalarEvent scalarEvent = (ScalarEvent) anchor.getEvents().get(0);

			addValueEvent(scalarEvent, name, scalarEvent.getValue(), false);
		} else {
			contentHandler.startElementStructure(name, getAnchorName(event), false);

			eventStreamParser.handle(this, anchor.getEvents());

			contentHandler.endElementStructure(name);
		}

	}

	public void addNameEvent(ScalarEvent event, String name) throws SAXException {
		addToActiveAnchors(event);
	}

	private void addValueAnchor(NodeEvent event) {
		Anchor anchor = new Anchor(event.getAnchor(), true);
		anchor.addEvent(event);

		anchorMap.put(anchor.getName(), anchor);
	}

	private void addStructureAnchor(NodeEvent event) throws SAXException {
		Anchor anchor = new Anchor(event.getAnchor(), false);

		if(activeAnchorMap.values().contains(anchor)) {
			throw new SAXParseException(
					"The anchor '" + anchor.getName() +
					"' is declared within the data structure of an anchor with the same name.'",
					null,
					null,
					event.getStartMark().getLine(),
					event.getStartMark().getColumn());
		}

		anchorMap.put(anchor.getName(), anchor);
		activeAnchorMap.put(Integer.valueOf(level), anchor);
	}

	private void addToActiveAnchors(Event event) {
		for(Anchor anchor : activeAnchorMap.values()) {
			anchor.addEvent(event);
		}
	}

	private void removeActiveAnchors() {
		activeAnchorMap.remove(Integer.valueOf(level));
	}

	private String getAnchorName(NodeEvent nodeEvent) {
		return addReferenceAttributes ? nodeEvent.getAnchor(): null;
	}

	private static class Anchor {

		private final String name;

		private final boolean valueAnchor;

		private final List<Event> events = new ArrayList<Event>();

		private Anchor(String name, boolean valueAnchor) {
			super();
			this.name = name;
			this.valueAnchor = valueAnchor;
		}

		public String getName() {
			return name;
		}

		public void addEvent(Event event) {
			events.add(event);
		}

		public List<Event> getEvents() {
			return events;
		}

		public boolean isValueAnchor() {
			return valueAnchor;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((events == null) ? 0 : events.hashCode());
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			Anchor other = (Anchor) obj;
			if (events == null) {
				if (other.events != null) {
					return false;
				}
			} else if (!events.equals(other.events)) {
				return false;
			}
			if (name == null) {
				if (other.name != null) {
					return false;
				}
			} else if (!name.equals(other.name)) {
				return false;
			}
			return true;
		}

	}
}
