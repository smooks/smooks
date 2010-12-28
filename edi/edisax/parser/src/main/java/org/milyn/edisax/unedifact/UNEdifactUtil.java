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
package org.milyn.edisax.unedifact;

import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.milyn.edisax.util.EDIUtils;
import org.milyn.edisax.model.EdifactModel;
import org.milyn.edisax.model.internal.Delimiters;
import org.milyn.edisax.model.internal.Description;
import org.xml.sax.SAXException;

/**
 * UN/EDIFACT utility methods.
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public abstract class UNEdifactUtil {

	public static EdifactModel getMappingModel(String messageName, Delimiters delimiters, Map<Description, EdifactModel> mappingModels) throws SAXException {
		Set<Entry<Description, EdifactModel>> modelSet = mappingModels.entrySet();
		String[] nameComponents = EDIUtils.split(messageName, delimiters.getComponent(), delimiters.getEscape());
		StringBuilder lookupNameBuilder = new StringBuilder();
		
		// First 4 components are mandatory...we use those as the lookup...
		for(int i = 0; i < 4; i++) {
			if(i > 0) {
				lookupNameBuilder.append(':');
			}
			lookupNameBuilder.append(nameComponents[i]);
		}
		String lookupName = lookupNameBuilder.toString().trim();
		
		for(Entry<Description, EdifactModel> mappingModel : modelSet) {
			Description description = mappingModel.getKey();
			String compoundName = description.getName() + ":" + description.getVersion();
			
			if(compoundName.equals(lookupName)) {
				return mappingModel.getValue();
			}
		}
		
		throw new SAXException("Mapping Model '" + messageName + "' not found in supplied set of Mapping model.");
	}
}
