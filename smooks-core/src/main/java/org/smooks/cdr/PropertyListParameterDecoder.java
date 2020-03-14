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

package org.smooks.cdr;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import org.smooks.delivery.ContentDeliveryConfig;

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
 * {@link org.smooks.cdr.Parameter} param = {@link org.smooks.cdr.SmooksResourceConfiguration resourceConfig}.{@link org.smooks.cdr.SmooksResourceConfiguration#getParameter(String) getParameter("magic-properties-param")};
 * {@link java.util.Properties} properties = (Properties)param.{@link org.smooks.cdr.Parameter#getValue(ContentDeliveryConfig) getValue}();
 * </pre>
 * <p/>
 * Note, we will make this filter easier in the next release.  You'll be able to call a method such
 * as "getDecodedParameter" on the {@link SmooksResourceConfiguration}, returning a decoded parameter Object.
 * 
 * @see org.smooks.cdr.SmooksResourceConfiguration
 * @author tfennelly
 */
public class PropertyListParameterDecoder extends ParameterDecoder {

	public Object decodeValue(String value) throws ParameterDecodeException {
		Properties properties = new Properties();
		
		try {
			properties.load(new ByteArrayInputStream(value.getBytes("UTF-8")));
		} catch (UnsupportedEncodingException e) {
			throw new ParameterDecodeException("Unexpected error.  'UTF-8' is not a supported character encoding.", e);
		} catch (IOException e) {
			throw new ParameterDecodeException("Unexpected error.  Unable to read ByteArrayInputStream based stream.", e);
		}
		
		return properties;
	}

}
