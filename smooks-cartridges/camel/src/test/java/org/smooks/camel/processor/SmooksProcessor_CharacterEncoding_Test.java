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

import java.io.InputStream;

import javax.xml.transform.stream.StreamSource;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;
import org.smooks.Smooks;
import org.smooks.javabean.Value;
import org.smooks.payload.Exports;
import org.smooks.payload.JavaResult;

/**
 * 
 * @author <a href="mailto:sorin7486@gmail.com">sorin7486@gmail.com</a>
 */
public class SmooksProcessor_CharacterEncoding_Test extends CamelTestSupport {
	
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
                .addVisitor(new Value("customer", "/order/header/customer", String.class)));
			}
			
		});
		enableJMX();
		context.start();
        Exchange response = template.request("direct:a", new Processor() {
            public void process(Exchange exchange) throws Exception {
            	InputStream in = this.getClass().getResourceAsStream("/EBCDIC-input-message");
                exchange.getIn().setBody(new StreamSource(in));
                exchange.setProperty("CamelCharsetName", "Cp1047");
            }
        });
        assertOutMessageBodyEquals(response, "Joe");
    }
}
