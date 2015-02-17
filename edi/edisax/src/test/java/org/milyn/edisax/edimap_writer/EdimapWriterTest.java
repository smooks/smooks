package org.milyn.edisax.edimap_writer;

import org.junit.Test;
import static org.junit.Assert.*;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.milyn.edisax.model.EDIConfigDigester;
import org.milyn.edisax.model.internal.Edimap;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class EdimapWriterTest {

    @Test
    public void test() throws IOException, SAXException {
        test("edimap-01.xml");
    }

    public void test(String edimapfile) throws IOException, SAXException {
        Edimap edimap = EDIConfigDigester.digestConfig(getClass().getResourceAsStream(edimapfile));
        StringWriter result = new StringWriter();

        edimap.write(result);

        System.out.println(result);

        XMLUnit.setIgnoreWhitespace( true );
        XMLAssert.assertXMLEqual(new InputStreamReader(getClass().getResourceAsStream(edimapfile)), new StringReader(result.toString()));
    }
}
