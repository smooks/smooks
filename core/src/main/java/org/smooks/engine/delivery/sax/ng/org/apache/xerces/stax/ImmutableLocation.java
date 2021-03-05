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

package org.smooks.engine.delivery.sax.ng.org.apache.xerces.stax;

import javax.xml.stream.Location;

/**
 * <p>An immutable StAX <code>Location</code>.</p>
 * 
 * @xerces.internal
 * 
 * @author Michael Glavassevich, IBM
 *
 * @version $Id$
 */
public class ImmutableLocation implements Location {
    
    private final int fCharacterOffset;
    private final int fColumnNumber;
    private final int fLineNumber;
    private final String fPublicId;
    private final String fSystemId;
    
    public ImmutableLocation(Location location) {
        this(location.getCharacterOffset(), location.getColumnNumber(), 
                location.getLineNumber(), location.getPublicId(), 
                location.getSystemId());
    }
    
    public ImmutableLocation(int characterOffset, int columnNumber, int lineNumber, String publicId, String systemId) {
        fCharacterOffset = characterOffset;
        fColumnNumber = columnNumber;
        fLineNumber = lineNumber;
        fPublicId = publicId;
        fSystemId = systemId;
    }

    public int getCharacterOffset() {
        return fCharacterOffset;
    }

    public int getColumnNumber() {
        return fColumnNumber;
    }

    public int getLineNumber() {
        return fLineNumber;
    }

    public String getPublicId() {
        return fPublicId;
    }

    public String getSystemId() {
        return fSystemId;
    }
}
