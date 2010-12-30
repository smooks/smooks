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
package org.milyn.archive;

import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.milyn.io.StreamUtils;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class ArchiveTest extends TestCase {

    public void test_produced_zip() throws IOException {
        Archive archive = new Archive("testarchive");

        archive.addEntry("//my/resource.txt", new ByteArrayInputStream("Hi!!".getBytes()));
        archive.addEntry("my/folder");
        archive.addEntry(Archive.class);

        assertEquals("my/resource.txt", archive.getEntryName(0));
        assertEquals("my/folder/", archive.getEntryName(1));
        assertEquals("org/milyn/archive/Archive.class", archive.getEntryName(2));

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
        assertEquals("org/milyn/archive/Archive.class", zipEntry.getName());
    }

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
        assertEquals("org/milyn/archive/Archive.class", zipEntry.getName());
    }

    public void test_toFileSystem() throws IOException {
        Archive archive = new Archive();

        archive.addEntry("//my/resource.txt", new ByteArrayInputStream("Hi!!".getBytes()));
        archive.addEntry("my/emptyfile.xxx");
        archive.addEntry(Archive.class);

        File folder = new File("./target/ArchiveTest-myzip-contents");

        archive.toFileSystem(folder);

        assertTrue(new File(folder, "my/resource.txt").exists());
        assertTrue(new File(folder, "my/emptyfile.xxx").exists());
        assertTrue(new File(folder, "org/milyn/archive/Archive.class").exists());
    }

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
}
