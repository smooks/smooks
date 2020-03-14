/*
 * Milyn - Copyright (C) 2006 - 2010
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License (version 2.1) as published
 * by the Free Software Foundation.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.
 *
 * See the GNU Lesser General Public License for more details:
 * http://www.gnu.org/licenses/lgpl.txt
 */

package org.smooks.delivery;


import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import javax.xml.transform.stream.StreamSource;

import org.junit.Before;
import org.junit.Test;
import org.smooks.Smooks;
import org.smooks.io.StreamUtils;
import org.smooks.cdr.SmooksResourceConfiguration;
import org.smooks.container.ExecutionContext;
import org.smooks.xml.SmooksXMLReader;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

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
        smooks.addConfiguration(new SmooksResourceConfiguration( "org.xml.sax.driver", MockReader.class.getName()));
    }

    @Test public void verifyByteStream()
    {
        smooks.filterSource(new StreamSource(new ByteArrayInputStream(bytes)));
        assertTrue(Arrays.equals(bytes, MockReader.readBytes));
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
