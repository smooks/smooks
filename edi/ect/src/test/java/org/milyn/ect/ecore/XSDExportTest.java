/*
 * Milyn - Copyright (C) 2006 - 2011
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License (version 2.1) as published by the Free Software
 * Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU Lesser General Public License for more details:
 * http://www.gnu.org/licenses/lgpl.txt
 */
package org.milyn.ect.ecore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.zip.ZipOutputStream;

import junit.framework.TestCase;

import org.milyn.archive.Archive;

public class XSDExportTest extends TestCase {

	public void testSchemaExport() throws Exception {
		InputStream inputStream = getClass().getResourceAsStream("/d03b.zip");
		Archive archive = SchemaConverter.INSTANCE.createArchive(inputStream, "org.milyn.edi.unedifact.d03b");
		archive.toOutputStream(new ZipOutputStream(new FileOutputStream(new File("./target/" + archive.getArchiveName()))));
	}

}
