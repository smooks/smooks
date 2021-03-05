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

package org.smooks.engine.delivery.sax.ng.org.apache.xerces.impl.xs.util;

import org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XMLLocator;

/**
 * An XMLLocator implementation used for schema error reporting.
 *
 * @xerces.internal 
 * 
 * @author Sandy Gao, IBM
 * @version $Id$
 */
public final class SimpleLocator implements XMLLocator {

    private String lsid;
    private String esid;
    private int line;
    private int column;
    private int charOffset;
    
    public SimpleLocator() {
    }
    
    public SimpleLocator(String lsid, String esid, int line, int column) {
        this(lsid, esid, line, column, -1);
    }
    
    public void setValues(String lsid, String esid, int line, int column) {
        setValues(lsid, esid, line, column, -1);
    }
    
    public SimpleLocator(String lsid, String esid, int line, int column, int offset) {
        this.line = line;
        this.column = column;
        this.lsid = lsid;
        this.esid = esid;
        charOffset = offset;
    }
    
    public void setValues(String lsid, String esid, int line, int column, int offset) {
        this.line = line;
        this.column = column;
        this.lsid = lsid;
        this.esid = esid;
        charOffset = offset;
    }
    
    public int getLineNumber() {
        return line;
    }
    
    public int getColumnNumber() {
        return column;
    }
    
    public int getCharacterOffset() {
        return charOffset;
    }
    
    public String getPublicId() {
        return null;
    }
    
    public String getExpandedSystemId() {
        return esid;
    }
    
    public String getLiteralSystemId() {
        return lsid;
    }
    
    public String getBaseSystemId() {
        return null;
    }

    public void setColumnNumber(int col) {
        this.column = col;
    }
    
    public void setLineNumber(int line) {
        this.line = line;
    }
    
    public void setCharacterOffset(int offset) {
        charOffset = offset;
    }
    
    /**
     * @see org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XMLResourceIdentifier#setBaseSystemId(String)
     */
    public void setBaseSystemId(String systemId) {}
    
    /**
     * @see org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XMLResourceIdentifier#setExpandedSystemId(String)
     */
    public void setExpandedSystemId(String systemId) {
        esid = systemId;
    }
    
    /**
     * @see org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XMLResourceIdentifier#setLiteralSystemId(String)
     */
    public void setLiteralSystemId(String systemId) {
        lsid = systemId;
    }
    
    /**
     * @see org.smooks.engine.delivery.sax.ng.org.apache.xerces.xni.XMLResourceIdentifier#setPublicId(String)
     */
    public void setPublicId(String publicId) {}
    
    /** 
     * Returns the encoding of the current entity.  
     * Since these locators are used in the construction of
     * XMLParseExceptions, which know nothing about encodings, there is
     * no point in having this object deal intelligently 
     * with encoding information.
     */
    public String getEncoding() {
        return null;
    }
    
    public String getXMLVersion() {
        return null;
    }
}
