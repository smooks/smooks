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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * {@link ParameterDecoder} used to convert a parameter String value to a
 * list of {@link Properties}.
 * <P/>
 * This decoder is pre-installed.
 * 
 * <h3 id="exampleusage">Example Usage</h3>
 * <pre>
 * 	&lt;param name="magic-properties-param" type="<b>properties</b>"&gt;
 * x=11111
 * y=22222
 * z=33333
 * &lt;/param&gt;
 * <pre>
 * <p/>
 * The code for accessing this parameter value:
 * <pre>
 * {@link Parameter} param = {@link ResourceConfig resourceConfig}.{@link ResourceConfig#getParameter(String) getParameter("magic-properties-param")};
 * {@link java.util.Properties} properties = (Properties)param.{@link Parameter#getValue(ContentDeliveryConfig) getValue}();
 * </pre>
 * <p/>
 * Note, we will make this filter easier in the next release.  You'll be able to call a method such
 * as "getDecodedParameter" on the {@link ResourceConfig}, returning a decoded parameter Object.
 * 
 * @see ResourceConfig
 * @author tfennelly
 */
public class PropertyListParameterDecoder extends ParameterDecoder<String> {

	public Object decodeValue(String value) throws ParameterDecodeException {
		Properties properties = new Properties();
		
		try {
			properties.load(new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8)));
		} catch (UnsupportedEncodingException e) {
			throw new ParameterDecodeException("Unexpected error.  'UTF-8' is not a supported character encoding.", e);
		} catch (IOException e) {
			throw new ParameterDecodeException("Unexpected error.  Unable to read ByteArrayInputStream based stream.", e);
		}
		
		return properties;
	}

}
