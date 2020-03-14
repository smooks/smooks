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

package org.smooks.xml;

import org.w3c.dom.ls.LSInput;

import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;
import java.io.Reader;

/**
 * {@link StreamSource} based {@link LSInput}.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class StreamSourceLSInput implements LSInput {

    private StreamSource input;

    public StreamSourceLSInput(StreamSource input) {
        this.input = input;
    }

    public Reader getCharacterStream() {
        return input.getReader();
    }

    public void setCharacterStream(Reader reader) {
    }

    public InputStream getByteStream() {
        return input.getInputStream();
    }

    public void setByteStream(InputStream inputStream) {
    }

    public String getStringData() {
        return null;
    }

    public void setStringData(String s) {
    }

    public String getSystemId() {
        return input.getSystemId();
    }

    public void setSystemId(String s) {
    }

    public String getPublicId() {
        return input.getPublicId();
    }

    public void setPublicId(String s) {
    }

    public String getBaseURI() {
        return null;
    }

    public void setBaseURI(String s) {
    }

    public String getEncoding() {
        return null;
    }

    public void setEncoding(String s) {
    }

    public boolean getCertifiedText() {
        return false;
    }

    public void setCertifiedText(boolean b) {
    }
}
