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

import org.smooks.engine.delivery.sax.ng.org.apache.xerces.stax.events.XMLEventImpl;

import java.io.IOException;
import java.io.Writer;

import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EntityDeclaration;

/**
 * @xerces.internal
 * 
 * @author Lucian Holland
 *
 * @version $Id$
 */
public final class EntityDeclarationImpl extends org.smooks.engine.delivery.sax.ng.org.apache.xerces.stax.events.XMLEventImpl implements
        EntityDeclaration {

    private final String fPublicId;
    private final String fSystemId;
    private final String fName;
    private final String fNotationName;

    /**
     * @param eventType
     * @param location
     * @param schemaType
     */
    public EntityDeclarationImpl(final String publicId, final String systemId, final String name, final String notationName, final Location location) {
        super(ENTITY_DECLARATION, location);
        fPublicId = publicId;
        fSystemId = systemId;
        fName = name;
        fNotationName = notationName;
    }

    /**
     * @see EntityDeclaration#getPublicId()
     */
    public String getPublicId() {
        return fPublicId;
    }

    /**
     * @see EntityDeclaration#getSystemId()
     */
    public String getSystemId() {
        return fSystemId;
    }

    /**
     * @see EntityDeclaration#getName()
     */
    public String getName() {
        return fName;
    }

    /**
     * @see EntityDeclaration#getNotationName()
     */
    public String getNotationName() {
        return fNotationName;
    }

    /**
     * @see EntityDeclaration#getReplacementText()
     */
    public String getReplacementText() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * @see EntityDeclaration#getBaseURI()
     */
    public String getBaseURI() {
        // TODO Auto-generated method stub
        return null;
    }
    
    public void writeAsEncodedUnicode(Writer writer) throws XMLStreamException {
        try {
            writer.write("<!ENTITY ");
            writer.write(fName);
            if (fPublicId != null) {
                writer.write(" PUBLIC \"");
                writer.write(fPublicId);
                writer.write("\" \"");
                writer.write(fSystemId);
                writer.write('"');
            }
            else {
                writer.write(" SYSTEM \"");
                writer.write(fSystemId);
                writer.write('"');
            }
            if (fNotationName != null) {
                writer.write(" NDATA ");
                writer.write(fNotationName);
            }
            writer.write('>');
        }
        catch (IOException ioe) {
            throw new XMLStreamException(ioe);
        }
    }
}
