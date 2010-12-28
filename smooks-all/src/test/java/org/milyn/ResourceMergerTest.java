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
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Arrays;

import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Unit test for {@link ResourceMerger}.
 * </p>
 * 
 * @author Daniel Bevenius
 * @since 1.4
 */
public class ResourceMergerTest
{
    private final static String CONTENT_HANDLER_PATH = "/META-INF/content-handlers.inf";
    
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();
    
    @Test
    public void merge_resourcesResources() throws Exception
    {
        final JavaArchive firstJar = ShrinkWrap.create(JavaArchive.class);
        firstJar.add(new StringAsset("dummyContent"), "/firstJar/someFile");
        addContentHandler("property1", firstJar);
        
        final JavaArchive secondJar = ShrinkWrap.create(JavaArchive.class);
        addContentHandler("property2", secondJar);
        
        final ResourceMerger resourceMerger = new ResourceMerger(CONTENT_HANDLER_PATH);
        final JavaArchive mergedJar = resourceMerger.mergeJars("smooks-all.jar", Arrays.asList(firstJar, secondJar));
        assertThat(mergedJar.getName(), is("smooks-all.jar"));
        
        final Node contentHandlers = mergedJar.get(CONTENT_HANDLER_PATH);
        assertThat(contentHandlers.getPath().get(), is(CONTENT_HANDLER_PATH));
        
        final String content = readContent(contentHandlers);
        assertThat(content, containsString("property1"));
        assertThat(content, containsString("property2"));
        
        final Node node = mergedJar.get("/firstJar/someFile");
        assertThat(node.getPath().get().toString(), is("/firstJar/someFile"));
        assertThat(readContent(node), is("dummyContent"));
    }
    
    private String readContent(final Node node) throws IOException
    {
        StringWriter stringWriter = new StringWriter();
        BufferedReader br = new BufferedReader(new InputStreamReader(node.getAsset().openStream()));
        String line = null;
        while( (line = br.readLine()) != null)
        {
            stringWriter.append(line);
        }
        return stringWriter.toString();
    }
    
    @Test
    public void merge_with_pre_existing_jar() throws Exception
    {
        final String jarname = "prexisting.jar";
        final JavaArchive preExistingJar = ShrinkWrap.create(JavaArchive.class, jarname);
        preExistingJar.add(new StringAsset("contentInPreExistingJar"), "/preExistingJar/file");
        
        final JavaArchive firstJar = ShrinkWrap.create(JavaArchive.class);
        firstJar.add(new StringAsset("contentInFirstJar"), "/firstJar/file");
        
        final File jarfile = tempFolder.newFile(jarname);
        preExistingJar.as(ZipExporter.class).exportZip(jarfile, true);
        
        final ResourceMerger resourceMerger = new ResourceMerger(CONTENT_HANDLER_PATH);
        final JavaArchive merged = resourceMerger.mergeJars(jarfile.getAbsolutePath(), Arrays.asList(firstJar));
        
        assertThat(readContent(merged.get("/preExistingJar/file")), is("contentInPreExistingJar"));
        assertThat(readContent(merged.get("/firstJar/file")), is("contentInFirstJar"));
    }
    
    private JavaArchive addContentHandler(final String entry, final JavaArchive jar)
    {
        return jar.addResource(new StringAsset(entry), CONTENT_HANDLER_PATH);
    }
}
