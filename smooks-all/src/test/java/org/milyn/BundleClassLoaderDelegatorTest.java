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

import static org.junit.Assert.assertNotNull;

import java.net.URL;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Unit test for {@link BundleClassLoaderDelegator}.
 * 
 * @author Daniel Bevenius
 *
 */
public class BundleClassLoaderDelegatorTest
{
    @Rule
    public TemporaryFolder folder = new TemporaryFolder();
    
    @Test
    public void getResourceFromBundle() throws Exception
    {
        final JavaArchive jar = ShrinkWrap.create(JavaArchive.class, "bundle.jar");
        jar.addResource(folder.newFile("test.properties"));
        
        BundleClassLoaderDelegator bcl = new BundleClassLoaderDelegator(new MockBundle(jar), getClass().getClassLoader());
        
        URL resource = bcl.getResource("test.properties");
        assertNotNull(resource);
    }
    
}
