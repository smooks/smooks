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
package org.smooks.edi.unedifact;

import com.thoughtworks.xstream.XStream;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Test;
import org.smooks.FilterSettings;
import org.smooks.Smooks;
import org.smooks.container.ExecutionContext;
import org.smooks.edi.unedifact.model.r41.UNEdifactInterchange41;
import org.smooks.edi.unedifact.model.r41.UNEdifactMessage41;
import org.smooks.edisax.EDIConfigurationException;
import org.smooks.edisax.util.EDIUtils;
import org.smooks.io.StreamUtils;
import org.smooks.payload.JavaResult;
import org.smooks.payload.StringResult;
import org.xml.sax.SAXException;

import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
@SuppressWarnings("unchecked")
public class UNEdifactReaderTest {

        @Test
	public void test_DOM() throws IOException, SAXException {
		test(FilterSettings.DEFAULT_DOM);
	}

        @Test
	public void test_SAX() throws IOException, SAXException {
		test(FilterSettings.DEFAULT_SAX);
	}

	public void test(FilterSettings filterSettings) throws IOException, SAXException {
		Smooks smooks = new Smooks("/org/smooks/edi/unedifact/smooks-config-xml.xml");
		StringResult result = new StringResult();

		smooks.setFilterSettings(filterSettings);
		smooks.filterSource(new StreamSource(getClass().getResourceAsStream("unedifact-msg-01.edi")), result);
		XMLUnit.setIgnoreWhitespace( true );
        XMLAssert.assertXMLEqual(new InputStreamReader(getClass().getResourceAsStream("unedifact-msg-expected-01.xml")), new StringReader(result.toString()));
	}

    @Test
    public void test_with_empty_nodes_ignored_DOM() throws IOException, SAXException {
        test_with_empty_nodes_ignored(FilterSettings.DEFAULT_DOM);
    }

    @Test
    public void test_with_empty_nodes_ignored_SAX() throws IOException, SAXException {
        test_with_empty_nodes_ignored(FilterSettings.DEFAULT_SAX);
    }

    public void test_with_empty_nodes_ignored(FilterSettings filterSettings) throws IOException, SAXException {
        Smooks smooks = new Smooks("/org/smooks/edi/unedifact/smooks-config-xml.xml");
        StringResult result = new StringResult();

        smooks.setFilterSettings(filterSettings);
        smooks.filterSource(new StreamSource(getClass().getResourceAsStream("unedifact-msg-01-with-empty-nodes.edi")), result);

//        System.out.println(result);

        XMLUnit.setIgnoreWhitespace( true );
        XMLAssert.assertXMLEqual(new InputStreamReader(getClass().getResourceAsStream("unedifact-msg-expected-01-with-empty-nodes-ignored.xml")), new StringReader(result.toString()));
    }

    @Test
    public void test_with_empty_nodes_not_ignored_DOM() throws IOException, SAXException {
        test_with_empty_nodes_not_ignored(FilterSettings.DEFAULT_DOM);
    }

    @Test
    public void test_with_empty_nodes_not_ignored_SAX() throws IOException, SAXException {
        test_with_empty_nodes_not_ignored(FilterSettings.DEFAULT_SAX);
    }

    public void test_with_empty_nodes_not_ignored(FilterSettings filterSettings) throws IOException, SAXException {
        Smooks smooks = new Smooks("/org/smooks/edi/unedifact/smooks-config-xml-not-ignored.xml");
        StringResult result = new StringResult();

        smooks.setFilterSettings(filterSettings);
        smooks.filterSource(new StreamSource(getClass().getResourceAsStream("unedifact-msg-01-with-empty-nodes.edi")), result);

//        System.out.println(result);

        XMLUnit.setIgnoreWhitespace( true );
        XMLAssert.assertXMLEqual(new InputStreamReader(getClass().getResourceAsStream("unedifact-msg-expected-01-with-empty-nodes-not-ignored.xml")), new StringReader(result.toString()));
    }

        @Test
	public void test_zipped() throws IOException, SAXException, EDIConfigurationException {
		createZip();

		Smooks smooks = new Smooks("/org/smooks/edi/unedifact/smooks-config-zip.xml");
		StringResult result = new StringResult();

		smooks.filterSource(new StreamSource(getClass().getResourceAsStream("unedifact-msg-01.edi")), result);

		XMLUnit.setIgnoreWhitespace( true );
        XMLAssert.assertXMLEqual(new InputStreamReader(getClass().getResourceAsStream("unedifact-msg-expected-01.xml")), new StringReader(result.toString()));
	}

