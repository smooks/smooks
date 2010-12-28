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

package org.milyn.templating.soapshipping;

import java.io.IOException;
import java.io.InputStream;

import org.milyn.Smooks;
import org.milyn.SmooksUtil;
import org.milyn.container.ExecutionContext;
import org.milyn.profile.DefaultProfileSet;
import org.milyn.templating.util.CharUtils;
import org.xml.sax.SAXException;

import junit.framework.TestCase;

public abstract class ShippingIntegTestBase extends TestCase {

    public void testTransform() throws SAXException, IOException {
        Smooks smooks = new Smooks();

        // Configure Smooks
        SmooksUtil.registerProfileSet(new DefaultProfileSet("shipping-request"), smooks);
        SmooksUtil.registerProfileSet(new DefaultProfileSet("shipping-response"), smooks);
        smooks.addConfigurations("trans-request.cdrl", getClass().getResourceAsStream("trans-request.cdrl"));
        smooks.addConfigurations("trans-response.cdrl", getClass().getResourceAsStream("trans-response.cdrl"));
                
        InputStream requestStream = getClass().getResourceAsStream("../request.xml");
        ExecutionContext context = smooks.createExecutionContext("shipping-request");
        String requestResult = SmooksUtil.filterAndSerialize(context, requestStream, smooks);
		CharUtils.assertEquals("Template test failed.", "/org/milyn/templating/soapshipping/request.xml.tran.expected", requestResult);

        InputStream responseStream = getClass().getResourceAsStream("../response.xml");
        context = smooks.createExecutionContext("shipping-response");
        String responseResult = SmooksUtil.filterAndSerialize(context, responseStream, smooks);
		CharUtils.assertEquals("Template test failed.", "/org/milyn/templating/soapshipping/response.xml.tran.expected", responseResult);
    }
}
