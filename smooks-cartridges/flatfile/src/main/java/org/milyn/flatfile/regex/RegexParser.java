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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.milyn.flatfile.variablefield.VariableFieldRecordParser;
import org.xml.sax.InputSource;

/**
 * Regex record parser.
 * <p/>
 * If there are no groups defined in the regexPattern this parser will use the
 * pattern to split the record into fields. If groups are defined, it will
 * extract the record field data from the groups defined in the pattern.
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class RegexParser<T extends RegexParserFactory> extends VariableFieldRecordParser<T> {

    private BufferedReader reader;
    private StringBuilder readerBuffer;
    private int groupCount;

    public void setDataSource(InputSource source) {
        Reader reader = source.getCharacterStream();

        if (reader == null) {
            throw new IllegalStateException(
                    "Invalid InputSource type supplied to RegexParser.  Must contain a Reader instance.");
        }

        this.reader = new BufferedReader(reader);
        this.readerBuffer = new StringBuilder();
        this.groupCount = getFactory().getRegexPattern().matcher("").groupCount();
    }

    @Override
    public List<String> nextRecordFieldValues() throws IOException {
        T factory = getFactory();
        Pattern pattern = factory.getRegexPattern();

        readerBuffer.setLength(0);
        factory.readRecord(reader, readerBuffer, (getRecordCount() + 1));

        if (readerBuffer.length() == 0) {
            return null;
        }

        if (groupCount > 0) {
            String recordString = readerBuffer.toString();
            List<String> fields = new ArrayList<String>();
            Matcher matcher = pattern.matcher(recordString);

            if (matcher.matches()) {
                for (int i = 0; i < matcher.groupCount(); i++) {
                    String fieldValue = matcher.group(i + 1);
                    if (fieldValue != null) {
                        fields.add(fieldValue);
                    }
                }
            } else {
                // Add the full record text as the only field value
                fields.add(recordString);
            }

            return fields;
        } else {
            return Arrays.asList(pattern.split(readerBuffer.toString()));
        }
    }

    @Override
    public List<String> getRecordHeaders() throws IOException {
        return new ArrayList<String>();
    }
}
