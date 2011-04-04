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

package org.milyn.flatfile;

import org.milyn.assertion.AssertArgument;
import org.milyn.cdr.SmooksConfigurationException;

import java.util.ArrayList;
import java.util.List;

/**
 * Record metadata.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class RecordMetaData {

    private String name;
    private List<FieldMetaData> fields;
    private boolean wildCardRecord;
    private int ignoredFieldCount;
    private int unignoredFieldCount;
    private List<String> fieldNames;

    /**
     * public constructor.
     * @param name Record name.
     * @param fields Record fields metadata.
     */
    public RecordMetaData(String name, List<FieldMetaData> fields) {
        this(name, fields, false);
    }

    /**
     * public constructor.
     * @param name Record name.
     * @param fields Record fields metadata.
     * @param wildCardRecord Wildcard record.  Accept any fields and generate the field names based on index.
     */
    public RecordMetaData(String name, List<FieldMetaData> fields, boolean wildCardRecord) {
        AssertArgument.isNotNullAndNotEmpty(name, "name");
        AssertArgument.isNotNull(fields, "fields");
        this.name = name.trim();
        this.fields = fields;
        this.wildCardRecord = wildCardRecord;
        countIgnoredFields();
        gatherFieldNames();
    }

    /**
     * Get the record name.
     * @return The record name.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the record fields metadata.
     * @return The record fields metadata.
     */
    public List<FieldMetaData> getFields() {
        return fields;
    }

    /**
     * Is this a wildcard record.
     * <p/>
     * If it is, accept all fields and use the field index to generate the field name.
     *
     * @return True of this is a wildcard record, otherwise false.
     */
    public boolean isWildCardRecord() {
        return wildCardRecord;
    }

    /**
     * Get the number of fields in this record that are ignored.
     * @return The number of fields in this record that are ignored.
     */
    public int getIgnoredFieldCount() {
        return ignoredFieldCount;
    }

    /**
     * Get the number of fields in this record that are not ignored.
     * @return The number of fields in this record that are not ignored.
     */
    public int getUnignoredFieldCount() {
        return unignoredFieldCount;
    }

    /**
     * Get a collection of all the field names (excluding ignored fields) in this record.
     * @return Acollection of all the field names in this record.
     */
    public List<String> getFieldNames() {
        return fieldNames;
    }

    /**
     * Assert that the supplied field name is one of the field names associated with this
     * record.
     *
     * @param fieldName The field name to test.
     */
    public void assertValidFieldName(String fieldName) {
        if(!fieldNames.contains(fieldName)) {
            throw new SmooksConfigurationException("Invalid field name '" + fieldName + "'.  Valid names: " + fieldNames + ".");
        }
    }


    private void countIgnoredFields() {
        for(FieldMetaData field : fields) {
            if(field.ignore()) {
                ignoredFieldCount++;
            } else {
                unignoredFieldCount++;
            }
        }
    }

    private void gatherFieldNames() {
        if (fields == null) {
            fieldNames = new ArrayList<String>();
        }

        fieldNames = new ArrayList<String>();

        for (FieldMetaData field : fields) {
            if (!field.ignore()) {
                fieldNames.add(field.getName());
            }
        }
    }
}
