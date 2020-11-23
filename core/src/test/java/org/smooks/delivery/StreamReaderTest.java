/*-
 * ========================LICENSE_START=================================
 * Smooks Core
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
package org.smooks.delivery;


import org.junit.Before;
import org.junit.Test;
import org.smooks.Smooks;
import org.smooks.cdr.ResourceConfig;
import org.smooks.container.ExecutionContext;
import org.smooks.io.StreamUtils;
import org.smooks.xml.SmooksXMLReader;
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
public class StreamReaderTest
{
    private final byte[] bytes = "bytes".getBytes();
    private Smooks smooks;

    @Before
    public void setup()
    {
        smooks = new Smooks();
        smooks.addConfiguration(new ResourceConfig( "org.xml.sax.driver", MockReader.class.getName()));
    }

    @Test public void verifyByteStream()
    {
        smooks.filterSource(new StreamSource(new ByteArrayInputStream(bytes)));
        assertArrayEquals(bytes, MockReader.readBytes);
    }

    public static class MockReader implements SmooksXMLReader
    {
        private ContentHandler handler;
        private static byte[] readBytes;

        public void parse(final InputSource inputSource) throws IOException, SAXException
        {
            final InputStream bin = inputSource.getByteStream();

            MockReader.readBytes = StreamUtils.readStream(bin);
            handler.startDocument();
            handler.endDocument();
        }

        public void setContentHandler(ContentHandler arg0)
        {
            this.handler = arg0;
        }

        public void setExecutionContext(ExecutionContext executionContext)
        {
        }

        public ContentHandler getContentHandler()
        {
            return null;
        }

        public DTDHandler getDTDHandler()
        {
            return null;
        }

        public EntityResolver getEntityResolver()
        {
            return null;
        }

        public ErrorHandler getErrorHandler()
        {
            return null;
        }

        public boolean getFeature(String arg0) throws SAXNotRecognizedException, SAXNotSupportedException
        {
            return false;
        }

        public Object getProperty(String arg0) throws SAXNotRecognizedException, SAXNotSupportedException
        {
            return null;
        }

        public void parse(String string) throws IOException, SAXException
        {
        }

        public void setDTDHandler(DTDHandler arg0)
        {
        }

        public void setEntityResolver(EntityResolver arg0)
        {
        }

        public void setErrorHandler(ErrorHandler arg0)
        {
        }

        public void setFeature(String arg0, boolean arg1) throws SAXNotRecognizedException, SAXNotSupportedException
        {
        }

        public void setProperty(String arg0, Object arg1) throws SAXNotRecognizedException, SAXNotSupportedException
        {
        }

    }
}
