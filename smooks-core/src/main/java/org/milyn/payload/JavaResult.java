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
package org.milyn.payload;

import com.thoughtworks.xstream.XStream;

import org.milyn.assertion.AssertArgument;
import org.milyn.javabean.context.StandaloneBeanContext;
import org.milyn.payload.FilterResult;

import javax.xml.transform.Result;
import java.io.StringWriter;
import java.util.*;

/**
 * Java filtration/transformation result.
 * <p/>
 * Used to extract a Java "{@link Result result}" Map from the transformation.
 * Simply set an instance of this class as the {@link Result} arg in the call
 * to {@link org.milyn.Smooks#filterSource(org.milyn.container.ExecutionContext, javax.xml.transform.Source, javax.xml.transform.Result...)} .
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class JavaResult extends FilterResult implements ResultExtractor<JavaResult> {
    
    private Map<String, Object> resultMap;

    /**
     * Public default constructor.
     */
    public JavaResult() {
        this(true);
    }
    
    /**
     * Public default constructor.
     */
    public JavaResult(boolean preserveOrder) {
    	if(preserveOrder) {
    		resultMap = new LinkedHashMap<String, Object>();
    	} else {
    		resultMap = new HashMap<String, Object>();
    	}
    }
    
    /**
     * Public constructor.
     * <p/>
     * See {@link #setResultMap(java.util.Map)}.
     * 
     * @param resultMap Result Map. This is the map onto which Java "result" objects will be set.
     */
    public JavaResult(Map<String, Object> resultMap) {
        AssertArgument.isNotNull(resultMap, "resultMap");
        this.resultMap = resultMap;
    }

    /**
     * Get the named bean from the Java Result Map.
     * @param name the name of the bean.
     * @return The bean Object, or null if the bean is not in the bean Result Map.
     * @see #getResultMap()
     */
    public Object getBean(String name) {
        return resultMap.get(name);
    }

    /**
     * Get the first instance of the specified bean type
     * from this JavaResult instance.
     * 
     * @param beanType The bean runtime class type.
     * @return The bean instance, otherwise null.
     */
	public <T> T getBean(Class<T> beanType) {
		return StandaloneBeanContext.getBean(beanType, resultMap);
	}

    /**
     * Get the Java result map.
     * @return The Java result map.
     * @see #getBean(String)
     */
    public Map<String, Object> getResultMap() {
        return resultMap;
    }

    /**
     * Set the Java result map.
     * @param resultMap The Java result map.
     */
    public void setResultMap(Map<String, Object> resultMap) {
        this.resultMap = resultMap;
    }

    /**
     * XML Serialized form of the bean Map associate with the
     * result instance.
     * @return XML Serialized form of the bean Map associate with the
     * result instance.
     */
    public String toString() {
        StringWriter stringBuilder = new StringWriter();
        XStream xstream = new XStream();

        if(resultMap != null && !resultMap.isEmpty()) {
            Set<Map.Entry<String, Object>> entries = resultMap.entrySet();

            for (Map.Entry<String, Object> entry : entries) {
                stringBuilder.write(entry.getKey() + ":\n");
                stringBuilder.write(xstream.toXML(entry.getValue()) + "\n\n");
            }
        }

        return stringBuilder.toString();
    }

    public Object extractFromResult(JavaResult result, Export export)
    {
        Set<String> extractSet = export.getExtractSet();

        if (extractSet == null) {
            return extractBeans(result, result.getResultMap().keySet());
        }

        if(extractSet.size() == 1) {
            return result.getBean(extractSet.iterator().next());
        } else {
            return extractBeans(result, extractSet);
        }
    }

    private Object extractBeans(JavaResult result, Collection<String> extractSet) {
        Map<String, Object> extractedObjects = new ResultMap<String, Object>();

        for(String extract : extractSet) {
            Object bean = result.getBean(extract);
            if(bean != null) {
                extractedObjects.put(extract, bean);
            }
        }

        return extractedObjects;
    }

    public static class ResultMap<K,V> extends HashMap<K,V> {        
    }
}
