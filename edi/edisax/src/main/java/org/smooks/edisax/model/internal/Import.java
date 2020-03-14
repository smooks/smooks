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

package org.smooks.edisax.model.internal;

import java.net.URI;
import java.net.URISyntaxException;

public class Import {

    private URI resourceURI;
    private String namespace;
    private Boolean truncatableSegments;
    private Boolean truncatableFields;
    private Boolean truncatableComponents;

    public String getResource() {
        return resourceURI.toString();
    }

    public void setResource(String value) {
        try {
			this.resourceURI = new URI(value);
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException("Invalid EDI import URI '" + value + "'.", e);
		}
    }

	public void setResourceURI(URI resourceURI) {
		this.resourceURI = resourceURI;
	}

	public URI getResourceURI() {
		return resourceURI;
	}

	public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String value) {
        this.namespace = value;
    }

    public Boolean isTruncatableFields() {
        return truncatableFields;
    }

    public void setTruncatableFields(Boolean value) {
        this.truncatableFields = value;
    }

    public Boolean isTruncatableComponents() {
        return truncatableComponents;
    }

    public void setTruncatableComponents(Boolean value) {
        this.truncatableComponents = value;
    }

    public Boolean isTruncatableSegments() {
        return truncatableSegments;
    }

    public void setTruncatableSegments(Boolean truncatableSegments) {
        this.truncatableSegments = truncatableSegments;
    }
}