        @Test
	public void test_java_binding_simple_messages() throws IOException, SAXException {
		Smooks smooks = new Smooks("/org/smooks/edi/unedifact/smooks-config-jb-01.xml");
		JavaResult jResult = new JavaResult();
		StringResult sResult = new StringResult();
		ExecutionContext execCtx = smooks.createExecutionContext();

		//execCtx.setEventListener(new HtmlReportGenerator("target/report.html"));
		smooks.filterSource(execCtx, new StreamSource(getClass().getResourceAsStream("unedifact-msg-02.edi")), jResult, sResult);

		List<UNEdifactMessage41> messages = (List<UNEdifactMessage41>) jResult.getBean("unEdifactMessages");

//		System.out.println(sResult);
//		System.out.println(new XStream().toXML(messages));

		XMLUnit.setIgnoreWhitespace( true );
        XMLAssert.assertXMLEqual(new InputStreamReader(getClass().getResourceAsStream("unedifact-msg-expected-02.xml")), new StringReader(new XStream().toXML(messages)));
	}

        @Test
	public void test_java_binding_interchange_01() throws IOException, SAXException {
		Smooks smooks = new Smooks("/org/smooks/edi/unedifact/smooks-config-jb-02.xml");
		JavaResult jResult = new JavaResult();
		StringResult sResult = new StringResult();
		ExecutionContext execCtx = smooks.createExecutionContext();

		//execCtx.setEventListener(new HtmlReportGenerator("target/report.html"));
		smooks.filterSource(execCtx, new StreamSource(getClass().getResourceAsStream("unedifact-msg-02.edi")), jResult, sResult);

		UNEdifactInterchange41 interchange = jResult.getBean(UNEdifactInterchange41.class);

//		System.out.println(new XStream().toXML(interchange));

		XMLUnit.setIgnoreWhitespace( true );
        XMLAssert.assertXMLEqual(new InputStreamReader(getClass().getResourceAsStream("unedifact-msg-expected-03.xml")), new StringReader(new XStream().toXML(interchange)));
	}

    @Test
    public void test_java_binding_interchange_02() throws IOException, SAXException {
        Smooks smooks = new Smooks("/org/smooks/edi/unedifact/smooks-config-jb-02.xml");
        JavaResult jResult = new JavaResult();
        StringResult sResult = new StringResult();
        ExecutionContext execCtx = smooks.createExecutionContext();

        //execCtx.setEventListener(new HtmlReportGenerator("target/report.html"));
        smooks.filterSource(execCtx, new StreamSource(getClass().getResourceAsStream("unedifact-msg-03.edi")), jResult, sResult);

        UNEdifactInterchange41 interchange = jResult.getBean(UNEdifactInterchange41.class);

//        System.out.println(new XStream().toXML(interchange));

        XMLUnit.setIgnoreWhitespace( true );
        XMLAssert.assertXMLEqual(new InputStreamReader(getClass().getResourceAsStream("unedifact-msg-expected-04.xml")), new StringReader(new XStream().toXML(interchange)));
    }

	private void createZip() throws IOException {
		File zipFile = new File("target/mapping-models.zip");

		zipFile.delete();

		ZipOutputStream zipStream = new ZipOutputStream(new FileOutputStream(zipFile));
		try {
			addZipEntry("test/models/MSG1-model.xml", "MSG1-model.xml", zipStream);
			addZipEntry("test/models/MSG2-model.xml", "MSG2-model.xml", zipStream);
			addZipEntry("test/models/MSG3-model.xml", "MSG3-model.xml", zipStream);
			addZipEntry(EDIUtils.EDI_MAPPING_MODEL_ZIP_LIST_FILE, "mapping-models.lst", zipStream);
		} finally {
			zipStream.close();
		}
	}

	private void addZipEntry(String name, String resource, ZipOutputStream zipStream) throws IOException {
		ZipEntry zipEntry = new ZipEntry(name);
		byte[] resourceBytes = StreamUtils.readStream(getClass().getResourceAsStream(resource));

		zipStream.putNextEntry(zipEntry);
		zipStream.write(resourceBytes);
	}
}
