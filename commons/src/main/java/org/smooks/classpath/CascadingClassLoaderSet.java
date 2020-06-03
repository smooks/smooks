/*-
 * ========================LICENSE_START=================================
 * Smooks Commons
 * %%
 * Copyright (C) 2020 Smooks
 * %%
 * Licensed under the terms of the Apache License Version 2.0 or,
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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

/**
 * Cascading {@link ClassLoader} set.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class CascadingClassLoaderSet extends ClassLoader {

    List<ClassLoader> classLoaders = new ArrayList<ClassLoader>();
    private int classLoaderCount;

    public CascadingClassLoaderSet addClassLoader(ClassLoader classLoader) {
        classLoaders.add(classLoader);
        classLoaderCount = classLoaders.size();
        return this;
    }

    @Override
    public Class<?> loadClass(String s) throws ClassNotFoundException {
        for (int i = 0; i < classLoaderCount; i++) {
            ClassLoader classLoader = classLoaders.get(i);
            try {
                Class klass = classLoader.loadClass(s);
                if (klass != null) {
                    return klass;
                }
            } catch (ClassNotFoundException e) {
                // Try the next classloader...
            }
        }

        throw new ClassNotFoundException("Failed to find class '" + s + "'.");
    }

    @Override
    public URL getResource(String s) {
        for (int i = 0; i < classLoaderCount; i++) {
            ClassLoader classLoader = classLoaders.get(i);
            URL resource = classLoader.getResource(s);
            if (resource != null) {
                return resource;
            }
        }

        return null;
    }

    @Override
    public Enumeration<URL> getResources(String s) throws IOException {
        List<URL> resources = new ArrayList<URL>();

        for (int i = 0; i < classLoaderCount; i++) {
            ClassLoader classLoader = classLoaders.get(i);
            Enumeration<URL> resourcesEnum = classLoader.getResources(s);

            if(resourcesEnum.hasMoreElements()) {
                resources.addAll(Collections.list(resourcesEnum));
            }
        }

        return Collections.enumeration(resources);
    }

    @Override
    public InputStream getResourceAsStream(String s) {
        for (int i = 0; i < classLoaderCount; i++) {
            ClassLoader classLoader = classLoaders.get(i);
            InputStream resource = classLoader.getResourceAsStream(s);
            if (resource != null) {
                return resource;
            }
        }

        return null;
    }
}
