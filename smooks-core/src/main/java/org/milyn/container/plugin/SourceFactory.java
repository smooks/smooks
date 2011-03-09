/*
 * Milyn - Copyright (C) 2006 - 2010
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License (version 2.1) as published
 * by the Free Software Foundation.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.
 * 
 * See the GNU Lesser General Public License for more details:
 * http://www.gnu.org/licenses/lgpl.txt
 */

package org.milyn.container.plugin;

import org.milyn.payload.JavaSource;
import org.w3c.dom.Node;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;

/**
 * Factory for creating javax.xml.transform.Source objects.
 *
 * @author <a href="mailto:daniel.bevenius@gmail.com">Daniel Bevenius</a>
 */
public class SourceFactory {
    private static SourceFactory factory = new SourceFactory();

    private SourceFactory() {
    }

    public static SourceFactory getInstance() {
        return factory;
    }

    public Source createSource(final Object from) {
        Source source;
        if (from instanceof String) {
            source = new StreamSource(new StringReader((String) from));
        } else if (from instanceof InputStream) {
            source = new StreamSource((InputStream) from);
        } else if (from instanceof Reader) {
            source = new StreamSource((Reader) from);
        } else if (from instanceof Node) {
            source = new DOMSource((Node) from);
        } else if (from instanceof Source) {
            source = (Source) from;
        } else if (from instanceof byte[]) {
            source = new StreamSource(new ByteArrayInputStream((byte[]) from));
        } else {
            source = new JavaSource(from);
        }
        return source;
    }

}
