/*-
 * ========================LICENSE_START=================================
 * Smooks :: All
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
package org.smooks;

import org.smooks.archive.Archive;
import org.smooks.assertion.AssertArgument;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.jar.JarInputStream;
import java.util.zip.ZipInputStream;

/**
 * ResourceMerger is able to merge java archives (jars) and in the process merge
 * any text based resource files. </p>
 * 
 * The primary use case for this class is when building the smooks-all.jar which
 * consists of all the jars in the Smooks project. In Smooks there are a few
 * types of resource files like 'content-handlers.inf', 'data-decoders.inf' that
 * exist in mulitple jars. This works well when loading resources from the
 * classpath as all jars will be searched and read. But when there is only a
 * single jar then only one of the jars resource files will be in the merged jar
 * and the others will be ignored. </p>
 * 
 * @author Daniel Bevenius
 * 
 */
public class ResourceMerger
{
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    private final List<String> resourcePaths = new ArrayList<String>();

    /**
     * @param resourcePath
     *            The path to the resource that should be merged into the jar
     *            produced by the merge method.
     */
    public ResourceMerger(final String resourcePath)
    {
        AssertArgument.isNotNull(resourcePath, "resourcePath");
        resourcePaths.add(resourcePath);
    }

    /**
     * @param resourcesPaths
     *            The path to the resource that should be merged into the jar
     *            produced by the merge method.
     */
    public ResourceMerger(final List<String> resourcesPaths)
    {
        AssertArgument.isNotNull(resourcePaths, "resourcePaths");
        this.resourcePaths.addAll(resourcesPaths);
    }

    /**
     * Will merge the jar files and produce a new jar with the contents of the resources 
     * configured merge together.
     * 
     * @param jarname
     *            Can be an existing jar file or will be the name of the new
     *            archive created.
     * @param archives
     *            An array of jar that are to be merge together.
     * @return {@link Archive} A new jar that will be the result of merging
     *         the jars and will have the contents of the resources merged.
     * @throws IOException
     */
    public Archive mergeJars(final String jarname, final File... archives) throws IOException
    {
        AssertArgument.isNotNull(jarname, "jarname");
        AssertArgument.isNotNull(archives, "archives");
        
        final List<Archive> jars = new ArrayList<Archive>();
        for (File jar : archives)
        {
            jars.add(new Archive(new JarInputStream(new FileInputStream(jar))));
        }
        return mergeJars(jarname, jars);
    }

    /**
     * Will merge the jar files and produce a new jar with the contents of the resources 
     * configured merge together.
     * 
     * @param jarname
     *            Can be an existing jar file or will be the name of the new
     *            archive created.
     * @param archives
     *            An array of jar that are to be merge together.
     * @return {@link Archive} A new jar that will be the result of merging
     *         the jars and will have the contents of the resources merged.
     * @throws IOException
     */
    public Archive mergeJars(final String jarname, final List<Archive> archives) throws IOException
    {
        AssertArgument.isNotNull(jarname, "jarname");
        AssertArgument.isNotNull(archives, "archives");
        
        final Archive all = getOrCreateArchive(jarname);
        final Map<String, List<byte[]>> pathToBytesMap = new HashMap<String, List<byte[]>>();

        for (Archive jar : archives)
        {
            for (String resourcePath : resourcePaths)
            {
                byte[] content = jar.getEntryBytes(resourcePath);
                if (content != null)
                {
                    List<byte[]> list = getContentListForResource(pathToBytesMap, resourcePath);
                    list.add(content);
                    pathToBytesMap.put(resourcePath, list);
                }
            }
            all.merge(jar);
        }
        return mergeResources(pathToBytesMap, all);
    }

    private Archive getOrCreateArchive(final String jarname) throws IOException
    {
        final File jarfile = new File(jarname);
        if (jarfile.exists())
        {
            return new Archive(new ZipInputStream(new FileInputStream(jarfile)));
        }
        else
        {
            return new Archive(jarname);
        }
    }

    private Archive mergeResources(Map<String, List<byte[]>> pathToBytesMap, Archive jar) throws IOException
    {
        final Set<Entry<String, List<byte[]>>> entrySet = pathToBytesMap.entrySet();
        for (Entry<String, List<byte[]>> resourceEntries : entrySet)
        {
            final String resourcePath = resourceEntries.getKey();
            final List<byte[]> nodes = resourceEntries.getValue();

            final StringWriter stringWriter = new StringWriter();
            for (byte[] content : nodes)
            {
                append(content, stringWriter);
            }
            jar.addEntry(resourcePath, stringWriter.toString());
        }
        return jar;
    }

    private void append(final byte[] content, final StringWriter to) throws IOException
    {
        BufferedReader in = null;
        try
        {
            in = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(content)));
            String line;
            while ((line = in.readLine()) != null) 
            {
                to.write(line);
                to.write(LINE_SEPARATOR);
            }
        } 
        finally
        {
            if (in != null)
            {
                in.close();
            }
        }
    }

    private List<byte[]> getContentListForResource(final Map<String, List<byte[]>> map, final String resourcePath)
    {
        List<byte[]> list = map.get(resourcePath);
        if (list == null)
        {
            list = new ArrayList<byte[]>();
        }
        return list;
    }

}
