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

package org.smooks.engine.delivery.sax.ng.org.apache.xerces.util;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.parser.XMLInputSource;

/**
 * <p>An <code>XMLInputSource</code> analogue to <code>javax.xml.transform.stax.StAXSource</code>.</p>
 * 
 * @version $Id$
 */
public final class StAXInputSource extends XMLInputSource {
    
    private final XMLStreamReader fStreamReader;
    private final XMLEventReader fEventReader;
    private final boolean fConsumeRemainingContent;
    
    public StAXInputSource(XMLStreamReader source) {
        this(source, false);
    }
    
    public StAXInputSource(XMLStreamReader source, boolean consumeRemainingContent) {
        super(null, getStreamReaderSystemId(source), null);
        if (source == null) {
            throw new IllegalArgumentException("XMLStreamReader parameter cannot be null.");
        }
        fStreamReader = source;
        fEventReader = null;
        fConsumeRemainingContent = consumeRemainingContent;
    }
    
    public StAXInputSource(XMLEventReader source) {
        this(source, false);
    }
    
    public StAXInputSource(XMLEventReader source, boolean consumeRemainingContent) {
        super(null, getEventReaderSystemId(source), null);
        if (source == null) {
            throw new IllegalArgumentException("XMLEventReader parameter cannot be null.");
        }
        fStreamReader = null;
        fEventReader = source;
        fConsumeRemainingContent = consumeRemainingContent;
    }
    
    public XMLStreamReader getXMLStreamReader() {
        return fStreamReader;
    }
    
    public XMLEventReader getXMLEventReader() {
        return fEventReader;
    }
    
    public boolean shouldConsumeRemainingContent() {
        return fConsumeRemainingContent;
    }
    
    public void setSystemId(String systemId){
        throw new UnsupportedOperationException("Cannot set the system ID on a StAXInputSource");
    }
    
    private static String getStreamReaderSystemId(XMLStreamReader reader) {
        if (reader != null) {
            return reader.getLocation().getSystemId();
        }
        return null;
    }
    
    private static String getEventReaderSystemId(XMLEventReader reader) {
        try {
            if (reader != null) {
                return reader.peek().getLocation().getSystemId();
            }
        }
        catch (XMLStreamException e) {}
        return null;
    }
    
} // StAXInputSource
