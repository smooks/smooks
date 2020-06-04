/*-
 * ========================LICENSE_START=================================
 * Smooks Commons
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
package org.smooks.resource;

import java.io.InputStream;
import java.net.URI;

import org.smooks.util.ClassUtil;

/**
 * Classpath Resource Locator. <p/> Loads resources from the classpath. The URI
 * must have no scheme or authority components, and have a leading slash
 * character ('/') on the path i.e. it must be relative to the root of the
 * classpath.
 * 
 * @author tfennelly
 */
public class ClasspathResourceLocator implements ContainerResourceLocator {

	public InputStream getResource(String configName, String defaultUri)
			throws IllegalArgumentException {
		return getResource(defaultUri);
	}

	public InputStream getResource(String uri) throws IllegalArgumentException {
		if (uri == null) {
			throw new IllegalArgumentException("null 'uri' arg in method call.");
		} else if (uri.charAt(0) != '/') {
			throw new IllegalArgumentException(
					"classpath 'uri' must be a valid classpath with a leading '/' char on the path i.e. specified relative to the root of the classpath.");
		}

		return ClassUtil.getResourceAsStream(uri, getClass());
	}

	public URI getBaseURI() {
		return null;
	}
}
