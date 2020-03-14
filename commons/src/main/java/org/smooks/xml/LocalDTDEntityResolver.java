/*
 Milyn - Copyright (C) 2006 - 2010

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
package org.smooks.xml;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;

/**
 * DTD resolver for local DTD's.
 *
 * @author tfennelly
 */
public class LocalDTDEntityResolver extends LocalEntityResolver {

    /**
     * DTD package for locating DTDs in the classpath.
     */
    private static final String DTD_CP_PACKAGE = "/org/smooks/dtd/";

    /**
     * Public default Constructor
     */
    public LocalDTDEntityResolver() {
        super(DTD_CP_PACKAGE);
    }

    /**
     * Public default Constructor <p/> This constructor allows specification of
     * a local file system folder from which DTDs can be loaded.
     *
     * @param localDTDFolder Local DTD folder.
     */
    public LocalDTDEntityResolver(File localDTDFolder) {
        super(localDTDFolder);
    }


    /*
     * (non-Javadoc)
     *
     * @see org.xml.sax.EntityResolver#resolveEntity(java.lang.String,
     *      java.lang.String)
     */
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        setDocType(systemId);
        return super.resolveEntity(publicId, systemId);
    }
}
