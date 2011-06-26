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
package org.milyn.edisax.registry;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.milyn.edisax.EDIConfigurationException;
import org.milyn.edisax.model.EdifactModel;
import org.milyn.edisax.model.internal.Delimiters;
import org.milyn.edisax.util.EDIUtils;
import org.xml.sax.SAXException;


/**
 * Base implementation of {@link MappingsRegistry} interface
 * 
 * @author zubairov
 *
 */
public abstract class AbstractMappingsRegistry implements MappingsRegistry {

	/**
	 * Internal storage 
	 */
	protected final Map<String, EdifactModel> content = new LinkedHashMap<String, EdifactModel>();

	/**
	 * {@inheritDoc}
	 */
	public EdifactModel getMappingModel(String messageName,
			Delimiters delimiters) throws EDIConfigurationException, SAXException, IOException {
		String[] nameComponents = EDIUtils.split(messageName,
				delimiters.getComponent(), delimiters.getEscape());
		StringBuilder lookupNameBuilder = new StringBuilder();
		// First 4 components are mandatory...we use those as the lookup...
		for (int i = 0; i < 4; i++) {
			if (i > 0) {
				lookupNameBuilder.append(':');
			}
			lookupNameBuilder.append(nameComponents[i]);
		}
		String lookupName = lookupNameBuilder.toString().trim();
		EdifactModel result = content.get(lookupName);
		if (result == null) {
            synchronized (content) {
                result = content.get(lookupName);
		        if (result == null) {
			        content.putAll(demandLoading(nameComponents));
                }
            }
			// Try again
			result = content.get(lookupName);
			if (result != null) {
				return result;
			}
		} else {
			return result;
		}
		throw new EDIConfigurationException("Mapping Model '" + messageName
				+ "' not found in supplied set of Mapping model.");
	}

	/**
	 * Loading mapping models on demand.
	 * This method should return either one or many mapping models
	 * loaded on-demand or just eagerly.
	 * 
	 * @param nameComponents
	 * @return
	 * @throws SAXException 
	 * @throws IOException 
	 * @throws EDIConfigurationException 
	 */
	protected abstract Map<String, EdifactModel> demandLoading(String[] nameComponents) throws EDIConfigurationException, IOException, SAXException;

	
}
