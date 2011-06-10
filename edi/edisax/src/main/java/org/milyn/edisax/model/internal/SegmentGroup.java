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

import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * Segment Group.
 * 
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class SegmentGroup extends MappingNode {

    private List<SegmentGroup> segments;
    private Integer minOccurs;
    private Integer maxOccurs;

    public List<SegmentGroup> getSegments() {
        if (segments == null) {
            segments = new ArrayList<SegmentGroup>();
        }
        return this.segments;
    }

    public String getSegcode() {
        return segments.get(0).getSegcode();
    }

    public Pattern getSegcodePattern() {
        return segments.get(0).getSegcodePattern();
    }

    public int getMinOccurs() {
        if (minOccurs == null) {
            return  1;
        } else {
            return minOccurs;
        }
    }

    public void setMinOccurs(Integer value) {
        this.minOccurs = value;
    }

    public int getMaxOccurs() {
        if (maxOccurs == null) {
            return  1;
        } else {
            return maxOccurs;
        }
    }

    public void setMaxOccurs(Integer value) {
        this.maxOccurs = value;
    }
}
