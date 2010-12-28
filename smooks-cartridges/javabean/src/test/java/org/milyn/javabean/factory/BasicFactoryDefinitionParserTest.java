/*
 * Milyn - Copyright (C) 2006 - 2010
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License (version 2.1) as published by the Free Software
 *  Foundation.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 *  See the GNU Lesser General Public License for more details:
 *  http://www.gnu.org/licenses/lgpl.txt
 */
package org.milyn.javabean.factory;

import junit.framework.TestCase;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.milyn.container.MockExecutionContext;

import java.util.ArrayList;
import java.util.LinkedList;

public class BasicFactoryDefinitionParserTest extends TestCase {

	public void test_create_StaticMethodFactory() {

		BasicFactoryDefinitionParser parser = new BasicFactoryDefinitionParser();

		Factory<?> factory = parser.parse("org.milyn.javabean.TestFactory#newArrayList");

		Object result = factory.create(new MockExecutionContext());

		assertNotNull(result);
		assertTrue(result instanceof ArrayList<?>);

	}

	public void test_create_FactoryInstanceFactory() {

		BasicFactoryDefinitionParser parser = new BasicFactoryDefinitionParser();

		Factory<?> factory = parser.parse("org.milyn.javabean.TestFactory#newInstance.newLinkedList");

		Object result = factory.create(new MockExecutionContext());

		assertNotNull(result);
		assertTrue(result instanceof LinkedList<?>);

	}


	public void test_caching() {

		BasicFactoryDefinitionParser parser = new BasicFactoryDefinitionParser();

		Factory<?> factory1 = parser.parse("org.milyn.javabean.TestFactory#newArrayList");
		Factory<?> factory2 = parser.parse("org.milyn.javabean.TestFactory#newArrayList");
		Factory<?> factory3 = parser.parse("org.milyn.javabean.TestFactory#newInstance.newLinkedList");

		assertSame(factory1, factory2);
		assertNotSame(factory1, factory3);

	}

	public void test_invalid_definition() {

		BasicFactoryDefinitionParser parser = new BasicFactoryDefinitionParser();

		InvalidFactoryDefinitionException exception = null;

		try {
			parser.parse("garbage");
		} catch (InvalidFactoryDefinitionException e) {
			exception = e;
		}

		if(exception == null) {
			fail("The parser didn't throw an exception");
		}

		assertTrue(exception.getMessage().contains("garbage"));
	}

	public void test_null_factory() {

		BasicFactoryDefinitionParser parser = new BasicFactoryDefinitionParser();

		Factory<?> factory = parser.parse("org.milyn.javabean.TestFactory#getNull.newLinkedList");

		NullPointerException exception = null;

		try {
			factory.create(new MockExecutionContext());
		} catch (NullPointerException e) {
			exception = e;
		}

		if(exception == null) {
			fail("The parser didn't throw an NullPointerException");
		}
	}

	public void test_invalid_class() {

		BasicFactoryDefinitionParser parser = new BasicFactoryDefinitionParser();


		FactoryException exception = null;

		try {
			parser.parse("org.milyn.javabean.DoesNotExist#newArrayList");
		} catch (FactoryException e) {
			exception = e;
		}

		if(exception == null) {
			fail("The parser didn't throw a FactoryException");
		}

		assertTrue(ExceptionUtils.indexOfThrowable(exception, ClassNotFoundException.class) >= 0);
	}

	public void test_invalid_method() {

		BasicFactoryDefinitionParser parser = new BasicFactoryDefinitionParser();

		FactoryException exception = null;

		try {
			parser.parse("org.milyn.javabean.TestFactory#doesNotExist");
		} catch (FactoryException e) {
			exception = e;
		}

		if(exception == null) {
			fail("The parser didn't throw a FactoryException");
		}

		assertTrue(ExceptionUtils.indexOfThrowable(exception, NoSuchMethodException.class) >= 0);
	}

	public void test_not_static_method() {

		BasicFactoryDefinitionParser parser = new BasicFactoryDefinitionParser();

		FactoryException exception = null;

		try {
			parser.parse("org.milyn.javabean.TestFactory#newLinkedList");
		} catch (FactoryException e) {
			exception = e;
		}

		if(exception == null) {
			fail("The parser didn't throw a FactoryException");
		}

		assertTrue(ExceptionUtils.indexOfThrowable(exception, NoSuchMethodException.class) >= 0);
		assertTrue(ExceptionUtils.getFullStackTrace(exception).contains("static"));
	}
}
