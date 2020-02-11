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

import org.apache.camel.Endpoint;
import org.apache.camel.support.DefaultComponent;
import org.milyn.smooks.camel.processor.SmooksProcessor;

import java.util.Map;

/**
 * Smook Camel Component.
 * <p/>
 * 
 * Example usage:
 * 
 * <pre>
 * from(&quot;direct:a&quot;).to(&quot;smooks://edi-to-xml-smooks-config.xml&quot;)
 * </pre>
 * 
 * @author Christian Mueller
 * @author Daniel Bevenius
 * 
 */
public class SmooksComponent extends DefaultComponent
{
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception
    {
        SmooksProcessor smooksProcessor = new SmooksProcessor(remaining, getCamelContext());
        configureSmooksProcessor(smooksProcessor, uri, remaining, parameters);
        return new SmooksEndpoint(uri, this, smooksProcessor);
    }

    protected void configureSmooksProcessor(SmooksProcessor smooksProcessor, String uri, String remaining,
            Map<String, Object> parameters) throws Exception
    {
        setProperties(smooksProcessor, parameters);
    }

}
