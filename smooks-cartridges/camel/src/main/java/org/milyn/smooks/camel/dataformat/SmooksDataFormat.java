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
package org.milyn.smooks.camel.dataformat;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.apache.camel.Exchange;
import org.apache.camel.Service;
import org.apache.camel.TypeConverter;
import org.apache.camel.processor.MarshalProcessor;
import org.apache.camel.spi.DataFormat;
import org.milyn.Smooks;
import org.milyn.container.ExecutionContext;
import org.milyn.payload.Exports;
import org.milyn.payload.StringResult;
import org.milyn.smooks.camel.component.SmooksComponent;
import org.milyn.smooks.camel.processor.SmooksProcessor;

/**
 * SmooksDataFormat is a Camel data format which is a pluggable transformer
 * capable of transforming from one dataformat to another and back again.
 * This means that what is marshaled can be unmarshaled by an instance of this
 * class.
 * <p/>
 * 
 * A smooks configuration for a SmooksDataFormat should not utilize Smooks
 * features such as routing that might allocated system resources. The reason
 * for this is that there is no functionality in the SmooksDataFormat which will
 * close those resources. If you need to use these Smooks features please take a
 * look at the {@link SmooksComponent} or {@link SmooksProcessor} as they hook
 * into Camels lifecycle manegment and will close resources correctly.
 * <p/>
 * 
 * @author Christian Mueller
 * @author Daniel Bevenius
 * 
 */
public class SmooksDataFormat implements DataFormat, CamelContextAware, Service
{
    private Smooks smooks;
    private CamelContext camelContext;
    private final String smooksConfig;
    
    public SmooksDataFormat(final String smooksConfig) throws Exception
    {
        this.smooksConfig = smooksConfig;
    }
    
    /**
     * Marshals the Object 'fromBody' to an OutputStream 'toStream'
     * </p>
     * 
     * The Camel framework will call this method from {@link MarshalProcessor#process(Exchange)}
     * and it will take care of setting the Out Message's body to the bytes written to the toStream
     * OutputStream.
     * 
     * @param exchange The Camel {@link Exchange}.
     * @param fromBody The object to be marshalled into the output stream.
     * @param toStream The output stream that will be written to.
     * 
     */
    public void marshal(final Exchange exchange, final Object fromBody, final OutputStream toStream) throws Exception
    {
        final ExecutionContext execContext = smooks.createExecutionContext();
        final TypeConverter typeConverter = exchange.getContext().getTypeConverter();
        final Source source = typeConverter.mandatoryConvertTo(Source.class, exchange, fromBody);
        final StringResult stringResult = new StringResult();
        smooks.filterSource(execContext, source, stringResult);
        
        toStream.write(stringResult.getResult().getBytes(execContext.getContentEncoding()));
    }

    /**
     * Unmarshals the fromStream to an Object.
     * </p>
     * The Camel framework will call this method from {@link UnMarshalProcessor#process(Exchange)}
     * and it will take care of setting the returned Object on the Out Message's body.
     * 
     * @param exchange The Camel {@link Exchange}.
     * @param fromStream The InputStream that will be unmarshalled into an Object instance.
     * 
     */
    public Object unmarshal(final Exchange exchange, final InputStream fromStream) throws Exception
    {
        final ExecutionContext execContext = smooks.createExecutionContext();
        final Exports exports = Exports.getExports(smooks.getApplicationContext());
        final Result[] results = exports.createResults();
        smooks.filterSource(execContext, new StreamSource(fromStream), results);
        return getResult(exports, results, exchange);
    }
    
    protected Object getResult(final Exports exports, final Result[] results, final Exchange exchange)
    {
        final List<Object> objects = Exports.extractResults(results, exports);
        if (objects.size() == 1)
        {
            return objects.get(0);
        }
        else
        {
	        return objects;
        }
    }

    public void setCamelContext(CamelContext camelContext)
    {
        this.camelContext = camelContext;
    }

    public CamelContext getCamelContext()
    {
        return camelContext;
    }

    public void start() throws Exception
    {
        final Smooks service = (Smooks) camelContext.getRegistry().lookup(Smooks.class.getName());
        if (service != null)
        {
            smooks = service;
        }
        else
        {
            smooks = new Smooks(smooksConfig);
        }
    }

    public void stop() throws Exception
    {
        if (smooks != null)
        {
	        smooks.close();
        }
    }

}