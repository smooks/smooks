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
package org.smooks.archive;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.smooks.support.StreamUtils;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class ArchiveTestCase {

	@Test
    public void test_produced_zip() throws IOException {
        Archive archive = new Archive("testarchive");

        archive.addEntry("//my/resource.txt", new ByteArrayInputStream("Hi!!".getBytes()));
        archive.addEntry("my/folder");
        archive.addEntry(Archive.class);

        assertEquals("my/resource.txt", archive.getEntryName(0));
        assertEquals("my/folder/", archive.getEntryName(1));
        assertEquals("org/smooks/archive/Archive.class", archive.getEntryName(2));

        assertEquals("Hi!!", new String(archive.getEntryValue(0)));

        ZipInputStream zipInputStream = archive.toInputStream();
        ZipEntry zipEntry;

        // my/resource.txt
        zipEntry = zipInputStream.getNextEntry();
        assertEquals("my/resource.txt", zipEntry.getName());
        assertEquals("Hi!!", new String(StreamUtils.readStream(zipInputStream)));

        // my/folder
        zipEntry = zipInputStream.getNextEntry();
        assertEquals("my/folder/", zipEntry.getName());
        assertTrue(zipEntry.isDirectory());

        // Archive.class
        zipEntry = zipInputStream.getNextEntry();
        assertEquals("org/smooks/archive/Archive.class", zipEntry.getName());
    }

	@Test
    public void test_addEntries() throws IOException {
        Archive archive1 = new Archive("testarchive1");

        archive1.addEntry("//my/resource.txt", new ByteArrayInputStream("Hi!!".getBytes()));
        archive1.addEntry("my/folder");
        archive1.addEntry(Archive.class);

        Archive archive2 = new Archive("testarchive1", archive1.toInputStream());

        ZipInputStream zipInputStream = archive2.toInputStream();
        ZipEntry zipEntry;

        // my/resource.txt
        zipEntry = zipInputStream.getNextEntry();
        assertEquals("my/resource.txt", zipEntry.getName());
        assertEquals("Hi!!", new String(StreamUtils.readStream(zipInputStream)));

        // my/folder
        zipEntry = zipInputStream.getNextEntry();
        assertEquals("my/folder/", zipEntry.getName());
        assertTrue(zipEntry.isDirectory());

        // Archive.class
        zipEntry = zipInputStream.getNextEntry();
        assertEquals("org/smooks/archive/Archive.class", zipEntry.getName());
    }

	@Test
    public void test_toFileSystem() throws IOException {
        Archive archive = new Archive();

        archive.addEntry("//my/resource.txt", new ByteArrayInputStream("Hi!!".getBytes()));
        archive.addEntry("my/emptyfile.xxx");
        archive.addEntry(Archive.class);

        File folder = new File("./target/ArchiveTest-myzip-contents");

        archive.toFileSystem(folder);

        assertTrue(new File(folder, "my/resource.txt").exists());
        assertTrue(new File(folder, "my/emptyfile.xxx").exists());
        assertTrue(new File(folder, "org/smooks/archive/Archive.class").exists());
    }

	@Test
    public void test_ManifestIsFirstWrite() throws IOException {
        Archive archive1 = new Archive();

        archive1.addEntry("my/resource.txt", new ByteArrayInputStream("Hi!!".getBytes()));
        archive1.addEntry("my/emptyfile.xxx");
        archive1.addEntry(JarFile.MANIFEST_NAME, new ByteArrayInputStream("Manifestio".getBytes()));

        assertEquals("my/resource.txt", archive1.getEntryName(0));
        assertEquals("my/emptyfile.xxx/", archive1.getEntryName(1));
        assertEquals(JarFile.MANIFEST_NAME, archive1.getEntryName(2));
        assertEquals(3, archive1.getEntries().size());        

        ByteArrayOutputStream outStream = new ByteArrayOutputStream();

        archive1.toOutputStream(new ZipOutputStream(outStream));

        Archive archive2 = new Archive(new ZipInputStream(new ByteArrayInputStream(outStream.toByteArray())));

        // The Manifest should have been moved to being the first entry after archive1 was serialized...
        assertEquals(JarFile.MANIFEST_NAME, archive2.getEntryName(0));
        assertEquals("my/resource.txt", archive2.getEntryName(1));
        assertEquals("my/emptyfile.xxx/", archive2.getEntryName(2));
        assertEquals(3, archive2.getEntries().size());        
    }

	@Test
    public void test_merge() throws Exception
    {
        Archive archive1 = new Archive();
        archive1.addEntry("archive1/resource.txt", new ByteArrayInputStream("Hi!!".getBytes()));
        archive1.addEntry("archive1/emptyfile.xxx/");
        archive1.addEntry(JarFile.MANIFEST_NAME, new ByteArrayInputStream("Manifestio".getBytes()));
        
        Archive archive2 = new Archive();
        archive2.addEntry("archive2/resource.txt", new ByteArrayInputStream("Hi!!".getBytes()));
        Archive merged = archive1.merge(archive2);
        assertNotNull(merged);
        assertEquals("archive1/resource.txt", merged.getEntryName(0));
        assertEquals("archive1/emptyfile.xxx/", merged.getEntryName(1));
        assertEquals(JarFile.MANIFEST_NAME, merged.getEntryName(2));
        assertEquals("archive2/resource.txt", merged.getEntryName(3));
    }
    
	@Test
    public void test_contains() throws Exception
    {
        Archive archive1 = new Archive();
        archive1.addEntry("archive1/resource.txt", new ByteArrayInputStream("Hi!!".getBytes()));
        assertTrue(archive1.contains("archive1/resource.txt"));
    }
}
