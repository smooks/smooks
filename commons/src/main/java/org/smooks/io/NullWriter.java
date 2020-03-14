/*
	Milyn - Copyright (C) 2006 - 2010

	This library is free software; you can redistribute it and/or
	modify it under the terms of the GNU Lesser General Public
	License (version 2.1) as published by the Free Software
	Foundation.

	This library is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

	See the GNU Lesser General Public License for more details:
	http://www.gnu.org/licenses/lgpl.txt
*/
package org.smooks.io;

import java.io.Writer;
import java.io.IOException;

/**
 * Null writer implementation.
 * <p/>
 * Data writen to this writer is swallowed (ala piping output to <i>/dev/null</i>).
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class NullWriter extends Writer {

    private Writer parentWriter;

    public NullWriter() {
        super();
    }

    public NullWriter(Object lock) {
        super(lock);
    }

    public NullWriter(Writer parentWriter) {
        super();
        this.parentWriter = parentWriter;
    }

    public Writer getParentWriter() {
        return parentWriter;
    }

    public void write(int c) throws IOException {
    }

    public void write(char cbuf[]) throws IOException {
    }

    public void write(String str) throws IOException {
    }

    public void write(String str, int off, int len) throws IOException {
    }

    public Writer append(CharSequence csq) throws IOException {
        return this;
    }

    public Writer append(CharSequence csq, int start, int end) throws IOException {
        return this;
    }

    public Writer append(char c) throws IOException {
        return this;
    }

    public void write(char cbuf[], int off, int len) throws IOException {
    }

    public void flush() throws IOException {
    }

    public void close() throws IOException {
    }
}
