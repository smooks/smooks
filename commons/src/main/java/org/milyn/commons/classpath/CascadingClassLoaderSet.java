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
package org.milyn.commons.classpath;

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
