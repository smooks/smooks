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
package org.milyn.javabean;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public class TestFactory {

	public static TestFactory newInstance() {
		return new TestFactory();
	}


	public static TestFactory getNull() {
		return null;
	}

	public List<?> newLinkedList() {
		return new LinkedList<Object>();
	}

	public static ArrayList<?> newArrayList() {
		return new ArrayList<Object>();
	}

	public static HashSet<?> newHashSet() {
		return new HashSet<Object>();
	}

}
