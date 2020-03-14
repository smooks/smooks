/*
 * Milyn - Copyright (C) 2006 - 2011
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License (version 2.1) as published by the Free Software
 * Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU Lesser General Public License for more details:
 * http://www.gnu.org/licenses/lgpl.txt
 */

package org.smooks.javabean.binding.model.get;

import org.smooks.assertion.AssertArgument;

import java.util.Map;

/**
 * {@link Map} getter.
 * <p/>
 * Allows {@link Map Maps} to be used as a node in an object graph.  Allows
 * support for Virtual Object Models etc.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class MapGetter<T extends Map> implements Getter<T> {

    private String property;

    public MapGetter(String property) {
        AssertArgument.isNotNullAndNotEmpty(property, "property");
        this.property = property;
    }

    public Object get(final T contextObject) {
        return contextObject.get(property);
    }
}
