/*-
 * ========================LICENSE_START=================================
 * Smooks Core
 * %%
 * Copyright (C) 2020 Smooks
 * %%
 * Licensed under the terms of the Apache License Version 2.0, or
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
package org.smooks.engine.resource.config;

import org.smooks.api.resource.config.Parameter;
import org.smooks.api.resource.config.ParameterDecodeException;
import org.smooks.api.resource.config.ResourceConfig;
import org.smooks.api.delivery.ContentDeliveryConfig;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.StringTokenizer;
import java.util.Vector;


/**
 * {@link ParameterDecoder} used to tokenize a parameter values into a {@link java.util.List}
 * or {@link java.util.HashSet}.
 * <p/>
 * Tokenizes parameter values into a {@link java.util.List} (param-type="string-list")
 * or {@link java.util.HashSet} (param-type="string-hashset") using {@link java.util.StringTokenizer}.
 * <p/>
 * Two default configurations of this decoder are pre-installed for all profiles.  They're named
 * "string-list" and "string-hashset".
 *
 * <h3 id="exampleusage">Example Usage</h3>
 * The following example illustrates use of the pre-installed "string-hashset" decoder:
 * <p/>
 * <b>Configuration</b>:
 * <pre>
 * &lt;resource-config target-profile="html4" selector="XXX"&gt;
 *      &lt;resource&gt;com.acme.XXXContentDeliveryUnit&lt;/resource&gt;
 *      &lt;param name="blockLevelElements" type="<b>string-hashset</b>"&gt;
 *          p,h1,h2,h3,h4,h5,h6,div,ul,ol,dl,menu,dir,pre,hr,blockquote,address,center,noframes,isindex,fieldset,table
 *      &lt;/param&gt;
 * &lt;/resource-config&gt;</pre>
 * <p/>
 * <b>Usage</b>:<br/>
 * ... and "com.acme.XXXContentDeliveryUnit" accesses this parameter value as follows:
 * <pre>
 * {@link Parameter} param = {@link ResourceConfig resourceConfig}.{@link ResourceConfig#getParameter(String, Class) getParameter("blockLevelElements")};
 * {@link java.util.HashSet} blockLevelElements = (HashSet)param.{@link Parameter#getValue(ContentDeliveryConfig) getValue(ContentDeliveryConfig)};
 * </pre>
 * <p/>
 * Note, we will make this filter easier in the next release.  You'll be able to call a method such
 * as "getDecodedParameter" on the {@link ResourceConfig}, returning a decoded parameter Object.
 *
 * See {@link ResourceConfig}.
 * @author tfennelly
 */
public class TokenizedStringParameterDecoder extends ParameterDecoder<String> {
	private Class   returnType;
	private String  delims;
	private boolean returnDelims;
	private boolean trimTokens;

	/**
	 * Public constructor.
	 * @param resourceConfig Configuration.
	 */
	public void setConfiguration(ResourceConfig resourceConfig) {
		delims = resourceConfig.getParameterValue("delims", String.class, ",");
		returnDelims = resourceConfig.getParameterValue("returnDelims", Boolean.class, false);
		trimTokens = resourceConfig.getParameterValue("trimTokens", Boolean.class, true);

		String paramType = resourceConfig.getParameterValue(Parameter.PARAM_TYPE_PREFIX, String.class, "string-list");
		if(paramType.equals("string-list")) {
			returnType = Vector.class;
		} else if(paramType.equals("string-hashset")) {
			returnType = LinkedHashSet.class;
		} else {
			throw new ParameterDecodeException("Unsupported decoded return type [" + paramType + "]");
		}
	}

	/**
	 * Decodes the value based on the smooks-resource configuration passed in the constructor.
	 */
	@SuppressWarnings("unchecked")
	public Object decodeValue(String value) throws ParameterDecodeException {
		Collection returnVal;
		StringTokenizer tokenizer;

		// Create the desired Collection.
		try {
			returnVal = (Collection)returnType.newInstance();
		} catch (Exception e) {
			throw new IllegalStateException("Unable to construct Collection instance.", e);
		}

		// Create the tokenizer.
		tokenizer = new StringTokenizer(value, delims, returnDelims);
		while(tokenizer.hasMoreTokens()) {
			if(trimTokens) {
				returnVal.add(tokenizer.nextToken().trim());
			} else {
				returnVal.add(tokenizer.nextToken());
			}
		}

		return returnVal;
	}

}
