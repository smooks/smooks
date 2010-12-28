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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Segment extends SegmentGroup implements ContainerNode {

    private List<Field> fields;
    private String segcode;
    private Pattern segcodePattern;
    private Boolean truncatable;
    private Boolean ignoreUnmappedFields;
    private String description;
    private String importXmlTag;

    public List<Field> getFields() {
        if (fields == null) {
            fields = new ArrayList<Field>();
        }
        return this.fields;
    }
    
    public Segment addField(Field field) {
    	getFields().add(field);
    	return this;
    }

    public String getSegcode() {
        return segcode;
    }

    public void setSegcode(String value) {
        this.segcode = value;
        segcodePattern = Pattern.compile("^" + segcode, Pattern.DOTALL);
    }

    public Pattern getSegcodePattern() {
        return segcodePattern;
    }

    @Override
    public String getJavaName() {
        if(getNodeTypeRef() != null) {
            if(importXmlTag != null) {
                return importXmlTag;
            } else {
                return getNodeTypeRef();
            }
        } else {
            return super.getJavaName();
        }
    }

    public boolean isTruncatable() {
        return truncatable != null && truncatable;
    }

    public void setTruncatable(Boolean value) {
        this.truncatable = value;
    }
    
    public void setIgnoreUnmappedFields(Boolean value) {
    	this.ignoreUnmappedFields = value;
    }
    
    public boolean isIgnoreUnmappedFields() {
    	return ignoreUnmappedFields != null && ignoreUnmappedFields;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImportXmlTag() {
        return importXmlTag;
    }

    public void setImportXmlTag(String importXmlTag) {
        this.importXmlTag = importXmlTag;
    }
}
