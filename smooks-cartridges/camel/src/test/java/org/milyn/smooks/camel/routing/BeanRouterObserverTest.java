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
package org.milyn.smooks.camel.routing;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Before;
import org.junit.Test;
import org.milyn.container.MockExecutionContext;
import org.milyn.javabean.lifecycle.BeanContextLifecycleEvent;
import org.milyn.javabean.lifecycle.BeanLifecycle;
import org.milyn.javabean.repository.BeanId;

/**
 * Unit test for {@link BeanRouterObserver}
 * 
 * @author Daniel Bevenius
 *
 */
public class BeanRouterObserverTest extends CamelTestSupport
{
    private static final String ENDPOINT_URI = "mock://beanRouterUnitTest";
    private MockEndpoint endpoint;
    
    @Before
    public void setup() throws Exception
    {
        endpoint = getMockEndpoint(ENDPOINT_URI);
    }
    
    @Test 
    public void onBeanLifecycleEventCreated() throws Exception
    {
        final String sampleBean = "testOrder";
        final String beanId = "orderId";
        final BeanRouter beanRouter = new BeanRouter(context);

        beanRouter.setBeanId(beanId);
        beanRouter.setToEndpoint(ENDPOINT_URI);
        beanRouter.initialize();

        final BeanRouterObserver beanRouterObserver = new BeanRouterObserver(beanRouter, beanId);
        final MockExecutionContext smooksExecutionContext = new MockExecutionContext();
        final BeanContextLifecycleEvent event = mock(BeanContextLifecycleEvent.class);
        
        when(event.getBeanId()).thenReturn(new BeanId(null, 0, beanId));
        when(event.getLifecycle()).thenReturn(BeanLifecycle.END_FRAGMENT);
        when(event.getBean()).thenReturn(sampleBean);
        when(event.getExecutionContext()).thenReturn(smooksExecutionContext);
		
        endpoint.setExpectedMessageCount(1);
        beanRouterObserver.onBeanLifecycleEvent(event);
        endpoint.assertIsSatisfied();
        endpoint.expectedBodiesReceived(sampleBean);
    }
    
}
