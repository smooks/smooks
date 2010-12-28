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

import java.lang.reflect.Constructor;

import javax.servlet.http.HttpServletResponse;

import org.milyn.classpath.ClasspathUtils;
import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.container.ExecutionContext;
import org.milyn.util.ClassUtil;

/**
 * ServletResponseWrapper Factory (GoF) class.
 * <p/>
 * Used by the {@link org.milyn.servlet.SmooksServletFilter} to allow browser aware 
 * manipulation of a Servlet response right at the response stream level via
 * {@link org.milyn.servlet.delivery.ServletResponseWrapper} implementations.
 * This mechanism allows different {@link javax.servlet.http.HttpServletResponseWrapper}
 * implementations to be applied to a Servlet response based on characteristics of
 * the requesting browser e.g. perform different image transformations.
 * <p/>
 * {@link org.milyn.servlet.delivery.ServletResponseWrapper} implementations are triggered
 * by a HTTP request parameter named "<b>smooksrw</b>".  Smooks
 * uses the parameter value as a selector to select the most specific
 * {@link org.milyn.servlet.delivery.ServletResponseWrapper} for the requesting useragent
 * (see {@link org.milyn.cdr.SmooksResourceConfiguration}). 
 * {@link org.milyn.servlet.delivery.XMLServletResponseWrapper} is the default
 * {@link org.milyn.servlet.delivery.ServletResponseWrapper} when no
 * <b>smooksrw</b> request parameter is specified, or no 
 * {@link org.milyn.servlet.delivery.ServletResponseWrapper} is configured for the
 * requesting device under the supplied <b>smooksrw</b> parameter value.
 * <p/>
 * See {@link org.milyn.servlet.delivery.XMLServletResponseWrapper} and 
 * {@link org.milyn.servlet.delivery.PassThruServletResponseWrapper}.
 * @author tfennelly
 */
public abstract class ServletResponseWrapperFactory {

	/**
	 * ServletResponseWrapper factory method.
	 * @param resourceConfig Content Delivery Resource definition for the ServletResponseWrapper.
	 * @param executionContext Container request.
	 * @param originalResponse Original Servlet Response.
	 * @return ServletResponseWrapper instance.
	 */
	public static ServletResponseWrapper createServletResponseWrapper(SmooksResourceConfiguration resourceConfig, ExecutionContext executionContext, HttpServletResponse originalResponse) {
		Constructor constructor;
		Class runtime = null;
		
		try {
            String className = ClasspathUtils.toClassName(resourceConfig.getResource());
			runtime = ClassUtil.forName(className, ServletResponseWrapperFactory.class);
		} catch (ClassNotFoundException e) {
			IllegalStateException state = new IllegalStateException("Unable to load " + resourceConfig.getResource());
			state.initCause(e);
			throw state;
		}
		try {
			constructor = runtime.getConstructor(new Class[] {ExecutionContext.class, HttpServletResponse.class});
		} catch (SecurityException e) {
			IllegalStateException state = new IllegalStateException("Container doesn't have permissions to load class " + runtime);
			state.initCause(e);
			throw state;
		} catch (NoSuchMethodException e) {
			IllegalStateException state = new IllegalStateException(runtime + " must contain a constructor with an arg signature of (" + ExecutionContext.class + ", " + HttpServletResponse.class + ")");
			state.initCause(e);
			throw state;
			}

		try {
			return (ServletResponseWrapper)constructor.newInstance(new Object[] {executionContext, originalResponse});
		} catch (ClassCastException e) {
			IllegalStateException state = new IllegalStateException("Failed to construct " + resourceConfig.getParameter("class") + ".  Must be an instance of " + ServletResponseWrapper.class);
			state.initCause(e);
			throw state;
		} catch (Exception e) {
			IllegalStateException state = new IllegalStateException("Failed to construct " + resourceConfig.getParameter("class") + ".");
			state.initCause(e);
			throw state;
		}
	}
}
