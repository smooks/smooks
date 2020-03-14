/*
 * Milyn - Copyright (C) 2006 - 2011
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License (version 2.1) as published by the Free Software
 * Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU Lesser General Public License for more details:
 * http://www.gnu.org/licenses/lgpl.txt
 */
package org.smooks.edisax.registry;

import java.io.IOException;

import org.smooks.edisax.EDIConfigurationException;
import org.smooks.edisax.model.EdifactModel;
import org.smooks.edisax.model.internal.Delimiters;
import org.xml.sax.SAXException;

/**
 * Registry that stores EDIFACT Mapping models and load them from the 
 * classpath on-demand
 * 
 * @author zubairov
 *
 */
public interface MappingsRegistry {

	/**
	 * Returns an {@link EdifactModel} based on the message name and delimiters that should be used to parse message name.
	 * 
	 * @param messageName
	 * @param delimiters
	 * @return
	 * @throws SAXException 
	 */
	EdifactModel getMappingModel(String messageName, Delimiters delimiters) throws EDIConfigurationException, SAXException, IOException;
	
}
