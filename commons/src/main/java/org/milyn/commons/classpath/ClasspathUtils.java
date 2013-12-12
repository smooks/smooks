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

package org.milyn.commons.classpath;

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
