/*
	Milyn - Copyright (C) 2010

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
package org.milyn.csv;

import org.apache.commons.lang.StringUtils;
import org.milyn.SmooksException;

public class CSVHeaderValidationException extends SmooksException {

	private static final long serialVersionUID = 1L;

	private String[] expected;
	private String[] found;

	public CSVHeaderValidationException(final String[] expected) {
		this(expected, new String[] {});
	}

	public CSVHeaderValidationException(final String[] expected, final String[] found) {
		super("CSV Header Validation Failure.");
		this.expected = expected;
		this.found = found;
	}

	public String[] getExpected() {
		return expected;
	}

	public String[] getFound() {
		return found;
	}

	public String getMessage() {
		return "expected == " + format(expected) + "; found == " + format(found);
	}

	private String format(final String[] strings) {
		return StringUtils.join(strings, ",");
	}

}
