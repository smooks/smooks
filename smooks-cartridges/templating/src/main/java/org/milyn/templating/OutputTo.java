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
package org.milyn.templating;

import org.milyn.assertion.AssertArgument;

/**
 * OutputTo template usage.
 * <p/>
 * Output the templating result to the specified outputStreamResource.
 *
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class OutputTo implements Usage {

    private String outputStreamResource;

    public OutputTo(String outputStreamResource) {
        AssertArgument.isNotNullAndNotEmpty(outputStreamResource, "outputStreamResource");
        this.outputStreamResource = outputStreamResource;
    }

    protected String getOutputStreamResource() {
        return outputStreamResource;
    }

    public static Usage stream(String outputStreamResource) {
        return new OutputTo(outputStreamResource);
    }
}