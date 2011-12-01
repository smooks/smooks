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

/**
 * Flat file record field.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class Field {

    private String name;
    private String value;
    private FieldMetaData metaData;

    /**
     * Public constructor.
     * @param name The field name. Used to create the field value element.
     * @param value The field value.
     */
    public Field(String name, String value) {
        AssertArgument.isNotNullAndNotEmpty(name, "name");
        this.name = name;
        this.value = value;
    }

    /**
     * Get the field name.
     * @return The field name.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the field value.
     * @return The field value.
     */
    public String getValue() {
        return value;
    }

    /**
     * Set optional field metadata.
     * @param metaData The metadata.
     * @return This field instance.
     */
    public Field setMetaData(FieldMetaData metaData) {
        this.metaData = metaData;
        return this;
    }

    /**
     * Get the optional field metadata.
     * @return The metadata, or null if none has been set.
     */
    public FieldMetaData getMetaData() {
        return metaData;
    }
}
