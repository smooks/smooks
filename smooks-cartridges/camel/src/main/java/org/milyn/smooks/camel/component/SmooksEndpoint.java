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
package org.milyn.smooks.camel.component;

import org.apache.camel.Component;
import org.apache.camel.Service;
import org.apache.camel.support.ProcessorEndpoint;
import org.milyn.SmooksException;
import org.milyn.smooks.camel.processor.SmooksProcessor;

/**
 * SmooksEndpoint is a wrapper around a {@link SmooksProcessor} instance and
 * adds lifecycle support by implementing Service. This enables a SmooksEndpoint
 * to be stopped and started.
 * <p/>
 * 
 * @author Daniel Bevenius
 * 
 */
public class SmooksEndpoint extends ProcessorEndpoint implements Service
{
    private SmooksProcessor smooksProcesor;

    public SmooksEndpoint(String endpointUri, Component component, SmooksProcessor processor)
    {
        super(endpointUri, component, processor);
        this.smooksProcesor = processor;
    }

    public void start()
    {
        try {
            smooksProcesor.start();
        } catch (Exception e) {
            throw new SmooksException(e.getMessage(), e);
        }
    }

    public void stop()
    {
        try {
            smooksProcesor.stop();
        } catch (Exception e) {
            throw new SmooksException(e.getMessage(), e);
        }
    }

}
