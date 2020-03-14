/*
 * Milyn - Copyright (C) 2006 - 2010
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License (version 2.1) as published by the Free Software
 *  Foundation.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 *  See the GNU Lesser General Public License for more details:
 *  http://www.gnu.org/licenses/lgpl.txt
 */

package org.smooks.model.csv;

import org.smooks.javabean.dynamic.serialize.DefaultNamespace;
import org.smooks.model.core.Reader;

/**
 * CSV Reader component.
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
@DefaultNamespace(uri = "http://www.milyn.org/xsd/smooks/csv-1.3.xsd", prefix = "csv13")
public class CSVReader implements Reader {

    private String fields;
    private Character separator;
    private Character quote;
    private Integer skipLines;
    private String rootElementName;
    private String recordElementName;
    private Boolean indent;
    private Boolean strict;
    private Boolean validateHeader;

    // Only one of the following binding configs will be wired into this bean...
    private SingleBinding singleBinding;
    private ListBinding listBinding;
    private MapBinding mapBinding;

    public String getFields() {
        return fields;
    }

    public void setFields(String fields) {
        this.fields = fields;
    }

    public Character getSeparator() {
        return separator;
    }

    public void setSeparator(Character separator) {
        this.separator = separator;
    }

    public Character getQuote() {
        return quote;
    }

    public void setQuote(Character quote) {
        this.quote = quote;
    }

    public Integer getSkipLines() {
        return skipLines;
    }

    public void setSkipLines(Integer skipLines) {
        this.skipLines = skipLines;
    }

    public String getRootElementName() {
        return rootElementName;
    }

    public void setRootElementName(String rootElementName) {
        this.rootElementName = rootElementName;
    }

    public String getRecordElementName() {
        return recordElementName;
    }

    public void setRecordElementName(String recordElementName) {
        this.recordElementName = recordElementName;
    }

    public Boolean isIndent() {
        return indent;
    }

    public void setIndent(Boolean indent) {
        this.indent = indent;
    }

    public Boolean isStrict() {
        return strict;
    }

    public void setStrict(Boolean strict) {
        this.strict = strict;
    }

    public Boolean isValidateHeader() {
        return validateHeader;
    }

    public void setValidateHeader(Boolean validateHeader) {
        this.validateHeader = validateHeader;
    }

    public SingleBinding getSingleBinding() {
        return singleBinding;
    }

    public void setSingleBinding(SingleBinding singleBinding) {
        this.singleBinding = singleBinding;
    }

    public ListBinding getListBinding() {
        return listBinding;
    }

    public void setListBinding(ListBinding listBinding) {
        this.listBinding = listBinding;
    }

    public MapBinding getMapBinding() {
        return mapBinding;
    }

    public void setMapBinding(MapBinding mapBinding) {
        this.mapBinding = mapBinding;
    }
}
