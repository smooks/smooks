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
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.spi.TypeConverterRegistry;
import org.milyn.payload.JavaResult;
import org.milyn.payload.StringResult;
import org.w3c.dom.Node;

import javax.xml.transform.Result;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * ResultConverter converts from different {@link Result} types.
 * 
 * @author Daniel Bevenius
 */
@Converter(generateLoader = true)
public class ResultConverter
{
    public static final String SMOOKS_RESULT_KEY = "SmooksResultKeys";
    
    private ResultConverter()
    {
    }

    @Converter
    public static Node toDocument(DOMResult domResult)
    {
        return domResult.getNode();
    }

    @SuppressWarnings("rawtypes")
    @Converter
    public static List toList(JavaResult.ResultMap javaResult, Exchange exchange)
    {
        String resultKey = (String) exchange.getProperty(SMOOKS_RESULT_KEY);
        if (resultKey != null)
        {
            return (List) getResultsFromJavaResult(javaResult, resultKey);
        } else
        {
            return (List) getSingleObjectFromJavaResult(javaResult);
        }
    }

    @SuppressWarnings("rawtypes")
	@Converter
    public static Integer toInteger(JavaResult.ResultMap result)
    {
        return (Integer) getSingleObjectFromJavaResult(result);
    }

    @SuppressWarnings("rawtypes")
	@Converter
    public static Double toDouble(JavaResult.ResultMap result)
    {
        return (Double) getSingleObjectFromJavaResult(result);
    }

    @Converter
    public static String toString(StringResult result)
    {
        return result.getResult();
    }

    
    @SuppressWarnings("rawtypes")
	public static Map toMap(JavaResult.ResultMap resultBeans, Exchange exchange)
    {
        Message outMessage = exchange.getOut();
        outMessage.setBody(resultBeans);

        @SuppressWarnings("unchecked")
		Set<Entry<String, Object>> entrySet = resultBeans.entrySet();
        for (Entry<String, Object> entry : entrySet)
        {
            outMessage.setBody(entry.getValue(), entry.getValue().getClass());
        }
        return resultBeans;
    }

    @SuppressWarnings("rawtypes")
	private static Object getResultsFromJavaResult(JavaResult.ResultMap resultMap, String resultKey)
    {
        return resultMap.get(resultKey);
    }

    private static Object getSingleObjectFromJavaResult(@SuppressWarnings("rawtypes") JavaResult.ResultMap resultMap)
    {
        if (resultMap.size() == 1)
        {
            return resultMap.values().iterator().next();
        }
        return null;
    }

    @Converter
    public static StreamSource toStreamSource(StringResult stringResult)
    {
        String result = stringResult.getResult();
        if (result != null)
        {
            StringReader stringReader = new StringReader(result);
            return new StreamSource(stringReader);
        }
        
        return null;
    }

    @SuppressWarnings("rawtypes")
    @Converter(fallback = true)
    public static <T> T convertTo(Class<T> type, Exchange exchange, Object value, TypeConverterRegistry registry) {
        if(value instanceof JavaResult.ResultMap) {
            for(Object mapValue : ((Map) value).values()) {
                if(type.isInstance(mapValue)) {
                    return type.cast(mapValue);
                }
            }
        } 

        return null;
    }
}
