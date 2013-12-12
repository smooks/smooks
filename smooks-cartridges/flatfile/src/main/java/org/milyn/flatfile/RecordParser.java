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

package org.milyn.flatfile;

import org.milyn.commons.cdr.SmooksConfigurationException;
import org.xml.sax.InputSource;

import java.io.IOException;

/**
 * Flat file Record Parser.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public interface RecordParser<T extends RecordParserFactory>  {

    /**
     * Set the parser factory that created the parser instance.
     * @param factory The parser factory that created the parser instance.
     */
    void setRecordParserFactory(T factory);

    /**
     * Set the Flat File data source on the parser.
     * @param source The flat file data source.
     */
    void setDataSource(InputSource source);

    /**
     * Initialize the parser instance.
     * @throws IOException Error initializing the reader.
     */
    void initialize() throws IOException;

    /**
     * Parse the next record from the message stream and produce a {@link Record} instance.
     * @return The records instance.
     * @throws IOException Error reading message stream.
     */
    Record nextRecord() throws IOException;

    /**
     * Uninitialize the parser instance.
     */
    void uninitialize();
}
