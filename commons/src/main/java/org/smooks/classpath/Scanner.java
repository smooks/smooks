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
package org.smooks.classpath;

import org.smooks.assertion.AssertArgument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private Filter filter;

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
        Set<String> alreadyScanned = new HashSet<String>();

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
            if(alreadyScanned.contains(file.getAbsolutePath())) {
                LOGGER.debug("Ignoring classpath URL '" + file.getAbsolutePath() + "'.  Already scanned this URL.");
                continue;
            } if (file.isDirectory()) {
                handleDirectory(file, null);
            } else {
                handleArchive(file);
            }
            alreadyScanned.add(file.getAbsolutePath());
        }
    }

    private void handleArchive(File file) throws IOException {
        if(filter.isIgnorable(file.getName())) {
            if(LOGGER.isDebugEnabled()) {
                LOGGER.debug("Ignoring archive: " + file);
            }
            return;
        }
        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("Scanning archive: " + file.getAbsolutePath());
        }

        ZipFile zip = new ZipFile(file);
        Enumeration<? extends ZipEntry> entries = zip.entries();

        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            String name = entry.getName();
            filter.filter(name);
        }
    }

    private void handleDirectory(File file, String path) {
        if(path != null && filter.isIgnorable(path)) {
            if(LOGGER.isDebugEnabled()) {
                LOGGER.debug("Ignoring directory (and subdirectories): " + path);
            }
            return;
        }
        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug("Scanning directory: " + file.getAbsolutePath());
        }

        for (File child : file.listFiles()) {
            String newPath = path == null?child.getName() : path + '/' + child.getName();

            if (child.isDirectory()) {
                handleDirectory(child, newPath);
            } else {
                filter.filter(newPath);
            }
        }
    }

}
