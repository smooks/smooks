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
import java.net.URI;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.milyn.edisax.EDIConfigurationException;
import org.milyn.edisax.model.EdifactModel;
import org.milyn.edisax.util.EDIUtils;
import org.xml.sax.SAXException;

/**
 * Default implementation of {@link MappingsRegistry}.
 * 
 * Default implementation loading EDIFACT models based on the specified list of model files
 * which could be provided either via constructor or via {@link #addModelReferences(String, URI)} method.
 * 
 * @author zubairov
 */
public class DefaultMappingsRegistry extends AbstractMappingsRegistry {

	private final Map<String, URI> modelReferences = new HashMap<String, URI>();
	
	/**
	 * Constructor mostly used for tests
	 * 
	 * @param models
	 */
	public DefaultMappingsRegistry(EdifactModel... models) {
		for (EdifactModel model : models) {
			content.put(EDIUtils.toLookupName(model.getDescription()), model);
		}
	}

	/**
	 * Loading mapping model out of ZIP file
	 * 
	 * @param string
	 * @param baseURI
	 * @throws SAXException 
	 * @throws IOException 
	 * @throws EDIConfigurationException 
	 */
	public DefaultMappingsRegistry(String mappingModelFiles, URI baseURI) throws EDIConfigurationException, IOException, SAXException {
		addModelReferences(mappingModelFiles, baseURI);
	}

	/**
	 * Add references to the lookup list. 
	 * 
	 * @param references
	 */
	public void addModelReferences(String models, URI baseURI) {
		String[] mappingModelFileTokens = models.split(",");
		for (String modelRef : mappingModelFileTokens) {
			modelReferences.put(modelRef, baseURI);
		}
	}
	
	/**
	 * This method load all mapping models which are declared in
	 * {@link #modelReferences} map and returns them all back.
	 * It is actually ignoring nameComponents parameter
	 * because no on-demand loading is happening here. 
	 * 
	 * @param nameComponents
	 * @return
	 * @throws SAXException 
	 * @throws IOException 
	 * @throws EDIConfigurationException 
	 */
	protected synchronized Map<String, EdifactModel> demandLoading(String[] nameComponents) throws EDIConfigurationException, IOException, SAXException {
		Map<String, EdifactModel> result = new LinkedHashMap<String, EdifactModel>();
		Set<Entry<String, URI>> set = modelReferences.entrySet();
		for (Entry<String, URI> entry : set) {
			EDIUtils.loadMappingModels(entry.getKey(), result, entry.getValue());
		}
		return result;
	}


}
