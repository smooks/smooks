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
package org.smooks.templating;

import org.smooks.container.ExecutionContext;
import org.smooks.io.AbstractOutputStreamResource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class MockOutStreamResource extends AbstractOutputStreamResource {

    public static ByteArrayOutputStream outputStream;

    public MockOutStreamResource() {
    }

    public MockOutStreamResource(String resourceName) {
        setResourceName(resourceName);
    }

    public OutputStream getOutputStream(ExecutionContext executionContext) throws IOException {
        return outputStream;
    }
}
