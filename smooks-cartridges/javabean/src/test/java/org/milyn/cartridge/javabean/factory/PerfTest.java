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
package org.milyn.cartridge.javabean.factory;

import junit.framework.TestCase;
import org.milyn.cartridge.javabean.factory.BasicFactoryDefinitionParser;
import org.milyn.cartridge.javabean.factory.Factory;
import org.milyn.cartridge.javabean.factory.MVELFactoryDefinitionParser;
import org.milyn.container.ExecutionContext;
import org.milyn.container.MockExecutionContext;

import java.lang.reflect.InvocationTargetException;

public class PerfTest extends TestCase {

	private static boolean DISABLED = true;

	private static final int PARSE_COUNT = 1000;

	private static final int INVOKE_COUNT = 1000000;

    public void test_parse_basic() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InterruptedException {
    	if(DISABLED) return;

    	ExecutionContext executionContext = new MockExecutionContext();

        loopParseBasic(10000, executionContext);

        Thread.sleep(1000);

        long start = System.currentTimeMillis();
        loopParseBasic(PARSE_COUNT, executionContext);
        System.out.println("Basic Parser Time: " + (System.currentTimeMillis() - start));
    }

    public void test_invoke_static_basic() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InterruptedException {
    	if(DISABLED) return;

    	ExecutionContext executionContext = new MockExecutionContext();

    	BasicFactoryDefinitionParser parser = new BasicFactoryDefinitionParser();
        Factory<?> factory = parser.parse("org.milyn.javabean.TestFactory#newArrayList");

        loopInvoke(10000, factory, executionContext);

        Thread.sleep(1000);

        long start = System.currentTimeMillis();
        loopInvoke(INVOKE_COUNT, factory, executionContext);
        System.out.println("Basic Invoke Static factory Time: " + (System.currentTimeMillis() - start));
    }

    public void test_invoke_instance_basic() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InterruptedException {
    	if(DISABLED) return;

    	ExecutionContext executionContext = new MockExecutionContext();

    	BasicFactoryDefinitionParser parser = new BasicFactoryDefinitionParser();
        Factory<?> factory = parser.parse("org.milyn.javabean.TestFactory#newInstance.newLinkedList");

        loopInvoke(10000, factory, executionContext);

        Thread.sleep(1000);

        long start = System.currentTimeMillis();
        loopInvoke(INVOKE_COUNT, factory, executionContext);
        System.out.println("Basic Invoke instance factory Time: " + (System.currentTimeMillis() - start));
    }


	public void test_parse_MVEL() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InterruptedException {
		if(DISABLED) return;

		ExecutionContext executionContext = new MockExecutionContext();

        loopParseMVEL(10000, executionContext);

        Thread.sleep(1000);

        long start = System.currentTimeMillis();
        loopParseMVEL(PARSE_COUNT, executionContext);
        System.out.println("MVEL Parser Time: " + (System.currentTimeMillis() - start));
    }

	public void test_invoke_static_MVEL() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InterruptedException {
		if(DISABLED) return;

		ExecutionContext executionContext = new MockExecutionContext();

    	MVELFactoryDefinitionParser parser = new MVELFactoryDefinitionParser();
        Factory<?> factory = parser.parse("org.milyn.javabean.TestFactory.newArrayList()");

        loopInvoke(10000, factory, executionContext);

        Thread.sleep(1000);

        long start = System.currentTimeMillis();
        loopInvoke(INVOKE_COUNT, factory, executionContext);

        System.out.println("MVEL Invoke static factory Time: " + (System.currentTimeMillis() - start));
    }

	public void test_invoke_instance_MVEL() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, InterruptedException {
		if(DISABLED) return;

		ExecutionContext executionContext = new MockExecutionContext();

    	MVELFactoryDefinitionParser parser = new MVELFactoryDefinitionParser();
        Factory<?> factory = parser.parse("org.milyn.javabean.TestFactory.newInstance().newLinkedList()");

        loopInvoke(10000, factory, executionContext);

        Thread.sleep(1000);

        long start = System.currentTimeMillis();
        loopInvoke(INVOKE_COUNT, factory, executionContext);

        System.out.println("MVEL Invoke instance factory Time: " + (System.currentTimeMillis() - start));
    }

	private void loopParseBasic(int count, ExecutionContext executionContext) {
		for(int i = 0; i < count; i++) {
			BasicFactoryDefinitionParser parser = new BasicFactoryDefinitionParser();
	        parser.parse("org.milyn.javabean.TestFactory#newInstance.newLinkedList");
        }
	}

	private void loopParseMVEL(int count, ExecutionContext executionContext) {
		for(int i = 0; i < count; i++) {
            MVELFactoryDefinitionParser parser = new MVELFactoryDefinitionParser();
            parser.parse("org.milyn.javabean.TestFactory.newInstance().newLinkedList()");
        }
	}

	private void loopInvoke(int count, Factory<?> factory, ExecutionContext executionContext) {
		for(int i = 0; i < count; i++) {
			factory.create(executionContext);
        }
	}

}
