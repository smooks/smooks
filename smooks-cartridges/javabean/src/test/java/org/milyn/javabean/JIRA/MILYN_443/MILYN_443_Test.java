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
package org.milyn.javabean.JIRA.MILYN_443;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.xml.transform.stream.StreamSource;

import org.milyn.FilterSettings;
import org.milyn.Smooks;
import org.milyn.javabean.Bean;
import org.milyn.payload.JavaResult;
import org.xml.sax.SAXException;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * http://jira.codehaus.org/browse/MILYN-443
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class MILYN_443_Test {

    @Test
	public void test_SAX() throws IOException, SAXException {
		test(FilterSettings.DEFAULT_SAX);
	}

    @Test	
    public void test_DOM() throws IOException, SAXException {
		test(FilterSettings.DEFAULT_DOM);
    }
   
    @Test 
    public void test_programmatic() {
		Smooks smooks = new Smooks();
		
		Properties namespaces = new Properties();
		namespaces.setProperty("e", "http://www.example.net");
		namespaces.setProperty("f", "http://www.blah");
		smooks.setNamespaces(namespaces);
		
		Bean beanConfig = new Bean(HashMap.class, "theBean");
		beanConfig.bindTo("attr1", "test1/@e:attr1");
		beanConfig.bindTo("attr2", "test1/@f:attr2");		
		smooks.addVisitor(beanConfig);
		
		test(smooks);
    }
    
	private void test(FilterSettings filterSettings) throws IOException, SAXException {
		Smooks smooks = new Smooks(getClass().getResourceAsStream("smooks-config.xml"));
		smooks.setFilterSettings(filterSettings);
		test(smooks);
	}

	private void test(Smooks smooks) {
		JavaResult result = new JavaResult();
		smooks.filterSource(new StreamSource(getClass().getResourceAsStream("message.xml")), result);
		
		Map theBean = (Map) result.getBean("theBean");
		assertEquals("xxx", theBean.get("attr1"));
		assertEquals(null, theBean.get("attr2"));
	}
}
