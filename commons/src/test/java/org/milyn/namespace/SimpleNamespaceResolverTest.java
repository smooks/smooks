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

import junit.framework.TestCase;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class SimpleNamespaceResolverTest extends TestCase {

    public void test_OK() {
        SimpleNamespaceResolver resolver = new SimpleNamespaceResolver();

        resolver.addNamespace("http://a", "a");
        resolver.addNamespace("http://b", "b");
        resolver.addNamespace("http://c", "c");

        assertEquals("a", resolver.getPrefix("http://a"));
        assertEquals("b", resolver.getPrefix("http://b"));
        assertEquals("c", resolver.getPrefix("http://c"));

        assertEquals("http://a", resolver.getUri("a"));
        assertEquals("http://b", resolver.getUri("b"));
        assertEquals("http://c", resolver.getUri("c"));
    }

    public void test_Duplicate_prefix() {
        SimpleNamespaceResolver resolver = new SimpleNamespaceResolver();

        resolver.addNamespace("http://a", "a");
        resolver.addNamespace("http://a", "a"); // Should just ignore this
        try {
            resolver.addNamespace("http://b", "a");
            fail("Expected IllegalArgumentException");
        } catch(IllegalArgumentException e) {
            assertEquals("Namespace prefix 'a' already defined for namespace uri 'http://a'.  Cannot redefined for uri 'http://b'.", e.getMessage());
        }
    }

    public void test_Duplicate_uri() {
        SimpleNamespaceResolver resolver = new SimpleNamespaceResolver();

        resolver.addNamespace("http://a", "a");
        resolver.addNamespace("http://a", "a"); // Should just ignore this
        try {
            resolver.addNamespace("http://a", "b");
            fail("Expected IllegalArgumentException");
        } catch(IllegalArgumentException e) {
            assertEquals("Namespace uri 'http://a' already defined for namespace prefix 'a'.  Cannot redefine for prefix 'b'.", e.getMessage());
        }
    }
}
