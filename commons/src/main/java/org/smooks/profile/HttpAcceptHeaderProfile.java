/*-
 * ========================LICENSE_START=================================
 * Smooks Commons
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
