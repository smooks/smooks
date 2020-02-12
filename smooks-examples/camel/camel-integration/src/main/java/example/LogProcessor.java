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

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogProcessor implements Processor
{
    private static final Logger LOGGER = LoggerFactory.getLogger(LogProcessor.class);
    private final String string;

    public LogProcessor(String string)
    {
        this.string = string;
    }

    public void process(Exchange exchange) throws Exception
    {
        LogEvent logEvent = (LogEvent) exchange.getIn().getBody();
        LOGGER.info("Logging event [" + string + "]" + logEvent);
    }

}
