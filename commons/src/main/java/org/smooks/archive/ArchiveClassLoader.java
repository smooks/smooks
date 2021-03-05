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

import org.smooks.assertion.AssertArgument;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

/**
 * {@link Archive} based {@link ClassLoader}.
 * 
 * <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class ArchiveClassLoader extends ClassLoader {

    private final Archive archive;
    private final Map<String, Class> loadedClasses = new HashMap<String, Class>();

    public ArchiveClassLoader(Archive archive) {
        this(Thread.currentThread().getContextClassLoader(), archive);
    }

    public ArchiveClassLoader(ClassLoader parent, Archive archive) {
        super(parent);
        AssertArgument.isNotNull(archive, "archive");
        this.archive = archive;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        Class loadedClass = loadedClasses.get(name);

        if(loadedClass != null) {
            return loadedClass;
        }

        String resName = name.replace('.', '/') + ".class";
        byte[] classBytes = archive.getEntryBytes(resName);

        if(classBytes != null) {
            loadedClass = defineClass(name, classBytes, 0, classBytes.length);
            loadedClasses.put(name, loadedClass);
            return loadedClass;
        } else {
            return super.loadClass(name);
        }
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        byte[] bytes = archive.getEntryBytes(name);
        if (bytes != null) {
            return new ByteArrayInputStream(bytes);
        } else {
            return super.getResourceAsStream(name);
        }
    }

    @Override
    protected URL findResource(String resName) {
        URL resource = archive.getEntryURL(resName);

        if (resource != null) {
            return resource;
        }

        return getParent().getResource(resName);
    }

    @Override
    protected Enumeration<URL> findResources(String resName) throws IOException {
        List<URL> resources = new ArrayList<URL>();
        URL resource = archive.getEntryURL(resName);

        if (resource != null) {
            resources.add(resource);
        }

        Enumeration<URL> parentResource = getParent().getResources(resName);
        resources.addAll(Collections.list(parentResource));

        return Collections.enumeration(resources);
    }
}
