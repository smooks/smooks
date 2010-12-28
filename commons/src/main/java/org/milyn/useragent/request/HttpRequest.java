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

package org.milyn.useragent.request;

import java.util.Enumeration;

/**
 * Http interface definition. <p/> Definition of access to the HTTP request
 * attributes - namely the request headers and parameters. <p/> Method
 * signatures are based on the servlet spec HttpServletRequest class. <p/>
 * 
 * @author Tom Fennelly
 */

public interface HttpRequest extends Request {

	/**
	 * Get the named HTTP request header.
	 * 
	 * @param name
	 *            The request header name.
	 * @return The value of the header with the specified name, or null if the
	 *         header isn't present in the request.
	 */
	public String getHeader(String name);

	/**
	 * Get the named HTTP request parameter from the request query string.
	 * 
	 * @param name
	 *            The name of the required request parameter.
	 * @return The value of the request parameter, or null if the parameter
	 *         isn't present in the request.
	 */
	public String getParameter(String name);

    /**
     * Returns an Enumeration of String  objects containing the names of the
     * parameters contained in this request. If the request has no parameters,
     * the method returns an empty Enumeration.
     * @return an Enumeration of String  objects, each String containing the
     * name of a request parameter; or an empty Enumeration if the request has
     * no parameters.
     */
    public abstract Enumeration getParameterNames();

    /**
     * Returns an array of String objects containing all of the values the given
     * request parameter has, or null if the parameter does not exist.
     * <p/>
     * If the parameter has a single value, the array has a length of 1.
     * @param name String containing the name of the parameter whose value is
     * requested.
     * @return an array of String objects containing the parameter's values.
     */
    public abstract String[] getParameterValues(java.lang.String name);
}
