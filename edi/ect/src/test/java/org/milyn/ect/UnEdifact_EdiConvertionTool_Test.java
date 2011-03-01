/*
 * Milyn - Copyright (C) 2006 - 2010
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License (version 2.1) as published by the Free Software
 *  Foundation.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 *  See the GNU Lesser General Public License for more details:
 *  http://www.gnu.org/licenses/lgpl.txt
 */

package org.milyn.ect;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import junit.framework.TestCase;

import org.milyn.ect.formats.unedifact.UnEdifactSpecificationReader;
import org.milyn.edisax.EDIConfigurationException;
import org.milyn.edisax.model.EDIConfigDigester;
import org.milyn.edisax.model.internal.Edimap;
import org.xml.sax.SAXException;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class UnEdifact_EdiConvertionTool_Test extends TestCase {

    public void test_D08A() throws IOException {
        ZipInputStream zipInputStream = new ZipInputStream(getClass().getResourceAsStream("D08A.zip"));
        File modelSetFile = new File("./target/D08A-mapping-model.zip");

        modelSetFile.delete();

        EdiConvertionTool.fromUnEdifactSpec(zipInputStream, new ZipOutputStream(new FileOutputStream(modelSetFile)), "org.milyn.edi.unedifact:d08a:1.0-SNAPSHOT");
    }

    public void test_MILYN_475() throws IOException, EDIConfigurationException, SAXException {
        ZipInputStream zipInputStream = new ZipInputStream(getClass().getResourceAsStream("D08A.zip"));
        UnEdifactSpecificationReader specReader = new UnEdifactSpecificationReader(zipInputStream, false);
        ByteArrayOutputStream serializedMap = new ByteArrayOutputStream();

        Edimap jupreq = specReader.getMappingModel("JUPREQ");
        Writer writer = new OutputStreamWriter(serializedMap);

        jupreq.write(writer);

        EDIConfigDigester.digestConfig(new ByteArrayInputStream(serializedMap.toByteArray()));
    }

    public void test_MILYN_476() throws IOException, EDIConfigurationException, SAXException {
        ZipInputStream zipInputStream = new ZipInputStream(getClass().getResourceAsStream("d93a.zip"));
        UnEdifactSpecificationReader specReader = new UnEdifactSpecificationReader(zipInputStream, false);
        ByteArrayOutputStream serializedMap = new ByteArrayOutputStream();

        Edimap jupreq = specReader.getMappingModel("INVOIC");
        Writer writer = new OutputStreamWriter(serializedMap);

        jupreq.write(writer);

        Edimap edimap = EDIConfigDigester.digestConfig(new ByteArrayInputStream(serializedMap.toByteArray()));
    }
}
