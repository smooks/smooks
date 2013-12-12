/*
 * Milyn - Copyright (C) 2006 - 2010
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License (version 2.1) as published
 * by the Free Software Foundation.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.
 * 
 * See the GNU Lesser General Public License for more details:
 * http://www.gnu.org/licenses/lgpl.txt
 */

package org.milyn.routing.file;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.milyn.FilterSettings;
import org.milyn.Smooks;
import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.cdr.annotation.Configurator;
import org.milyn.commons.io.FileUtils;
import org.milyn.container.ExecutionContext;
import org.milyn.container.MockApplicationContext;
import org.milyn.container.MockExecutionContext;
import org.milyn.delivery.Fragment;
import org.milyn.io.AbstractOutputStreamResource;
import org.milyn.cartridge.javabean.Bean;
import org.milyn.payload.StringSource;
import org.milyn.templating.OutputTo;
import org.milyn.templating.TemplatingConfiguration;
import org.milyn.templating.freemarker.FreeMarkerTemplateProcessor;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;

/**
 * Unit test for {@link FileOutputStreamResource}
 *
 * @author <a href="mailto:daniel.bevenius@gmail.com">Daniel Bevenius</a>
 */
public class FileOutputStreamResourceTest {
    private String resourceName = "testResourceName";
    private String fileNamePattern = "testFileName";
    private String destinationDirectory = System.getProperty("java.io.tmpdir");
    private String listFileName = "testListFileName";
    private FileOutputStreamResource resource = new FileOutputStreamResource();
    private SmooksResourceConfiguration config;
    private File file1 = new File("target/config-01-test/1/1.xml");
    private File file2 = new File("target/config-01-test/2/2.xml");
    private File file3 = new File("target/config-01-test/3/3.xml");

    @Before
    public void setUp() throws Exception {
        config = createConfig(resourceName, fileNamePattern, destinationDirectory, listFileName);
        Configurator.configure(resource, config, new MockApplicationContext());
        deleteFiles();
    }

    @Test
    public void configure() {
        assertEquals(resourceName, resource.getResourceName());
    }

    @Test
    public void visit() throws Exception {
        MockExecutionContext executionContext = new MockExecutionContext();
        resource.visitBefore((Element) null, executionContext);

        OutputStream outputStream = AbstractOutputStreamResource.getOutputStream(resource.getResourceName(), executionContext);
        assertTrue(outputStream instanceof FileOutputStream);

        resource.executeVisitLifecycleCleanup(new Fragment((Element) null), executionContext);

        assertThatFilesWereGenerated(executionContext);
    }

    private void assertThatFilesWereGenerated(ExecutionContext executionContext) throws Exception {
        File file = new File(destinationDirectory, fileNamePattern);
        assertTrue(file.exists());

        List<String> listFileNames = FileListAccessor.getListFileNames(executionContext);
        assertNotNull(listFileNames);
        assertTrue(listFileNames.size() == 1);

        for (String listFile : listFileNames) {
            List<String> fileList = FileListAccessor.getFileList(executionContext, listFile);
            assertTrue(fileList.size() == 1);
            for (String fileName : fileList) {
                File file2 = new File(fileName);
                assertEquals(fileNamePattern, file2.getName());
                file2.delete();
            }
            new File(listFile).delete();
        }

    }

    @Test
    public void testConfig01() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("config-01.xml"));

        try {
            smooks.filterSource(new StringSource("<root><a>1</a><a>2</a><a>3</a></root>"));

            assertEquals("1", getFileContents(file1));
            assertEquals("2", getFileContents(file2));
            assertEquals("3", getFileContents(file3));
        } finally {
            smooks.close();
        }
    }

    @Test
    public void config01Programmatic() throws IOException, SAXException {
        Smooks smooks = new Smooks();

        try {
            smooks.setFilterSettings(FilterSettings.DEFAULT_SAX);

            smooks.addVisitor(new Bean(HashMap.class, "object").bindTo("a", "a"));
            smooks.addVisitor(new FreeMarkerTemplateProcessor(new TemplatingConfiguration("${object.a}").setUsage(OutputTo.stream("fileOS"))), "a");
            smooks.addVisitor(new FileOutputStreamResource().setFileNamePattern("${object.a}.xml").setDestinationDirectoryPattern("target/config-01-test/${object.a}").setResourceName("fileOS"), "a");

            smooks.filterSource(new StringSource("<root><a>1</a><a>2</a><a>3</a></root>"));

            assertEquals("1", getFileContents(file1));
            assertEquals("2", getFileContents(file2));
            assertEquals("3", getFileContents(file3));
        } finally {
            smooks.close();
        }
    }

    @Test
    public void testAppendingToOutputFile() throws Exception {
        final Smooks smooks = new Smooks();
        final String outputFileName = "appended.txt";
        final String outputStreamRef = "fileOS";
        final File destinationDir = new File("target/config-01-test");
        final File outputFile = new File(destinationDir, outputFileName);

        try {
            smooks.setFilterSettings(FilterSettings.DEFAULT_SAX);
            smooks.addVisitor(new Bean(HashMap.class, "object").bindTo("a", "a"));
            smooks.addVisitor(new FreeMarkerTemplateProcessor(new TemplatingConfiguration("${object.a}")
                    .setUsage(OutputTo.stream(outputStreamRef))), "a");
            smooks.addVisitor(new FileOutputStreamResource()
                    .setAppend(true)
                    .setFileNamePattern(outputFileName)
                    .setDestinationDirectoryPattern(destinationDir.getAbsolutePath())
                    .setResourceName(outputStreamRef)
                    , "a");

            smooks.filterSource(new StringSource("<root><a>1</a><a>2</a><a>3</a></root>"));

            assertEquals("123", getFileContents(outputFile));
        } finally {
            smooks.close();
            outputFile.delete();
        }
    }

    private String getFileContents(File file) throws IOException {
        return new String(FileUtils.readFile(file));
    }

    @After
    public void tearDown() throws Exception {
        deleteFiles();
    }

    public void deleteFiles() {
        file1.delete();
        file2.delete();
        file3.delete();
    }

    private SmooksResourceConfiguration createConfig(
            final String resourceName,
            final String fileName,
            final String destinationDirectory,
            final String listFileName) {
        SmooksResourceConfiguration config = new SmooksResourceConfiguration("x", FileOutputStreamResource.class.getName());
        config.setParameter("resourceName", resourceName);
        config.setParameter("fileNamePattern", fileName);
        config.setParameter("destinationDirectoryPattern", destinationDirectory);
        config.setParameter("listFileNamePattern", listFileName);
        return config;
    }

}
