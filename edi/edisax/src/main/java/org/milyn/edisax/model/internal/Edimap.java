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

package org.milyn.edisax.model.internal;

import org.milyn.edisax.util.EdimapWriter;

import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class Edimap {

	private URI src;
    private List<Import> imports;
    private Description description;
    private Delimiters delimiters;
    private SegmentGroup segments;
    private Boolean ignoreUnmappedSegments;

    public Edimap() {    	
    }
    
    public Edimap(URI src) {
    	this.src = src;
    }
    
    public URI getSrc() {
    	return src;
    }
    
    public List<Import> getImports() {
        if (imports == null) {
            imports = new ArrayList<Import>();
        }
        return this.imports;
    }

    public Description getDescription() {
        return description;
    }

    public void setDescription(Description value) {
        this.description = value;
    }

    public Delimiters getDelimiters() {
        return delimiters;
    }

    public void setDelimiters(Delimiters value) {
        this.delimiters = value;
    }

    public void setIgnoreUnmappedSegments(Boolean value) {
	this.ignoreUnmappedSegments = value;
    }

    public boolean isIgnoreUnmappedSegments() {
	return ignoreUnmappedSegments != null && ignoreUnmappedSegments;
    }

public SegmentGroup getSegments() {
        return segments;
    }

    public void setSegments(SegmentGroup value) {
        this.segments = value;
    }

    public void write(Writer writer) throws IOException {
        EdimapWriter.write(this, writer);
    }
}
