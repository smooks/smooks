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
package org.smooks.csv;

import org.apache.commons.lang.StringUtils;
import org.smooks.SmooksException;

import java.util.ArrayList;
import java.util.List;

public class CSVHeaderValidationException extends SmooksException {

	private static final long serialVersionUID = 1L;

	private List<String> expected;
	private List<String> found;

	public CSVHeaderValidationException(final List<String> expected) {
		this(expected, new ArrayList<String>());
	}

	public CSVHeaderValidationException(final List<String> expected, final List<String> found) {
		super("CSV Header Validation Failure.");
		this.expected = expected;
		this.found = found;
	}

	public List<String> getExpected() {
		return expected;
	}

	public List<String> getFound() {
		return found;
	}

	public String getMessage() {
		return "expected == " + format(expected) + "; found == " + format(found);
	}

	private String format(final List<String> strings) {
		return StringUtils.join(strings, ",");
	}

}
