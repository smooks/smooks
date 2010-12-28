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
import javax.servlet.http.HttpServletResponseWrapper;

import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.ContentHandler;

/**
 * Abstract Servlet Response Wrapper for the Smooks framework.
 * <p/>
 * Implementations are hooked into the {@link org.milyn.SmooksServletFilter} by the
 * {@link org.milyn.servlet.delivery.ServletResponseWrapperFactory} to allow browser aware 
 * manipulation of a Servlet response right at the response stream level. 
 * <p/>
 * See {@link org.milyn.servlet.delivery.XMLServletResponseWrapper} and 
 * {@link org.milyn.servlet.delivery.PassThruServletResponseWrapper}.
 * @author tfennelly
 */
public abstract class ServletResponseWrapper extends HttpServletResponseWrapper implements ContentHandler {
	
	/**
	 * Container request instance.
	 */
	private ExecutionContext executionContext;
	
	/**
	 * Constructor.
	 * @param executionContext Container request.
	 * @param originalResponse Original servlet response.
	 */
	public ServletResponseWrapper(ExecutionContext executionContext, HttpServletResponse originalResponse) {
		super(originalResponse);
		this.executionContext = executionContext;
	}
	
	/**
	 * Get the {@link ExecutionContext} instance associated with this response wrapper.
	 * @return The {@link ExecutionContext}.
	 */
	public ExecutionContext getContainerRequest() {
		return executionContext;
	}

	/**
	 * Transform and serialise the supplied content to the target OutputStream.
	 * @throws IOException Probable cause: Unable to get target ServletOutputStream.
	 */
	public abstract void deliverResponse() throws IOException;

	/**
	 * Close response resources.
	 * <p/>
	 * Ensure all resources etc are closed
	 */
	public abstract void close();

	public void setConfiguration(SmooksResourceConfiguration configuration) {
	}
}