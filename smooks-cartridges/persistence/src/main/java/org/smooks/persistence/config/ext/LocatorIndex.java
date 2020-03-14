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
package org.smooks.persistence.config.ext;

import org.smooks.container.ApplicationContext;

/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public class LocatorIndex {

	private static final String CONTEXT = LocatorIndex.class.getName() + "#Context";

	private int lookupperCount = 0;

	public int increment() {
		return lookupperCount++;
	}

	public int currentIndex() {
		return lookupperCount;
	}

	public static LocatorIndex getLocatorIndex(ApplicationContext applicationContext) {

		LocatorIndex counter = (LocatorIndex) applicationContext.getAttribute(CONTEXT);

		if(counter == null) {

			counter = new LocatorIndex();

			applicationContext.setAttribute(CONTEXT, counter);

		}

		return counter;

	}

}
