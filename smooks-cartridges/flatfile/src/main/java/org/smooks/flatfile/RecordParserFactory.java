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

package org.smooks.flatfile;

/**
 * {@link RecordParser} factory class.
 * <p/>
 * Configurable be the Smooks {@link org.smooks.cdr.annotation.Configurator}
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public interface RecordParserFactory {

    /**
     * Create a new Flat File {@link RecordParser} instance.
     * @return A new {@link RecordParser} instance.
     */
    RecordParser newRecordParser();
}
