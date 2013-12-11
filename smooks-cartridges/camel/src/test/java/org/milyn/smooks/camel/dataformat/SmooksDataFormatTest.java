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

import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.DefaultExchange;
import org.apache.camel.processor.MarshalProcessor;
import org.apache.camel.processor.UnmarshalProcessor;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.milyn.commons.io.StreamUtils;
import org.milyn.payload.JavaSource;

/**
 * Unit test for {@link SmooksDataFormat}
 * 
 * @author Daniel Bevenius
 *
 */
public class SmooksDataFormatTest extends CamelTestSupport
{
    private static final String SMOOKS_CONFIG = "/org/milyn/smooks/camel/dataformat/smooks-config.xml";
    private static final String CUSTOMER_XML = "/org/milyn/smooks/camel/dataformat/customer.xml";
    private static final String CUSTOMER_XML_EXPECTED = "/org/milyn/smooks/camel/dataformat/customer-expected.xml";
    private DefaultCamelContext camelContext;
    private SmooksDataFormat dataFormatter;
    
    @Before
    public void setup() throws Exception
    {
        camelContext = new DefaultCamelContext();
        dataFormatter = new SmooksDataFormat(SMOOKS_CONFIG);
        dataFormatter.setCamelContext(camelContext);
        dataFormatter.start();
        XMLUnit.setIgnoreWhitespace(true);
    }
    
    @After
    public void stopDataFormatter() throws Exception
    {
        dataFormatter.stop();
    }
    
    @Override
    public boolean isUseRouteBuilder() {
        // each unit test include their own route builder
        return false;
    }
    
    
    @Test
    public void unmarshal() throws Exception
    {
        final UnmarshalProcessor unmarshalProcessor = new UnmarshalProcessor(dataFormatter);
        final DefaultExchange exchange = new DefaultExchange(camelContext);
        exchange.getIn().setBody(getCustomerInputStream(CUSTOMER_XML));
        
        unmarshalProcessor.process(exchange);
        
        assertEquals(Customer.class, exchange.getOut().getBody().getClass());
    }
    
    @Test
    public void marshal() throws Exception
    {
        final MarshalProcessor marshalProcessor = new MarshalProcessor(dataFormatter);
        final DefaultExchange exchange = new DefaultExchange(camelContext);
        final Customer customer = new Customer();
        customer.setFirstName("John");
        customer.setLastName("Cocktolstol");
        customer.setAge(35);
        customer.setCountry("USA");
        
        exchange.getIn().setBody(customer, JavaSource.class);
        
        marshalProcessor.process(exchange);
        
        assertXMLEqual(getCustomerXml(CUSTOMER_XML_EXPECTED), exchange.getOut().getBody(String.class));
    }
    
    @Test
    public void unmarshalMarshalThroughCamel() throws Exception
    {
        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception
            {
                from("direct:a")
                .unmarshal(dataFormatter)
                .marshal(dataFormatter);
            }
        });
        
        context.start();
        
        final Exchange exchange = template.request("direct:a", new Processor() {
            public void process(final Exchange exchange) throws Exception {
                exchange.getIn().setBody(getCustomerInputStream(CUSTOMER_XML));
            }
        });
        assertXMLEqual(getCustomerXml(CUSTOMER_XML_EXPECTED), exchange.getOut().getBody(String.class));
    }
    
    private InputStream getCustomerInputStream(final String resource)
    {
        return getClass().getResourceAsStream(resource);
    }
    
    private String getCustomerXml(final String resource) throws IOException
    {
        return StreamUtils.readStream(new InputStreamReader(getCustomerInputStream(resource)));
    }

}
