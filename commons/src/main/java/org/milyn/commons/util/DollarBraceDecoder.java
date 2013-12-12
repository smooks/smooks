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
package org.milyn.commons.util;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.List;
import java.util.ArrayList;

/**
 * Decoder for Strings containing "${xxxxx}" type patterns.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class DollarBraceDecoder {

    /**
     * Match against "${}", where there's at least one of any character
     * except '}' in between the braces ("{}").
     */
    public static final String PATTERN = "\\$\\{[^\\}]+\\}";
    private static final Pattern pattern = Pattern.compile(PATTERN);

    /**
     * Get the ${} tokens from the supplied string.
     * <p/>
     * For example, if the input string is "xxxx ${A} xxxxx ${B} ...", this method
     * will return a List containing 2 String, "A" and "B".
     *
     * @param string The String from which to extract the ${} tokens.
     * @return The list of tokens.
     */
    public static List<String> getTokens(String string) {
        List<String> tokens = new ArrayList<String>();
        Matcher m = pattern.matcher(string);

        while (m.find()) {
            int start = m.start();
            int end = m.end();
            String token = string.substring((start + 2), (end - 1));

            tokens.add(token);
        }

        return tokens;
    }

    /**
     * Replace all the ${} tokens with the specified String.
     * <p/>
     * For example, if the input string is "xxxx ${A} xxxxx ${B} ..." and the
     * replacement String "?", this method will return "xxxx ? xxxxx ? ...".
     *
     * @param string The String on which to perform the replacement.
     * @param replacement The replacement string.
     * @return The String with the tokens replaced.
     */
    public static String replaceTokens(String string, String replacement) {
        List<String> tokens = getTokens(string);

        // Could obviously do this more efficiently by matching and finding
        // and using a StringBuilder, but this is fine for now :-)
        for (String token : tokens) {
            string = string.replace("${" + token + "}", replacement);
        }

        return string;
    }
}
