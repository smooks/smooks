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

package org.milyn.routing.file.naming;

/**
 * Exception indicating a NamingStrategyException
 * @author <a href="mailto:daniel.bevenius@gmail.com">Daniel Bevenius</a>			
 *
 */
public class NamingStrategyException extends Exception
{

	private static final long serialVersionUID = 1L;

	public NamingStrategyException()
	{
		super();
	}

	public NamingStrategyException(String message, Throwable cause)
	{
		super( message, cause );
	}

	public NamingStrategyException(String message)
	{
		super( message );
	}

	public NamingStrategyException(Throwable cause)
	{
		super( cause );
	}

}
