/*-
 * ========================LICENSE_START=================================
 * Commons
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

package org.smooks.support;

import org.junit.jupiter.api.Test;
import org.smooks.xml.LocalDTDEntityResolver;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * XmlUtilTest
 * <p>
 * Relations: XmlUtil extends java.lang.Object <br>
 *
 * @author Tom Fennelly
 * @see XmlUtils
 */

public class XmlUtilTestCase {


    /**
     * Test method: void removeEntities(Reader, Writer) removeEntities throws
     * java.io.IOException
     */
    @Test
    public void testRemoveEntities_1() {
        try {
            XmlUtils.removeEntities(null, null);
            fail("no IllegalArgumentException on null reader and null writer");
        } catch (IllegalArgumentException excep) {
            // OK
        } catch (Exception excep) {
            fail("Unhandled exception: " + excep.getMessage());
        }
    }

    /**
     * Test method: void removeEntities(Reader, Writer) removeEntities throws
     * java.io.IOException
     */
    @Test
    public void testRemoveEntities_2() {
        try {
            Reader reader = new StringReader("abcdefg");
            XmlUtils.removeEntities(reader, null);
            fail("no IllegalArgumentException on null writer");
        } catch (IllegalArgumentException excep) {
            // OK
        } catch (Exception excep) {
            fail("Unhandled exception: " + excep.getMessage());
        }
    }

    /**
     * Test method: void removeEntities(Reader, Writer) removeEntities throws
     * java.io.IOException
     */
    @Test
    public void testRemoveEntities_3() {
        try {
            StringReader reader = new StringReader("abcdefg");
            StringWriter writer = new StringWriter();

            XmlUtils.removeEntities(reader, writer);
            assertEquals("abcdefg", writer.toString());
        } catch (Exception excep) {
            fail("Unhandled exception: " + excep.getMessage());
        }
    }

    /**
     * Test method: void removeEntities(Reader, Writer) removeEntities throws
     * java.io.IOException
     */
    @Test
    public void testRemoveEntities_4() {
        try {
            StringReader reader = new StringReader("a&bcdefg");
            StringWriter writer = new StringWriter();

            XmlUtils.removeEntities(reader, writer);
            assertEquals("a&bcdefg", writer.toString());
        } catch (Exception excep) {
            fail("Unhandled exception: " + excep.getMessage());
        }
    }

    /**
     * Test method: void removeEntities(Reader, Writer) removeEntities throws
     * java.io.IOException
     */
    @Test
    public void testRemoveEntities_5() {
        try {
            StringReader reader = new StringReader("a;bcdefg");
            StringWriter writer = new StringWriter();

            XmlUtils.removeEntities(reader, writer);
            assertEquals("a;bcdefg", writer.toString());
        } catch (Exception excep) {
            fail("Unhandled exception: " + excep.getMessage());
        }
    }

    /**
     * Test method: void removeEntities(Reader, Writer) removeEntities throws
     * java.io.IOException
     */
    @Test
    public void testRemoveEntities_6() {
        try {
            StringReader reader = new StringReader("&abcdefg;");
            StringWriter writer = new StringWriter();

            XmlUtils.removeEntities(reader, writer);
            assertEquals("&abcdefg;", writer.toString());
        } catch (Exception excep) {
            fail("Unhandled exception: " + excep.getMessage());
        }
    }

    /**
     * Test method: void removeEntities(Reader, Writer) removeEntities throws
     * java.io.IOException
     */
    @Test
    public void testRemoveEntities_7() {
        try {
            StringReader reader = new StringReader("&&abcdefg");
            StringWriter writer = new StringWriter();

            XmlUtils.removeEntities(reader, writer);
            assertEquals("&&abcdefg", writer.toString());
        } catch (Exception excep) {
            fail("Unhandled exception: " + excep.getMessage());
        }
    }

    /**
     * Test method: void removeEntities(Reader, Writer) removeEntities throws
     * java.io.IOException
     */
    @Test
    public void testRemoveEntities_8() {
        try {
            StringReader reader = new StringReader("&;abcdefg;");
            StringWriter writer = new StringWriter();

            XmlUtils.removeEntities(reader, writer);
            assertEquals("&;abcdefg;", writer.toString());
        } catch (Exception excep) {
            fail("Unhandled exception: " + excep.getMessage());
        }
    }

    /**
     * Test method: void removeEntities(Reader, Writer) removeEntities throws
     * java.io.IOException
     */
    @Test
    public void testRemoveEntities_9() {
        try {
            StringReader reader = new StringReader("&amp;bcdefg");
            StringWriter writer = new StringWriter();

            XmlUtils.removeEntities(reader, writer);
            assertEquals("&bcdefg", writer.toString());
        } catch (Exception excep) {
            fail("Unhandled exception: " + excep.getMessage());
        }
    }

