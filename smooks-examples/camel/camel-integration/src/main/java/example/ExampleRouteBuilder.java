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

import java.io.File;

public class ExampleRouteBuilder extends RouteBuilder {
    public ExampleRouteBuilder() {
    }

    @Override
    public void configure() throws Exception {
        // Set up the route for the initial input-message.xml
        from("file://" + getWorkingDir() + "?fileName=input-message.xml&noop=true").routeId("inputFileRoute")
                .log("Read file [${file:name}]").to("smooks://smooks-config.xml");

        // Set up routes for endpoints defined in smooks-config.xml
        from("direct:ireland").process(new LogProcessor("ie")).to("jms:queue:ireland");
        from("direct:greatbritain").process(new LogProcessor("gb")).to("jms:queue:greatbritian");
    }

    private File getWorkingDir() {
        String userDir = System.getProperty("user.dir");
        File workingDir = new File(userDir);
        return workingDir;
    }

}
