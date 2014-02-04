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
package org.milyn.smooks.camel.processor;

import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Produce;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;
import org.milyn.Smooks;
import org.milyn.payload.Exports;
import org.milyn.payload.StringResult;

/**
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class SmooksProcessor_File_In_StringResult_Test extends CamelTestSupport {

    @EndpointInject(uri = "mock:a")
    private MockEndpoint mock;

    @Produce(uri = "direct:start")
    private ProducerTemplate producer;

	@Test
    public void test() throws Exception {
        deleteDirectory("target/smooks");
        mock.expectedMessageCount(1);
        producer.sendBody("<blah />");
        assertMockEndpointsSatisfied();
        assertEquals("<blah />", mock.assertExchangeReceived(0).getIn().getBody(String.class));
    }

	/* (non-Javadoc)
	 * @see org.apache.camel.test.junit4.CamelTestSupport#createRouteBuilder()
	 */
	@Override
	protected RouteBuilder createRouteBuilder() throws Exception {
	    
        return new RouteBuilder() {
            public void configure() {
                from("direct:start").
                process(new SmooksProcessor(new Smooks().setExports(new Exports(StringResult.class)), context)).
        		to("mock:a");
            }
        };
	}
}
