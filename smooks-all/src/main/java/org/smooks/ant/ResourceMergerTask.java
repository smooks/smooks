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
package org.smooks.ant;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.Resource;
import org.apache.tools.ant.types.resources.FileResource;
import org.smooks.ResourceMerger;
import org.smooks.archive.Archive;
import org.smooks.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarFile;

/**
 * ResourceMergerTask is an ANT task that delegate to {@link ResourceMerger}
 * to merge jars and in the process merge string base resource files.
 * </p>
 * 
 * @author Daniel Bevenius
 * @since 1.4
 *
 */
public class ResourceMergerTask extends Task
{
    /**
     * The name of an existing jar or will be used as name of the jar that will be created.
     */
	private String jarName;
	
	/**
	 * List of resource paths that should be merge into one. 
	 */
	private final List<String> resourcesPaths = new ArrayList<String>();
	
	/**
	 * List of classes that should be included in the produced jar. This is in addition to the classes in the specified jars.
	 */
	private final List<String> classes = new ArrayList<String>();
	
	/**
	 * The jar files that are to be merged together.
	 */
	private FileSet fileSet;
	
    /**
     * The jar MANIFEST.MF to be used.
     */
    private File manifest;
    
    @Override
    public void execute() throws BuildException
    {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        try
        {
	        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
            mergeJars();
        } 
        catch (final IOException e)
        {
            throw new BuildException(e.getMessage(), e);
        }
        finally
        {
            Thread.currentThread().setContextClassLoader(contextClassLoader);
        }
    }
    
    private void mergeJars() throws IOException
    {
        log("Building " + jarName);
        final List<File> jars = getJarsFromFileSet();
        final ResourceMerger resourceMerger = new ResourceMerger(resourcesPaths);
        final Archive mergedJar = resourceMerger.mergeJars(jarName, jars.toArray(new File[]{}));
        setManifest(mergedJar);
        final File newJar = exportJarFile(mergedJar, jarName);
        log("Produced [" + newJar.getAbsolutePath());
    }
    
    private void setManifest(Archive to) throws IOException
    {
        if (manifest != null)
        {
	        to.addEntry(JarFile.MANIFEST_NAME, FileUtils.readFile(manifest));
        }
    }
    
    private File exportJarFile(final Archive jar, final String jarname) throws IOException
    {
        final File newJar = new File(jarName);
        if (newJar.exists())
        {
            newJar.delete();
        }
        jar.toOutputStream(new java.util.zip.ZipOutputStream(new FileOutputStream(newJar)));
        return newJar;
    }
    
    @SuppressWarnings("unchecked")
    private List<File> getJarsFromFileSet()
    {
        final List<File> jars = new ArrayList<File>();
        final Iterator<Resource> iterator = fileSet.iterator();
        while(iterator.hasNext())
        {
            jars.add(((FileResource) iterator.next()).getFile());
        }
        return jars;
    }

    public void addFileSet(FileSet jars)
    {
        this.fileSet = jars;
    }

    public void setJarName(String jarName)
    {
        this.jarName = jarName;
    }

    public void setResources(String resources)
    {
        String[] split = resources.split(",");
        for (String resource : split)
        {
	        resourcesPaths.add(resource.trim());
        }
    }

    public void setManifest(File manifest)
    {
        this.manifest = manifest;
    }

    public void setClasses(String classes)
    {
        String[] split = classes.split(",");
        this.classes.addAll(Arrays.asList(split));
    }
}
