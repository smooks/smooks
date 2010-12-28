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
package org.milyn.general;

import junit.framework.TestCase;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.Source;
import java.io.IOException;
import java.io.InputStreamReader;

import org.xml.sax.SAXException;
//import org.milyn.FilterSettings;
import org.milyn.Smooks;

import com.envoisolutions.sxc.xpath.XPathBuilder;
import com.envoisolutions.sxc.xpath.XPathEvaluator;
import com.envoisolutions.sxc.xpath.XPathEvent;
import com.envoisolutions.sxc.xpath.XPathEventHandler;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class SXCComparisonTest extends TestCase {

    private static final int NUM_WARMUPS = 10;
    private static final int NUM_ITERATIONS = 50000;

    public void test_sxc_big() throws Exception {
        runSXC("root/test/global", "big_global.xml");
    }

    public void test_sxc_small() throws Exception {
        runSXC("*/Password", "small-soap-message.xml");
    }

    public void test_smooks_big() throws IOException, SAXException {
        runSmooks("root/test/global", "big_global.xml");
    }

    public void test_smooks_small() throws Exception {
    	runSmooks("Password", "small-soap-message.xml");
    }

	private void runSXC(String target, String message) throws Exception {
        
		final boolean[] match = new boolean[]{false};
		final long[] invCount = new long[]{0};
        
        XPathEventHandler eventHandler = new XPathEventHandler() {

            public void onMatch(XPathEvent event) throws XMLStreamException {
                match[0] = true;
                int attrCount = event.getReader().getAttributeCount();
                
                for(int i = 0; i < attrCount; i++) {                	
                	String attr = event.getReader().getAttributeLocalName(i);
                }
                
                String elementText = event.getReader().getElementText();
                elementText = elementText;
                
                invCount[0]++;
            }

        };
		
		XPathBuilder builder = new XPathBuilder();
        builder.listen(target, eventHandler);

        XPathEvaluator evaluator = builder.compile();

        for(int i = 0; i < NUM_WARMUPS; i++) {
            evaluator.evaluate(getMessageReader(message));
        }

        long start = System.currentTimeMillis();
        for(int i = 0; i < NUM_ITERATIONS; i++) {
            evaluator.evaluate(getMessageReader(message));
        }
        System.out.println("Took: " + (System.currentTimeMillis() - start));
        assertTrue(match[0]);
        //System.out.println(invCount[0]);
	}

    private void runSmooks(String target, String message) throws IOException, SAXException {
    	
    	// Comment out to remove compile errors for pre 1.3 runtime
    	
//        Smooks smooks = new Smooks();
//
//        smooks.setFilterSettings(FilterSettings.newSAXSettings().setReaderPoolSize(1));
//        
//        smooks.addVisitor(new SAXVisitor(), target);
//        
//        for(int i = 0; i < NUM_WARMUPS; i++) {
//            smooks.filterSource(getMessageReader(message));
//        }
//
//        SAXVisitor.match = false;
//        long start = System.currentTimeMillis();
//        for(int i = 0; i < NUM_ITERATIONS; i++) {
//            smooks.filterSource(getMessageReader(message));
//        }
//        System.out.println("Took: " + (System.currentTimeMillis() - start));
//        assertTrue(SAXVisitor.match);
    }
    
    private Source getMessageReader(String message) {
        return new StreamSource(new InputStreamReader(getClass().getResourceAsStream(message)));
    }
}
