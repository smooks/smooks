/*-
 * ========================LICENSE_START=================================
 * Core
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
package org.smooks.testkit.resource;

import org.smooks.support.StreamUtils;
import org.smooks.api.resource.ContainerResourceLocator;

import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Hashtable;


@SuppressWarnings("WeakerAccess")
public class MockContainerResourceLocator implements ContainerResourceLocator {

	public static final File TEST_STANDALONE_CTX_BASE = new File("src/test/standalone-ctx");
	private final Hashtable streams = new Hashtable();

	@SuppressWarnings("unchecked")
	public void setResource(String nameOrURI, InputStream stream) {
		try {
			byte[] streamData = StreamUtils.readStream(stream);
			streams.put(nameOrURI, streamData);
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage());
		}
	}

	public InputStream getResource(String configName, String defaultURI) throws IllegalArgumentException, IOException {
		return getResource(defaultURI);
	}

	public InputStream getResource(String uri) throws IllegalArgumentException, IOException {
		String relUri = uri;

		if (uri.charAt(0) == '\\' || uri.charAt(0) == '/') {
			relUri = uri.substring(1);
		}
		// Try loading the resource from the standalone test context
		File resFile = new File(TEST_STANDALONE_CTX_BASE, relUri);
		if (resFile.exists() && !resFile.isDirectory()) {
			return Files.newInputStream(Paths.get(resFile.toURI()));
		}

		// Check has it been set in this mock instance.
		byte[] resBytes = (byte[]) streams.get(uri);
		if (resBytes == null) {
			throw new IllegalStateException("Resource [" + uri + "] not set in MockContainerResourceLocator OR loadable from the test standalone context.  Use MockContainerResourceLocator.setResource()");
		}

		return new ByteArrayInputStream(resBytes);
	}

	public URI getBaseURI() {
		return URI.create("./");
	}
}
