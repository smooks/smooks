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
package org.milyn.edi.test;

import org.apache.xerces.jaxp.validation.XMLSchemaFactory;
import org.custommonkey.xmlunit.XMLUnit;
import org.milyn.Smooks;
import org.milyn.archive.Archive;
import org.milyn.archive.ArchiveClassLoader;
import org.milyn.assertion.AssertArgument;
import org.milyn.classpath.CascadingClassLoaderSet;
import org.milyn.ect.EdiConvertionTool;
import org.milyn.edisax.util.IllegalNameException;
import org.milyn.ejc.EJCExecutor;
import org.milyn.io.StreamUtils;
import org.milyn.payload.StringResult;
import org.milyn.payload.StringSource;
import org.milyn.xml.EclipseFragmentXMLValidator;
import org.milyn.smooks.edi.unedifact.model.UNEdifactInterchange;
import org.milyn.smooks.edi.unedifact.model.UNEdifactInterchangeFactory;
import org.milyn.test.ant.AntRunner;
import org.milyn.util.CollectionsUtil;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.zip.ZipInputStream;

/**
 * EDIFACT Directory (.zip Specification file) test harness.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class EdifactDirTestHarness implements UNEdifactInterchangeFactory {

    private String urn;
    private Archive mappingModel;
    private Archive bindingModel;
    private ArchiveClassLoader mappingModelClassLoader;
    private ArchiveClassLoader bindingModelClassLoader;
    private UNEdifactInterchangeFactory factory;

    public EdifactDirTestHarness(File edifactSpecFile, String... messages) {
        AssertArgument.isNotNull(edifactSpecFile, "edifactSpecFile");
        if(!edifactSpecFile.exists()) {
            throw new IllegalArgumentException("Unable to locate EDI Spec file '" + edifactSpecFile.getAbsolutePath() + "'.");
        }
        if(!edifactSpecFile.isFile()) {
            throw new IllegalArgumentException("Specified EDI Spec file '" + edifactSpecFile.getAbsolutePath() + "' is not a file.");
        }

        String zipFileName = edifactSpecFile.getName();

        urn = "org.milyn.edi.unedifact:" + zipFileName.substring(0, zipFileName.indexOf('.')) + "-mapping:1.0";
        try {
            mappingModel = EdiConvertionTool.fromUnEdifactSpec(edifactSpecFile, urn, messages);
            mappingModelClassLoader = new ArchiveClassLoader(mappingModel);
            bindingModel = buildBindingModel(urn, messages);
            bindingModelClassLoader = new ArchiveClassLoader(mappingModelClassLoader, bindingModel);
        } catch (Exception e) {
            throw new IllegalStateException("Error loading specification '" + edifactSpecFile.getAbsolutePath() + "'.", e);
        }
    }

    public String getUrn() {
        return urn;
    }

    public Archive getMappingModel() {
        return mappingModel;
    }

    public Archive getBindingModel() {
        return bindingModel;
    }

    public ArchiveClassLoader getMappingModelClassLoader() {
        return mappingModelClassLoader;
    }

    public ArchiveClassLoader getBindingModelClassLoader() {
        return bindingModelClassLoader;
    }

    public UNEdifactInterchange fromUNEdifact(InputStream ediStream, Result... additionalResults) throws IOException {
        ClassLoader origTCCL = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(bindingModelClassLoader);
        try {
            return getFactory().fromUNEdifact(ediStream, additionalResults);
        } catch (Exception e) {
            throw new IllegalStateException("Error processing EDIFACT stream for '" + urn + "'.", e);
        } finally {
            Thread.currentThread().setContextClassLoader(origTCCL);
        }
    }

    public UNEdifactInterchange fromUNEdifact(InputSource ediSource, Result... additionalResults) throws IOException {
        ClassLoader origTCCL = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(bindingModelClassLoader);
        try {
            return getFactory().fromUNEdifact(ediSource, additionalResults);
        } catch (Exception e) {
            throw new IllegalStateException("Error processing EDIFACT stream for '" + urn + "'.", e);
        } finally {
            Thread.currentThread().setContextClassLoader(origTCCL);
        }
    }

    public void toUNEdifact(UNEdifactInterchange interchange, Writer writer) throws IOException {
        ClassLoader origTCCL = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(bindingModelClassLoader);
        try {
            getFactory().toUNEdifact(interchange, writer);
        } catch (Exception e) {
            throw new IllegalStateException("Error writing EDIFACT stream for '" + urn + "'.", e);
        } finally {
            Thread.currentThread().setContextClassLoader(origTCCL);
        }
    }

    public void setReportPath(String reportPath) {
        getFactory().setReportPath(reportPath);
    }

    public UNEdifactInterchangeFactory getFactory() {
        if (factory == null) {
            ClassLoader origTCCL = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(bindingModelClassLoader);
            try {
                factory = (UNEdifactInterchangeFactory) bindingModelClassLoader.loadClass(EJCTestUtil.ORG_SMOOKS_EJC_TEST + ".TESTInterchangeFactory").getMethod("getInstance").invoke(null);
            } catch (Exception e) {
                throw new IllegalStateException("Error loading Factory class for '" + urn + "'.", e);
            } finally {
                Thread.currentThread().setContextClassLoader(origTCCL);
            }
        }

        return factory;
    }

    /**
     * Test that the supplied EDIDFACT input stream can be read and bound into
     * a {@link UNEdifactInterchange} instance and then serialized back to
     * EDIFACT, without altering the EDIFACT message.
     *
     * @param edifactStream The EDIFACT input stream.
     * @throws IOException Error reading/writing.
     */
    public void assertJavaReadWriteOK(InputStream edifactStream) throws IOException {
        assertJavaReadWriteOK(new InputStreamReader(edifactStream, "UTF-8"));
    }

    /**
     * Test that the supplied EDIDFACT input stream can be read and bound into
     * a {@link UNEdifactInterchange} instance and then serialized back to
     * EDIFACT, without altering the EDIFACT message.
     *
     * @param edifactStream The EDIFACT input stream.
     * @throws IOException Error reading/writing.
     */
    public void assertJavaReadWriteOK(Reader edifactStream) throws IOException {
        String inputMessage = StreamUtils.readStream(edifactStream);
        InputSource ediSource = new InputSource();

        ediSource.setByteStream(new ByteArrayInputStream(inputMessage.getBytes()));
        ediSource.setCharacterStream(new StringReader(inputMessage));

        UNEdifactInterchange interchange = fromUNEdifact(ediSource);
        StringWriter writer = new StringWriter();
        interchange.write(writer);

        String normalizedInput = StreamUtils.normalizeLines(inputMessage, true);
        String normalizedOutput = StreamUtils.normalizeLines(writer.toString(), true);

        if (!normalizedOutput.equals(normalizedInput)) {
            throw new IllegalStateException("EDIFACT Java Object model read + write failed to produce an equivalent EDIFACT message.  " +
                    "Input EDIFACT message: \n\n\t" + inputMessage + "\n\n Serialized Java result was: \n\n\t" + writer.toString() + "\n");
        }
    }

    public void assertXMLOK(InputStream edifactIn, InputStream expectedXMLOut) throws IOException, SAXException {
        assertXMLOK(edifactIn, expectedXMLOut, true);
    }

    public void assertXMLOK(InputStream edifactIn, InputStream expectedXMLOut, boolean validate) throws IOException, SAXException {
        StringResult xmlResult = new StringResult();
        String expectedXML = StreamUtils.readStreamAsString(expectedXMLOut);

        fromUNEdifact(edifactIn, xmlResult);

//        System.out.println(xmlResult);
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.compareXML(expectedXML, xmlResult.getResult());

        if (validate) {
            ClassLoader origTCCL = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(bindingModelClassLoader);
            try {
                XMLSchemaFactory schemaFactory = new XMLSchemaFactory();

                try {
                    schemaFactory.setProperty("http://apache.org/xml/properties/security-manager", null);   // Need to turn this thing off, otherwise it throws stupid errors.
                } catch (SAXException e) {
                    // Ignore...
                }

                EclipseFragmentXMLValidator validator = new EclipseFragmentXMLValidator(schemaFactory);
                validator.validate(new StringSource(xmlResult.getResult()));
            } catch (Exception e) {
                throw new IllegalStateException("Error processing EDIFACT stream for '" + urn + "'.", e);
            } finally {
                Thread.currentThread().setContextClassLoader(origTCCL);
            }
        }
    }

    public void smooksFilterSource(String smooksConfig, Source source, Result... results) {
        ClassLoader origTCCL = Thread.currentThread().getContextClassLoader();
        CascadingClassLoaderSet filteringClassLoader = new CascadingClassLoaderSet();

        filteringClassLoader.addClassLoader(bindingModelClassLoader);
        filteringClassLoader.addClassLoader(origTCCL);

        Thread.currentThread().setContextClassLoader(filteringClassLoader);

        try {
            Smooks smooks = new Smooks(smooksConfig);

            smooks.filterSource(source, results);
        } catch (Exception e) {
            throw new IllegalStateException("Error filtering source stream using Smooks configuration file '" + smooksConfig + "' for EDIFACT URN '" + urn + "'.", e);
        } finally {
            Thread.currentThread().setContextClassLoader(origTCCL);
        }
    }

    private Archive buildBindingModel(String urn, String[] messages) throws IOException, SAXException, IllegalNameException, ClassNotFoundException {
        ClassLoader origTCCL = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(mappingModelClassLoader);
        try {
            AntRunner antRunner = new AntRunner("build.xml");
            EJCExecutor ejc = new EJCExecutor();
            File destDir = new File("target/ejc/src");

            antRunner.run("delete");

            if(messages != null && messages.length != 0) {
                ejc.setMessages(CollectionsUtil.toSet(messages));
            }
            ejc.setDestDir(destDir);
            ejc.setEdiMappingModel("urn:" + urn);
            ejc.setPackageName(EJCTestUtil.ORG_SMOOKS_EJC_TEST);

            // Build the source...
            ejc.execute();

            // Compile it...
            antRunner.run("compile");

            // Build and return an archive instance from the compiled sources...
            return new Archive(new ZipInputStream(new FileInputStream("./target/ejc/ejc.jar")));
        } finally {
            Thread.currentThread().setContextClassLoader(origTCCL);
        }
    }
}
