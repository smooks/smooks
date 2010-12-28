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
package org.milyn.payload;

import org.milyn.container.ExecutionContext;

import javax.xml.transform.Source;

/**
 * Filtration/Transformation {@link javax.xml.transform.Source}.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public abstract class FilterSource implements Source {

    public static final String CONTEXT_KEY = FilterSource.class.getName() + "#CONTEXT_KEY";

    private String systemId;

    public static Source getSource(ExecutionContext executionContext) {
        return (Source) executionContext.getAttribute(CONTEXT_KEY);
    }

    public static void setSource(ExecutionContext executionContext, Source source) {
        if(source != null) {
            executionContext.setAttribute(CONTEXT_KEY, source);
        } else {
            executionContext.removeAttribute(CONTEXT_KEY);
        }
    }

    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    public String getSystemId() {
        return systemId;
    }
}