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
package org.milyn.fixedlength.prog;

import org.milyn.FilterSettings;
import org.milyn.Smooks;
import org.milyn.assertion.AssertArgument;
import org.milyn.fixedlength.FixedLengthBinding;
import org.milyn.fixedlength.FixedLengthBindingType;
import org.milyn.fixedlength.FixedLengthReaderConfigurator;
import org.milyn.payload.JavaResult;

import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.UUID;

/**
 * Fixed Length {@link java.util.List} Binder class.
 * <p/>
 * Simple Fixed Length records to Object {@link java.util.List} binding class.
 * <p/>
 * Exmaple usage:
 * <pre>
 * public class PeopleBinder {
 *     // Create and cache the binder instance..
 *     private FixedLengthListBinder binder = new FixedLengthListBinder("firstname[10],lastname[10],gender[1],age[3],country[2]", Person.class);
 *
 *     public List&lt;Person&gt; bind(Reader fixedLengthStream) {
 *         return binder.bind(fixedLengthStream);
 *     }
 * }
 * </pre>
 *
 *
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 */
public class FixedLengthListBinder {

    private String beanId = UUID.randomUUID().toString();
    private Smooks smooks;

    public FixedLengthListBinder(String fields, Class recordType) {
        AssertArgument.isNotNullAndNotEmpty(fields, "fields");
        AssertArgument.isNotNull(recordType, "recordType");

        smooks = new Smooks();
        smooks.setFilterSettings(FilterSettings.DEFAULT_SAX);
        smooks.setReaderConfig(new FixedLengthReaderConfigurator(fields)
                .setBinding(new FixedLengthBinding(beanId, recordType, FixedLengthBindingType.LIST)));
    }

    public List bind(Reader fixedLengthStream) {
        AssertArgument.isNotNull(fixedLengthStream, "fixedLengthStream");

        JavaResult javaResult = new JavaResult();

        smooks.filterSource(new StreamSource(fixedLengthStream), javaResult);

        return (List) javaResult.getBean(beanId);
    }

    public List bind(InputStream fixedLengthStream) {
        return bind(new InputStreamReader(fixedLengthStream));
    }
}