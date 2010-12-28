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
package org.milyn.smooks.edi;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.milyn.cdr.annotation.AppContext;
import org.milyn.cdr.annotation.ConfigParam;
import org.milyn.container.ApplicationContext;
import org.milyn.delivery.ContentHandler;
import org.milyn.delivery.annotation.Initialize;
import org.milyn.edisax.EDIConfigurationException;
import org.milyn.edisax.util.EDIUtils;
import org.milyn.edisax.model.EdifactModel;
import org.milyn.edisax.model.internal.Description;
import org.xml.sax.SAXException;

/**
 * Class for loading EDI Mapping Models into the {@link ApplicationContext}
 * during initialization.
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class ModelLoader implements ContentHandler {

	@ConfigParam
	private String mappingModel;

    @AppContext
    private ApplicationContext applicationContext;
	
    @Initialize
    public void initialize() throws EDIConfigurationException, IOException, SAXException {    	
    	Map<Description, EdifactModel> mappingModels = getMappingModels(mappingModel, applicationContext, true);

    	if(mappingModels.isEmpty()) {
    		EDIUtils.loadMappingModels(mappingModel, mappingModels, applicationContext.getResourceLocator().getBaseURI());
    	}
    }

	public static Map<Description, EdifactModel> getMappingModels(String mappingModelKey, ApplicationContext applicationContext) throws EDIConfigurationException {
		return getMappingModels(mappingModelKey, applicationContext, false);
	}
    
	private static Map<Description, EdifactModel> getMappingModels(String mappingModelKey, ApplicationContext applicationContext, boolean create) throws EDIConfigurationException {
		Map<String, Map<Description, EdifactModel>> modelRegistry = (Map<String, Map<Description, EdifactModel>>) applicationContext.getAttribute(ModelLoader.class);
		
		if(modelRegistry == null) {
			modelRegistry = new HashMap<String, Map<Description, EdifactModel>>();
			applicationContext.setAttribute(ModelLoader.class, modelRegistry);
		}
		
		Map<Description, EdifactModel> mappingModelMap = modelRegistry.get(mappingModelKey);
		if(mappingModelMap == null) {
			if(!create) {
				throw new EDIConfigurationException("Unable to get EDI Mapping Model Map for '" + mappingModelKey + "'.  Models not bound to ApplicationContext.  ModelLoader should have loaded these models during initialization phase.");
			}
			mappingModelMap = new HashMap<Description, EdifactModel>();
			modelRegistry.put(mappingModelKey, mappingModelMap);
		}
		
		return mappingModelMap;
	}
}
