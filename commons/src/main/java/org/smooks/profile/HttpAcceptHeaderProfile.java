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

package org.smooks.profile;

/**
 * HTTP Accept header profile. <p/> This profile implementation exposes the
 * requesting useragent's "Accept" header media types as part of the profile
 * set. This class represents a single media type. See RFC2068 section 14.1.
 * <p/> The profile name is of the form
 * 
 * <pre>
 *  	&quot;accept&quot; &quot;:&quot; &quot;media-range&quot;
 * </pre>
 * 
 * <p/> This class also provides access to the Accept header media parameters
 * via the getParam methods ({@link #getParam(String)} and
 * {@link #getParamNumeric(String, double)}).
 * 
 * @author tfennelly
 */
public class HttpAcceptHeaderProfile extends BasicProfile {

	private static final long serialVersionUID = 1L;

	/**
	 * Accept Parameters.
	 */
	private String[] params;

	/**
	 * Public constructor.
	 * 
	 * @param media
	 *            Accept media (media-range).
	 * @param params
	 *            Accept parameters.
	 */
	public HttpAcceptHeaderProfile(String media, String[] params) {
		super("accept:" + media);

		// Just trim all the params - save doing it later ;-)
		for (int i = 0; i < params.length; i++) {
			params[i] = params[i].trim();
		}
		this.params = params;
	}

	/**
	 * Get the named parameter as a string.
	 * 
	 * @param name
	 *            Parameter name.
	 * @return Parameter value, or null if not defined.
	 */
	public String getParam(String name) {
		if (name == null) {
			throw new IllegalArgumentException(
					"null 'name' arg in method call.");
		}

		for (int i = 0; i < params.length; i++) {
			if (params[i].startsWith(name + "=")) {
				return params[i].substring((name + "=").length()).trim();
			}
		}

		return null;
	}

	/**
	 * Get the named parameter as a numeric value. <p/> Use this method for
	 * extracting
	 * 
	 * @param name
	 *            Parameter name.
	 * @return Parameter value as a float, or <code>defaultVal</code> if not
	 *         defined.
	 * @throws NumberFormatException
	 *             The requested parameter value is a not a numeric value.
	 */
	public double getParamNumeric(String name, double defaultVal)
			throws NumberFormatException {
		String paramVal = getParam(name);

		if (paramVal != null) {
			return Double.parseDouble(paramVal);
		}

		return defaultVal;
	}
}