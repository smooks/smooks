/*
 * Milyn - Copyright (C) 2006 - 2010
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License (version 2.1) as published by the Free Software
 *  Foundation.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 *  See the GNU Lesser General Public License for more details:
 *  http://www.gnu.org/licenses/lgpl.txt
 */

package org.milyn.javabean;

/**
 * Data encoder.
 * <p/>
 * This is an extension interface for adding encode capability to a
 * {@link org.milyn.javabean.DataDecoder} implementation.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 * @see org.milyn.javabean.DataDecoder
 */
public interface DataEncoder extends DataDecoder {

    /**
     * Encode an object to a string.
     * @param object The object to be encoded.
     * @return The encoded object.
     * @throws DataDecodeException Error encoding object.
     */
    public String encode(Object object) throws DataDecodeException;
}
