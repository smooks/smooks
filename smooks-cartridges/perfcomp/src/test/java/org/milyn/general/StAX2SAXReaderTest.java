package org.milyn.general;

import org.junit.Test;
import static org.junit.Assert.*;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.InputStreamReader;

import org.milyn.Smooks;
import org.milyn.container.ExecutionContext;

/**
 * @author
 */
public class StAX2SAXReaderTest {

    @Test
    public void test() throws IOException, SAXException {
        StAX2SAXReader reader = new StAX2SAXReader();
        ContentHandler contentHandler = new DefaultHandler();
        Smooks smooks = new Smooks(getClass().getResourceAsStream("smooks-sax-sxc-test.xml"));
        ExecutionContext executionContext = smooks.createExecutionContext();
        reader.setContentHandler(contentHandler);
        reader.setExecutionContext(executionContext);

        reader.parse(new InputSource(getMessageReader()));
    }

    private InputStreamReader getMessageReader() {
        return new InputStreamReader(getClass().getResourceAsStream("stax-test-01.xml"));
    }
}
