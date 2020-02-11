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
package org.milyn.smooks.camel.converters;

import org.apache.camel.Converter;
import org.milyn.payload.JavaResult;
import org.milyn.smooks.camel.Coordinate;

import java.util.Map;

/**
 * Converts a JavaResult to a Coordinate object. 
 * <p/>
 * This converter is only intended for testing purposes.
 * 
 * @author Daniel Bevenius
 *
 */
@Converter(generateLoader = true)
public class CoordinateConverter
{
	private CoordinateConverter()
	{
	}

	@Converter
	public static Coordinate toCoordinate(JavaResult result)
	{
		Object singleObject = getSingleObjectFromJavaResult(result);
		return (Coordinate) singleObject;
	}
	
	private static Object getSingleObjectFromJavaResult(JavaResult result)
	{
		Map<String, Object> resultMap = result.getResultMap();
		if(resultMap.size() == 1) 
		{
			return resultMap.values().iterator().next();
		}
		return null;
	}
}
