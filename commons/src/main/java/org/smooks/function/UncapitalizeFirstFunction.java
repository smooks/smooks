/*
 * Milyn - Copyright (C) 2006 - 2010
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License (version 2.1) as published by the Free Software
 * Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU Lesser General Public License for more details:
 * http://www.gnu.org/licenses/lgpl.txt
 */

package org.smooks.function;

/**
 * Uncapitalizes the first word of a String.
 *
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 */
public class UncapitalizeFirstFunction implements StringFunction {

    /**
     * Uncapitalizes the first word of a String.
     *
     * @param input The String
     * @return The manipulated String
     */
    public String execute(String input) {
        for (int i = 0; i < input.length(); i++) {
            final char ch = input.charAt(i);
            if (!Character.isWhitespace(ch)) {
                if (Character.isLowerCase(ch)) {
                    return input;
                }
                final char[] chars = input.toCharArray();
                chars[i] = Character.toLowerCase(ch);

                return new String(chars);
            }
        }
        return input;

    }
}