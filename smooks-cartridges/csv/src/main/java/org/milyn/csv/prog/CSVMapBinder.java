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
package org.milyn.csv.prog;

import org.milyn.Smooks;
import org.milyn.FilterSettings;
import org.milyn.csv.CSVRecordParserConfigurator;
import org.milyn.flatfile.Binding;
import org.milyn.flatfile.BindingType;
import org.milyn.payload.JavaResult;
import org.milyn.commons.assertion.AssertArgument;

import javax.xml.transform.stream.StreamSource;
import java.util.UUID;
import java.util.Map;
import java.io.Reader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * CSV {@link java.util.Map} Binder class.
 * <p/>
 * Simple CSV to Object {@link java.util.Map} binding class.
 * <p/>
 * Exmaple usage:
 * <pre>
 * public class PeopleBinder {
 *     // Create and cache the binder instance..
 *     private CSVMapBinder binder = new CSVMapBinder("firstname,lastname,gender,age,country", Person.class, "firstname");
 *
 *     public Map&lt;String, Person&gt; bind(Reader csvStream) {
 *         return binder.bind(csvStream);
 *     }
 * }
 * </pre>
 *
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class CSVMapBinder {

    private String beanId = UUID.randomUUID().toString();
    private Smooks smooks;

    public CSVMapBinder(String fields, Class recordType, String keyField) {
        AssertArgument.isNotNullAndNotEmpty(fields, "fields");
        AssertArgument.isNotNull(recordType, "recordType");
        AssertArgument.isNotNullAndNotEmpty(keyField, "keyField");

        smooks = new Smooks();
        smooks.setFilterSettings(FilterSettings.DEFAULT_SAX);
        smooks.setReaderConfig(new CSVRecordParserConfigurator(fields)
                .setBinding(new Binding(beanId, recordType, BindingType.MAP).setKeyField(keyField)));
    }

    public Map bind(Reader csvStream) {
        AssertArgument.isNotNull(csvStream, "csvStream");

        JavaResult javaResult = new JavaResult();

        smooks.filterSource(new StreamSource(csvStream), javaResult);

        return (Map) javaResult.getBean(beanId);
    }

    public Map bind(InputStream csvStream) {
        return bind(new InputStreamReader(csvStream));
    }
}