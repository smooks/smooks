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

package org.milyn.smooks.edi.unedifact.model;

import org.milyn.edisax.model.internal.Delimiters;

import java.io.IOException;
import java.io.Serializable;
import java.io.Writer;

/**
 * UN/EDIFACT message interchange.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public interface UNEdifactInterchange extends Serializable {

    /**
     * Write the interchange to the specified writer.
     * <p/>
     * Uses the default UN/EDIFACT delimiter set.
     *
     * @param writer The target writer.
     * @throws IOException Error writing interchange.
     */
    void write(Writer writer) throws IOException;

    /**
     * Write the interchange to the specified writer.
     * @param writer The target writer.
     * @param delimiters The delimiters.
     * @throws IOException Error writing interchange.
     */
    void write(Writer writer, Delimiters delimiters) throws IOException;
}