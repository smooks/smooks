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

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.StringWriter;

import javax.xml.transform.stream.StreamSource;

import org.apache.camel.TypeConverter;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.Before;
import org.junit.Test;
import org.smooks.payload.StringResult;

/**
 * Unit test for {@link ResultConverter}.
 * 
 * @author Daniel Bevenius
 *
 */
public class ResultConverterTest
{
    private TypeConverter typeConverter;

    @Before
    public void getTypeConverter()
    {
        DefaultCamelContext camelContext = new DefaultCamelContext();
        typeConverter = camelContext.getTypeConverter();
    }
    
    @Test
    public void convertStringResultToStreamSource() throws Exception
    {
        StringResult stringResult = createStringResult("Bajja");
        
        StreamSource streamSource = typeConverter.convertTo(StreamSource.class, stringResult);
        
        BufferedReader reader = new BufferedReader(streamSource.getReader());
        assertEquals("Bajja", reader.readLine());
    }
    
    private StringResult createStringResult(final String string)
    {
        StringWriter stringWriter = new StringWriter();
        stringWriter.write(string);
        StringResult stringResult = new StringResult();
        stringResult.setWriter(stringWriter);
        return stringResult;
    }

}
