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
package org.smooks.api.resource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

/**
 * Interface for locating stream resources from the container..
 * 
 * @author tfennelly
 */
public interface ContainerResourceLocator extends ExternalResourceLocator {

	/**
	 * Get the resource specified by the container 'config' value. <p/> If the
	 * config value isn't specified, uses the defaultLocation.
	 * 
	 * @param configName
	 *            The container configuration entry name whose value specifies
	 *            the location of the resource.
	 * @param defaultUri
	 *            The default location for the resource.
	 * @return The InputStream associated with resource.
	 * @throws IllegalArgumentException
	 *             Illegal argument. Check the cause exception for more
	 *             information.
	 * @throws IOException
	 *             Unable to get the resource stream.
	 */
	InputStream getResource(String configName, String defaultUri) throws IllegalArgumentException, IOException;
	
    /**
     * Get the base URI for the locator instance.
     * @return The base URI for the locator instance.
     */
	URI getBaseURI();
}
