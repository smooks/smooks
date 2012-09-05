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

import org.apache.camel.builder.RouteBuilder;
import org.milyn.smooks.camel.dataformat.SmooksDataFormat;

/**
 * @author Daniel Bevenius
 */
public class ExampleRouteBuilder extends RouteBuilder {
    public ExampleRouteBuilder() {
    }

    @Override
    public void configure() throws Exception {
        SmooksDataFormat smooksDataFormat = new SmooksDataFormat("smooks-config.xml");
        smooksDataFormat.setCamelContext(getContext());
        smooksDataFormat.start();

        // Starting with Camel 2.5 the path can be specified as file:.
        // See https://issues.apache.org/activemq/browse/CAMEL-3063 for more
        // information.
        from("file://input-dir?noop=true")
                .log("Before unmarshal with SmooksDataFormat: ${body}")
                .unmarshal(smooksDataFormat)
                .log("After unmarshal with SmooksDataFormat: ${body}")
                .to("mock:result");
    }
}
