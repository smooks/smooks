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

package org.milyn.servlet.delivery;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.milyn.container.ExecutionContext;

/**
 * Pass-through ServletResponseWrapper.
 * <p/>
 * Pass content through without performing any filtering.  Triggered
 * by supplying "<b>smooksrw</b>=<b>passthru-smooksrw</b>" as a HTTP request parameter.  
 * <p/>
 * This response wrapper is configured to be applicable to all devices through the 
 * following content delivery resource configuration:  
 * <pre>
 * &lt;?xml version="1.0"?&gt;
 * &lt;!DOCTYPE smooks-resource-list PUBLIC "-//MILYN//DTD SMOOKS 1.0//EN" 
 * 			"http://milyn.codehaus.org/dtd/smooksres-list-1.0.dtd"&gt;
 * 
 * &lt;!--
 * 	This CDRL defines common delivery units shared across all browsers.
 * 
 * 	NB: See device-profile.xml
 * --&gt;
 * 
 * &lt;smooks-resource-list default-useragent="*"&gt; 
 * 	&lt;!--
 * 		Pass-Thru response filter configuration - this basically turns off SmooksDOMFilter filtering.
 * 	--&gt;
 * 	&lt;smooks-resource selector="<b>passthru-smooksrw</b>" path="<b>org.milyn.delivery.response.PassThruServletResponseWrapper</b>" /&gt;
 * &lt;/smooks-resource-list&gt;</pre>
 * 
 * @author tfennelly
 */
public class PassThruServletResponseWrapper extends ServletResponseWrapper {

	public PassThruServletResponseWrapper(ExecutionContext executionContext, HttpServletResponse originalResponse) {
		super(executionContext, originalResponse);
	}

	public void deliverResponse() throws IOException {
		// Leave to the HttpServletResponseWrapper
		getResponse().getOutputStream().write(new String("<html><body>yo</body></html>").getBytes());
	}

	public void close() {
	}
}
