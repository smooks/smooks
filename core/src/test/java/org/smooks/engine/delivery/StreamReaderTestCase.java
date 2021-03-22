/*-
 * ========================LICENSE_START=================================
 * Core
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
package org.smooks.engine.delivery;


import org.junit.Before;
import org.junit.Test;
import org.smooks.Smooks;
import org.smooks.engine.resource.config.DefaultResourceConfig;
import org.smooks.api.ExecutionContext;
import org.smooks.support.StreamUtils;
import org.smooks.api.resource.reader.SmooksXMLReader;
import org.xml.sax.ContentHandler;
import org.xml.sax.*;

import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertArrayEquals;

/**
 * Test for JIRA: http://jira.codehaus.org/browse/MILYN-291,
 * "Add test(s) for StreamReader functionality"
 * <p/>
 *
 * @author <a href="mailto:daniel.bevenius@gmail.com">Daniel Bevenius</a>
 */
public class StreamReaderTestCase
{
    private final byte[] bytes = "bytes".getBytes();
    private Smooks smooks;

    @Before
    public void setup() {
        smooks = new Smooks();
        smooks.addConfiguration(new DefaultResourceConfig("org.xml.sax.driver", MockReader.class.getName()));
    }

    @Test
    public void verifyByteStream() {
        smooks.filterSource(new StreamSource(new ByteArrayInputStream(bytes)));
        assertArrayEquals(bytes, MockReader.readBytes);
    }

    public static class MockReader implements SmooksXMLReader {
        private ContentHandler handler;
        private static byte[] readBytes;

        @Override
        public void parse(final InputSource inputSource) throws IOException, SAXException {
            final InputStream bin = inputSource.getByteStream();

            MockReader.readBytes = StreamUtils.readStream(bin);
            handler.startDocument();
            handler.endDocument();
        }

        @Override
        public void setContentHandler(ContentHandler arg0) {
            this.handler = arg0;
        }

        @Override
        public void setExecutionContext(ExecutionContext executionContext) {
        }

        @Override
        public ContentHandler getContentHandler() {
            return null;
        }

        @Override
        public DTDHandler getDTDHandler() {
            return null;
        }

        @Override
        public EntityResolver getEntityResolver() {
            return null;
        }

        @Override
        public ErrorHandler getErrorHandler() {
            return null;
        }

        @Override
        public boolean getFeature(String arg0) throws SAXNotRecognizedException, SAXNotSupportedException {
            return false;
        }

        @Override
        public Object getProperty(String arg0) throws SAXNotRecognizedException, SAXNotSupportedException {
            return null;
        }

        @Override
        public void parse(String string) throws IOException, SAXException {
        }

        @Override
        public void setDTDHandler(DTDHandler arg0) {
        }

        @Override
        public void setEntityResolver(EntityResolver arg0) {
        }

        @Override
        public void setErrorHandler(ErrorHandler arg0) {
        }

        @Override
        public void setFeature(String arg0, boolean arg1) throws SAXNotRecognizedException, SAXNotSupportedException {
        }

        @Override
        public void setProperty(String arg0, Object arg1) throws SAXNotRecognizedException, SAXNotSupportedException {
        }
    }
}