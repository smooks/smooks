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

import org.milyn.delivery.ContentDeliveryConfig;

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
 * {@link org.milyn.cdr.Parameter} param = {@link org.milyn.cdr.SmooksResourceConfiguration resourceConfig}.{@link org.milyn.cdr.SmooksResourceConfiguration#getParameter(String) getParameter("blockLevelElements")};
 * {@link java.util.HashSet} blockLevelElements = (HashSet)param.{@link org.milyn.cdr.Parameter#getValue(ContentDeliveryConfig) getValue(ContentDeliveryConfig)};
 * </pre>
 * <p/>
 * Note, we will make this filter easier in the next release.  You'll be able to call a method such
 * as "getDecodedParameter" on the {@link SmooksResourceConfiguration}, returning a decoded parameter Object.
 *
 * See {@link org.milyn.cdr.SmooksResourceConfiguration}.
 * @author tfennelly
 */
public class TokenizedStringParameterDecoder extends ParameterDecoder {
	private Class   returnType;
	private String  delims;
	private boolean returnDelims;
	private boolean trimTokens;

	/**
	 * Public constructor.
	 * @param resourceConfig Configuration.
	 */
	public void setConfiguration(SmooksResourceConfiguration resourceConfig) {
		delims = resourceConfig.getStringParameter("delims", ",");
		returnDelims = resourceConfig.getBoolParameter("returnDelims", false);
		trimTokens = resourceConfig.getBoolParameter("trimTokens", true);

		String paramType = resourceConfig.getStringParameter(Parameter.PARAM_TYPE_PREFIX, "string-list");
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
