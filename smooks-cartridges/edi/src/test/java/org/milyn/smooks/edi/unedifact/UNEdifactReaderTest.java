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
package org.milyn.smooks.edi.unedifact;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.xml.transform.stream.StreamSource;

import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.milyn.FilterSettings;
import org.milyn.Smooks;
import org.milyn.container.ExecutionContext;
import org.milyn.edisax.EDIConfigurationException;
import org.milyn.edisax.util.EDIUtils;
import org.milyn.commons.io.StreamUtils;
import org.milyn.payload.JavaResult;
import org.milyn.payload.StringResult;
import org.milyn.smooks.edi.unedifact.model.r41.UNEdifactInterchange41;
import org.milyn.smooks.edi.unedifact.model.r41.UNEdifactMessage41;
import org.xml.sax.SAXException;

import com.thoughtworks.xstream.XStream;

import junit.framework.TestCase;

/**
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class UNEdifactReaderTest extends TestCase {

	public void test_DOM() throws IOException, SAXException {
		test(FilterSettings.DEFAULT_DOM);
	}

	public void test_SAX() throws IOException, SAXException {
		test(FilterSettings.DEFAULT_SAX);
	}
	
	public void test(FilterSettings filterSettings) throws IOException, SAXException {
		Smooks smooks = new Smooks("/org/milyn/smooks/edi/unedifact/smooks-config-xml.xml");
		StringResult result = new StringResult();
		
		smooks.setFilterSettings(filterSettings);
		smooks.filterSource(new StreamSource(getClass().getResourceAsStream("unedifact-msg-01.edi")), result);
		XMLUnit.setIgnoreWhitespace( true );
        XMLAssert.assertXMLEqual(new InputStreamReader(getClass().getResourceAsStream("unedifact-msg-expected-01.xml")), new StringReader(result.toString()));		
	}

    public void test_with_empty_nodes_ignored_DOM() throws IOException, SAXException {
        test_with_empty_nodes_ignored(FilterSettings.DEFAULT_DOM);
    }

    public void test_with_empty_nodes_ignored_SAX() throws IOException, SAXException {
        test_with_empty_nodes_ignored(FilterSettings.DEFAULT_SAX);
    }

    public void test_with_empty_nodes_ignored(FilterSettings filterSettings) throws IOException, SAXException {
        Smooks smooks = new Smooks("/org/milyn/smooks/edi/unedifact/smooks-config-xml.xml");
        StringResult result = new StringResult();

        smooks.setFilterSettings(filterSettings);
        smooks.filterSource(new StreamSource(getClass().getResourceAsStream("unedifact-msg-01-with-empty-nodes.edi")), result);

//        System.out.println(result);

        XMLUnit.setIgnoreWhitespace( true );
        XMLAssert.assertXMLEqual(new InputStreamReader(getClass().getResourceAsStream("unedifact-msg-expected-01-with-empty-nodes-ignored.xml")), new StringReader(result.toString()));
    }

    public void test_with_empty_nodes_not_ignored_DOM() throws IOException, SAXException {
        test_with_empty_nodes_not_ignored(FilterSettings.DEFAULT_DOM);
    }

    public void test_with_empty_nodes_not_ignored_SAX() throws IOException, SAXException {
        test_with_empty_nodes_not_ignored(FilterSettings.DEFAULT_SAX);
    }

    public void test_with_empty_nodes_not_ignored(FilterSettings filterSettings) throws IOException, SAXException {
        Smooks smooks = new Smooks("/org/milyn/smooks/edi/unedifact/smooks-config-xml-not-ignored.xml");
        StringResult result = new StringResult();

        smooks.setFilterSettings(filterSettings);
        smooks.filterSource(new StreamSource(getClass().getResourceAsStream("unedifact-msg-01-with-empty-nodes.edi")), result);

//        System.out.println(result);

        XMLUnit.setIgnoreWhitespace( true );
        XMLAssert.assertXMLEqual(new InputStreamReader(getClass().getResourceAsStream("unedifact-msg-expected-01-with-empty-nodes-not-ignored.xml")), new StringReader(result.toString()));
    }

	public void test_zipped() throws IOException, SAXException, EDIConfigurationException {
		createZip();
		
		Smooks smooks = new Smooks("/org/milyn/smooks/edi/unedifact/smooks-config-zip.xml");
		StringResult result = new StringResult();
		
		smooks.filterSource(new StreamSource(getClass().getResourceAsStream("unedifact-msg-01.edi")), result);

		XMLUnit.setIgnoreWhitespace( true );
        XMLAssert.assertXMLEqual(new InputStreamReader(getClass().getResourceAsStream("unedifact-msg-expected-01.xml")), new StringReader(result.toString()));		
	}
	
	public void test_java_binding_simple_messages() throws IOException, SAXException {
		Smooks smooks = new Smooks("/org/milyn/smooks/edi/unedifact/smooks-config-jb-01.xml");
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
	
	public void test_java_binding_interchange_01() throws IOException, SAXException {
		Smooks smooks = new Smooks("/org/milyn/smooks/edi/unedifact/smooks-config-jb-02.xml");
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

    public void test_java_binding_interchange_02() throws IOException, SAXException {
        Smooks smooks = new Smooks("/org/milyn/smooks/edi/unedifact/smooks-config-jb-02.xml");
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
