package org.milyn.general;

import junit.framework.TestCase;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStreamReader;

/**
 * @author
 */
public class StAX2SAXReaderTest extends TestCase {

    public void test() throws IOException, SAXException {
        StAX2SAXReader reader = new StAX2SAXReader();

        reader.parse(new InputSource(getMessageReader()));
    }

    private InputStreamReader getMessageReader() {
        return new InputStreamReader(getClass().getResourceAsStream("stax-test-01.xml"));
    }
}
