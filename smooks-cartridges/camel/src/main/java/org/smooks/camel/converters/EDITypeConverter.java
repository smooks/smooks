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

package org.smooks.camel.converters;

import org.apache.camel.Converter;
import org.smooks.edisax.unedifact.UNEdifactInterchangeParser;
import org.smooks.edi.EDIWritable;
import org.smooks.edi.unedifact.model.r41.UNEdifactInterchange41;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

/**
 * Type Converter for EDI.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
@Converter(generateLoader = true)
public class EDITypeConverter {

    @Converter
    public String toUNEDIFACTString(UNEdifactInterchange41 ediMessage) throws IOException {
        StringWriter ediWriter = new StringWriter();

        try {
            ediMessage.write(ediWriter);
            return ediWriter.toString();
        } finally {
            ediWriter.flush();
            ediWriter.close();
        }
    }

    @Converter
    public InputStream toUNEDIFACTInputStream(UNEdifactInterchange41 ediMessage) throws IOException {
        String serialized = toUNEDIFACTString(ediMessage);
        return new ByteArrayInputStream(serialized.getBytes());
    }

    @Converter
    public String toUNEDIFACTString(EDIWritable ediMessage) throws IOException {
        StringWriter ediWriter = new StringWriter();

        try {
            ediMessage.write(ediWriter, UNEdifactInterchangeParser.defaultUNEdifactDelimiters);
            return ediWriter.toString();
        } finally {
            ediWriter.flush();
            ediWriter.close();
        }
    }

    @Converter
    public InputStream toUNEDIFACTInputStream(EDIWritable ediMessage) throws IOException {
        String serialized = toUNEDIFACTString(ediMessage);
        return new ByteArrayInputStream(serialized.getBytes());
    }
}
