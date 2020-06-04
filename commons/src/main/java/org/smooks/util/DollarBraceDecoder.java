/*-
 * ========================LICENSE_START=================================
 * Smooks Commons
 * %%
 * Copyright (C) 2020 Smooks
 * %%
 * Licensed under the terms of the Apache License Version 2.0, or
 * the GNU Lesser General Public License version 3.0 or later.
 * 
 * SPDX-License-Identifier: Apache-2.0 OR LGPL-3.0-or-later
 * 
 * ======================================================================
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * ======================================================================
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * =========================LICENSE_END==================================
 */
package org.smooks.util;

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
