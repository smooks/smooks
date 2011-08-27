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

package org.milyn.csv;

import org.milyn.cdr.annotation.ConfigParam;
import org.milyn.flatfile.RecordParser;
import org.milyn.flatfile.variablefield.VariableFieldRecordParserFactory;

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

    @ConfigParam(name = "skip-line-count", defaultVal = "0")
    private int skipLines;

    @ConfigParam(name = "head-line-number", defaultVal = "0")
    private int headLine;

    @ConfigParam(defaultVal = "false")
    private boolean validateHeader;

    public RecordParser newRecordParser() {
        return new CSVRecordParser();
    }

    public char getSeparator() {
        return separator;
    }

    public char getQuoteChar() {
        return quoteChar;
    }

    public int getSkipLines() {
        if (skipLines < 0) {
            return 0;
        } else {
            return skipLines;
        }
    }

    public int getHeadLine() {
        if (headLine < 1) {
            return getSkipLines() + 1;
        } else {
            return headLine;
        }
    }

    public boolean validateHeader() {
        return validateHeader;
    }
}
