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

import java.util.List;

/**
 * Flat file record.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class Record {

    private String name;
    private List<Field> fields;
    private RecordMetaData recordMetaData;

    /**
     * Public constructor.
     * @param name The record name.  This will be used to create the element that will
     * enclose the record field elements.
     * @param fields The record fields.
     * @param recordMetaData Record metadata.
     */
    public Record(String name, List<Field> fields, RecordMetaData recordMetaData) {
        AssertArgument.isNotNullAndNotEmpty(name, "name");
        AssertArgument.isNotNullAndNotEmpty(fields, "fields");
        this.name = name;
        this.fields = fields;
        this.recordMetaData = recordMetaData;
    }

    /**
     * Get the name of the record.
     * @return The record name.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the record fields.
     * @return The record fields.
     */
    public List<Field> getFields() {
        return fields;
    }

    /**
     * Get the record metadata.
     * @return The record metadata.
     */
    public RecordMetaData getRecordMetaData() {
        return recordMetaData;
    }
}
