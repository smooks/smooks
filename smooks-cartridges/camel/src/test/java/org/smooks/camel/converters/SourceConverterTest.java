/*
 * Milyn - Copyright (C) 2006 - 2010
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License (version 2.1) as published
 * by the Free Software Foundation.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.
 * 
 * See the GNU Lesser General Public License for more details:
 * http://www.gnu.org/licenses/lgpl.txt
 */
package org.smooks.camel.converters;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Map;

import org.apache.camel.TypeConverter;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.Before;
import org.junit.Test;
import org.smooks.payload.JavaSourceWithoutEventStream;

/**
 * Unit test for {@link SourceConverter}. </p>
 * 
 * @author Daniel Bevenius
 * 
 */
public class SourceConverterTest
{
    private TypeConverter typeConverter;

    @Before
    public void getTypeConverter()
    {
        DefaultCamelContext camelContext = new DefaultCamelContext();
        typeConverter = camelContext.getTypeConverter();
    }

    @Test
    public void convertStringToJavaSourceWithoutEventStream()
    {
        final String payload = "dummyPayload";
        final JavaSourceWithoutEventStream javaSource = typeConverter.convertTo(JavaSourceWithoutEventStream.class, payload);
        final Map<String, Object> beans = javaSource.getBeans();
        final String actualPayload = (String) beans.get("string");

        assertThat(payload, is(actualPayload));
    }
    
}
