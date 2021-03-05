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

import java.io.IOException;
import java.io.Writer;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;

import org.smooks.engine.delivery.sax.ng.org.apache.xerces.stax.events.XMLEventImpl;
import org.smooks.engine.delivery.sax.ng.org.apache.xerces.util.XMLChar;

/**
 * @xerces.internal
 * 
 * @author Lucian Holland
 *
 * @version $Id$
 */
public final class CharactersImpl extends org.smooks.engine.delivery.sax.ng.org.apache.xerces.stax.events.XMLEventImpl implements Characters {

    private final String fData;

    /**
     * Standard constructor.
     * @param eventType
     * @param location
     * @param schemaType
     */
    public CharactersImpl(final String data, final int eventType, final Location location) {
        super(eventType, location);
        fData = (data != null) ? data : "";
    }

    /**
     * @see Characters#getData()
     */
    public String getData() {
        return fData;
    }

    /**
     * @see Characters#isWhiteSpace()
     */
    public boolean isWhiteSpace() {
        final int length = fData != null ? fData.length() : 0;
        if (length == 0) {
            return false;
        }
        for (int i = 0; i < length; ++i) {
            if (!XMLChar.isSpace(fData.charAt(i))) {
                return false;
            }
        }
        return true; 
    }

    /**
     * @see Characters#isCData()
     */
    public boolean isCData() {
        return CDATA == getEventType();
    }

    /**
     * @see Characters#isIgnorableWhiteSpace()
     */
    public boolean isIgnorableWhiteSpace() {
        return SPACE == getEventType();
    }
    
    public void writeAsEncodedUnicode(Writer writer) throws XMLStreamException {
        try {
            writer.write(fData);
        }
        catch (IOException ioe) {
            throw new XMLStreamException(ioe);
        }
    }
}