    /**
     * Test method: void removeEntities(Reader, Writer) removeEntities throws
     * java.io.IOException
     */
    @Test
    public void testRemoveEntities_10() {
        try {
            StringReader reader = new StringReader("&amp;amp;");
            StringWriter writer = new StringWriter();

            XmlUtils.removeEntities(reader, writer);
            assertEquals("&amp;", writer.toString());
        } catch (Exception excep) {
            fail("Unhandled exception: " + excep.getMessage());
        }
    }

    /**
     * Test method: void removeEntities(Reader, Writer) removeEntities throws
     * java.io.IOException
     */
    @Test
    public void testRemoveEntities_11() {
        try {
            StringReader reader = new StringReader("&amp;&;");
            StringWriter writer = new StringWriter();

            XmlUtils.removeEntities(reader, writer);
            assertEquals("&&;", writer.toString());
        } catch (Exception excep) {
            fail("Unhandled exception: " + excep.getMessage());
        }
    }

    /**
     * Test method: void removeEntities(Reader, Writer) removeEntities throws
     * java.io.IOException
     */
    @Test
    public void testRemoveEntities_12() {
        try {
            StringReader reader = new StringReader("&");
            StringWriter writer = new StringWriter();

            XmlUtils.removeEntities(reader, writer);
            assertEquals("&", writer.toString());
        } catch (Exception excep) {
            fail("Unhandled exception: " + excep.getMessage());
        }
    }

    /**
     * Test method: void removeEntities(Reader, Writer) removeEntities throws
     * java.io.IOException
     */
    @Test
    public void testRemoveEntities_13() {
        try {
            StringReader reader = new StringReader("&;");
            StringWriter writer = new StringWriter();

            XmlUtils.removeEntities(reader, writer);
            assertEquals("&;", writer.toString());
        } catch (Exception excep) {
            fail("Unhandled exception: " + excep.getMessage());
        }
    }

    /**
     * Test method: void removeEntities(Reader, Writer) removeEntities throws
     * java.io.IOException
     */
    @Test
    public void testRemoveEntities_14() {
        try {
            StringReader reader = new StringReader("");
            StringWriter writer = new StringWriter();

            XmlUtils.removeEntities(reader, writer);
            assertEquals("", writer.toString());
        } catch (Exception excep) {
            fail("Unhandled exception: " + excep.getMessage());
        }
    }

    /**
     * Test method: void removeEntities(Reader, Writer) removeEntities throws
     * java.io.IOException
     */
    @Test
    public void testRemoveEntities_15() {
        try {
            StringReader reader = new StringReader("abc&#;");
            StringWriter writer = new StringWriter();

            XmlUtils.removeEntities(reader, writer);
            assertEquals("abc&#;", writer.toString());
        } catch (Exception excep) {
            fail("Unhandled exception: " + excep.getMessage());
        }
    }

    /**
     * Test method: void removeEntities(Reader, Writer) removeEntities throws
     * java.io.IOException
     */
    @Test
    public void testRemoveEntities_16() {
        try {
            StringReader reader = new StringReader("&#;");
            StringWriter writer = new StringWriter();

            XmlUtils.removeEntities(reader, writer);
            assertEquals("&#;", writer.toString());
        } catch (Exception excep) {
            fail("Unhandled exception: " + excep.getMessage());
        }
    }

    /**
     * Test method: void removeEntities(Reader, Writer) removeEntities throws
     * java.io.IOException
     */
    @Test
    public void testRemoveEntities_17() {
        try {
            StringReader reader = new StringReader("abc&#");
            StringWriter writer = new StringWriter();

            XmlUtils.removeEntities(reader, writer);
            assertEquals("abc&#", writer.toString());
        } catch (Exception excep) {
            fail("Unhandled exception: " + excep.getMessage());
        }
    }

    /**
     * Test method: void removeEntities(Reader, Writer) removeEntities throws
     * java.io.IOException
     */
    @Test
    public void testRemoveEntities_18() {
        try {
            StringReader reader = new StringReader("&#");
            StringWriter writer = new StringWriter();

            XmlUtils.removeEntities(reader, writer);
            assertEquals("&#", writer.toString());
        } catch (Exception excep) {
            fail("Unhandled exception: " + excep.getMessage());
        }
    }

