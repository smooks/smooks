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

package org.milyn.flatfile.regex;

import org.milyn.cdr.annotation.ConfigParam;
import org.milyn.commons.javabean.DataDecodeException;
import org.milyn.commons.javabean.DataDecoder;
import org.milyn.flatfile.RecordParser;
import org.milyn.flatfile.variablefield.VariableFieldRecordParserFactory;

import java.util.regex.Pattern;

/**
 * Regex record parser factory.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class RegexParserFactory extends VariableFieldRecordParserFactory {

    @ConfigParam(decoder = RegexPatternDecoder.class)
    private Pattern regexPattern;

    public RecordParser newRecordParser() {
        return new RegexParser();
    }

    /**
     * Get the Regex Pattern instance to be used for parsing.
     *
     * @return The Regex Pattern instance to be used for parsing.
     */
    public Pattern getRegexPattern() {
        return regexPattern;
    }

    public static class RegexPatternDecoder implements DataDecoder {
        public Object decode(String data) throws DataDecodeException {
            return Pattern.compile(data, (Pattern.MULTILINE | Pattern.DOTALL));
        }
    }
}
