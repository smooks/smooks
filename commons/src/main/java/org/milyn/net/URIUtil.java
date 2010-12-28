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
package org.milyn.net;

import org.milyn.assertion.AssertArgument;

import java.net.URI;
import java.net.URISyntaxException;
import java.io.File;

/**
 * {@link java.net.URI} utility methods.
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public abstract class URIUtil {

    /**
     * Get the parent URI of the supplied URI
     * @param uri The input URI for which the parent URI is being requrested.
     * @return The parent URI.  Returns a URI instance equivalent to "../" if
     * the supplied URI path has no parent.
     * @throws URISyntaxException Failed to reconstruct the parent URI.
     */
    public static URI getParent(URI uri) throws URISyntaxException {
        AssertArgument.isNotNull(uri, "uri");

        String parentPath = new File(uri.getPath()).getParent();

        if(parentPath == null) {
            return new URI("../");
        }

        return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), parentPath.replace('\\', '/'), uri.getQuery(), uri.getFragment());
    }
}
