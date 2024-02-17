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
package org.smooks.classpath;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smooks.assertion.AssertArgument;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Classpath scanner.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class Scanner {

    private static final Logger LOGGER = LoggerFactory.getLogger(Scanner.class);
    private final Filter filter;

    public Scanner(Filter filter) {
        AssertArgument.isNotNull(filter, "filter");
        this.filter = filter;
    }

    public void scanClasspath(ClassLoader classLoader) throws IOException {
        if (!(classLoader instanceof URLClassLoader)) {
            LOGGER.warn("Not scanning classpath for ClassLoader '" + classLoader.getClass().getName() + "'.  ClassLoader must implement '" + URLClassLoader.class.getName() + "'.");
            return;
        }

        URL[] urls = ((URLClassLoader) classLoader).getURLs();
        Set<String> alreadyScanned = new HashSet<>();

        for (URL url : urls) {
            String urlPath = url.getFile();

            urlPath = URLDecoder.decode(urlPath, "UTF-8");
            if (urlPath.startsWith("file:")) {
                urlPath = urlPath.substring(5);
            }

            if (urlPath.indexOf('!') > 0) {
                urlPath = urlPath.substring(0, urlPath.indexOf('!'));
            }

            File file = new File(urlPath);
            if (alreadyScanned.contains(file.getAbsolutePath())) {
                LOGGER.debug("Ignoring classpath URL '" + file.getAbsolutePath() + "'.  Already scanned this URL.");
                continue;
            }
            if (file.isDirectory()) {
                handleDirectory(file, null);
            } else {
                handleArchive(file);
            }
            alreadyScanned.add(file.getAbsolutePath());
        }
    }

    private void handleArchive(File file) {
        if (filter.isIgnorable(file.getName())) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Ignoring archive: " + file);
            }
            return;
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Scanning archive: " + file.getAbsolutePath());
        }

        try {
            try (ZipFile zip = new ZipFile(file)) {
                final Enumeration<? extends ZipEntry> entries = zip.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry entry = entries.nextElement();
                    String name = entry.getName();
                    filter.filter(name);
                }
            }
        } catch (Exception e) {
            LOGGER.warn(String.format("%s: %s. Unable to scan [%s] for Smooks resources", e.getClass().getName(), e.getMessage(), file));
        }
    }

    private void handleDirectory(File file, String path) {
        if (path != null && filter.isIgnorable(path)) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Ignoring directory (and subdirectories): " + path);
            }
            return;
        }
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Scanning directory: " + file.getAbsolutePath());
        }

        for (File child : file.listFiles()) {
            String newPath = path == null ? child.getName() : path + '/' + child.getName();

            if (child.isDirectory()) {
                handleDirectory(child, newPath);
            } else {
                filter.filter(newPath);
            }
        }
    }

}
