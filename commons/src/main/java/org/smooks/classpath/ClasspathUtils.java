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
package org.smooks.classpath;

/**
 * Classpath manipulation utility methods.
 * @author tfennelly
 */
public abstract class ClasspathUtils {

    /**
     * Convert the Java-class-file-name to the equivalent Java-class-name (dot 
     * delimited package name).
     * <p/>
     * EG:<br/>
     * a/b/c/X.class converts to a.b.c.X<br/>
     * a/b/c/X converts to a.b.c.X<br/>
     * a.b.c.X converts to a.b.c.X<br/>
     * a.b.c.X.class converts to a.b.c.X<br/>
     * @param fileName The file name String to be translated.
     * @return Java Class runtime name representation of the supplied file name String.
     */
    public static String toClassName(String fileName) {
    	StringBuffer className;
    	
    	if(fileName == null) {
    		throw new IllegalArgumentException("null 'fileName' arg in method call.");
    	}
    	fileName = fileName.trim();
    	if(fileName.equals("")) {
    		throw new IllegalArgumentException("empty 'fileName' arg in method call.");
    	}
    	
    	className = new StringBuffer(fileName);
    	// Fixup the name - replace '/' with '.' and remove ".class" if
    	// present.
    	if(fileName.endsWith(".class") && fileName.length() > 6) {
    		className.setLength(className.length() - 6);
    	}
    	for(int i = 0; i < className.length(); i++) {
    		if(className.charAt(i) == '/') {
    			className.setCharAt(i, '.');
    		}
    	}
    	
    	return className.toString();
    }

    /**
     * Convert the Java-class-name (dot delimited package name)to the 
     * equivalent Java-class-file-name .
     * <p/>
     * EG:<br/>
     * a.b.c.X converts to a/b/c/X.class<br/>
     * a.b.c.X.class converts to a/b/c/X.class<br/>
     * a/b/c/X.class converts to a/b/c/X.class<br/>
     * a/b/c/X converts to a/b/c/X.class<br/>
     * @param className The class name string to be translated.
     * @return The file name representaion of the supplied runtime class String.
     */
    public static String toFileName(String className) {
    	StringBuffer fileName;
    	
    	if(className == null) {
    		throw new IllegalArgumentException("null 'className' arg in method call.");
    	}
    	className = className.trim();
    	if(className.equals("")) {
    		throw new IllegalArgumentException("empty 'className' arg in method call.");
    	}
    	
    	fileName = new StringBuffer(className);
    	// Fixup the name - replace '.' with '/' and append ".class" ( possibly 
    	// after already removing it - to avoid it from being converted 
    	// to "/class").
    	if(className.endsWith(".class") && className.length() > 6) {
    		fileName.setLength(className.length() - 6);
    	}
    	for(int i = 0; i < fileName.length(); i++) {
    		if(fileName.charAt(i) == '.') {
    			fileName.setCharAt(i, '/');
    		}
    	}
    	fileName.append(".class");
    	
    	return fileName.toString();
    }

}
