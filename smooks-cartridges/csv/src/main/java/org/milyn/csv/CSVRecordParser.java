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

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.milyn.flatfile.FieldMetaData;
import org.milyn.flatfile.RecordMetaData;
import org.milyn.flatfile.variablefield.VariableFieldRecordParser;
import org.xml.sax.InputSource;

/**
 * CSV record parser.
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class CSVRecordParser<T extends CSVRecordParserFactory> extends VariableFieldRecordParser<T> {

    private static Log logger = LogFactory.getLog(CSVRecordParser.class);

    private au.com.bytecode.opencsv.CSVReader csvLineReader;
    private int lineNumber = 0;

    private String[] headers;

    /**
     * {@inheritDoc}
     */
    public void setDataSource(InputSource source) {
        Reader reader = source.getCharacterStream();

        if (reader == null) {
            throw new IllegalStateException(
                    "Invalid InputSource type supplied to CSVRecordParser.  Must contain a Reader instance.");
        }

        // Create the CSV line reader...
        T factory = getFactory();
        csvLineReader = new au.com.bytecode.opencsv.CSVReader(reader, factory.getSeparator(), factory.getQuoteChar());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> nextRecordFieldValues() throws IOException {
        lineNumber++; // Increment immediately... first line is #1

        T factory = getFactory();
        String[] csvRecord = csvLineReader.readNext();

        if (csvRecord == null) {
            return null;
        }

        RecordMetaData recordMetaData = factory.getRecordMetaData(csvRecord);
        List<FieldMetaData> fieldsMetaData = recordMetaData.getFields();

        if (lineNumber == factory.getHeadLine()) {
            this.headers = csvRecord;
            if (factory.strict() && this.headers.length < recordMetaData.getRequiredHeaderFieldCount()) {
                logger.debug("[CORRUPT-CSV] CSV record #" + lineNumber + " invalid header record ["
                        + Arrays.asList(csvRecord) + "].  The record should contain " + fieldsMetaData.size()
                        + " fields [" + recordMetaData.getFieldNames() + "], but contains " + csvRecord.length
                        + " fields.  Aborting!!");
                return null;
            }
            if (factory.validateHeader()) {
                validateHeader();
                return nextRecordFieldValues();
            } else if (recordMetaData.getRequiredHeaderFieldCount() > 0) {
                return nextRecordFieldValues();
            }
        }

        if (lineNumber <= factory.getSkipLines()) {
            return nextRecordFieldValues();
        }

        if (factory.strict() && csvRecord.length < getUnignoredFieldCount(recordMetaData)) {
            logger.debug("[CORRUPT-CSV] CSV record #" + lineNumber + " invalid [" + Arrays.asList(csvRecord)
                    + "].  The record should contain " + fieldsMetaData.size() + " fields ["
                    + recordMetaData.getFieldNames() + "], but contains " + csvRecord.length + " fields.  Ignoring!!");
            return nextRecordFieldValues();
        }

        return Arrays.asList(csvRecord);
    }

    private void validateHeader() throws IOException {
        RecordMetaData recordMetaData = getFactory().getRecordMetaData();

        if (headers == null) {
            throw new CSVHeaderValidationException(recordMetaData.getFieldNames());
        }

        if (validateHeader(recordMetaData.getFields())) {
            return;
        }

        throw new CSVHeaderValidationException(recordMetaData.getFieldNames(), Arrays.asList(headers));
    }

    private boolean validateHeader(final List<FieldMetaData> fieldsMetaData) {
        if (fieldsMetaData.size() != headers.length) {
            return false;
        }

        int n = 0;
        for (FieldMetaData field : fieldsMetaData) {
            if (!field.ignore() && !field.useHeader()) {
                if (headers.length <= n) {
                    return false;
                }

                String header = headers[n];
                if (header == null) {
                    header = "";
                }

                String name = field.getName();
                if (name == null) {
                    name = "";
                }

                if (!name.equals(header)) {
                    return false;
                }
            }
            n++;
        }

        return true;
    }

    @Override
    public List<String> getRecordHeaders() throws IOException {
        return Arrays.asList(headers);
    }

}
