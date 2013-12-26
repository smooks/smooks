/*
 * Milyn - Copyright (C) 2006 - 2010
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License (version 2.1) as published by the Free Software
 * Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU Lesser General Public License for more details:
 * http://www.gnu.org/licenses/lgpl.txt
 */

package org.milyn.flatfile.regex;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Assert;
import org.junit.Test;
import org.milyn.Smooks;
import org.milyn.io.StreamUtils;
import org.milyn.payload.JavaResult;
import org.milyn.payload.StringResult;
import org.milyn.payload.StringSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.List;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class RegexParserTest {

    @Test
    public void test_01() throws IOException, SAXException {
        testHelper("01", "a|b|c\n\rd|e|f");
    }

    @Test
    public void test_02() throws IOException, SAXException {
        testHelper("02", "a|b|c\n\rd|e|f");
    }

    @Test
    public void test_03() throws IOException, SAXException {
        testHelper("03", "a|b|c\nd|e|f");
    }

    @Test
    public void test_04() throws IOException, SAXException {
        testHelper("04", "a|b|c\nd|e|f");
    }

    @Test
    public void test_05() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("smooks-config-05.xml"));
        JavaResult result = new JavaResult();

        smooks.filterSource(new StringSource("a|b|c\n\rd|e|f"), result);

        List<FSTRecord> fstRecords = (List<FSTRecord>) result.getBean("fstRecords");

        Assert.assertEquals(2, fstRecords.size());
        Assert.assertEquals("a|b|c", fstRecords.get(0).toString());
        Assert.assertEquals("d|e|f", fstRecords.get(1).toString());
    }

    @Test
    public void test_06() throws IOException, SAXException {
        // Should result in unmatched records because the regex's do
        // not match the input...
        testHelper("06", "name|Tom|Fennelly\n\r" +
                "address|Skeagh Bridge|Tinnakill");
    }

    @Test
    public void test_07() throws IOException, SAXException {
        testHelper("07", "name|Tom|Fennelly\n\r" +
                "address|Skeagh Bridge|Tinnakill");
    }

    @Test
    public void test_08() throws IOException, SAXException {
        testHelper("08", "name|Tom|Fennelly\n\r" +
                "address|Skeagh Bridge|Tinnakill");
    }

    @Test
    public void test_09() throws IOException, SAXException {
        testHelper("09", "1|Tom|Fennelly" +
                "2|Mike|Fennelly");
    }

    @Test
    public void test_10() throws IOException, SAXException {
        testHelper("10", "10/26 03:04:21.016 A1 : EVENT=Msg_Rcvd, E_ID=1, D_ID=D1, M_ID=M1, R=23525235\n" +
                "10/26 03:04:21.032 B12 : EVENT=Msg_Sent, E_ID=2, D_ID=D1, M_ID=M1, R=34523455\n" +
                "10/26 03:04:21.040 A1 : EVENT=Msg_Rcvd, E_ID=3, D_ID=D2, M_ID=M2, R=15894578\n" +
                "10/26 03:04:22.000 A1 : EVENT=Filler\n" +
                "10/26 03:04:21.076 A30 : EVENT=Msg_Rcvd, E_ID=7, D_ID=D2, M_ID=M4, R=97847854");
    }

    public void testHelper(String config, String message) throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("smooks-config-" + config + ".xml"));
        String expected = StreamUtils.readStreamAsString(getClass().getResourceAsStream("expected-" + config + ".xml"));

        StringResult result = new StringResult();
        smooks.filterSource(new StringSource(message), result);

//        System.out.println(result);
        XMLUnit.setIgnoreWhitespace(true);
        XMLAssert.assertXMLEqual(expected, result.toString());
    }
}
