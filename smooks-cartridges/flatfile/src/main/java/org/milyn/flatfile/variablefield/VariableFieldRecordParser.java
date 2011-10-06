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

package org.milyn.flatfile.variablefield;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.milyn.flatfile.Field;
import org.milyn.flatfile.FieldMetaData;
import org.milyn.flatfile.Record;
import org.milyn.flatfile.RecordMetaData;
import org.milyn.flatfile.RecordParser;
import org.milyn.function.StringFunctionExecutor;

/**
 * Abstract variable field record parser.
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public abstract class VariableFieldRecordParser<T extends VariableFieldRecordParserFactory> implements RecordParser<T> {

    private T factory;
    private int recordCount = 0;

    /**
     * Parse the next record from the flat file input stream and produce the set
     * of record field values.
     * 
     * @return The next records field values.
     * @throws IOException Error reading message stream.
     */
    public abstract List<String> nextRecordFieldValues() throws IOException;

    public abstract List<String> getRecordHeaders() throws IOException;

    /**
     * {@inheritDoc}
     */
    public final void setRecordParserFactory(T factory) {
        this.factory = factory;
    }

    /**
     * Get the parser factory instance that created this parser instance.
     * 
     * @return The parser factory instance that created this parser instance.
     */
    public T getFactory() {
        return factory;
    }

    /**
     * Get the number of records read so far by this parser instance.
     * 
     * @return The number of records read so far by this parser instance.
     */
    public int getRecordCount() {
        return recordCount;
    }

    /**
     * {@inheritDoc}
     */
    public final Record nextRecord() throws IOException {
        List<String> fieldValues = nextRecordFieldValues();

        if (fieldValues == null || fieldValues.isEmpty()) {
            return null;
        }

        RecordMetaData recordMetaData = factory.getRecordMetaData(fieldValues);
        List<Field> fields = new ArrayList<Field>();

        try {
            if (recordMetaData == VariableFieldRecordParserFactory.unknownVRecordType) {
                fields.add(new Field(recordMetaData.getFields().get(0).getName(), fieldValues.get(0)));
                return new Record(recordMetaData.getName(), fields, recordMetaData);
            } else {
                List<FieldMetaData> fieldsMetaData = recordMetaData.getFields();
                int fieldValueOffset = 0;

                if (factory.isMultiTypeRecordSet()) {
                    // Skip the first field value because it's the field name...
                    fieldValueOffset = +1;
                }

                for (int i = 0; i < fieldValues.size(); i++) {
                    int fieldValueIndex = i + fieldValueOffset;

                    if (fieldValueIndex > fieldValues.size() - 1) {
                        break;
                    }
                    if (!recordMetaData.isWildCardRecord() && i > fieldsMetaData.size() - 1) {
                        // We're done... ignore the rest of the fields...
                        break;
                    }

                    Field field;
                    String value = fieldValues.get(fieldValueIndex);

                    if (recordMetaData.isWildCardRecord() || i > fieldsMetaData.size() - 1) {
                        field = new Field("field_" + i, value);
                    } else {
                        FieldMetaData fieldMetaData = fieldsMetaData.get(i);

                        if (fieldMetaData.ignore()) {
                            i += fieldMetaData.getIgnoreCount() - 1;
                            if (i < 0) {
                                // An overflow has resulted...
                                i = Integer.MAX_VALUE - 1;
                            }
                            continue;
                        }

                        StringFunctionExecutor stringFunction = fieldMetaData.getStringFunctionExecutor();
                        if (stringFunction != null) {
                            value = stringFunction.execute(value);
                        }

                        if (fieldMetaData.useHeader()) {
                            if (i > getRecordHeaders().size() - 1) {
                                field = new Field("field_" + i, value);
                            } else {
                                field = new Field(getRecordHeaders().get(i), value);
                            }
                        } else {
                            field = new Field(fieldMetaData.getName(), value);
                        }

                        field.setMetaData(fieldMetaData);
                    }

                    fields.add(field);
                }
            }
        } finally {
            recordCount++;
        }

        return new Record(recordMetaData.getName(), fields, recordMetaData);
    }

    /**
     * Get the unignored field count for the specified record.
     * 
     * @param recordMetaData The record metadata.
     * @return The unignored field count.
     */
    public int getUnignoredFieldCount(RecordMetaData recordMetaData) {
        if (factory.isMultiTypeRecordSet()) {
            // Need to account for the leading identifier field on each
            // record...
            return recordMetaData.getUnignoredFieldCount() + 1;
        } else {
            return recordMetaData.getUnignoredFieldCount();
        }
    }
}
