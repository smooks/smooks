/*
	Milyn - Copyright (C) 2006 - 2010

	This library is free software; you can redistribute it and/or
	modify it under the terms of the GNU Lesser General Public
	License (version 2.1) as published by the Free Software
	Foundation.

	This library is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

	See the GNU Lesser General Public License for more details:
	http://www.gnu.org/licenses/lgpl.txt
*/
package org.milyn.csv;

import org.milyn.ReaderConfigurator;
import org.milyn.GenericReaderConfigurator;
import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.cdr.SmooksConfigurationException;
import org.milyn.assertion.AssertArgument;
import org.milyn.flatfile.FlatFileReader;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * CSV Reader configurator.
 * <p/>
 * Supports programmatic {@link CSVReader} configuration on a {@link org.milyn.Smooks#setReaderConfig(org.milyn.ReaderConfigurator) Smooks} instance.
 *
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 * @deprecated Use the {@link CSVRecordParserConfigurator}.
 */
public class CSVReaderConfigurator implements ReaderConfigurator {

    private String csvFields;
    private char separatorChar = ',';
    private char quoteChar = '"';
    private int skipLineCount = 0;
    private Charset encoding = Charset.forName("UTF-8");
    private String rootElementName = "csv-set";
    private String recordElementName = "csv-record";
    private CSVBinding binding;
    private String targetProfile;
    private boolean indent = false;
    private boolean strict = true;

    public CSVReaderConfigurator(String csvFields) {
        AssertArgument.isNotNullAndNotEmpty(csvFields, "csvFields");
        this.csvFields = csvFields;
    }

    public CSVReaderConfigurator setSeparatorChar(char separatorChar) {
        AssertArgument.isNotNull(separatorChar, "separatorChar");
        this.separatorChar = separatorChar;
        return this;
    }

    public CSVReaderConfigurator setQuoteChar(char quoteChar) {
        AssertArgument.isNotNull(quoteChar, "quoteChar");
        this.quoteChar = quoteChar;
        return this;
    }

    public CSVReaderConfigurator setSkipLineCount(int skipLineCount) {
        AssertArgument.isNotNull(skipLineCount, "skipLineCount");
        this.skipLineCount = skipLineCount;
        return this;
    }

    public CSVReaderConfigurator setEncoding(Charset encoding) {
        AssertArgument.isNotNull(encoding, "encoding");
        this.encoding = encoding;
        return this;
    }

    public CSVReaderConfigurator setRootElementName(String csvRootElementName) {
        AssertArgument.isNotNullAndNotEmpty(csvRootElementName, "rootElementName");
        this.rootElementName = csvRootElementName;
        return this;
    }

    public CSVReaderConfigurator setRecordElementName(String csvRecordElementName) {
        AssertArgument.isNotNullAndNotEmpty(csvRecordElementName, "recordElementName");
        this.recordElementName = csvRecordElementName;
        return this;
    }

    public CSVReaderConfigurator setIndent(boolean indent) {
        this.indent = indent;
        return this;
    }

    public CSVReaderConfigurator setStrict(boolean strict) {
        this.strict = strict;
        return this;
    }

    public CSVReaderConfigurator setBinding(CSVBinding binding) {
        this.binding = binding;
        return this;
    }

    public CSVReaderConfigurator setTargetProfile(String targetProfile) {
        AssertArgument.isNotNullAndNotEmpty(targetProfile, "targetProfile");
        this.targetProfile = targetProfile;
        return this;
    }

    public List<SmooksResourceConfiguration> toConfig() {
        GenericReaderConfigurator configurator = new GenericReaderConfigurator(FlatFileReader.class);

        configurator.getParameters().setProperty("parserFactory", CSVRecordParserFactory.class.getName());
        configurator.getParameters().setProperty("fields", csvFields);
        configurator.getParameters().setProperty("separator", Character.toString(separatorChar));
        configurator.getParameters().setProperty("quote-char", Character.toString(quoteChar));
        configurator.getParameters().setProperty("skip-line-count", Integer.toString(skipLineCount));
        configurator.getParameters().setProperty("encoding", encoding.name());
        configurator.getParameters().setProperty("rootElementName", rootElementName);
        configurator.getParameters().setProperty("recordElementName", recordElementName);
        configurator.getParameters().setProperty("indent", Boolean.toString(indent));
        configurator.getParameters().setProperty("strict", Boolean.toString(strict));

        if(binding != null) {
            configurator.getParameters().setProperty("bindBeanId", binding.getBeanId());
            configurator.getParameters().setProperty("bindBeanClass", binding.getBeanClass().getName());
            configurator.getParameters().setProperty("bindingType", binding.getBindingType().toString());
            if(binding.getBindingType() == CSVBindingType.MAP) {
                if(binding.getKeyField() == null) {
                    throw new SmooksConfigurationException("CSV 'MAP' Binding must specify a 'keyField' property on the binding configuration.");
                }
                configurator.getParameters().setProperty("bindMapKeyField", binding.getKeyField());                
            }
        }

        configurator.setTargetProfile(targetProfile);

        return configurator.toConfig();
    }
}
