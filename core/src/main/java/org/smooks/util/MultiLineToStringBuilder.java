/*-
 * ========================LICENSE_START=================================
 * Smooks Core
 * %%
 * Copyright (C) 2020 Smooks
 * %%
 * Licensed under the terms of the Apache License Version 2.0, or
 * the GNU Lesser General Public License version 3.0 or later.
 * 
 * SPDX-License-Identifier: Apache-2.0 OR LGPL-3.0-or-later
 * 
 * ======================================================================
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * ======================================================================
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * =========================LICENSE_END==================================
 */
package org.smooks.util;

import org.apache.commons.lang.StringUtils;
import org.smooks.container.ExecutionContext;
import org.smooks.payload.FilterResult;
import org.smooks.payload.FilterSource;

import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Static utility class for generating JSON like multi line Strings
 * from Maps, List, Arrays and the ExecutionContext.
 *
 * These methods do traverse sub Maps/Collections/Arrays. They can handle references
 * to parent nodes or to them selfs.
 *
 * @author maurice_zeijen
 *
 */
public class MultiLineToStringBuilder {

	private static final int SPACES = 3;
	private static final String PARENT_OPEN = "PARENT-";
	private static final String PARENT_CLOSE = "";
	private static final String SPACE = " ";
	private static final String QUOTE = "\"";
	private static final String SQUARE_BRACKET_CLOSE = "]";
	private static final String SQUARE_BRACKET_OPEN = "[";
	private static final String THIS = "THIS";
	private static final String NULL = "NULL";
	private static final String VALUE_KEY_SEPARATOR = " : ";
	private static final String COMMA = ",";
	private static final String CURLY_BRACKET_CLOSE = "}";
	private static final String CURLY_BRACKET_OPEN = "{";
	private static final String NL = System.getProperty("line.separator");
	private static final Pattern NL_PATTERN = Pattern.compile("\r\n|\n|\r");

	private static final List<String> EXECUTION_CONTEXT_FILTER_LIST = new ArrayList<>();

	static {
		//These keys are exluded for the execution context string
		EXECUTION_CONTEXT_FILTER_LIST.add(FilterResult.RESULTS_TYPED_KEY.toString());
		EXECUTION_CONTEXT_FILTER_LIST.add(FilterSource.SOURCE_TYPED_KEY.toString());
	}

	private MultiLineToStringBuilder() {
	}

	/**
	 * Creates a multi line JSON like string for the execution context
	 *
	 * @param executionContext The ExecutionContext
	 * @return The String representation
	 */
    public static String toString(ExecutionContext executionContext) {
    	Stack<Object> stack = new Stack<Object>();
    	stack.push(executionContext);

    	StringBuilder builder = new StringBuilder();

    	builder.append("BeanContext : ");
    	builder.append(toString(executionContext.getBeanContext().getBeanMap(), stack, new ArrayList<String>()));
    	builder.append(NL);
    	builder.append(NL);
    	builder.append("Attributes : ");
    	builder.append(toString(executionContext.getAll(), stack, EXECUTION_CONTEXT_FILTER_LIST));

    	return builder.toString();
    }

    /**
     * Creates a multi line JSON like string representation from a Map
     *
     * @param map The Map to create the string from
     * @return The String representation of the Map
     */
    public static String toString(Map<?, ?> map) {
    	return toString(map, Collections.emptyList());
	}

    /**
     * Creates a multi line JSON like string representation from a Map
     *
     * @param map The Map to create the string from
     * @param filterKeyList A list of objects that are ignored when encountered as keys
     * @return The String representation of the Map
     */
    public static String toString(Map<?, ?> map, List<?> filterKeyList) {
		Stack<Object> stack = new Stack<Object>();
		stack.add(new Object()); // A little hack to make sure that the first level is rendered correctly
    	return toString(map, stack, filterKeyList);
	}

    /**
     * Creates a multi line JSON like string representation from a Collection.
     *
     * @param map The Map to create the string from
     * @return The String representation of the Map
     */
	public static String toString(Collection<?> collection) {
		return toString(collection, Collections.emptyList());
	}

	/**
     * Creates a multi line JSON like string representation from a Collection
     *
     * @param map The Map to create the string from
     * @param filterKeyList A list of objects that are ignored when encountered as keys
     * @return The String representation of the Map
     */
	public static String toString(Collection<?> collection, List<?> filterKeyList) {
		Stack<Object> stack = new Stack<Object>();
		stack.add(new Object()); // A little hack to make sure that the first level is rendered correctly

		return toString(collection, stack, filterKeyList);
	}

	/**
     * Creates a multi line JSON like string representation from an Array
     *
     * @param map The Map to create the string from
     * @return The String representation of the Map
     */
	public static String toString(Object[] array) {
		return toString(array, Collections.emptyList());
	}

	/**
     * Creates a multi line JSON like string representation from an Array
     *
     * @param map The Map to create the string from
     * @param filterKeyList A list of objects that are ignored when encountered as keys
     * @return The String representation of the Map
     */
	public static String toString(Object[] array, List<?> filterKeyList) {
		Stack<Object> stack = new Stack<Object>();
		stack.add(new Object()); // A little hack to make sure that the first level is renderd ok

		return toString(array, stack, filterKeyList);
	}

