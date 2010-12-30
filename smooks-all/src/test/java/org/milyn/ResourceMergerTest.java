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
package org.milyn;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.milyn.archive.Archive;

/**
 * Unit test for {@link ResourceMerger}.
 * </p>
 * 
 * @author Daniel Bevenius
 * @since 1.4
 */
public class ResourceMergerTest
{
    private final static String CONTENT_HANDLER_PATH = "META-INF/content-handlers.inf";
    
    private final ResourceMerger resourceMerger = new ResourceMerger(CONTENT_HANDLER_PATH);
    
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();
    
    @Test
    public void merge_resources_from_two_jars() throws Exception
    {
        final Archive firstJar = setupFirstJar();
        final Archive secondJar = setupSecondJar();
        
        final Archive mergedJar = resourceMerger.mergeJars("smooks-all.jar", Arrays.asList(firstJar, secondJar));
        
        assertThat(mergedJar.getArchiveName(), is("smooks-all.jar"));
        
        final String contentHandlers = readContent(mergedJar, CONTENT_HANDLER_PATH);
        assertThat(contentHandlers, containsString("property1"));
        assertThat(contentHandlers, containsString("property2"));
        
        final String content = readContent(mergedJar, "firstJar/someFile");
        assertThat(content, is("dummyContent"));
    }
    
    private Archive setupFirstJar() throws Exception
    {
        final Archive jar = new Archive("firstJar");
        addToJar(jar, "firstJar/someFile", "dummyContent");
        return addToJar(jar, CONTENT_HANDLER_PATH, "property1");
    }
    
    private Archive setupSecondJar() throws Exception
    {
        final Archive jar = new Archive("secondJar");
        return addToJar(jar, CONTENT_HANDLER_PATH, "property2");
    }
    
    private String readContent(final Archive jar, final String path) throws IOException
    {
        return new String(jar.getEntries().get(path));
    }
    
    @Test
    public void merge_with_pre_existing_jar() throws Exception
    {
        final File jarfile = setupPreExistingJar();
        final Archive firstJar = setupFirstJar();
        
        final Archive merged = resourceMerger.mergeJars(jarfile.getAbsolutePath(), Arrays.asList(firstJar));
        
        assertThat(readContent(merged, "firstJar/someFile"), is("dummyContent"));
        assertThat(readContent(merged, "preExistingJar/file"), is("contentInPreExistingJar"));
    }
    
    private File setupPreExistingJar() throws Exception
    {
        final String jarname = "prexisting.jar";
        final Archive preExistingJar = createPreExistingJar();
        return exportJarToFile(jarname, preExistingJar);
    }
    
    private Archive createPreExistingJar() throws Exception
    {
        final String jarname = "prexisting.jar";
        final Archive preExistingJar = new Archive(jarname);
        addToJar(preExistingJar, "preExistingJar/file", "contentInPreExistingJar");
        addToJar(preExistingJar, JarFile.MANIFEST_NAME, "manifesto");
        return preExistingJar;
    }

    private File exportJarToFile(final String jarname, final Archive preExistingJar) throws Exception
    {
        final File jarfile = tempFolder.newFile(jarname);
        preExistingJar.toOutputStream(new JarOutputStream(new FileOutputStream(jarfile)));
        return jarfile;
    }
    
    @Test
    public void verify_that_metainf_is_the_first_entry_in_jar() throws Exception
    {
        final Archive firstJar = setupFirstJar();
        final File jarfile = setupPreExistingJar();
        
        final Archive merged = resourceMerger.mergeJars(jarfile.getAbsolutePath(), Arrays.asList(firstJar));
        
        final byte[] content = merged.getEntries().get(JarFile.MANIFEST_NAME);
        assertThat(content, is(notNullValue()));
        
        final File newFile = exportJarToFile(merged, "output.jar");
        final Manifest manifest = getManifest(newFile.getAbsolutePath());
        assertNotNull("Manifest could not be found by JarInputStream", manifest);
    }
    
    private File exportJarToFile(final Archive jar, final String to) throws Exception
    {
        final File newFile = tempFolder.newFile(to);
        final JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(newFile));
        jar.toOutputStream(jarOutputStream);
        return newFile;
    }
    
    private Manifest getManifest(final String jarName) throws Exception
    {
        final JarInputStream jarInputStream = new JarInputStream(new FileInputStream(jarName));
        return jarInputStream.getManifest();
    }
    
    private Archive addToJar(final Archive jar, final String path, final String content) throws IOException
    {
        jar.addEntry(path, new ByteArrayInputStream(content.getBytes()));
        return jar;
    }
    
}