    /**
     * Test method: void removeEntities(Reader, Writer) removeEntities throws
     * java.io.IOException
     */
    @Test
    public void testRemoveEntities_19() {
        try {
            StringReader reader = new StringReader("&#x");
            StringWriter writer = new StringWriter();

            XmlUtils.removeEntities(reader, writer);
            assertEquals("&#x", writer.toString());
        } catch (Exception excep) {
            fail("Unhandled exception: " + excep.getMessage());
        }
    }

    /**
     * Test method: void removeEntities(Reader, Writer) removeEntities throws
     * java.io.IOException
     */
    @Test
    public void testRemoveEntities_20() {
        try {
            StringReader reader = new StringReader("&#x;");
            StringWriter writer = new StringWriter();

            XmlUtils.removeEntities(reader, writer);
            assertEquals("&#x;", writer.toString());
        } catch (Exception excep) {
            fail("Unhandled exception: " + excep.getMessage());
        }
    }

    /**
     * Test method: void removeEntities(Reader, Writer) removeEntities throws
     * java.io.IOException
     */
    @Test
    public void testRemoveEntities_21() {
        try {
            StringReader reader = new StringReader("&#60;");
            StringWriter writer = new StringWriter();

            XmlUtils.removeEntities(reader, writer);
            assertEquals("<", writer.toString());
        } catch (Exception excep) {
            fail("Unhandled exception: " + excep.getMessage());
        }
    }

    /**
     * Test method: void removeEntities(Reader, Writer) removeEntities throws
     * java.io.IOException
     */
    @Test
    public void testRemoveEntities_22() {
        try {
            StringReader reader = new StringReader("&#x3C;");
            StringWriter writer = new StringWriter();

            XmlUtils.removeEntities(reader, writer);
            assertEquals("<", writer.toString());
        } catch (Exception excep) {
            fail("Unhandled exception: " + excep.getMessage());
        }
    }

    /**
     * Test method: void removeEntities(Reader, Writer) removeEntities throws
     * java.io.IOException
     */
    @Test
    public void testRemoveEntities_23() {
        try {
            StringReader reader = new StringReader("a&#x3C;");
            StringWriter writer = new StringWriter();

            XmlUtils.removeEntities(reader, writer);
            assertEquals("a<", writer.toString());
        } catch (Exception excep) {
            fail("Unhandled exception: " + excep.getMessage());
        }
    }

    /**
     * Test method: void removeEntities(Reader, Writer) removeEntities throws
     * java.io.IOException
     */
    @Test
    public void testRemoveEntities_24() {
        try {
            StringReader reader = new StringReader("a&#x3C;a");
            StringWriter writer = new StringWriter();

            XmlUtils.removeEntities(reader, writer);
            assertEquals("a<a", writer.toString());
        } catch (Exception excep) {
            fail("Unhandled exception: " + excep.getMessage());
        }
    }

    /**
     * Test method: void removeEntities(Reader, Writer) removeEntities throws
     * java.io.IOException
     */
    @Test
    public void testRemoveEntities_25() {
        try {
            StringReader reader = new StringReader("a&#X3C;a");
            StringWriter writer = new StringWriter();

            XmlUtils.removeEntities(reader, writer);
            assertEquals("a<a", writer.toString());
        } catch (Exception excep) {
            fail("Unhandled exception: " + excep.getMessage());
        }
    }

    /**
     * Test method: void removeEntities(Reader, Writer) removeEntities throws
     * java.io.IOException
     */
    @Test
    public void testRemoveEntities_26() {
        try {
            assertEquals("a<a & & &g", XmlUtils
                    .removeEntities("a&#X3C;a & &amp; &g"));
        } catch (Exception excep) {
            fail("Unhandled exception: " + excep.getMessage());
        }
    }

    /**
     * Test method: void removeEntities(Reader, Writer) removeEntities throws
     * java.io.IOException
     */
    @Test
    public void testRewriteEntities_01() {
        try {
            StringReader reader = new StringReader(
                    "a&yen;a  &#171; hh & ; jgha& &amp; gaa &j");
            StringWriter writer = new StringWriter();

            XmlUtils.rewriteEntities(reader, writer);
            assertEquals("a&#165;a  &#171; hh & ; jgha& &#38; gaa &j", writer
                    .toString());
        } catch (Exception excep) {
            fail("Unhandled exception: " + excep.getMessage());
        }
    }

