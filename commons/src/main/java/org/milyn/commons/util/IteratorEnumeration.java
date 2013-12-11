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

package org.milyn.commons.util;

import java.util.Enumeration;
import java.util.Iterator;

/**
 * {@link java.util.Iterator} to {@link java.util.Enumeration} Adapter.
 * 
 * @author tfennelly
 */
public class IteratorEnumeration implements Enumeration {
	private Iterator iterator;

	public IteratorEnumeration(Iterator iterator) {
		this.iterator = iterator;
	}

	public boolean hasMoreElements() {
		return iterator.hasNext();
	}

	public Object nextElement() {
		return iterator.next();
	}
}