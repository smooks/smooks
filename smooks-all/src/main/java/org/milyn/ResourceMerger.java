/*
 * Milyn - Copyright (C) 2006 - 2010
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License (version 2.1) as published
 * by the Free Software Foundation.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.
 * 
 * See the GNU Lesser General Public License for more details:
 * http://www.gnu.org/licenses/lgpl.txt
 */
package org.milyn;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.Node;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

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
    private List<String> resourcePaths = new ArrayList<String>();

    /**
     * @param resourcePath
     *            The path to the resource that should be merged into the jar
     *            produced by the merge method.
     */
    public ResourceMerger(final String resourcePath)
    {
        resourcePaths.add(resourcePath);
    }

    /**
     * @param resourcePaths
     *            The path to the resource that should be merged into the jar
     *            produced by the merge method.
     */
    public ResourceMerger(final List<String> resourcesPaths)
    {
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
     * @return {@link JavaArchive} A new jar that will be the result of merging
     *         the jars and will have the contents of the resources merged.
     * @throws IOException
     */
    public JavaArchive mergeJars(final String jarname, final File... archives) throws IOException
    {
        final List<JavaArchive> jars = new ArrayList<JavaArchive>();
        for (File jar : archives)
        {
            jars.add(ShrinkWrap.createFromZipFile(JavaArchive.class, jar));
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
     * @return {@link JavaArchive} A new jar that will be the result of merging
     *         the jars and will have the contents of the resources merged.
     * @throws IOException
     */
    public JavaArchive mergeJars(final String newJarName, final List<JavaArchive> archives) throws IOException
    {
        final JavaArchive all = getOrCreateJavaArchive(newJarName);
        final Map<String, List<Node>> pathToNodeMap = new HashMap<String, List<Node>>();

        for (JavaArchive jar : archives)
        {
            for (String resourcePath : resourcePaths)
            {
                if (jar.contains(resourcePath))
                {
                    final Map<ArchivePath, Node> content = jar.getContent(new ResourceFilter(resourcePath));
                    final Node node = content.get(ArchivePaths.create(resourcePath));
                    if (node != null)
                    {
                        List<Node> list = getNodesForResource(pathToNodeMap, resourcePath);
                        list.add(node);
                        pathToNodeMap.put(resourcePath, list);
                    }
                }
            }
            all.merge(jar);
        }
        return mergeResources(pathToNodeMap, all);
    }

    private JavaArchive getOrCreateJavaArchive(final String jarname)
    {
        final File jarfile = new File(jarname);
        return jarfile.exists() ? ShrinkWrap.createFromZipFile(JavaArchive.class, jarfile) : ShrinkWrap.create(
                JavaArchive.class, jarname);
    }

    private JavaArchive mergeResources(Map<String, List<Node>> pathToNodeMap, JavaArchive jar) throws IOException
    {
        final Set<Entry<String, List<Node>>> entrySet = pathToNodeMap.entrySet();
        for (Entry<String, List<Node>> resourceEntries : entrySet)
        {
            final String resourcePath = resourceEntries.getKey();
            final List<Node> nodes = resourceEntries.getValue();
            jar.delete(ArchivePaths.create(resourcePath));

            final StringWriter stringWriter = new StringWriter();
            for (Node resoureFileNode : nodes)
            {
                append(resoureFileNode.getAsset(), stringWriter);
            }
            jar.addResource(new StringAsset(stringWriter.toString()), resourcePath);
        }
        return jar;
    }

    private void append(final Asset asset, final StringWriter to) throws IOException
    {
        BufferedReader in = null;
        try
        {
            in = new BufferedReader(new InputStreamReader(asset.openStream()));
            final char[] buffer = new char[1024];
            int n;
            while ((n = in.read(buffer)) != -1) 
            {
	            to.write(buffer, 0, n);
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

    private List<Node> getNodesForResource(final Map<String, List<Node>> map, final String resourcePath)
    {
        List<Node> list = map.get(resourcePath);
        if (list == null)
        {
            list = new ArrayList<Node>();
        }
        return list;
    }

    public static List<JavaArchive> fromFiles(final List<File> jars)
    {
        final List<JavaArchive> javaArchives = new ArrayList<JavaArchive>();
        for (File jar : jars)
        {
            javaArchives.add(ShrinkWrap.createFromZipFile(JavaArchive.class, jar));
        }
        return javaArchives;
    }

}
