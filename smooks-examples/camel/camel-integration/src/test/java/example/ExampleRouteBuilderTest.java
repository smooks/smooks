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
package example;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

public class ExampleRouteBuilderTest extends CamelTestSupport
{

    @Override
    public RouteBuilder createRouteBuilder()
    {
        return new ExampleRouteBuilder();
    }

    @Test
    public void route() throws Exception
    {
        MockEndpoint irelandMockQueue = getMockEndpoint("jms:queue:ireland");
        irelandMockQueue.setExpectedMessageCount(1);

        Thread.sleep(1000);

        irelandMockQueue.assertIsSatisfied(1000);
    }

    @Override
    protected CamelContext createCamelContext() throws Exception
    {
        CamelContext context = super.createCamelContext();
        context.addComponent("jms", context.getComponent("mock"));
        return context;
    }

}