    /**
     * This method is required because the tests can be run from either ANT or
     * from within the IDE.
     *
     * @return The DTD folder File.
     */
    private File getDTDfolder() {
        // The DTDs are located in milyn/smooks/dtd
        try {
            File dtdFolder = new File("src/test/dtd");

            if (!dtdFolder.exists()) {
                fail("Can't run test - working dir must be 'commons' dir.");
            } else {
                return dtdFolder;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Test
    public void testParseStream() {
        try {
            Document doc = XmlUtils.parseStream(getClass().getResourceAsStream(
                            "/a.adf"), new LocalDTDEntityResolver(getDTDfolder()), XmlUtils.VALIDATION_TYPE.DTD,
                    true);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        try {
            Document doc = XmlUtils.parseStream(getClass().getResourceAsStream(
                            "/b.adf"), new LocalDTDEntityResolver(getDTDfolder()), XmlUtils.VALIDATION_TYPE.DTD,
                    true);
            fail("No failure for invalid document.");
        } catch (SAXException e) {
            // OK
        } catch (IOException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    @Test
    public void testParseStreamGivenXmlEntityAttack() throws IOException, SAXException, ParserConfigurationException {
        try {
            XmlUtils.parseStream(getClass().getResourceAsStream("/xxe.xml"));
        } catch (SAXParseException e) {
            assertEquals("DOCTYPE is disallowed when the feature \"http://apache.org/xml/features/disallow-doctype-decl\" set to true.", e.getMessage());
        }
    }

    @Test
    public void testParseStreamGivenXIncludeAttack() throws IOException, SAXException, ParserConfigurationException {
        XmlUtils.parseStream(getClass().getResourceAsStream("/xinclude.xml"));
    }

    private Document getXPathDocument() {
        try {
            ByteArrayInputStream stream = new ByteArrayInputStream(
                    "<x><y attrib='attribval'/><z>zval</z></x> ".getBytes());

            return XmlUtils.parseStream(stream, new LocalDTDEntityResolver(
                    getDTDfolder()), XmlUtils.VALIDATION_TYPE.NONE, true);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
        return null;
    }

    @Test
    public void testGetString() {
        Document doc = getXPathDocument();

        assertEquals("attribval", XmlUtils.getString(doc, "/x/y/@attrib"));
        assertEquals("zval", XmlUtils.getString(doc, "/x/z/text()"));
        assertEquals("y", XmlUtils.getString(doc, "/x/*[1]/name()"));
        assertEquals("z", XmlUtils.getString(doc, "/x/*[2]/name()"));
        assertEquals("<x><y attrib=\"attribval\"></y><z>zval</z></x>", XmlUtils.getString(doc, "/x"));
    }

    @Test
    public void test_indent_01() throws IOException {
        test_indent("xml1.xml");
    }

    @Test
    public void test_indent_02() throws IOException {
        test_indent("xml2.xml");
    }

    @Test
    public void test_indent_03() throws IOException {
        test_indent("xml3.xml");
    }

    @Test
    public void test_indent_04() throws IOException {
        test_indent("xml4.xml");
    }

    @Test
    public void test_indent_05() throws IOException {
        test_indent("xml5.xml");
    }

    @Test
    public void test_indent_06() throws IOException {
        test_indent("xml6.xml");
    }

    public void test_indent(String inXml) throws IOException {
        String xmlString = StreamUtils.readStreamAsString(getClass().getResourceAsStream(inXml), "UTF-8");
        String indentedXML = XmlUtils.indent(xmlString, 4);

        String indentedXmlExpected = StreamUtils.readStreamAsString(getClass().getResourceAsStream(inXml + ".outexpected"), "UTF-8");

        assertEquals(normalizeLines(indentedXmlExpected, false), normalizeLines(indentedXML, false));
    }

    /**
     * Read the lines lines of characters from the supplied string, trim each line (optional)
     * and add a single newline character.
     *
     * @param string The String.
     * @param trim   Trim each line i.e. to ignore leading and trailing whitespace.
     * @return String containing the line trimmed stream.
     * @throws IOException
     */
    private String normalizeLines(String string, boolean trim) throws IOException {
        return normalizeLines(new StringReader(string), trim);
    }

    /**
     * Read the lines lines of characters from the stream, trim each line (optional)
     * and add a single newline character.
     *
     * @param charStream Character stream.
     * @param trim       Trim each line i.e. to ignore leading and trailing whitespace.
     * @return String containing the line trimmed stream.
     * @throws IOException
     */
    private String normalizeLines(Reader charStream, boolean trim) throws IOException {
        StringBuffer stringBuf = new StringBuffer();
        BufferedReader reader = new BufferedReader(charStream);
        String line;

        while ((line = reader.readLine()) != null) {
            if (trim) {
                stringBuf.append(line.trim());
            } else {
                stringBuf.append(line);
            }
            stringBuf.append('\n');
        }

        return stringBuf.toString();
    }
}
