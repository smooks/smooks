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

package org.milyn.edisax.unedifact;

import org.milyn.namespace.SimpleNamespaceResolver;

/**
 * UN/EDIFACT Namespace resolver.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class UNEdifactNamespaceResolver extends SimpleNamespaceResolver {

    public static final String NAMESPACE_ROOT = "urn:org.milyn.edi.unedifact";

    public UNEdifactNamespaceResolver(String transmissionNamespace) {
        addNamespace(transmissionNamespace, "env");
    }

    @Override
    public String getPrefix(String uri) {
        String prefix = super.getPrefix(uri);
        if (prefix == null) {
            prefix = "ns" + namespacesByPrefix.size();
            addNamespace(uri, prefix);
        }
        return prefix;
    }
}
