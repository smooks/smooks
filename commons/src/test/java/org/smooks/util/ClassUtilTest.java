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
package org.smooks.util;

import junit.framework.TestCase;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author <a href="mailto:daniel.bevenius@gmail.com">Daniel Bevenius</a>
 *
 */
public class ClassUtilTest extends TestCase
{
	private final String fileName = "META-INF/classes.inf";
	private final String testClassesDirName = "target" + File.separator + "test-classes";
	private final File jarFile = new File ( testClassesDirName + File.separator + "test.jar");

	public void test_getClassesNegative()
	{
		try
		{
			ClassUtil.getClasses( null, null);
		}
		catch (Exception e)
		{
			assertTrue ( e instanceof IllegalArgumentException );
		}
	}

	public void test_getClasses()
  {
		List<Class<Object>> classes = ClassUtil.getClasses( fileName, Object.class);
		assertNotNull( classes );
		assertTrue( classes.contains( String.class ) );
		assertTrue( classes.contains( Integer.class ) );
	}

	@Override
	public void setUp() throws MalformedURLException
	{
		File testClassesDir = new File( testClassesDirName );
		URLClassLoader urlc = new URLClassLoader( new URL[] { jarFile.toURI().toURL(), testClassesDir.toURI().toURL() } );
		Thread.currentThread().setContextClassLoader( urlc );
	}

	public void test_indexOffAssignableClass() {

		assertEquals(0, ClassUtil.indexOfFirstAssignableClass(ArrayList.class, List.class)) ;
		assertEquals(1, ClassUtil.indexOfFirstAssignableClass(ArrayList.class, String.class, List.class)) ;
		assertEquals(1, ClassUtil.indexOfFirstAssignableClass(ArrayList.class, String.class, List.class, List.class)) ;
		assertEquals(-1, ClassUtil.indexOfFirstAssignableClass(ArrayList.class, String.class, String.class, String.class)) ;

	}

	public void test_containsAssignableClass() {

		assertEquals(true, ClassUtil.containsAssignableClass(ArrayList.class, List.class)) ;
		assertEquals(true, ClassUtil.containsAssignableClass(ArrayList.class, String.class, List.class)) ;
		assertEquals(true, ClassUtil.containsAssignableClass(ArrayList.class, String.class, List.class, List.class)) ;
		assertEquals(false, ClassUtil.containsAssignableClass(ArrayList.class, String.class, String.class, String.class)) ;

	}

    public void test_get_setter() {
        Method nameSetter = ClassUtil.getSetterMethodByProperty("name", Animal.class, String.class);
        Method ageSetter = ClassUtil.getSetterMethodByProperty("age", Animal.class, int.class);

        assertNotNull(nameSetter);
        assertEquals("setName", nameSetter.getName());
        assertNotNull(ageSetter);
        assertEquals("setAge", ageSetter.getName());
    }

    public void test_get_getter() {
        Method nameGetter = ClassUtil.getGetterMethodByProperty("name", Animal.class, String.class);
        Method ageGetter = ClassUtil.getGetterMethodByProperty("age", Animal.class, int.class);

        assertNotNull(nameGetter);
        assertEquals("getName", nameGetter.getName());
        assertNotNull(ageGetter);
        assertEquals("getAge", ageGetter.getName());
    }
}
