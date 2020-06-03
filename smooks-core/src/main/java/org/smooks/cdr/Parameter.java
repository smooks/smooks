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

import java.util.List;

import org.smooks.delivery.ContentDeliveryConfig;
import org.w3c.dom.Element;

/**
 * SmooksResourceConfiguration Parameter.
 * <p/>
 * Wrapper for a param.  Handles decoding.
 * @author tfennelly
 */
public class Parameter {
	public static final String PARAM_TYPE_PREFIX = "param-type:";
	private final String name;
	String value;
	private String type;
	private Object objValue;

    private Element xml;

    /**
	 * Public constructor.
	 * @param name Parameter name.
	 * @param value Parameter value.
	 */
	public Parameter(String name, String value) {
		if(name == null || (name = name.trim()).equals("")) {
			throw new IllegalArgumentException("null or empty 'name' arg in constructor call.");
		}
		if(value == null) {
			throw new IllegalArgumentException("null 'value' arg in constructor call.");
		}
		this.name = name;
		this.value = value;
	}

    /**
	 * Public constructor.
	 * @param name Parameter name.
	 * @param value Parameter value.
	 */
	public Parameter(String name, Object value) {
		if(name == null || (name = name.trim()).equals("")) {
			throw new IllegalArgumentException("null or empty 'name' arg in constructor call.");
		}
		if(value == null) {
			throw new IllegalArgumentException("null 'value' arg in constructor call.");
		}
		this.name = name;
        this.value = value.toString();
		this.objValue = value;
	}

	/**
	 * Public constructor.
	 * @param name Parameter name.
	 * @param value Parameter value.
	 * @param type Parameter type.  This argument identifies the
	 * {@link ParameterDecoder} to use for decoding the param value.
	 */
	public Parameter(String name, String value, String type) {
		this(name, value);

		// null type attribute is OK - means no decoder is used on the value
		this.type = type;
	}

	/**
	 * Get the parameter name.
	 * @return The parameter name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get the parameter type.
	 * @return The parameter type.
	 */
	public String getType() {
		return type;
	}

	/**
	 * Get the parameter value "undecoded".
	 * @return Parameter value.
	 */
	public String getValue() {
		return value;
	}

	/**
	 * Get the parameter value "decoded" into an Object.
	 * <p/>
	 * Uses the supplied <code>deliveryConfig</code> to get the {@link ParameterDecoder}
	 * implementation to be used to decode the parameter value.  Looks up the
	 * {@link ParameterDecoder} using the parameter type - selector="decoder-<i>&lt;type&gt;</i>".
	 * @param deliveryConfig Requesting device {@link ContentDeliveryConfig}.
	 * @return Decoded value.
	 * @throws ParameterDecodeException Unable to decode parameter value.
	 */
	public Object getValue(ContentDeliveryConfig deliveryConfig) throws ParameterDecodeException {
		if(objValue == null) {
			synchronized (value) {
				if(objValue == null) {
					if(type == null) {
						objValue = value;
					} else {
						List decoders = deliveryConfig.getObjects(PARAM_TYPE_PREFIX + type);
						if(!decoders.isEmpty()) {
							try {
								ParameterDecoder paramDecoder = (ParameterDecoder)decoders.get(0);
								objValue = paramDecoder.decodeValue(value);
							} catch(ClassCastException cast) {
								throw new ParameterDecodeException("Configured ParameterDecoder '" + PARAM_TYPE_PREFIX + type + "' for device must be of type " + ParameterDecoder.class);
							}
						} else {
							throw new ParameterDecodeException("ParameterDecoder '" + PARAM_TYPE_PREFIX + type + "' not defined for requesting device.");
						}
					}
				}
			}
		}

		return objValue;
	}

    /**
     * Get the object value associated with this parameter.
     * @return The object value, or null if not set.
     */
    public Object getObjValue() {
        return objValue;
    }

    /* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return value;
	}

    /**
     * Set the DOM element node associated with the parameter definition.
     * <p/>
     * Only relevant for XML based configs.
     *
     * @param xml Parameter configuration xml.
     */
    public Parameter setXML(Element xml) {
        this.xml = xml;
        return this;
    }

    /**
     * Get the DOM element node associated with the parameter definition.
     * <p/>
     * Only relevant for XML based configs.
     *
     * @return Parameter configuration xml.
     */
    public Element getXml() {
        return xml;
    }
}
