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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.resources.FileResource;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.milyn.ResourceMerger;

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
	 * True indicates that it is OK to overwrite an existing jar file. False will produce a build error.
	 */
    private boolean overwrite;
    
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
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        final JavaArchive mergedJar = resourceMerger.mergeJars(jarName, ResourceMerger.fromFiles(jars));
        mergedJar.setManifest(manifest);
        addAllClasses(mergedJar);
        
        final File newJar = exportJarFile(mergedJar, jarName);
        
        log("Produced [" + newJar.getAbsolutePath());
    }
    
    private File exportJarFile(final JavaArchive jar, final String jarname)
    {
        final File newJar = new File(jarName);
        if (newJar.exists())
        {
            newJar.delete();
        }
        jar.as(ZipExporter.class).exportZip(newJar, overwrite);
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

    private void addAllClasses(final JavaArchive to)
    {
        for (String className : classes)
        {
            to.addClass(className.trim());
        }
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

    public void setOverwrite(boolean overwrite)
    {
        this.overwrite = overwrite;
    }

    public void setClasses(String classes)
    {
        String[] split = classes.split(",");
        this.classes.addAll(Arrays.asList(split));
    }
}
