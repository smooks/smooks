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
package org.milyn.javabean.decoders;

import org.milyn.SmooksException;
import org.milyn.config.Configurable;
import org.milyn.expression.MVELExpressionEvaluator;
import org.milyn.javabean.DataDecoder;
import org.milyn.javabean.DataDecodeException;

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
