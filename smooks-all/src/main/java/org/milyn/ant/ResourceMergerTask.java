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
package org.milyn.ant;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarFile;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.resources.FileResource;
import org.milyn.ResourceMerger;
import org.milyn.archive.Archive;
import org.milyn.io.FileUtils;

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
	private List<String> resourcesPaths = new ArrayList<String>();
	
	/**
	 * List of classes that should be included in the produced jar. This is in addition to the classes in the specified jars.
	 */
	private List<String> classes = new ArrayList<String>();
	
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
        final Iterator<FileResource> iterator = fileSet.iterator();
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
