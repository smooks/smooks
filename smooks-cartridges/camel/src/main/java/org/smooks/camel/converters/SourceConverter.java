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

import org.apache.camel.Converter;
import org.apache.camel.component.file.GenericFile;
import org.smooks.payload.JavaResult;
import org.smooks.payload.JavaSource;
import org.smooks.payload.JavaSourceWithoutEventStream;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.InputStream;

/**
 * SourceConverter is a Camel {@link Converter} that converts from different
 * formats to {@link Source} instances. </p>
 * 
 * @author Daniel Bevenius
 */
@Converter(generateLoader = true)
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
    
    @Converter
    public static Source toStreamSource(GenericFile<File> genericFile){
    	return new StreamSource((File)genericFile.getBody());
    }

}
