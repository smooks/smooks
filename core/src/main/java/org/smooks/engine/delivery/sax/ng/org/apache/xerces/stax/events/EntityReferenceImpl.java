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
import javax.xml.stream.events.EntityReference;

/**
 * @xerces.internal
 * 
 * @author Lucian Holland
 *
 * @version $Id$
 */
public final class EntityReferenceImpl extends org.smooks.engine.delivery.sax.ng.org.apache.xerces.stax.events.XMLEventImpl implements
        EntityReference {
    
    /**
     * The name of the entity.
     */
    private final String fName;

    /**
     * The entity declaration for this entity reference.
     */
    private final EntityDeclaration fDecl;
    
    /**
     * Constructor.
     * @param decl
     * @param location
     */
    public EntityReferenceImpl(final EntityDeclaration decl, final Location location) {
        this(decl != null ? decl.getName() : "", decl, location);
    }
    
    /**
     * Constructor.
     * @param name
     * @param decl
     * @param location
     */
    public EntityReferenceImpl(final String name, final EntityDeclaration decl, final Location location) {
        super(ENTITY_REFERENCE, location);
        fName = (name != null) ? name : "";
        fDecl = decl;
    }

    /**
     * @see EntityReference#getDeclaration()
     */
    public EntityDeclaration getDeclaration() {
        return fDecl;
    }

    /**
     * @see EntityReference#getName()
     */
    public String getName() {
        return fName;
    }
    
    public void writeAsEncodedUnicode(Writer writer) throws XMLStreamException {
        try {
            writer.write('&');
            writer.write(fName);
            writer.write(';');
        }
        catch (IOException ioe) {
            throw new XMLStreamException(ioe);
        }
    }
}
