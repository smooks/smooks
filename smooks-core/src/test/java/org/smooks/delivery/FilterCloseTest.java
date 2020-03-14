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
package org.smooks.delivery;

import org.junit.Test;
import static org.junit.Assert.*;
import org.smooks.Smooks;
import org.smooks.container.ExecutionContext;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.*;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class FilterCloseTest {

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
        smooks.filterSource(execContext, new StreamSource(inStream), new StreamResult(outStream));

        // Test io reader/writer close...
        // We need to +1 this because Xerces always closes input streams/readers and there's no 
        // way of turning that off
        TestReader reader = new TestReader("<x/>", expectedCloseCallCount + 1);
        TestWriter writer = new TestWriter(expectedCloseCallCount);
        execContext = smooks.createExecutionContext(profile);
        smooks.filterSource(execContext, new StreamSource(reader), new StreamResult(writer));
    }

    private class TestInputStream extends ByteArrayInputStream {
        private int closeCallCount = 0;
        private int expectedCloseCallCount;

        public TestInputStream(byte[] buf, int expectedCloseCallCount) {
            super(buf);
            this.expectedCloseCallCount = expectedCloseCallCount;
        }
        public void close() throws IOException {
            closeCallCount++;
            super.close();
            if(closeCallCount > expectedCloseCallCount) {
                fail("'close' method called an unexpected number of times. Expected to be called " + expectedCloseCallCount + " times. Current call count: " + closeCallCount);
            }
        }
    }

    private class TestOutputStream extends ByteArrayOutputStream {
        private int closeCallCount = 0;
        private int expectedCloseCallCount;
        public TestOutputStream(int expectedCloseCallCount) {
            this.expectedCloseCallCount = expectedCloseCallCount;
        }
        public void close() throws IOException {
            closeCallCount++;
            super.close();
            if(closeCallCount > expectedCloseCallCount) {
                fail("'close' method called an unexpected number of times. Expected to be called " + expectedCloseCallCount + " times. Current call count: " + closeCallCount);
            }
        }
    }

    private class TestReader extends StringReader {
        private int closeCallCount = 0;
        private int expectedCloseCallCount;
        public TestReader(String s, int expectedCloseCallCount) {
            super(s);
            this.expectedCloseCallCount = expectedCloseCallCount;
        }
        public void close() {
            closeCallCount++;
            super.close();
            if(closeCallCount > expectedCloseCallCount) {
                fail("'close' method called an unexpected number of times. Expected to be called " + expectedCloseCallCount + " times. Current call count: " + closeCallCount);
            }
        }
    }

    private class TestWriter extends StringWriter {
        private int closeCallCount = 0;
        private int expectedCloseCallCount;

        public TestWriter(int expectedCloseCallCount) {
            this.expectedCloseCallCount = expectedCloseCallCount;
        }
        public void close() throws IOException {
            closeCallCount++;
            super.close();
            if(closeCallCount > expectedCloseCallCount) {
                fail("'close' method called an unexpected number of times. Expected to be called " + expectedCloseCallCount + " times. Current call count: " + closeCallCount);
            }
        }
    }
}
