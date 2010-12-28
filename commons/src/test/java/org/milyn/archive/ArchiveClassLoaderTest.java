/*
 * Milyn - Copyright (C) 2006 - 2010
 *
 * 	This library is free software; you can redistribute it and/or
 * 	modify it under the terms of the GNU Lesser General Public
 * 	License (version 2.1) as published by the Free Software
 * 	Foundation.
 *
 * 	This library is distributed in the hope that it will be useful,
 * 	but WITHOUT ANY WARRANTY; without even the implied warranty of
 * 	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * 	See the GNU Lesser General Public License for more details:
 * 	http://www.gnu.org/licenses/lgpl.txt
 */
package org.milyn.archive;

import junit.framework.TestCase;
import org.milyn.io.StreamUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class ArchiveClassLoaderTest extends TestCase {

    public void test_produced_zip() throws IOException, ClassNotFoundException {
        Archive archive = new Archive("testarchive");

        archive.addEntry("//my/resource.txt", new ByteArrayInputStream("Hi!!".getBytes()));
        archive.addEntry(Archive.class);

        ArchiveClassLoader classLoader = new ArchiveClassLoader(archive);

        Class clazzInst = classLoader.loadClass(Archive.class.getName());

        // Classes are loaded by different classloaders, so shouldn't be the same class instance...
        assertNotSame(Archive.class, clazzInst);
        // But should be the same class...
        assertEquals(Archive.class.getName(), clazzInst.getName());

        String hiString = StreamUtils.readStreamAsString(classLoader.getResourceAsStream("my/resource.txt"));
        assertEquals("Hi!!", hiString);
    }
}