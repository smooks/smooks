/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.smooks.engine.delivery.sax.ng.org.apache.xerces.stax.events;

import java.io.StringWriter;

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.smooks.engine.delivery.sax.ng.org.apache.xerces.stax.EmptyLocation;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.stax.ImmutableLocation;

/**
 * @xerces.internal
 * 
 * @author Lucian Holland
 *
 * @version $Id$
 */
abstract class XMLEventImpl implements XMLEvent {

    /**
     * Constant representing the type of this event. 
     * {@see javax.xml.stream.XMLStreamConstants}
     */
    private int fEventType;
    
    /**
     * Location object for this event.
     */
    private Location fLocation;

    /**
     * Constructor.
     */
    XMLEventImpl(final int eventType, final Location location) {
        fEventType = eventType;
        if (location != null) {
            fLocation = new ImmutableLocation(location);
        }
        else {
            fLocation = EmptyLocation.getInstance();
        }
    }

    /**
     * @see XMLEvent#getEventType()
     */
    public final int getEventType() {
        return fEventType;
    }

    /**
     * @see XMLEvent#getLocation()
     */
    public final Location getLocation() {
        return fLocation;
    }

    /**
     * @see XMLEvent#isStartElement()
     */
    public final boolean isStartElement() {
        return START_ELEMENT == fEventType;
    }

    /**
     * @see XMLEvent#isAttribute()
     */
    public final boolean isAttribute() {
        return ATTRIBUTE == fEventType;
    }

    /**
     * @see XMLEvent#isNamespace()
     */
    public final boolean isNamespace() {
        return NAMESPACE == fEventType;
    }

    /**
     * @see XMLEvent#isEndElement()
     */
    public final boolean isEndElement() {
        return END_ELEMENT == fEventType;
    }

    /**
     * @see XMLEvent#isEntityReference()
     */
    public final boolean isEntityReference() {
        return ENTITY_REFERENCE == fEventType;
    }

    /**
     * @see XMLEvent#isProcessingInstruction()
     */
    public final boolean isProcessingInstruction() {
        return PROCESSING_INSTRUCTION == fEventType;
    }

    /**
     * @see XMLEvent#isCharacters()
     */
    public final boolean isCharacters() {
        return CHARACTERS == fEventType ||
            CDATA == fEventType ||
            SPACE == fEventType;
    }

    /**
     * @see XMLEvent#isStartDocument()
     */
    public final boolean isStartDocument() {
        return START_DOCUMENT == fEventType;
    }

    /**
     * @see XMLEvent#isEndDocument()
     */
    public final boolean isEndDocument() {
        return END_DOCUMENT == fEventType;
    }

    /**
     * @see XMLEvent#asStartElement()
     */
    public final StartElement asStartElement() {
        return (StartElement) this;
    }

    /**
     * @see XMLEvent#asEndElement()
     */
    public final EndElement asEndElement() {
        return (EndElement) this;
    }

    /**
     * @see XMLEvent#asCharacters()
     */
    public final Characters asCharacters() {
        return (Characters) this;
    }

    /**
     * @see XMLEvent#getSchemaType()
     */
    public final QName getSchemaType() {
        return null;
    }
    
    public final String toString() {
        final StringWriter writer = new StringWriter();
        try {
            writeAsEncodedUnicode(writer);
        }
        catch (XMLStreamException xse) {}
        return writer.toString();
    }
}
