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

package org.milyn.container.plugin;

import org.milyn.payload.ByteResult;
import org.milyn.payload.JavaResult;
import org.milyn.payload.StringResult;

import javax.xml.transform.Result;

/**
 * Factory for javax.xml.transform.Result objects.
 * 
 * @author <a href="mailto:daniel.bevenius@gmail.com">Daniel Bevenius</a>			
 *
 */
public class ResultFactory
{
	private static ResultFactory factory = new ResultFactory();
	
	private ResultFactory() {} 
	
	public static ResultFactory getInstance()
	{
		return factory;
	}
	
	public Result createResult( final ResultType type )
	{
    	Result result = null;
		switch ( type )
		{
		case STRING:
            result = new StringResult();
			break;
		case BYTES:
            result = new ByteResult();
			break;
		case JAVA:
            result = new JavaResult(true);
			break;
		case NORESULT:
			break;

		default:
			result = null;
			break;
		}
		
		return result;
	}
}
