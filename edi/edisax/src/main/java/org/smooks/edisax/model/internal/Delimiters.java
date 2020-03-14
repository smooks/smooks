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

import java.util.LinkedHashSet;
import java.util.Set;

public class Delimiters {

    private String segment;
    private String field;
    private String fieldRepeat;
    private String component;
    private String subComponent;
    private String escape;
    private String decimalSeparator;
    private volatile char[] segmentDelimiter;
    private boolean ignoreCRLF;
    private Set<Character> delimiterChars = new LinkedHashSet<Character>();

    public String getSegment() {
        return segment;
    }

    public Delimiters setSegment(String value) {
        this.segment = value;
		initSegmentDelimiter();
        initDelimiterChars();
        return this;
    }

    public String getField() {
        return field;
    }

    public Delimiters setField(String value) {
        this.field = value;
        initDelimiterChars();
        return this;
    }

    public String getFieldRepeat() {
		return fieldRepeat;
	}

    public Delimiters setFieldRepeat(String fieldRepeat) {
		this.fieldRepeat = fieldRepeat;
        initDelimiterChars();
		return this;
	}

    public String getComponent() {
        return component;
    }

    public Delimiters setComponent(String value) {
        this.component = value;
        initDelimiterChars();
        return this;
    }

    public String getSubComponent() {
        return subComponent;
    }

    public Delimiters setSubComponent(String value) {
        this.subComponent = value;
        initDelimiterChars();
        return this;
    }

    public String getEscape() {
        return escape;
    }

    public Delimiters setEscape(String escape) {
        this.escape = escape;
        initDelimiterChars();
        return this;
    }

    public Delimiters setDecimalSeparator(String decimalSeparator) {
	    this.decimalSeparator = decimalSeparator;
	    initDelimiterChars();
	    return this;
    }

    public String getDecimalSeparator() {
	    return decimalSeparator;
    }

    public char[] getSegmentDelimiter() {
		if(segmentDelimiter == null) {
			initSegmentDelimiter();
		}
		return segmentDelimiter;
	}

    public boolean ignoreCRLF() {
		if(segmentDelimiter == null) {
			initSegmentDelimiter();
		}
		return ignoreCRLF;
	}

    private synchronized void initSegmentDelimiter() {
		if(segmentDelimiter != null) {
			return;
		}

        this.ignoreCRLF = segment.endsWith("!$");

        if (ignoreCRLF) {
            segmentDelimiter = segment.replace("!$", "").toCharArray();
        } else {
            segmentDelimiter = segment.toCharArray();
        }
	}

    public boolean removeableNodeToken(String string, DelimiterType delimiterType) {
        if(string.length() == 0) {
            return true;
        }

        int stringLen = string.length();

        for(int i = 0; i < stringLen; i++) {
            char c = string.charAt(i);

            switch(delimiterType) {
                case SEGMENT:
                    if(equals(segment, c)) {
                        continue;
                    }
                case FIELD:
                    if(equals(field, c)) {
                        continue;
                    }
                case COMPONENT:
                    if(equals(component, c)) {
                        continue;
                    }
                case SUB_COMPONENT:
                    if(equals(subComponent, c)) {
                        continue;
                    }
                case DECIMAL_SEPARATOR:
                    if(equals(decimalSeparator, c)) {
                        continue;
                    }
                default :
                    return false;
            }
        }

        return true;
    }

    public String escape(String string) {
        if(string == null) {
            return null;
        }
        if(string.length() == 0) {
            return string;
        }
        if(delimiterChars.isEmpty()) {
            return string;
        }

        StringBuilder escapeBuffer = new StringBuilder();
        int stringLen = string.length();

        for(int i = 0; i < stringLen; i++) {
            char c = string.charAt(i);

            if(delimiterChars.contains(c)) {
                escapeBuffer.append(escape);
            }
            escapeBuffer.append(c);
        }

        return escapeBuffer.toString();
    }

    private void initDelimiterChars() {
        delimiterChars.clear();

        if(segmentDelimiter != null && (segmentDelimiter.length == 0 || segmentDelimiter.length > 1)) {
            return;
        } else if(field != null && (field.length() == 0 || field.length() > 1)) {
            return;
        } else if(fieldRepeat != null && (fieldRepeat.length() == 0 || fieldRepeat.length() > 1)) {
            return;
        } else if(component != null && (component.length() == 0 || component.length() > 1)) {
            return;
        } else if(subComponent != null && (subComponent.length() == 0 || subComponent.length() > 1)) {
            return;
        } else if(escape == null || (escape.length() == 0 || escape.length() > 1)) {
            return;
        }

        if(segmentDelimiter != null) {
            delimiterChars.add(segmentDelimiter[0]);
        }
        if(field != null) {
            delimiterChars.add(field.charAt(0));
        }
        if(fieldRepeat != null) {
            delimiterChars.add(fieldRepeat.charAt(0));
        }
        if(component != null) {
            delimiterChars.add(component.charAt(0));
        }
        if(subComponent != null) {
            delimiterChars.add(subComponent.charAt(0));
        }
        if(escape != null) {
            delimiterChars.add(escape.charAt(0));
        }
    }

    private boolean equals(String delimiter, char c) {
        return delimiter != null && delimiter.length() == 1 && delimiter.charAt(0) == c;
    }

    @Override
    public Object clone() {
        Delimiters delimiters = new Delimiters();
        delimiters.segment = segment;
        delimiters.field = field;
        delimiters.fieldRepeat = fieldRepeat;
        delimiters.component = component;
        delimiters.subComponent = subComponent;
        delimiters.escape = escape;
        delimiters.decimalSeparator = decimalSeparator;
        delimiters.segmentDelimiter = segmentDelimiter;
        delimiters.ignoreCRLF = ignoreCRLF;
        delimiters.delimiterChars.addAll(delimiterChars);
        return delimiters;
    }
}
