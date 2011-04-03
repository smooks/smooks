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

import org.milyn.GenericReaderConfigurator;
import org.milyn.ReaderConfigurator;
import org.milyn.assertion.AssertArgument;
import org.milyn.cdr.SmooksConfigurationException;
import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.flatfile.Binding;
import org.milyn.flatfile.BindingType;
import org.milyn.flatfile.FlatFileReader;
import org.milyn.flatfile.variablefield.VariableFieldRecordParserConfigurator;
import org.milyn.flatfile.variablefield.VariableFieldRecordParserFactory;

import java.nio.charset.Charset;
import java.util.List;

/**
 * CSV Record Parser configurator.
 * <p/>
 * Supports programmatic configuration of {@link CSVRecordParserFactory} and {@link CSVRecordParser}.
 *
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class CSVRecordParserConfigurator extends VariableFieldRecordParserConfigurator {

    private String csvFields;
    private char separatorChar = ',';
    private char quoteChar = '"';
    private int skipLineCount = 0;
    private String rootElementName = "csv-set";
    private String recordElementName = "csv-record";

    public CSVRecordParserConfigurator(String csvFields) {
        super(CSVRecordParserFactory.class);
        AssertArgument.isNotNullAndNotEmpty(csvFields, "csvFields");
        this.csvFields = csvFields;
    }

    public CSVRecordParserConfigurator setSeparatorChar(char separatorChar) {
        AssertArgument.isNotNull(separatorChar, "separatorChar");
        this.separatorChar = separatorChar;
        return this;
    }

    public CSVRecordParserConfigurator setQuoteChar(char quoteChar) {
        AssertArgument.isNotNull(quoteChar, "quoteChar");
        this.quoteChar = quoteChar;
        return this;
    }

    public CSVRecordParserConfigurator setSkipLineCount(int skipLineCount) {
        AssertArgument.isNotNull(skipLineCount, "skipLineCount");
        this.skipLineCount = skipLineCount;
        return this;
    }

    public CSVRecordParserConfigurator setRootElementName(String csvRootElementName) {
        AssertArgument.isNotNullAndNotEmpty(csvRootElementName, "rootElementName");
        this.rootElementName = csvRootElementName;
        return this;
    }

    public CSVRecordParserConfigurator setRecordElementName(String csvRecordElementName) {
        AssertArgument.isNotNullAndNotEmpty(csvRecordElementName, "recordElementName");
        this.recordElementName = csvRecordElementName;
        return this;
    }

    public List<SmooksResourceConfiguration> toConfig() {
        getParameters().setProperty("fields", csvFields);
        getParameters().setProperty("separator", Character.toString(separatorChar));
        getParameters().setProperty("quote-char", Character.toString(quoteChar));
        getParameters().setProperty("skip-line-count", Integer.toString(skipLineCount));
        getParameters().setProperty("rootElementName", rootElementName);
        getParameters().setProperty("recordElementName", recordElementName);

        return super.toConfig();
    }
}
