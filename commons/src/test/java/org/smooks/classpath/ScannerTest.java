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
package org.smooks.classpath;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class ScannerTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScannerTest.class);
	
	private URLClassLoader classLoader;

        @Before	
	public void setUp() throws MalformedURLException
	{
		File targetDir = new File ( "target" );
        File classesDir = new File( targetDir, "classes" );
        File testClassesDir = new File( targetDir, "test-classes" );
        classLoader = new URLClassLoader(new URL[] { classesDir.toURI().toURL(), testClassesDir.toURI().toURL() });
	}

    @Test
    public void test_instanceof_has_include() throws IOException {
    	
        InstanceOfFilter filter = new InstanceOfFilter(Filter.class, null, new String[] {"org/smooks"});
        Scanner scanner = new Scanner(filter);

        long start = System.currentTimeMillis();
        scanner.scanClasspath(classLoader);
        LOGGER.debug("Took: " + (System.currentTimeMillis() - start));
        List<Class> classes = filter.getClasses();

        LOGGER.debug(classes.toString());
        assertEquals(4, classes.size());
        assertTrue(classes.contains(InstanceOfFilter.class));
        assertTrue(classes.contains(IsAnnotationPresentFilter.class));
        assertTrue(classes.contains(AbstractFilter.class));
        assertTrue(classes.contains(Filter.class));
    }

    @Test
    public void test_annotated_has_include() throws IOException {
        IsAnnotationPresentFilter filter = new IsAnnotationPresentFilter(TestAnnotation.class, null, new String[] {"org/smooks"});
        Scanner scanner = new Scanner(filter);

        long start = System.currentTimeMillis();
        scanner.scanClasspath(classLoader);
        LOGGER.debug("Took: " + (System.currentTimeMillis() - start));
        List<Class> classes = filter.getClasses();

        LOGGER.debug(classes.toString());
        assertEquals(2, classes.size());
        assertTrue(classes.contains(AnnotatedClass1.class));
        assertTrue(classes.contains(AnnotatedClass1.class));
    }

    @Test
    public void test_instanceof_no_include() throws IOException {
        InstanceOfFilter filter = new InstanceOfFilter(Filter.class);
        Scanner scanner = new Scanner(filter);

        long start = System.currentTimeMillis();
        scanner.scanClasspath(classLoader);
        LOGGER.debug("Took: " + (System.currentTimeMillis() - start));
        List<Class> classes = filter.getClasses();

        LOGGER.debug(classes.toString());
        assertEquals(4, classes.size());
        assertTrue(classes.contains(InstanceOfFilter.class));
        assertTrue(classes.contains(IsAnnotationPresentFilter.class));
        assertTrue(classes.contains(AbstractFilter.class));
        assertTrue(classes.contains(Filter.class));
    }
}
