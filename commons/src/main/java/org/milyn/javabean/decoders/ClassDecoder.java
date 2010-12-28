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
package org.milyn.javabean.decoders;

import org.milyn.javabean.DataDecodeException;
import org.milyn.javabean.DecodeType;
import org.milyn.util.ClassUtil;

/**
 * Class decoder.
 * <p/>
 * Decodes the supplied data as a Class name to return a Class instance.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
@DecodeType({Class.class})
public class ClassDecoder implements org.milyn.javabean.DataDecoder {

    public Object decode(String data) throws DataDecodeException {
        try {
            return ClassUtil.forName(data.trim(), ClassDecoder.class);
        } catch (ClassNotFoundException e) {
            throw new DataDecodeException("Failed to decode '" + data + "' as a Java Class.", e);
        }
    }
}