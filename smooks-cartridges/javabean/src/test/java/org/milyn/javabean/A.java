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

import java.util.List;

/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public class A {

	private List<B> bList;

	private B b;

	private B[] bArray;

	/**
	 * @return the bList
	 */
	public List<B> getBList() {
		return bList;
	}

	/**
	 * @param list the bList to set
	 */
	public void setBList(List<B> list) {
		bList = list;
	}

	/**
	 * @return the bList
	 */
	public B[]  getBArray() {
		return bArray;
	}

	/**
	 * @param list the bList to set
	 */
	public void setBArray(B[] array) {
		bArray = array;
	}

	/**
	 * @return the b
	 */
	public B getB() {
		return b;
	}

	/**
	 * @param b the b to set
	 */
	public void setB(B b) {
		this.b = b;
	}

}
