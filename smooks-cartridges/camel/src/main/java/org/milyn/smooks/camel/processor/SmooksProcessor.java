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
package org.milyn.smooks.camel.processor;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;

import org.apache.camel.CamelContext;
import org.apache.camel.CamelContextAware;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.Service;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.milyn.Smooks;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.Visitor;
import org.milyn.delivery.VisitorAppender;
import org.milyn.event.report.HtmlReportGenerator;
import org.milyn.payload.Exports;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Smooks {@link Processor} for Camel.
 * 
 * @author Christian Mueller
 * @author Daniel Bevenius
 */
public class SmooksProcessor implements Processor, Service, CamelContextAware
{
    public static final String SMOOKS_EXECUTION_CONTEXT = "CamelSmooksExecutionContext";
    private final Log log = LogFactory.getLog(SmooksProcessor.class);
    private Smooks smooks;
    private String configUri;
    private String reportPath;

    private Set<VisitorAppender> visitorAppenders = new HashSet<VisitorAppender>();
    private Map<String, Visitor> selectorVisitorMap = new HashMap<String, Visitor>();
    private CamelContext camelContext;

    public SmooksProcessor(final Smooks smooks, final CamelContext camelContext)
    {
        this.smooks = smooks;
        this.camelContext = camelContext;
    }

    public SmooksProcessor(final String configUri, final CamelContext camelContext) throws IOException, SAXException
    {
        this.configUri = configUri;
        this.camelContext = camelContext;
    }

    public void process(final Exchange exchange) throws Exception
    {
    	//forward headers
    	exchange.getOut().setHeaders(exchange.getIn().getHeaders());  
        
    	final ExecutionContext executionContext = smooks.createExecutionContext();
        executionContext.setAttribute(Exchange.class, exchange);
        exchange.getIn().setHeader(SMOOKS_EXECUTION_CONTEXT, executionContext);
        setupSmooksReporting(executionContext);

        final Exports exports = Exports.getExports(smooks.getApplicationContext());
        if (exports.hasExports())
        {
            final Result[] results = exports.createResults();
	        smooks.filterSource(executionContext, getSource(exchange), results);
	        setResultOnBody(exports, results, exchange);
        }
        else
		{
	        smooks.filterSource(executionContext, getSource(exchange));
        }
        
        executionContext.removeAttribute(Exchange.class);
    }
    
    protected void setResultOnBody(final Exports exports, final Result[] results, final Exchange exchange)
    {
        final Message message = exchange.getOut();
        final List<Object> objects = Exports.extractResults(results, exports);
        if (objects.size() == 1)
        {
            Object value = objects.get(0);
            message.setBody(value);
        }
        else
        {
	        message.setBody(objects);
        }
    }
    
    private void setupSmooksReporting(final ExecutionContext executionContext)
    {
        if (reportPath != null)
        {
            try
            {
                executionContext.setEventListener(new HtmlReportGenerator(reportPath));
            } 
            catch (final IOException e)
            {
                log.info("Could not generate Smooks Report. The reportPath specified was [" + reportPath + "].", e);
            }
        }
    }

    private Source getSource(final Exchange exchange)
    {
        Object payload = exchange.getIn().getBody();

        if(payload instanceof Source) 
        {
            return (Source) payload;
        }
        
        if(payload instanceof Node) 
        {
            return new DOMSource((Node) payload);
        }
        
        if(payload instanceof InputStream) 
        {
            return new StreamSource((InputStream) payload);
        }
        
        if(payload instanceof Reader) 
        {
            return new StreamSource((Reader) payload);
        }

        return exchange.getIn().getBody(Source.class);
    }

    public String getSmooksConfig()
    {
        return configUri;
    }

    public void setSmooksConfig(final String smooksConfig)
    {
        this.configUri = smooksConfig;
    }

    /**
     * Add a visitor instance.
     * 
     * @param visitor
     *            The visitor implementation.
     * @param targetSelector
     *            The message fragment target selector.
     * @return This instance.
     */
    public SmooksProcessor addVisitor(Visitor visitor, String targetSelector)
    {
        selectorVisitorMap.put(targetSelector, visitor);
        return this;
    }

    /**
     * Add a visitor instance to <code>this</code> Smooks instance via a
     * {@link VisitorAppender}.
     * 
     * @param appender
     *            The visitor appender.
     * @return This instance.
     */
    public SmooksProcessor addVisitor(VisitorAppender appender)
    {
        visitorAppenders.add(appender);
        return this;
    }

    public void setReportPath(String reportPath)
    {
        this.reportPath = reportPath;
    }

    public void start() throws Exception
    {
        if (smooks == null)
        {
            smooks = createSmooks(configUri);
        }
        smooks.getApplicationContext().setAttribute(CamelContext.class, camelContext);
        addAppenders(smooks, visitorAppenders);
        addVisitors(smooks, selectorVisitorMap);
        log.info(this + " Started");
    }

    private Smooks createSmooks(String configUri) throws IOException, SAXException
    {
        if (smooks != null)
        {
            return smooks;
        }
        
        final Smooks service = (Smooks) camelContext.getRegistry().lookup(Smooks.class.getName());
        if (service != null)
        {
            log.info("Found smooks in registry: " + service.getClass().getName());
            if (!Boolean.TRUE.equals(service.getApplicationContext().getAttribute("ConfiguredWithOSGIHeader")))
            {
            	service.addConfigurations(configUri);
            }
            return service;
        }

        return new Smooks(configUri);
    }

    private void addAppenders(Smooks smooks, Set<VisitorAppender> appenders)
    {
        for (VisitorAppender appender : visitorAppenders)
            smooks.addVisitor(appender);
    }

    private void addVisitors(Smooks smooks, Map<String, Visitor> selectorVisitorMap)
    {
        for (Entry<String, Visitor> entry : selectorVisitorMap.entrySet())
            smooks.addVisitor(entry.getValue(), entry.getKey());
    }

    public void stop() throws Exception
    {
        if (smooks != null)
        {
            smooks.close();
            smooks = null;
        }
        log.info(this + " Stopped");
    }

    @Override
    public String toString()
    {
        return "SmooksProcessor [configUri=" + configUri + "]";
    }

    public void setCamelContext(CamelContext camelContext)
    {
        this.camelContext = camelContext;
    }

    public CamelContext getCamelContext()
    {
        return camelContext;
    }

}