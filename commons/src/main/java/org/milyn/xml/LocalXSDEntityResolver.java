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
package org.milyn.xml;

import org.milyn.assertion.AssertArgument;

import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.Source;
import java.io.File;

/**
 * XSD resolver for local XSD's.
 *
 * @author tfennelly
 */
public class LocalXSDEntityResolver extends LocalEntityResolver {

	/**
	 * XSD package for locating XSDs in the classpath.
	 */
	private static final String XSD_CP_PACKAGE = "/org/milyn/xsd/";
    /**
     * Schema sources for this entity resolver.
     */
    private Source[] schemaSources;

    /**
	 * Public Constructor.
     * @param schemaSources Schema sources.
	 */
	public LocalXSDEntityResolver(Source[] schemaSources) {
        super(XSD_CP_PACKAGE);
        AssertArgument.isNotNull(schemaSources, "schemaSources");
        if(schemaSources.length == 0) {
            throw new IllegalArgumentException("Empty list of schemas supplied in arg 'schemaSources'.");
        }
        this.schemaSources = schemaSources;
        setDocType(schemaSources[0].getSystemId());
    }

    /**
     * Get the schema sources associated with this resolver instance.
     * @return The schema sources.
     */
    public Source[] getSchemaSources() {
        return schemaSources;
    }
}
