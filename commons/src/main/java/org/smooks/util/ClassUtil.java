/*-
 * ========================LICENSE_START=================================
 * Smooks Commons
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
package org.smooks.util;

import org.smooks.assertion.AssertArgument;
import org.smooks.classpath.InstanceOfFilter;
import org.smooks.classpath.IsAnnotationPresentFilter;
import org.smooks.classpath.Scanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

/**
 * Utility methods to aid in class/resource loading.
 *
 * @author Kevin Conner
 */
public class ClassUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClassUtil.class);
    private static final Map<String, Class> primitives;

    static {
        primitives = new HashMap<String, Class>();
        primitives.put("int", Integer.TYPE);
        primitives.put("long", Long.TYPE);
        primitives.put("boolean", Boolean.TYPE);
        primitives.put("float", Float.TYPE);
        primitives.put("double", Double.TYPE);
        primitives.put("char", Character.TYPE);
        primitives.put("byte", Byte.TYPE);
        primitives.put("short", Short.TYPE);
    }

    /**
	 * Load the specified class.
	 *
	 * @param className
	 *            The name of the class to load.
	 * @param caller
	 *            The class of the caller.
	 * @return The specified class.
	 * @throws ClassNotFoundException
	 *             If the class cannot be found.
	 */
	public static Class forName(final String className, final Class caller) throws ClassNotFoundException {
		final ClassLoader threadClassLoader = Thread.currentThread().getContextClassLoader();

        Class primitiveClass = primitives.get(className);
        if(primitiveClass != null) {
            return primitiveClass;
        }

        if (threadClassLoader != null) {
			try {
				return threadClassLoader.loadClass(className);
			} catch (final ClassNotFoundException ignored) {
			} // ignore
		}

		ClassLoader classLoader = caller.getClassLoader();
		if (classLoader != null) {
			try {
				return classLoader.loadClass(className);
			} catch (final ClassNotFoundException ignored) {
			} // ignore
		}

		return Class.forName(className, true, ClassLoader.getSystemClassLoader());
	}

    /**
     * Get the specified resource as a stream.
     *
     * @param resourceName
     *            The name of the class to load.
     * @param caller
     *            The class of the caller.
     * @return The input stream for the resource or null if not found.
     */
    public static InputStream getResourceAsStream(final String resourceName, final Class caller) {
        final String resource;

        if (!resourceName.startsWith("/")) {
            final Package callerPackage = caller.getPackage();
            if (callerPackage != null) {
                resource = callerPackage.getName().replace('.', '/') + '/'
                        + resourceName;
            } else {
                resource = resourceName;
            }

            return getResourceAsStream(resource, caller.getClassLoader());
        } else {
            return getResourceAsStream(resourceName, caller.getClassLoader());
        }
    }

	/**
	 * Get the specified resource as a stream.
	 *
	 * @param resourceName The name of the class to load.
	 * @param classLoader The ClassLoader to use, if the resource is not located via the
     * Thread context ClassLoader.
	 * @return The input stream for the resource or null if not found.
	 */
	public static InputStream getResourceAsStream(final String resourceName, final ClassLoader classLoader) {
		final ClassLoader threadClassLoader = Thread.currentThread().getContextClassLoader();
        final String resource;

        if (resourceName.startsWith("/")) {
            resource = resourceName.substring(1);
        } else {
            resource = resourceName;
        }

		if (threadClassLoader != null) {
			final InputStream is = threadClassLoader.getResourceAsStream(resource);
			if (is != null) {
				return is;
			}
		}

		if (classLoader != null) {
			final InputStream is = classLoader.getResourceAsStream(resource);
			if (is != null) {
				return is;
			}
		}

		return ClassLoader.getSystemResourceAsStream(resource);
	}

    public static List<URL> getResources(String resourcePath, Class<?> caller) throws IOException {
        return getResources(resourcePath, caller.getClassLoader());
    }

	public static List<URL> getResources(String resourcePath, ClassLoader callerClassLoader) throws IOException {
        Set<URL> resources = new LinkedHashSet<URL>();

        if(resourcePath.startsWith("/")) {
            resourcePath = resourcePath.substring(1);
        }

        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        if (contextClassLoader != null) {
			resources.addAll(CollectionsUtil.toList(contextClassLoader.getResources(resourcePath)));
		}

		if (callerClassLoader != null) {
            resources.addAll(CollectionsUtil.toList(callerClassLoader.getResources(resourcePath)));
		}

		return new ArrayList<URL>(resources);
	}

    @SuppressWarnings("unused")
    public static List<Class> findInstancesOf(final Class type, String[] igrnoreList, String[] includeList) {
        InstanceOfFilter filter = new InstanceOfFilter(type, igrnoreList, includeList);
        return findInstancesOf(type, filter);
    }

    @SuppressWarnings("unused")
    public static List<Class> findInstancesOf(final Class type) {
        InstanceOfFilter filter = new InstanceOfFilter(type);
        return findInstancesOf(type, filter);
    }

    private static List<Class> findInstancesOf(Class type, InstanceOfFilter filter) {
        Scanner scanner = new Scanner(filter);

        try {
            long startTime = System.currentTimeMillis();
            scanner.scanClasspath(Thread.currentThread().getContextClassLoader());
            LOGGER.debug("Scanned classpath for instances of '" + type.getName() + "'.  Found " + filter.getClasses().size() + " matches. Scan took " + (System.currentTimeMillis() - startTime) + "ms.");
        } catch (IOException e) {
            throw new RuntimeException("Failed to search classspath for instances of '" + type.getName() + "'.", e);
        }

        return filter.getClasses();
    }

    @SuppressWarnings("unused")
    public static List<Class> findAnnotatedWith(Class<? extends Annotation> type, String[] igrnoreList, String[] includeList) {
        IsAnnotationPresentFilter filter = new IsAnnotationPresentFilter(type, igrnoreList, includeList);
        return findAnnotatedWith(type, filter);
    }

    @SuppressWarnings("unused")
    public static List<Class> findAnnotatedWith(Class<? extends Annotation> type) {
        IsAnnotationPresentFilter filter = new IsAnnotationPresentFilter(type);
        return findAnnotatedWith(type, filter);
    }

    private static List<Class> findAnnotatedWith(Class<? extends Annotation> type, IsAnnotationPresentFilter filter) {
        Scanner scanner = new Scanner(filter);

        try {
            long startTime = System.currentTimeMillis();
            scanner.scanClasspath(Thread.currentThread().getContextClassLoader());
            LOGGER.debug("Scanned classpath for class annotated with annotation '" + type.getName() + "'.  Found " + filter.getClasses().size() + " matches. Scan took " + (System.currentTimeMillis() - startTime) + "ms.");
        } catch (IOException e) {
            throw new RuntimeException("Failed to search classspath for class annotated with annotation '" + type.getName() + "'.", e);
        }

        return filter.getClasses();
    }

    @SuppressWarnings("unused")
    public static Object newProxyInstance(Class[] classes, InvocationHandler handler) {
        final ClassLoader threadClassLoader = Thread.currentThread().getContextClassLoader();

        if (threadClassLoader != null) {
            return Proxy.newProxyInstance(threadClassLoader, classes, handler);
        } else {
            return Proxy.newProxyInstance(ClassUtil.class.getClassLoader(), classes, handler);
        }
    }

    /**
     * Will try to create a List of classes that are listed
     * in the passed in file.
     * The fileName is expected to be found on the classpath.
     *
     * @param fileName The name of the file containing the list of classes,
     * one class name per line.
     * @param instanceOf The instanceof filter.
     * @return List<Class<T>>	list of the classes contained in the file.
     */
    public static <T> List<Class<T>> getClasses(final String fileName, Class<T> instanceOf) {
    	AssertArgument.isNotNull( fileName, "fileName" );
        AssertArgument.isNotNull( instanceOf, "instanceOf" );

        long start = System.currentTimeMillis();
        List<Class<T>> classes = new ArrayList<Class<T>>();
        List<URL> cpURLs;
        int resCount = 0;

        try {
            cpURLs = getResources(fileName, ClassUtil.class);
        } catch (IOException e) {
            throw new RuntimeException("Error getting resource URLs for resource : " + fileName, e);
        }

        for (URL url : cpURLs) {
            addClasses(url, instanceOf, classes);
            resCount++;
        }

        LOGGER.debug("Loaded " + classes.size() + " classes from " + resCount + " URLs through class list file "
                + fileName + ".  Process took " + (System.currentTimeMillis() - start) + "ms.  Turn on debug logging for more info.");

        return classes;
    }

    @SuppressWarnings("unchecked")
    private static <T>  void addClasses(URL url, Class<T> instanceOf, List<Class<T>> classes) {
        InputStream ins = null;
        BufferedReader br = null;

        try
    	{
            String className;
            int count = 0;

            // Get the input stream from the connection.  Need to set the defaultUseCaches
            URLConnection connection = url.openConnection();
            connection.setUseCaches(true);
            ins = connection.getInputStream();

            br = new BufferedReader( new InputStreamReader( ins ));
	    	while( (className = br.readLine()) != null )
	    	{
                Class clazz;

                className = className.trim();

                // Ignore blank lines and lines that start with a hash...
                if(className.equals("") || className.startsWith("#")) {
                    continue;
                }

                try {
                    clazz = forName(className, ClassUtil.class);
                } catch (ClassNotFoundException e) {
                    LOGGER.debug("Failed to load class '" + className + "'. Class not found.", e);
                    continue;
                }

                if(instanceOf.isAssignableFrom(clazz)) {
                    if(!contains(clazz.getName(), classes)) {
                        classes.add(clazz);
                    }
                    LOGGER.debug( "Adding " + className + " to list of classes");
                    count++;
                } else {
                    LOGGER.debug("Not adding class '" + clazz.getName() + "' to list.  Class does not implement/extend '" + instanceOf.getName() + "'.");
                }
            }
            LOGGER.debug("Loaded '" + count + "' classes listed in '" + url + "'.");
    	}
    	catch (IOException e)
		{
            throw new RuntimeException("Failed to read from file : " + url, e);
		}
    	finally
    	{
            close(br);
            close(ins);
        }
    }

    private static <T> boolean contains(String name, List<Class<T>> classes) {
        for (Class<T> aClass : classes) {
            if(aClass.getName().equals(name)) {
                LOGGER.debug("Class '" + name + "' already found on classpath.  Not adding to list.");
                return true;
            }
        }

        return false;
    }

    private static void close( final Closeable closable ) {
    	if(  closable != null )
    	{
			try
			{
				closable.close();
			}
    		catch (IOException e)
			{
    			LOGGER.debug( "Exception while trying to close : " + closable, e);
			}
    	}
    }

    public static String toFilePath(Package aPackage) {
        return "/" + aPackage.getName().replace('.', '/');
    }

    /**
	 * Checks if the class in the first parameter is assignable
	 * to one of the classes in the second or any later parameter.
	 *
	 * @param toFind  The class to check for assignment compatibility.
	 * @param classes The classes against which {@code toFind} should be checked.
	 * @return {@literal true} if {@code toFind} is assignable to any one of the
   * specified classes.
	 */
	public static boolean containsAssignableClass(final Class<?> toFind, final Class<?> ... classes) {
		return indexOfFirstAssignableClass(toFind, classes) != -1;
	}

    public static <U> void setField(Field field, U instance, Object value) throws IllegalAccessException {
        boolean isAccessible = field.isAccessible();

        if(!isAccessible) {
            field.setAccessible(true);
        }

        try {
            field.set(instance, value);
        } finally {
            field.setAccessible(isAccessible);
        }
    }

    public static <U> Object getField(Field field, U instance) throws IllegalAccessException {
        boolean isAccessible = field.isAccessible();

        if(!isAccessible) {
            field.setAccessible(true);
        }

        try {
            return field.get(instance);
        } finally {
            field.setAccessible(isAccessible);
        }
    }

    public static List<Field> getAnnotatedFields(Class runtimeClass, Class<? extends Annotation> annotationClass) {
    	List<Field> streamWriterFields = new ArrayList<Field>();
    	getAnnotatedFields(runtimeClass, streamWriterFields, annotationClass);
    	return streamWriterFields;
    }

    private static void getAnnotatedFields(Class runtimeClass, List<Field> annotatedFields, Class<? extends Annotation> annotationClass) {
        Field[] fields = runtimeClass.getDeclaredFields();

        // Work back up the Inheritance tree first...
        Class superClass = runtimeClass.getSuperclass();
        if(superClass != null) {
        	getAnnotatedFields(superClass, annotatedFields, annotationClass);
        }

        for (Field field : fields) {
        	if(field.isAnnotationPresent(annotationClass)) {
        		annotatedFields.add(field);
        	}
        }
    }

    /**
	 * Gets the array index of the first class within an array of classes to
   * which a specified class is assignable.
   *
	 * @param toFind  The class to check for assignment compatibility.
	 * @param classes The classes against which {@code toFind} should be checked.
	 * @return The array index of the first class within {@code classes} to which
   * {@code toFind} is assignable, if it can be assigned to one of the specified
   * classes, {@code -1} otherwise.
	 */
	public static int indexOfFirstAssignableClass(final Class<?> toFind, final Class<?> ... classes) {

		for(int i = 0; i < classes.length; i++) {
			final Class<?> cls = classes[i];

			if(cls.isAssignableFrom(toFind)) {
				return i;
			}

		}
		return -1;
	}

    public static String toSetterName(String property) {
        StringBuilder setterName = new StringBuilder();

        // Add the property string to the buffer...
        setterName.append(property);
        // Uppercase the first character...
        setterName.setCharAt(0, Character.toUpperCase(property.charAt(0)));
        // Prefix with "set"...
        setterName.insert(0, "set");

        return setterName.toString();
    }

    public static String toGetterName(String property) {
        StringBuilder getterName = new StringBuilder();

        // Add the property string to the buffer...
        getterName.append(property);
        // Uppercase the first character...
        getterName.setCharAt(0, Character.toUpperCase(property.charAt(0)));
        // Prefix with "get"...
        getterName.insert(0, "get");

        return getterName.toString();
    }

    public static String toIsGetterName(String property) {
        StringBuilder getterName = new StringBuilder();

        // Add the property string to the buffer...
        getterName.append(property);
        // Uppercase the first character...
        getterName.setCharAt(0, Character.toUpperCase(property.charAt(0)));
        // Prefix with "is"...
        getterName.insert(0, "is");

        return getterName.toString();
    }

    public static Method getSetterMethod(String setterName, Object bean, Class<?> setterParamType) {
        return getSetterMethod(setterName, bean.getClass(), setterParamType);
    }

    public static Method getSetterMethod(String setterName, Class beanclass, Class<?> setterParamType) {
        Method[] methods = beanclass.getMethods();

        for(Method method : methods) {
            if(method.getName().equals(setterName)) {
                Class<?>[] params = method.getParameterTypes();
                if(params.length == 1 && params[0].isAssignableFrom(setterParamType)) {
                    return method;
                }
            }
        }

        return null;
    }

    @SuppressWarnings("SameParameterValue")
    static Method getSetterMethodByProperty(String propertyName, Class<?> beanClass, Class<?> setterParamType) {
        return getSetterMethod(toSetterName(propertyName), beanClass, setterParamType);
    }

    public static Method getGetterMethod(String getterName, Object bean, Class<?> returnType) {
        return getGetterMethod(getterName, bean.getClass(), returnType);
    }

    private static Method getGetterMethod(String getterName, Class beanclass, Class<?> returnType) {
        Method[] methods = beanclass.getMethods();

        for(Method method : methods) {
            if(method.getName().equals(getterName)) {
                if(returnType != null) {
                    if(method.getReturnType().isAssignableFrom(returnType)) {
                        return method;
                    }
                } else {
                    return method;
                }
            }
        }

        return null;
    }

    public static Method getGetterMethodByProperty(String propertyName, Class<?> beanClass, Class<?> returnType) {
        Method getter = getGetterMethod(toGetterName(propertyName), beanClass, returnType);
        if(getter == null) {
            getter = getGetterMethod(toIsGetterName(propertyName), beanClass, returnType);
        }
        return getter;
    }
}
