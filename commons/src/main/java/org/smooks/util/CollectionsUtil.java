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

import java.util.*;

/**
 * Collections Utilities.
 * 
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class CollectionsUtil {

    /**
     * Private constructor.
     */
    private CollectionsUtil() {
    }

    /**
     * Create an Object {@link Set} from the supplied objects.
     * @param objects The objects to be added to the set.
     * @return The {@link Set}.
     */
    public static <T> Set<T> toSet(T... objects) {
        Set<T> theSet = new HashSet<T>();
        addToCollection(theSet, objects);
        return theSet;
    }

    /**
     * Create an Object {@link List} from the supplied objects.
     * @param objects The objects to be added to the list.
     * @return The {@link List}.
     */
    public static <T> List<T> toList(T... objects) {
        List<T> theList = new ArrayList<T>();
        addToCollection(theList, objects);
        return theList;
    }

    /**
     * Create an Object {@link List} from the supplied Enumeration of objects.
     * @param objects The objects to be added to the list.
     * @return The {@link List}.
     */
    public static <T> List<T> toList(Enumeration<T> objects) {
        List<T> theList = new ArrayList<T>();
        while(objects.hasMoreElements()) {
        	theList.add(objects.nextElement());
        }        
        return theList;
    }

    private static <T> void addToCollection(Collection<T> theCollection, T... objects) {
        for(T object : objects) {
            theCollection.add(object);
        }
    }
}
