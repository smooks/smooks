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
package org.milyn.namespace;

import java.util.Properties;

/**
 * Namespace resolver.
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class SimpleNamespaceResolver implements NamespaceResolver {

    protected Properties namespacesByPrefix = new Properties();
    protected Properties namespacesByUri = new Properties();

    public SimpleNamespaceResolver addNamespace(String uri, String prefix) {
        String inUseUriPrefix = namespacesByUri.getProperty(uri);
        String inUsePrefixUri = namespacesByPrefix.getProperty(prefix);

        if(inUsePrefixUri != null) {
            if(inUsePrefixUri.equals(uri)) {
                // Already defined...
                return this;
            }
            throw new IllegalArgumentException("Namespace prefix '" + prefix + "' already defined for namespace uri '" + inUsePrefixUri + "'.  Cannot redefined for uri '" + uri + "'.");
        } else if(inUseUriPrefix != null) {
            if(inUseUriPrefix.equals(prefix)) {
                // Already defined...
                return this;
            }
            throw new IllegalArgumentException("Namespace uri '" + uri + "' already defined for namespace prefix '" + inUseUriPrefix + "'.  Cannot redefine for prefix '" + prefix + "'.");
        }

        namespacesByPrefix.setProperty(prefix, uri);
        namespacesByUri.setProperty(uri, prefix);
        return this;
    }

    public String getPrefix(String uri) {
        return namespacesByUri.getProperty(uri);
    }

    public String getUri(String prefix) {
        return namespacesByPrefix.getProperty(prefix);
    }

    public NamespaceResolver clone() {
        SimpleNamespaceResolver clone = new SimpleNamespaceResolver();
        clone.namespacesByPrefix.putAll(namespacesByPrefix);
        clone.namespacesByUri.putAll(namespacesByUri);
        return clone;
    }
}
