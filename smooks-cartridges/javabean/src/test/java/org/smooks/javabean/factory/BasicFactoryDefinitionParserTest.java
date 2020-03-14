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
package org.smooks.javabean.factory;

import org.junit.Test;
import static org.junit.Assert.*;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.smooks.container.MockExecutionContext;

import java.util.ArrayList;
import java.util.LinkedList;

public class BasicFactoryDefinitionParserTest {

        @Test
	public void test_create_StaticMethodFactory() {

		BasicFactoryDefinitionParser parser = new BasicFactoryDefinitionParser();

		Factory<?> factory = parser.parse("org.smooks.javabean.TestFactory#newArrayList");

		Object result = factory.create(new MockExecutionContext());

		assertNotNull(result);
		assertTrue(result instanceof ArrayList<?>);

	}

        @Test
	public void test_create_FactoryInstanceFactory() {

		BasicFactoryDefinitionParser parser = new BasicFactoryDefinitionParser();

		Factory<?> factory = parser.parse("org.smooks.javabean.TestFactory#newInstance.newLinkedList");

		Object result = factory.create(new MockExecutionContext());

		assertNotNull(result);
		assertTrue(result instanceof LinkedList<?>);

	}

        @Test
	public void test_caching() {

		BasicFactoryDefinitionParser parser = new BasicFactoryDefinitionParser();

		Factory<?> factory1 = parser.parse("org.smooks.javabean.TestFactory#newArrayList");
		Factory<?> factory2 = parser.parse("org.smooks.javabean.TestFactory#newArrayList");
		Factory<?> factory3 = parser.parse("org.smooks.javabean.TestFactory#newInstance.newLinkedList");

		assertSame(factory1, factory2);
		assertNotSame(factory1, factory3);

	}

        @Test
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

        @Test
	public void test_null_factory() {

		BasicFactoryDefinitionParser parser = new BasicFactoryDefinitionParser();

		Factory<?> factory = parser.parse("org.smooks.javabean.TestFactory#getNull.newLinkedList");

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

        @Test
	public void test_invalid_class() {

		BasicFactoryDefinitionParser parser = new BasicFactoryDefinitionParser();


		FactoryException exception = null;

		try {
			parser.parse("org.smooks.javabean.DoesNotExist#newArrayList");
		} catch (FactoryException e) {
			exception = e;
		}

		if(exception == null) {
			fail("The parser didn't throw a FactoryException");
		}

		assertTrue(ExceptionUtils.indexOfThrowable(exception, ClassNotFoundException.class) >= 0);
	}

        @Test
	public void test_invalid_method() {

		BasicFactoryDefinitionParser parser = new BasicFactoryDefinitionParser();

		FactoryException exception = null;

		try {
			parser.parse("org.smooks.javabean.TestFactory#doesNotExist");
		} catch (FactoryException e) {
			exception = e;
		}

		if(exception == null) {
			fail("The parser didn't throw a FactoryException");
		}

		assertTrue(ExceptionUtils.indexOfThrowable(exception, NoSuchMethodException.class) >= 0);
	}

        @Test
	public void test_not_static_method() {

		BasicFactoryDefinitionParser parser = new BasicFactoryDefinitionParser();

		FactoryException exception = null;

		try {
			parser.parse("org.smooks.javabean.TestFactory#newLinkedList");
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
