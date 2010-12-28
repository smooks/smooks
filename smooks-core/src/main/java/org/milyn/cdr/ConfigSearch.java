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
package org.milyn.cdr;

import java.util.Properties;
import java.util.Set;
import java.util.Map.Entry;

/**
 * Configuration lookup metadata.
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class ConfigSearch {

	private String configNS;
	private String selector;
	private String selectorNS;
	private String resource;
	private Properties params = new Properties();
	
	public String getConfigNS() {
		return configNS;
	}

	public ConfigSearch configNS(String configNS) {
		this.configNS = configNS;
		return this;
	}

	public String getSelector() {
		return selector;
	}

	public ConfigSearch selector(String selector) {
		this.selector = selector;
		return this;
	}

	public String getSelectorNS() {
		return selectorNS;
	}

	public ConfigSearch selectorNS(String selectorNS) {
		this.selectorNS = selectorNS;
		return this;
	}

	public String getResource() {
		return resource;
	}

	public ConfigSearch resource(String resource) {
		this.resource = resource;
		return this;
	}

	public ConfigSearch param(String name, String value) {
		params.setProperty(name, value);
		return this;
	}
	
	public boolean matches(SmooksResourceConfiguration config) {
		if(configNS != null) {
			if(config.getExtendedConfigNS() == null || !config.getExtendedConfigNS().startsWith(configNS)) {
				return false;
			}
		}
		if(selector != null) {
			if(config.getSelector() == null || !config.getSelector().equalsIgnoreCase(selector)) {
				return false;
			}
		}
		if(selectorNS != null) {
			if(config.getSelectorNamespaceURI() == null || !config.getSelectorNamespaceURI().equals(selectorNS)) {
				return false;
			}
		}
		if(resource != null) {
			if(config.getResource() == null || !config.getResource().equals(resource)) {
				return false;
			}
		}
		
		if(!params.isEmpty()) {
			Set<Entry<Object, Object>> entries = params.entrySet();
			for(Entry<Object, Object> entry : entries) {
				String expectedValue = (String) entry.getValue();
				String actualValue = config.getStringParameter((String) entry.getKey());
				
				if(!expectedValue.equals(actualValue)) {
					return false;
				}
			}
		}
		
		return true;
	}
}
