/*-
 * ========================LICENSE_START=================================
 * Core
 * %%
 * Copyright (C) 2020 - 2021 Smooks
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
package org.smooks.io;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import java.io.InputStream;
import java.io.Reader;

public class DocumentInputSource extends InputSource {

    private final Document document;

    public DocumentInputSource(Document document) {
        this.document = document;
    }

    public DocumentInputSource() {
        throw new UnsupportedOperationException();
    }

    public DocumentInputSource(String systemId) {
        throw new UnsupportedOperationException();
    }

    public DocumentInputSource(InputStream byteStream) {
        throw new UnsupportedOperationException();
    }

    public DocumentInputSource(Reader characterStream) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setPublicId(String publicId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setSystemId(String systemId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setByteStream(InputStream byteStream) {
        throw new UnsupportedOperationException();
    }

    @Override
    public InputStream getByteStream() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setEncoding(String encoding) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCharacterStream(Reader characterStream) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Reader getCharacterStream() {
        throw new UnsupportedOperationException();
    }

    public Document getDocument() {
        return document;
    }
}