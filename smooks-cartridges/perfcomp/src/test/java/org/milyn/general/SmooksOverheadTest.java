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
package org.smooks.general;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import javax.xml.transform.stream.StreamSource;

import org.junit.Test;
import static org.junit.Assert.*;

import org.smooks.Smooks;
import org.smooks.payload.JavaResult;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.ext.DefaultHandler2;
import org.xml.sax.helpers.XMLReaderFactory;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.XppDriver;
import com.thoughtworks.xstream.io.xml.StaxDriver;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class SmooksOverheadTest {

    private static final int NUM_WARMUPS = 10;
    //private static final int NUM_ITERATIONS = 10000000;
    private static final int NUM_ITERATIONS = 100;

    @Test
    public void test_saxonly_timings() throws SAXException, IOException {
        for(int i = 0; i < NUM_WARMUPS; i++) {
            readBySax();
        }

        long start = System.currentTimeMillis();
        for(int i = 0; i < NUM_ITERATIONS; i++) {
            readBySax();
        }
        System.out.println("SAX only Took: " + (System.currentTimeMillis() - start));
    }

    @Test
    public void test_smookssax_timings_novis() throws IOException, SAXException {
        runSmooks("smooks-sax-empty.xml");
    }

    @Test
    public void test_smookssax_java3() throws IOException, SAXException {
        runSmooks("smooks-sax-java3.xml");
    }

    @Test
    public void test_smookssax_java3_object() throws IOException, SAXException {
        runSmooks("smooks-sax-java3-object.xml");
    }

    @Test
    public void test_smookssax_timings_1vis() throws IOException, SAXException {
        runSmooks("smooks-sax-1vis.xml");
    }

    @Test
    public void test_smookssax_timings_2vis() throws IOException, SAXException {
        runSmooks("smooks-sax-2vis.xml");
    }

    @Test
    public void test_smookssax_java1() throws IOException, SAXException {
        runSmooks("smooks-sax-java1.xml");
    }

    @Test
    public void test_smookssax_java2() throws IOException, SAXException {
        runSmooks("smooks-sax-java2.xml");
    }

    @Test
    public void test_smookssax_java4() throws IOException, SAXException {
        runSmooks("smooks-sax-java4.xml");
    }

    @Test
    public void test_configLoad() throws IOException, SAXException {
        for(int i = 0; i < NUM_WARMUPS; i++) {
            readAndLoadConfig();
        }
        long start = System.currentTimeMillis();
        for(int i = 0; i < NUM_ITERATIONS; i++) {
            readAndLoadConfig();
        }
        long took = System.currentTimeMillis() - start;
        System.out.println("took: " + took + " total");
        System.out.println("took: " + (took/NUM_ITERATIONS) + " per config");
    }

    private void readAndLoadConfig() throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream("smooks-sax-java4.xml"));
        smooks.createExecutionContext();
    }

    private void runSmooks(String config) throws IOException, SAXException {
        Smooks smooks = new Smooks(getClass().getResourceAsStream(config));

        for(int i = 0; i < NUM_WARMUPS; i++) {
            JavaResult javaResult = new JavaResult();
            smooks.filterSource(new StreamSource(getMessageReader()), javaResult);
        }

        long start = System.currentTimeMillis();
        JavaResult javaResult = null;
        for(int i = 0; i < NUM_ITERATIONS; i++) {
            javaResult = new JavaResult();
            smooks.filterSource(new StreamSource(getMessageReader()), javaResult);
        }
        System.out.println(config + " took: " + (System.currentTimeMillis() - start));
        List orderItems = (List) javaResult.getBean("orderItemList");
        if(orderItems != null) {
        	System.out.println("Num order items: " + orderItems.size());
        }
    }

    private void readBySax() throws SAXException, IOException {
        XMLReader reader = XMLReaderFactory.createXMLReader();
        DefaultHandler2 handler = new DefaultHandler2();

        reader.setContentHandler(handler);
        reader.setProperty("http://xml.org/sax/properties/lexical-handler", handler);
        reader.setFeature("http://xml.org/sax/features/namespaces", true);
        reader.setFeature("http://xml.org/sax/features/namespace-prefixes", true);

        reader.parse(new InputSource(getMessageReader()));
    }

    public void test_xstream() {
        XStream xstream = new XStream(new StaxDriver());
        //XStream xstream = new XStream(new XppDriver());
        xstream.fromXML(getMessageReader());

        for(int i = 0; i < NUM_WARMUPS; i++) {
            xstream.fromXML(getMessageReader());
        }

        long start = System.currentTimeMillis();
        for(int i = 0; i < NUM_ITERATIONS; i++) {
            xstream.fromXML(getMessageReader());
        }
        System.out.println("XStream Took: " + (System.currentTimeMillis() - start));

        List orderItems = (List) xstream.fromXML(getMessageReader());
        System.out.println("Num order items: " + orderItems.size());
    }

    private InputStreamReader getMessageReader() {
        //return new InputStreamReader(getClass().getResourceAsStream("order-message.xml"));
        return new InputStreamReader(getClass().getResourceAsStream("orderItem-list-05.xml"));
    }
}
