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
package org.smooks.javabean.decoders;

import org.smooks.javabean.DataDecoder;
import org.smooks.javabean.DataDecodeException;
import org.smooks.javabean.DecodeType;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * {@link URI} Decoder.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
@DecodeType(URI.class)
public class URIDecoder implements DataDecoder {

    public Object decode(String data) throws DataDecodeException {
        try {
            return new URI(data.trim());
        } catch (URISyntaxException e) {
            throw new DataDecodeException("Failed to decode URI value '" + data + "'.", e);
        }
    }
}
