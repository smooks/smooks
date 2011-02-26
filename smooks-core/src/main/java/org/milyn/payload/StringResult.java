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

import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

/**
 * Utility class for creating a String based {@link javax.xml.transform.stream.StreamResult}.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class StringResult extends StreamResult implements ResultExtractor<StringResult> {

    public StringResult() {
        super();
        StringWriter writer = new StringWriter();
        setWriter(writer);
    }

    public String getResult() {
        return getWriter().toString();
    }

    public String toString() {
        return getResult();
    }

    public Object extractFromResult(StringResult result, Export export) {
        return getResult();
    }
}