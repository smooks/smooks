/*-
 * ========================LICENSE_START=================================
 * Smooks :: All
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
package org.smooks;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.smooks.archive.Archive;

import java.io.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

/**
 * Unit test for {@link ResourceMerger}.
 * </p>
 *
 * @author Daniel Bevenius
 * @since 1.4
 */
public class ResourceMergerTest {
    private final static String CONTENT_HANDLER_PATH = "META-INF/content-handlers.inf";

    private final ResourceMerger resourceMerger = new ResourceMerger(CONTENT_HANDLER_PATH);

    @Rule
    public final TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void merge_resources_from_two_jars() throws Exception {
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

    private Archive setupFirstJar() throws Exception {
        final Archive jar = new Archive("firstJar");
        addToJar(jar, "firstJar/someFile", "dummyContent");
        return addToJar(jar, CONTENT_HANDLER_PATH, "property1");
    }

    private Archive setupSecondJar() throws Exception {
        final Archive jar = new Archive("secondJar");
        return addToJar(jar, CONTENT_HANDLER_PATH, "property2");
    }

    private String readContent(final Archive jar, final String path) throws IOException {
        return new String(jar.getEntryBytes(path));
    }

    @Test
    public void merge_with_pre_existing_jar() throws Exception {
        final File jarfile = setupPreExistingJar();
        final Archive firstJar = setupFirstJar();

        final Archive merged = resourceMerger.mergeJars(jarfile.getAbsolutePath(), Collections.singletonList(firstJar));

        assertThat(readContent(merged, "firstJar/someFile"), is("dummyContent"));
        assertThat(readContent(merged, "preExistingJar/file"), is("contentInPreExistingJar"));
    }

    private File setupPreExistingJar() throws Exception {
        final String jarname = "prexisting.jar";
        final Archive preExistingJar = createPreExistingJar();
        return exportJarToFile(preExistingJar);
    }

    private Archive createPreExistingJar() throws Exception {
        final String jarname = "prexisting.jar";
        final Archive preExistingJar = new Archive(jarname);
        addToJar(preExistingJar, "preExistingJar/file", "contentInPreExistingJar");
        addToJar(preExistingJar, JarFile.MANIFEST_NAME, "manifesto");
        return preExistingJar;
    }

    private File exportJarToFile(final Archive preExistingJar) throws Exception {
        final File jarfile = tempFolder.newFile("prexisting.jar");
        preExistingJar.toOutputStream(new JarOutputStream(new FileOutputStream(jarfile)));
        return jarfile;
    }

    @Test
    public void verify_that_metainf_is_the_first_entry_in_jar() throws Exception {
        final Archive firstJar = setupFirstJar();
        final File jarfile = setupPreExistingJar();

        final Archive merged = resourceMerger.mergeJars(jarfile.getAbsolutePath(), Collections.singletonList(firstJar));

        final byte[] content = merged.getEntryBytes(JarFile.MANIFEST_NAME);
        assertThat(content, is(notNullValue()));

        final File newFile = exportJarToFile(merged, "output.jar");
        final Manifest manifest = getManifest(newFile.getAbsolutePath());
        assertNotNull("Manifest could not be found by JarInputStream", manifest);
    }

    private File exportJarToFile(final Archive jar, final String to) throws Exception {
        final File newFile = tempFolder.newFile(to);
        final JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(newFile));
        jar.toOutputStream(jarOutputStream);
        return newFile;
    }

    private Manifest getManifest(final String jarName) throws Exception {
        final JarInputStream jarInputStream = new JarInputStream(new FileInputStream(jarName));
        return jarInputStream.getManifest();
    }

    private Archive addToJar(final Archive jar, final String path, final String content) throws IOException {
        jar.addEntry(path, new ByteArrayInputStream(content.getBytes()));
        return jar;
    }

}
