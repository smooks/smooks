/*
 * Milyn - Copyright (C) 2006 - 2010
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

package org.milyn.namespace;

/**
 * Namespace resolver.
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public interface NamespaceResolver {

    /**
     * Add a namespace prefix.
     * @param uri The namespace URI.
     * @param prefix The namespace prefix.
     * @return This resolver instance.
     */
    NamespaceResolver addNamespace(String uri, String prefix);

    /**
     * Resolve a namespace prefix from the namespace URI.
     * @param uri The namespace URI.
     * @return The namespace prefix, or null if the URI is unknown.
     */
    String getPrefix(String uri);

    /**
     * Resolve a namespace URI from the namespace prefix.
     * @param prefix The namespace prefix.
     * @return The namespace prefix, or null if the prefix is unknown.
     */
    String getUri(String prefix);

    /**
     * Clone the resolver.
     * @return The cloned resolver.
     */
    NamespaceResolver clone();
}