	/**
     * Creates a multi line JSON like string representation from a Map
     *
     * @param map The Map to create the string from
     * @param filterKeyList A list of objects that are ignored when encountered as keys
     * @return The String representation of the Map
     */
	private static String toString(Map<?, ?> map, Stack<Object> parentStack, List<?> filterKeys) {
    	StringBuilder builder = new StringBuilder();
    	builder.append(CURLY_BRACKET_OPEN);

    	String indent = StringUtils.repeat(SPACE, parentStack.size()*SPACES);
    	String bracketIndent = StringUtils.repeat(SPACE, (parentStack.size()-1)*SPACES);

    	int i = 0;
    	int size = map.entrySet().size();
    	for(Entry<?, ?> entry : map.entrySet()) {
    		String key = entry.getKey().toString();

    		if(filterKeys.contains(key)) {
    			continue;
    		}

    		builder.append(NL);

    		Object value = entry.getValue();

    		builder.append(indent);
    		builder.append(QUOTE);
    		builder.append(key);
    		builder.append(QUOTE);

    		builder.append(VALUE_KEY_SEPARATOR);
    		if(value == null) {
    			builder.append(NULL);
    		} else {
	    		if(isTraversable(value) && parentStack.contains(value)) {
	    			processParent(parentStack, builder, value);
	    		} else if(value == map){
	    			builder.append(THIS);
	    		} else {
	    			processValue(map, value, key, parentStack, builder, filterKeys);
	    		}
    		}
    		i++;
    		if(i < size) {
    			builder.append(COMMA);
    		}
    	}
    	if(i > 0) {
    		builder.append(NL);
    		builder.append(bracketIndent);
    	}
    	builder.append(CURLY_BRACKET_CLOSE);
    	return builder.toString();
    }



    private static String toString(Collection<?> collection, Stack<Object> parentStack, List<?> filterKeys) {
    	StringBuilder builder = new StringBuilder();
    	builder.append(SQUARE_BRACKET_OPEN);

    	String indent = StringUtils.repeat(SPACE, parentStack.size()*SPACES);
    	String bracketIndent = StringUtils.repeat(SPACE, (parentStack.size()-1)*SPACES);

    	int i = 0;
    	int size = collection.size();
    	for(Object value : collection) {
    		builder.append(NL);

    		builder.append(indent);

    		if(value == null) {
    			builder.append(NULL);
    		} else {
	    		if(isTraversable(value) && parentStack.contains(value)) {
	    			processParent(parentStack, builder, value);
	    		} else if(value == collection){
	    			builder.append(THIS);
	    		} else {
	    			processValue(collection, value, null, parentStack, builder, filterKeys);
	    		}
    		}
    		i++;
    		if(i < size) {
    			builder.append(COMMA);
    		}

    	}
    	if(i > 0) {
    		builder.append(NL);
    		builder.append(bracketIndent);
    	}
    	builder.append(SQUARE_BRACKET_CLOSE);
    	return builder.toString();
    }



    private static String toString(Object[] array, Stack<Object> parentStack, List<?> filterKeys) {
    	StringBuilder builder = new StringBuilder();
    	builder.append(SQUARE_BRACKET_OPEN);

    	String indent = StringUtils.repeat(SPACE, parentStack.size()*SPACES);
    	String bracketIndent = StringUtils.repeat(SPACE, (parentStack.size()-1)*SPACES);

    	int i = 0;
    	int size = array.length;
    	for(Object value : array) {
    		builder.append(NL);

    		builder.append(indent);

    		if(value == null) {
    			builder.append(NULL);
    		} else {
	    		if(isTraversable(value) && parentStack.contains(value)) {
	    			processParent(parentStack, builder, value);
	    		} else if(value == array){
	    			builder.append(THIS);
	    		} else {
	    			processValue(array, value, null, parentStack, builder, filterKeys);
	    		}
    		}
    		i++;
    		if(i < size) {
    			builder.append(COMMA);
    		}

    	}
    	if(i > 0) {
    		builder.append(NL);
    		builder.append(bracketIndent);
    	}
    	builder.append(SQUARE_BRACKET_CLOSE);
    	return builder.toString();
    }

	private static void processParent(Stack<Object> parentStack, StringBuilder builder, Object value) {
		int index = parentStack.indexOf(value);

		builder.append(PARENT_OPEN)
		       .append(parentStack.size() - index)
               .append(PARENT_CLOSE);
	}

	@SuppressWarnings("unchecked")
	private static void processValue(Object current, Object value, String key, Stack<Object> parentStack, StringBuilder builder, List<?> filterKeys) {
		if(value instanceof Map<?, ?>) {
			parentStack.push(current);
			builder.append(toString((Map<?, ?>) value, parentStack, filterKeys));
			parentStack.pop();
		} else if(value instanceof Collection<?>) {
			parentStack.push(current);
			builder.append(toString((Collection<?>) value, parentStack, filterKeys));
			parentStack.pop();
		} else if(value.getClass().isArray()) {
			parentStack.push(current);
			builder.append(toString((Object[]) value, parentStack, filterKeys));
			parentStack.pop();
		} else {

			if(value instanceof Number) {
				builder.append(value);
			} else {
				builder.append(QUOTE);
				String valueStr = value.toString();

				Matcher matcher = NL_PATTERN.matcher(valueStr);
				if(matcher.find()) {
					int keyLength = key == null ? 0 : key.length() + VALUE_KEY_SEPARATOR.length();
					String spaces = StringUtils.repeat(SPACE, keyLength + (parentStack.size()*SPACES));

					valueStr = matcher.replaceAll(NL + spaces);
					builder.append(valueStr);
				} else {
					builder.append(valueStr);
				}
				builder.append(QUOTE);
			}
		}
	}

    private static boolean isTraversable(Object obj) {
    	return obj instanceof Collection<?> || obj instanceof Map<?, ?> || obj.getClass().isArray();
    }
}
