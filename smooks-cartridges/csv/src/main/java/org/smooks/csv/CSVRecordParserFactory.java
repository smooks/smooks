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

package org.smooks.csv;

import org.smooks.cdr.annotation.ConfigParam;
import org.smooks.flatfile.RecordParser;
import org.smooks.flatfile.variablefield.VariableFieldRecordParserFactory;

/**
 * CSV Record Parser factory.
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class CSVRecordParserFactory extends VariableFieldRecordParserFactory {

    @ConfigParam(defaultVal = ",")
    private char separator;

    @ConfigParam(name = "quote-char", defaultVal = "\"")
    private char quoteChar;

    @ConfigParam(name = "escape-char", defaultVal = "\\")
    private char escapeChar;

    public RecordParser newRecordParser() {
        return new CSVRecordParser();
    }

    public char getSeparator() {
        return separator;
    }

    public char getQuoteChar() {
        return quoteChar;
    }

    public char getEscapeChar() {
        return escapeChar;
    }
}
