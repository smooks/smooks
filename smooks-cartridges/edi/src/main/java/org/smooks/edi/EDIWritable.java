/*
 * Milyn - Copyright (C) 2006 - 2010
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License (version 2.1) as published by the Free Software
 *  Foundation.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 *  See the GNU Lesser General Public License for more details:
 *  http://www.gnu.org/licenses/lgpl.txt
 */

package org.smooks.edi;

import org.smooks.edisax.model.internal.Delimiters;

import java.io.IOException;
import java.io.Writer;

/**
 * EDI Writable bean.
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public interface EDIWritable {

    /**
     * Write the bean to the specified {@link Writer} instance.
     * @param writer The target writer.
     * @param delimiters The delimiters.
     * @throws IOException Error writing bean.
     */
    void write(Writer writer, Delimiters delimiters) throws IOException;
}
