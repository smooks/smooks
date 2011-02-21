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

package org.milyn.javabean.binding.model.get;

import org.milyn.javabean.binding.BeanSerializationException;

/**
 * Bean ModelSet node getter.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public interface Getter<T> {

    /**
     * Get a value from the supplied context object.
     * @param contextObject The context object from which the get operation is to be applied.
     * @return The value returned from the get invocation.
     * @throws BeanSerializationException Exception applying get operation on the context object instance.
     */
    Object get(final T contextObject) throws BeanSerializationException;
}
