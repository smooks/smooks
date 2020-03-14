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
