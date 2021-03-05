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

import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;

/**
 * @xerces.internal
 * 
 * @author Lucian Holland
 * 
 * @version $Id$
 */
public class AttributeImpl extends org.smooks.engine.delivery.sax.ng.org.apache.xerces.stax.events.XMLEventImpl implements Attribute {

    private final boolean fIsSpecified;
    private final QName fName;
    private final String fValue;
    private final String fDtdType;

    /**
     * Constructor.
     */
    public AttributeImpl(final QName name, final String value, final String dtdType, final boolean isSpecified, final Location location) {
        this(ATTRIBUTE, name, value, dtdType, isSpecified, location);
    }

    protected AttributeImpl(final int type, final QName name, final String value, final String dtdType, final boolean isSpecified, final Location location) {
        super(type, location);
        fName = name;
        fValue = value;
        fDtdType = dtdType;
        fIsSpecified = isSpecified;
    }
    
    /**
     * @see Attribute#getName()
     */
    public final QName getName() {
        return fName;
    }

    /**
     * @see Attribute#getValue()
     */
    public final String getValue() {
        return fValue;
    }

    /**
     * @see Attribute#getDTDType()
     */
    public final String getDTDType() {
        return fDtdType;
    }

    /**
     * @see Attribute#isSpecified()
     */
    public final boolean isSpecified() {
        return fIsSpecified;
    }
    
    public final void writeAsEncodedUnicode(Writer writer) throws XMLStreamException {
        try {
            // Write name
            String prefix = fName.getPrefix();
            if (prefix != null && prefix.length() > 0) {
                writer.write(prefix);
                writer.write(':');
            }
            writer.write(fName.getLocalPart());
            // Write value
            writer.write("=\"");
            writer.write(fValue);
            writer.write('"');
        }
        catch (IOException ioe) {
            throw new XMLStreamException(ioe);
        }
    }
}
