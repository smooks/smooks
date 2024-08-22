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

import org.junit.jupiter.api.Test;
import org.smooks.Smooks;
import org.smooks.api.ExecutionContext;
import org.smooks.io.sink.StreamSink;
import org.smooks.io.sink.WriterSink;
import org.smooks.io.source.ReaderSource;
import org.smooks.io.source.StreamSource;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class FilterCloseTestCase {

	@Test
    public void test_close_io() throws IOException, SAXException {
        test_close_io("dom-dont-close", 0);
        test_close_io("sax-dont-close", 0);
        test_close_io("dom-close", 1);
        test_close_io("sax-close", 1);
    }

    public void test_close_io(String profile, int expectedCloseCallCount) throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("smooks-config-close-filter-io.xml"));
        ExecutionContext execContext;

        // Test io stream close....
        // We need to +1 this because Xerces always closes input streams/readers and there's no
        // way of turning that off
        TestInputStream inStream = new TestInputStream("<x/>".getBytes(), expectedCloseCallCount + 1);
        TestOutputStream outStream = new TestOutputStream(expectedCloseCallCount);
        execContext = smooks.createExecutionContext(profile);
        smooks.filterSource(execContext, new StreamSource(inStream), new StreamSink(outStream));

        // Test io reader/writer close...
        // We need to +1 this because Xerces always closes input streams/readers and there's no 
        // way of turning that off
        TestReader reader = new TestReader("<x/>", expectedCloseCallCount + 1);
        TestWriter writer = new TestWriter(expectedCloseCallCount);
        execContext = smooks.createExecutionContext(profile);
        smooks.filterSource(execContext, new ReaderSource(reader), new WriterSink(writer));
    }

    private static class TestInputStream extends ByteArrayInputStream {
        private int closeCallCount;
        private final int expectedCloseCallCount;

        public TestInputStream(byte[] buf, int expectedCloseCallCount) {
            super(buf);
            this.expectedCloseCallCount = expectedCloseCallCount;
        }

        @Override
        public void close() throws IOException {
            closeCallCount++;
            super.close();
            if(closeCallCount > expectedCloseCallCount) {
                fail("'close' method called an unexpected number of times. Expected to be called " + expectedCloseCallCount + " times. Current call count: " + closeCallCount);
            }
        }
    }

    private static class TestOutputStream extends ByteArrayOutputStream {
        private int closeCallCount;
        private final int expectedCloseCallCount;
        public TestOutputStream(int expectedCloseCallCount) {
            this.expectedCloseCallCount = expectedCloseCallCount;
        }

        @Override
        public void close() throws IOException {
            closeCallCount++;
            super.close();
            if(closeCallCount > expectedCloseCallCount) {
                fail("'close' method called an unexpected number of times. Expected to be called " + expectedCloseCallCount + " times. Current call count: " + closeCallCount);
            }
        }
    }

    private static class TestReader extends StringReader {
        private int closeCallCount;
        private final int expectedCloseCallCount;
        public TestReader(String s, int expectedCloseCallCount) {
            super(s);
            this.expectedCloseCallCount = expectedCloseCallCount;
        }

        @Override
        public void close() {
            closeCallCount++;
            super.close();
            if(closeCallCount > expectedCloseCallCount) {
                fail("'close' method called an unexpected number of times. Expected to be called " + expectedCloseCallCount + " times. Current call count: " + closeCallCount);
            }
        }
    }

    private static class TestWriter extends StringWriter {
        private int closeCallCount;
        private final int expectedCloseCallCount;

        public TestWriter(int expectedCloseCallCount) {
            this.expectedCloseCallCount = expectedCloseCallCount;
        }

        @Override
        public void close() throws IOException {
            closeCallCount++;
            super.close();
            if(closeCallCount > expectedCloseCallCount) {
                fail("'close' method called an unexpected number of times. Expected to be called " + expectedCloseCallCount + " times. Current call count: " + closeCallCount);
            }
        }
    }
}
