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
package org.milyn.calc;

import static org.testng.AssertJUnit.*;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.cdr.annotation.Configurator;
import org.milyn.container.MockExecutionContext;
import org.milyn.javabean.context.BeanContext;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

/**
 * Unit test for the Counter class
 *
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 *
 */
public class CounterTest {

	private final String selector = "x";

	private final String beanId = "bean";


	private SmooksResourceConfiguration config;

	private MockExecutionContext executionContext;
	private BeanContext beanContext;


	@Test ( groups = "unit" )
    public void test_default_count() throws ParserConfigurationException, SAXException, IOException
    {

		config.setParameter("beanId", beanId );

		Counter counter = new Counter();
		Configurator.configure( counter, config, executionContext.getContext() );

		counter.visitBefore((Element)null, executionContext);

		Long value = getCounterValue();

		assertEquals(0, value.longValue());

		counter.visitBefore((Element)null, executionContext);

		value = getCounterValue();

		assertEquals(1, value.longValue());

    }

	@Test ( groups = "unit" )
    public void test__static_amount() throws ParserConfigurationException, SAXException, IOException
    {

		config.setParameter("beanId", beanId );
		config.setParameter("amount", "10" );

		Counter counter = new Counter();
		Configurator.configure( counter, config, executionContext.getContext() );

		counter.visitBefore((Element)null, executionContext);

		Long value = getCounterValue();

		assertEquals(0, value.longValue());

		counter.visitBefore((Element)null, executionContext);

		value = getCounterValue();

		assertEquals(10, value.longValue());

		counter.visitBefore((Element)null, executionContext);

		value = getCounterValue();

		assertEquals(20, value.longValue());

    }

	@Test ( groups = "unit" )
    public void test_static_start() throws ParserConfigurationException, SAXException, IOException
    {

		config.setParameter("beanId", beanId );
		config.setParameter("start", "100" );

		Counter counter = new Counter();
		Configurator.configure( counter, config, executionContext.getContext() );

		counter.visitBefore((Element)null, executionContext);

		Long value = getCounterValue();

		assertEquals(100, value.longValue());

		counter.visitBefore((Element)null, executionContext);

		value = getCounterValue();

		assertEquals(101, value.longValue());

    }

	@Test ( groups = "unit" )
    public void test_direction() throws ParserConfigurationException, SAXException, IOException
    {

		config.setParameter("beanId", beanId );
		config.setParameter("direction", "DECREMENT" );

		Counter counter = new Counter();
		Configurator.configure( counter, config, executionContext.getContext() );

		counter.visitBefore((Element)null, executionContext);

		Long value = getCounterValue();

		assertEquals(0, value.longValue());

		counter.visitBefore((Element)null, executionContext);

		value = getCounterValue();

		assertEquals(-1, value.longValue());

    }

	@Test ( groups = "unit" )
    public void test_amountExpression() throws ParserConfigurationException, SAXException, IOException
    {

		config.setParameter("beanId", beanId );
		config.setParameter("amountExpression", "5*5" );

		Counter counter = new Counter();
		Configurator.configure( counter, config, executionContext.getContext() );

		counter.visitBefore((Element)null, executionContext);

		Long value = getCounterValue();

		assertEquals(0, value.longValue());

		counter.visitBefore((Element)null, executionContext);

		value = getCounterValue();

		assertEquals(25, value.longValue());

    }


	@Test ( groups = "unit" )
    public void test_startExpression() throws ParserConfigurationException, SAXException, IOException
    {

		config.setParameter("beanId", beanId );
		config.setParameter("startExpression", "5*5" );

		Counter counter = new Counter();
		Configurator.configure( counter, config, executionContext.getContext() );

		counter.visitBefore((Element)null, executionContext);

		Long value = getCounterValue();

		assertEquals(25, value.longValue());

		counter.visitBefore((Element)null, executionContext);

		value = getCounterValue();

		assertEquals(26, value.longValue());

    }


	@Test ( groups = "unit" )
    public void test_resetCondition() throws ParserConfigurationException, SAXException, IOException
    {

		config.setParameter("beanId", beanId );
		config.setParameter("resetCondition", "bean == 1" );

		Counter counter = new Counter();
		Configurator.configure( counter, config, executionContext.getContext() );

		counter.visitBefore((Element)null, executionContext);

		Long value = getCounterValue();

		assertEquals(0, value.longValue());

		counter.visitBefore((Element)null, executionContext);

		value = getCounterValue();

		assertEquals(1, value.longValue());

		counter.visitBefore((Element)null, executionContext);

		value = getCounterValue();

		assertEquals(0, value.longValue());

    }


	private long getCounterValue() {
		return getCounterValue(beanId);
	}

	private long getCounterValue(String beanId) {
		Object valueObj = beanContext.getBean(beanId);

		assertNotNull(valueObj);
		assertTrue(valueObj instanceof Long);

		return (Long) valueObj;
	}

	@BeforeMethod ( groups = "unit" )
	public void init() {

		config = new SmooksResourceConfiguration(selector, Counter.class.getName());
		executionContext = new MockExecutionContext();
		beanContext = executionContext.getBeanContext();
	}

}
