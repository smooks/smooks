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
package org.smooks.javabean.decoders;

import org.smooks.SmooksException;
import org.smooks.config.Configurable;
import org.smooks.expression.MVELExpressionEvaluator;
import org.smooks.javabean.DataDecoder;
import org.smooks.javabean.DataDecodeException;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Data Preprocesses Decoder.
 * <p/>
 * Wraps the underlying decoder, allowing you to preprocess the data before
 * passing to the base decoder.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class PreprocessDecoder implements DataDecoder, Configurable {
	
	public static final String VALUE_PRE_PROCESSING = "valuePreprocess";
	public static final String BASE_DECODER = "baseDecoder";
	
	private Properties config;
	private MVELExpressionEvaluator expression;
	private DataDecoder baseDecoder;

	public void setConfiguration(Properties config) throws SmooksException {
		this.config = config;
		
		expression = new MVELExpressionEvaluator(config.getProperty(VALUE_PRE_PROCESSING));
		expression.setToType(String.class);
		
		String baseDecoderName = config.getProperty(BASE_DECODER);
		if(baseDecoderName != null) {
			baseDecoder = DataDecoder.Factory.create(baseDecoderName);
			if(baseDecoder instanceof Configurable) {
				((Configurable)baseDecoder).setConfiguration(config);
			}
		}
	}

    public Properties getConfiguration() {
        return config;
    }

    public void setBaseDecoder(DataDecoder baseDecoder) {
		this.baseDecoder = baseDecoder;
		if(baseDecoder instanceof Configurable) {
			((Configurable)baseDecoder).setConfiguration(config);
		}
	}

	public DataDecoder getBaseDecoder() {
		return baseDecoder;
	}

	public Object decode(String data) throws DataDecodeException {
		if(data != null) {
			Map<String, String> contextObj = new HashMap<String, String>();
			
			// Make it available under the strings "data" or "value"...
			contextObj.put("data", data);
			contextObj.put("value", data);
			
			return baseDecoder.decode((String)expression.exec(contextObj));
		}
		
		return null;
    }
}
