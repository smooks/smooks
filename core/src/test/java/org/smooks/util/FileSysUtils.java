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
package org.smooks.util;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

/**
 * File System utility methods.
 * @author tfennelly
 */
public abstract class FileSysUtils {

	/**
	 * Get the project root folder.
	 * @return Smooks project root directory.
	 */
	public static File getProjectRootDir() {
		File localDir = new File("./");
		
		try {
			if(!localDir.getCanonicalPath().endsWith("ant")) {
//				fail("Working directory should be '<project.root>\\ant' folder.  Set Working Directory if running test from inside IDE.");
			}
			return localDir.getCanonicalFile().getParentFile();
		} catch (IOException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}
		
		return null;
	}


	/**
	 * Get a "project" file.
	 * <p/>
	 * @param path File path relative to the project root. 
	 * @return Smooks project file.
	 */
	public static File getProjectFile(String path) {
		return new File(getProjectRootDir(), path);
	}
}
