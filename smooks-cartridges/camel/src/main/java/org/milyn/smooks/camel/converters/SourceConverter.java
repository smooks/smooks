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
package org.milyn.smooks.camel.converters;

import java.io.InputStream;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.camel.Converter;
import org.milyn.payload.JavaResult;
import org.milyn.payload.JavaSource;
import org.milyn.payload.JavaSourceWithoutEventStream;

/**
 * SourceConverter is a Camel {@link Converter} that converts from different
 * formats to {@link Source} instances. </p>
 * 
 * @author Daniel Bevenius
 */
@Converter
public class SourceConverter
{
    private SourceConverter()
    {
    }

    @Converter
    public static JavaSourceWithoutEventStream toJavaSourceWithoutEventStream(Object payload)
    {
        return new JavaSourceWithoutEventStream(payload);
    }

    @Converter
    public static JavaSource toJavaSource(Object payload)
    {
        return new JavaSource(payload);
    }

    @Converter
    public static Source toStreamSource(InputStream in)
    {
        return new StreamSource(in);
    }

    @Converter
    public static JavaSource toJavaSource(JavaResult result)
    {
        return new JavaSource(result.getResultMap().values());
    }

}
