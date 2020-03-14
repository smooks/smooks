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
package org.smooks.xml;

import java.util.Properties;

/**
 * XML Namespace URI static definitions.
 * @author tfennelly
 */
public interface Namespace {

    public static final Properties SMOOKS_PREFIX_MAPPINGS = new SmooksNamespaceMappings();

    public static final String SMOOKS_URI = SmooksNamespaceMappings.SMOOKS_URI;

    static class SmooksNamespaceMappings extends Properties {
        private static final String SMOOKS_URI = "http://milyn.codehaus.org/smooks".intern();
        private SmooksNamespaceMappings() {
            setProperty("param-type", SMOOKS_URI + "/param-type");
            setProperty("decoder", SMOOKS_URI + "/decoder");
        }
    }
}
