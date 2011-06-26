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
 * Namespace declaration stack aware interface.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public interface NamespaceDeclarationStackAware {

    /**
     * Set the Namespace Declaration Stack on the implementing class.
     *
     * @param namespaceDeclarationStack Namespace Declaration Stack.
     */
    void setNamespaceDeclarationStack(NamespaceDeclarationStack namespaceDeclarationStack);
}
