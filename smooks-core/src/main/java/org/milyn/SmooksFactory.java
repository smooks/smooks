/*
 * Milyn - Copyright (C) 2006 - 2010
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License (version 2.1) as published
 * by the Free Software Foundation.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.
 * 
 * See the GNU Lesser General Public License for more details:
 * http://www.gnu.org/licenses/lgpl.txt
 */
package org.milyn;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;

/**
 * A factory that creates {@link Smooks} instances.
 * 
 * @author Daniel Bevenius
 *
 */
public interface SmooksFactory {
    
    /**
     * Creates a Smooks instance that is unconfigured
     * 
     * @return Smooks a new Smooks instance that is unconfigured.
     */
    Smooks createInstance();
    
    /**
     * Creates a Smooks instance that is unconfigured
     * 
     * @param config the Smooks configuration to add to the created Smooks instance.
     * @return Smooks a new Smooks instance that is configured with the passed in config.
     */
    Smooks createInstance(InputStream config) throws IOException, SAXException;
    
    /**
     * Creates a Smooks instance that is unconfigured
     * 
     * @param config the Smooks configuration to add to the created Smooks instance.
     * @return Smooks a new Smooks instance that is configured with the passed in config.
     */
    Smooks createInstance(String config) throws IOException, SAXException;

}
