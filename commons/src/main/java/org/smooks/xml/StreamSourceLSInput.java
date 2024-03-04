/*-
 * ========================LICENSE_START=================================
 * Commons
 * %%
 * Copyright (C) 2020 Smooks
 * %%
 * Licensed under the terms of the Apache License Version 2.0, or
 * the GNU Lesser General Public License version 3.0 or later.
 *
 * SPDX-License-Identifier: Apache-2.0 OR LGPL-3.0-or-later
 *
 * ======================================================================
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * ======================================================================
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * =========================LICENSE_END==================================
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

    private final StreamSource input;

    public StreamSourceLSInput(StreamSource input) {
        this.input = input;
    }

    @Override
    public Reader getCharacterStream() {
        return input.getReader();
    }

    @Override
    public void setCharacterStream(Reader reader) {
    }

    @Override
    public InputStream getByteStream() {
        return input.getInputStream();
    }

    @Override
    public void setByteStream(InputStream inputStream) {
    }

    @Override
    public String getStringData() {
        return null;
    }

    public void setStringData(String s) {
    }

    @Override
    public String getSystemId() {
        return input.getSystemId();
    }

    @Override
    public void setSystemId(String s) {
    }

    @Override
    public String getPublicId() {
        return input.getPublicId();
    }

    @Override
    public void setPublicId(String s) {
    }

    @Override
    public String getBaseURI() {
        return null;
    }

    @Override
    public void setBaseURI(String s) {
    }

    @Override
    public String getEncoding() {
        return null;
    }

    @Override
    public void setEncoding(String s) {
    }

    @Override
    public boolean getCertifiedText() {
        return false;
    }

    @Override
    public void setCertifiedText(boolean b) {
    }
}
