/*
	Milyn - Copyright (C) 2006 - 2010

	This library is free software; you can redistribute it and/or
	modify it under the terms of the GNU Lesser General Public
	License (version 2.1) as published by the Free Software
	Foundation.

	This library is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

	See the GNU Lesser General Public License for more details:
	http://www.gnu.org/licenses/lgpl.txt
*/
package org.milyn.classpath;

import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class ScannerTest extends TestCase {
	private Logger log = Logger.getLogger( ScannerTest.class );
	
	private URLClassLoader classLoader;
	
	public void setUp() throws MalformedURLException
	{
		File targetDir = new File ( "target" );
        File classesDir = new File( targetDir, "classes" );
        File testClassesDir = new File( targetDir, "test-classes" );
        classLoader = new URLClassLoader(new URL[] { classesDir.toURL(), testClassesDir.toURL() });
	}

    public void test_instanceof_has_include() throws IOException {
    	
        InstanceOfFilter filter = new InstanceOfFilter(Filter.class, null, new String[] {"org/milyn"});
        Scanner scanner = new Scanner(filter);

        long start = System.currentTimeMillis();
        scanner.scanClasspath(classLoader);
        log.debug("Took: " + (System.currentTimeMillis() - start));
        List<Class> classes = filter.getClasses();

        log.debug(classes);
        assertEquals(4, classes.size());
        assertTrue(classes.contains(InstanceOfFilter.class));
        assertTrue(classes.contains(IsAnnotationPresentFilter.class));
        assertTrue(classes.contains(AbstractFilter.class));
        assertTrue(classes.contains(Filter.class));
    }

    public void test_annotated_has_include() throws IOException {
        IsAnnotationPresentFilter filter = new IsAnnotationPresentFilter(TestAnnotation.class, null, new String[] {"org/milyn"});
        Scanner scanner = new Scanner(filter);

        long start = System.currentTimeMillis();
        scanner.scanClasspath(classLoader);
        log.debug("Took: " + (System.currentTimeMillis() - start));
        List<Class> classes = filter.getClasses();

        log.debug(classes);
        assertEquals(2, classes.size());
        assertTrue(classes.contains(AnnotatedClass1.class));
        assertTrue(classes.contains(AnnotatedClass1.class));
    }

    public void test_instanceof_no_include() throws IOException {
        InstanceOfFilter filter = new InstanceOfFilter(Filter.class);
        Scanner scanner = new Scanner(filter);

        long start = System.currentTimeMillis();
        scanner.scanClasspath(classLoader);
        log.debug("Took: " + (System.currentTimeMillis() - start));
        List<Class> classes = filter.getClasses();

        log.debug(classes);
        assertEquals(4, classes.size());
        assertTrue(classes.contains(InstanceOfFilter.class));
        assertTrue(classes.contains(IsAnnotationPresentFilter.class));
        assertTrue(classes.contains(AbstractFilter.class));
        assertTrue(classes.contains(Filter.class));
    }
}
