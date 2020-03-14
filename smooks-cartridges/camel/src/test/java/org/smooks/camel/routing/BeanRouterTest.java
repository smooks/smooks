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
package org.smooks.camel.routing;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Before;
import org.junit.Test;
import org.smooks.Smooks;
import org.smooks.SmooksException;
import org.smooks.cdr.SmooksResourceConfiguration;
import org.smooks.cdr.annotation.Configurator;
import org.smooks.container.ExecutionContext;
import org.smooks.container.MockApplicationContext;
import org.smooks.container.standalone.StandaloneExecutionContext;
import org.smooks.javabean.context.BeanContext;
import org.smooks.javabean.lifecycle.BeanContextLifecycleEvent;
import org.smooks.javabean.lifecycle.BeanLifecycle;

/**
 * Unit test for {@link BeanRouter}.
 * 
 * @author Daniel Bevenius
 *
 */
public class BeanRouterTest extends CamelTestSupport
{
	private static final String END_POINT_URI = "mock://beanRouterUnitTest";
	private static final String BEAN_ID = "testBeanId";
	private static final String HEADER_ID = "testHeaderId";
	
	private StandaloneExecutionContext smooksExecutionContext;
	private MockEndpoint endpoint;
	private MyBean myBean = new MyBean("bajja");
	private BeanContext beanContext;
	
	@Test
    public void visitAfter() throws Exception
    {
    	endpoint.setExpectedMessageCount(1);
    	endpoint.expectedBodiesReceived(myBean);
    	createBeanRouter(BEAN_ID, END_POINT_URI).visitAfter(null, smooksExecutionContext);
    	endpoint.assertIsSatisfied();
    }

    @Test (expected = SmooksException.class)
    public void visitAfterWithMissingBeanInSmookBeanContext() throws SmooksException, IOException
    {
    	when(beanContext.getBean(BEAN_ID)).thenReturn(null);
    	createBeanRouter(BEAN_ID, END_POINT_URI).visitAfter(null, smooksExecutionContext);
    }

    @Test
    public void routeUsingOnlyBeanId() throws Exception
    {
    	endpoint.setExpectedMessageCount(1);
    	endpoint.expectedBodiesReceived(myBean);

    	final Smooks smooks = new Smooks();
        final ExecutionContext execContext = smooks.createExecutionContext();
        
    	BeanRouter beanRouter = createBeanRouter(null, BEAN_ID, END_POINT_URI);
    	beanRouter.executeExecutionLifecycleInitialize(execContext);
        execContext.getBeanContext().addBean(BEAN_ID, myBean);

        // Force an END event
        execContext.getBeanContext().notifyObservers(new BeanContextLifecycleEvent(execContext,
                null, BeanLifecycle.END_FRAGMENT, execContext.getBeanContext().getBeanId(BEAN_ID), myBean));

    	endpoint.assertIsSatisfied();
    }

    @Test
    public void routeBeanWithHeaders() throws Exception
    {
    	endpoint.setExpectedMessageCount(1);
    	endpoint.expectedHeaderReceived(HEADER_ID, myBean);

    	final Smooks smooks = new Smooks();
        final ExecutionContext execContext = smooks.createExecutionContext();
        
    	BeanRouter beanRouter = createBeanRouter(null, BEAN_ID, END_POINT_URI);
    	beanRouter.executeExecutionLifecycleInitialize(execContext);
        execContext.getBeanContext().addBean(BEAN_ID, myBean);
        execContext.getBeanContext().addBean(HEADER_ID, myBean);

        // Force an END event
        execContext.getBeanContext().notifyObservers(new BeanContextLifecycleEvent(execContext,
                null, BeanLifecycle.END_FRAGMENT, execContext.getBeanContext().getBeanId(BEAN_ID), myBean));

    	endpoint.assertIsSatisfied();
    }
    
    @Before
	public void setupSmooksExeceutionContext() throws Exception
	{
		endpoint = createAndConfigureMockEndpoint(END_POINT_URI);
		Exchange exchange = createExchangeAndSetFromEndpoint(endpoint);
		BeanContext beanContext = createBeanContextAndSetBeanInContext(BEAN_ID, myBean);
		
		smooksExecutionContext = createStandaloneExecutionContext();
		setExchangeAsAttributeInExecutionContext(exchange);
		makeExecutionContextReturnBeanContext(beanContext);
	}
	
	private MockEndpoint createAndConfigureMockEndpoint(String endpointUri) throws Exception
	{
		MockEndpoint mockEndpoint = getMockEndpoint(endpointUri);
		return mockEndpoint;
	}

	private Exchange createExchangeAndSetFromEndpoint(MockEndpoint endpoint)
	{
		Exchange exchange = endpoint.createExchange();
		exchange.setFromEndpoint(endpoint);
		return exchange;
	}

	private BeanContext createBeanContextAndSetBeanInContext(String beanId, Object bean)
	{
		beanContext = mock(BeanContext.class);
		when(beanContext.getBean(beanId)).thenReturn(bean);
		return beanContext;
	}

	private StandaloneExecutionContext createStandaloneExecutionContext()
	{
		return mock(StandaloneExecutionContext.class);
	}

	private void setExchangeAsAttributeInExecutionContext(Exchange exchange)
	{
		when(smooksExecutionContext.getAttribute(Exchange.class)).thenReturn(exchange);
	}
	
	private void makeExecutionContextReturnBeanContext(BeanContext beanContext)
	{
		when(smooksExecutionContext.getBeanContext()).thenReturn(beanContext);
	}
	
	private BeanRouter createBeanRouter(String beanId, String endpointUri)
	{
	    return createBeanRouter("dummySelector", beanId, endpointUri);
	}
	
	private BeanRouter createBeanRouter(String selector, String beanId, String endpointUri)
	{
		BeanRouter beanRouter = new BeanRouter();
		SmooksResourceConfiguration resourceConfig = new SmooksResourceConfiguration();
		if (selector != null)
		{
			resourceConfig.setSelector(selector);
		}
		resourceConfig.setParameter("beanId", beanId);
		resourceConfig.setParameter("toEndpoint", endpointUri);
		
		MockApplicationContext appContext = new MockApplicationContext();
		appContext.setAttribute(CamelContext.class, context);
		Configurator.configure(beanRouter, resourceConfig, appContext);
		
		return beanRouter;
	}
	
	public static class MyBean
	{
		private final String name;

		public MyBean(String name)
		{
			this.name = name;
		}
		
		public String getName()
		{
			return name;
		}
	}
}
