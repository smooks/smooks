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
package org.smooks.camel.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;
import org.smooks.Smooks;
import org.smooks.javabean.Bean;
import org.smooks.javabean.Value;
import org.smooks.payload.Exports;
import org.smooks.payload.JavaResult;
import org.smooks.payload.StringSource;
import org.smooks.camel.Coordinate;

import java.util.Map;

/**
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class SmooksProcessor_JavaResult_Test extends CamelTestSupport {
	
    @Override
    public boolean isUseRouteBuilder() {
        // each unit test include their own route builder
        return false;
    }
	
	@Test
    public void test_single_value() throws Exception {
		context.addRoutes(new RouteBuilder() {
			@Override
			public void configure() throws Exception
			{
                from("direct:a")
                .process(new SmooksProcessor(new Smooks().setExports(new Exports(JavaResult.class)), context)
                .addVisitor(new Value("x", "/coord/@x", Integer.class)));
			}
			
		});
		enableJMX();
		context.start();
        Exchange response = template.request("direct:a", new Processor() {
            public void process(Exchange exchange) throws Exception {
                exchange.getIn().setBody(new StringSource("<coord x='1234' />"));
            }
        });
        assertOutMessageBodyEquals(response, 1234);
    }

	@Test
    public void test_multi_value() throws Exception {
		context.addRoutes(new RouteBuilder() {
			@Override
			public void configure() throws Exception
			{
                from("direct:b").process(new SmooksProcessor(new Smooks().setExports(new Exports(JavaResult.class)), context).
                		addVisitor(new Value("x", "/coord/@x", Integer.class)).
                		addVisitor(new Value("y", "/coord/@y", Double.class)));
			}
		});
		context.start();
        Exchange response = template.request("direct:b", new Processor() {
            public void process(Exchange exchange) throws Exception {
                exchange.getIn().setBody(new StringSource("<coord x='1234' y='98765.76' />"));
            }
        });
        Map javaResult = response.getOut().getBody(Map.class);
        Integer x = (Integer) javaResult.get("x");
        assertEquals(1234, (int) x );
        Double y = (Double) javaResult.get("y");
        assertEquals(98765.76D, (double) y, 0.01D);
    }
	
	@Test
    public void test_bean() throws Exception {
		context.addRoutes(new RouteBuilder() {
			@Override
			public void configure() throws Exception
			{
                from("direct:c").process(new SmooksProcessor(new Smooks().setExports(new Exports(JavaResult.class)), context).
            		addVisitor(new Bean(Coordinate.class, "coordinate").
    				bindTo("x", "/coord/@x").
    				bindTo("y", "/coord/@y")));
			}
		});
		context.start();
        Exchange response = template.request("direct:c", new Processor() {
            public void process(Exchange exchange) throws Exception {
                exchange.getIn().setBody(new StringSource("<coord x='111' y='222' />"));
            }
        });
        
        Coordinate coord = response.getMessage().getBody(Coordinate.class);
        
        assertEquals(111, coord.getX());
        assertEquals(222, coord.getY());
    }

}
