/*-
 * ========================LICENSE_START=================================
 * Smooks Core
 * %%
 * Copyright (C) 2020 Smooks
 * %%
 * Licensed under the terms of the Apache License Version 2.0 or,
 * the GNU Lesser General Public License version 3.0 or later.
 * 
 * SPDX-License-Identifier: Apache-2.0 OR LGPL-3.0-or-later
 * 
 * ======================================================================
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * ======================================================================
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * =========================LICENSE_END==================================
 */
package org.smooks.cdr;

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
