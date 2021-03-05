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
package org.smooks.io;

import org.smooks.assertion.AssertArgument;

import java.io.*;

/**
 * File utilities.
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public abstract class FileUtils {
    
    public static void copyFile(String from, String to) throws IOException {
        File fromFile = new File(from);
        File toFile = new File(to);

        writeFile(readFile(fromFile), toFile);
    }

    /**
     * Read the contents of the specified file.
     * @param file The file to read.
     * @return The file contents.
     * @throws IOException Error readiong file.
     */
    public static byte[] readFile(File file) throws IOException {
        AssertArgument.isNotNull(file, "file");

        if(!file.exists()) {
            throw new IllegalArgumentException("No such file '" + file.getAbsoluteFile() + "'.");
        } else if(file.isDirectory()) {
            throw new IllegalArgumentException("File '" + file.getAbsoluteFile() + "' is a directory.  Cannot read.");
        }

        InputStream stream = new FileInputStream(file);
        try {
            return StreamUtils.readStream(stream);
        } finally {
            stream.close();
        }
    }

    public static void writeFile(byte[] bytes, File file) throws IOException {
        if(file.isDirectory()) {
            throw new IllegalArgumentException("File '" + file.getAbsoluteFile() + "' is an existing directory.  Cannot write.");
        }

        File folder = file.getParentFile();
        if(folder != null && !folder.exists()) {
            folder.mkdirs();
        }

        FileOutputStream stream = new FileOutputStream(file);
        try {
            stream.write(bytes);
            stream.flush();
        } finally {
            stream.close();
        }
    }

    /**
     * Delete directory structure.
     * <p/>
     * Performs a recursive delete.
     *
     * @param dir The directory to be deleted.
     * @return true of the directory is deleted, otherwise false.
     */
    public static boolean deleteDir(File dir) {
        AssertArgument.isNotNull(dir, "dir");
        if (!dir.isDirectory()) {
            throw new IllegalArgumentException("File '" + dir.getAbsolutePath() + "' is not a directory.");
        }

        File[] files = dir.listFiles();

        for (File file : files) {
            if (file.isDirectory()) {
                deleteDir(file);
            } else {
                file.delete();
            }
        }

        return dir.delete();
    }
}
