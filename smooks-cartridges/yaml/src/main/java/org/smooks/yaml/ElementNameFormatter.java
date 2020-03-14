/*
	Milyn - Copyright (C) 2008

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
package org.smooks.yaml;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Cleans up or replaces names mend for XML elements.
 *
 * @author maurice_zeijen
 */
public class ElementNameFormatter {

	private static final Pattern ILLEGAL_ELEMENT_NAME_PATTERN = Pattern.compile("^[.]|[^a-zA-Z0-9_.-]");

    private final Map<String, String> keyMap;

    private final String keyWhitspaceReplacement;

    private final String keyPrefixOnNumeric;

    private final String illegalElementNameCharReplacement;

    private final boolean doKeyReplacement ;

    private final boolean doKeyWhitspaceReplacement;

    private final boolean doPrefixOnNumericKey;

    private final boolean doIllegalElementNameCharReplacement;

	public ElementNameFormatter(Map<String, String> keyMap, String keyWhitspaceReplacement, String keyPrefixOnNumeric, String illegalElementNameCharReplacement) {
		this.keyMap = keyMap;
		this.keyWhitspaceReplacement = keyWhitspaceReplacement;
		this.keyPrefixOnNumeric = keyPrefixOnNumeric;
		this.illegalElementNameCharReplacement = illegalElementNameCharReplacement;

		doKeyReplacement = !keyMap.isEmpty();
		doKeyWhitspaceReplacement = keyWhitspaceReplacement != null;
		doPrefixOnNumericKey = keyPrefixOnNumeric != null;
		doIllegalElementNameCharReplacement = illegalElementNameCharReplacement != null;
	}

	/**
	 * @param text
	 * @return
	 */
	public String format(String text) {

		boolean replacedKey = false;
		if(doKeyReplacement) {

			String mappedKey = keyMap.get(text);

			replacedKey = mappedKey != null;
			if(replacedKey) {
				text = mappedKey;
			}

		}

		if(!replacedKey) {
			if(doKeyWhitspaceReplacement) {
				text = text.replace(" ", keyWhitspaceReplacement);
			}

			if(doPrefixOnNumericKey && Character.isDigit(text.charAt(0))) {
				text = keyPrefixOnNumeric + text;
			}

			if(doIllegalElementNameCharReplacement) {
				Matcher matcher = ILLEGAL_ELEMENT_NAME_PATTERN.matcher(text);
				text = matcher.replaceAll(illegalElementNameCharReplacement);
			}
		}
		return text;
	}

}
