package org.milyn.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.milyn.container.ExecutionContext;
import org.milyn.payload.FilterResult;
import org.milyn.payload.FilterSource;

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

	private static final List<String> EXECUTION_CONTEXT_FILTER_LIST = new ArrayList<String>();

	static {
		//These keys are exluded for the execution context string
		EXECUTION_CONTEXT_FILTER_LIST.add(FilterResult.CONTEXT_KEY);
		EXECUTION_CONTEXT_FILTER_LIST.add(FilterSource.CONTEXT_KEY);
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
    	builder.append(toString(executionContext.getAttributes(), stack, EXECUTION_CONTEXT_FILTER_LIST));

    	return builder.toString();
    }

    /**
     * Creates a multi line JSON like string representation from a Map
     *
     * @param map The Map to create the string from
     * @return The String representation of the Map
     */
    public static String toString(Map<? extends Object, ? extends Object> map) {
    	return toString(map, Collections.emptyList());
	}

    /**
     * Creates a multi line JSON like string representation from a Map
     *
     * @param map The Map to create the string from
     * @param filterKeyList A list of objects that are ignored when encountered as keys
     * @return The String representation of the Map
     */
    public static String toString(Map<? extends Object, ? extends Object> map, List<?> filterKeyList) {
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
	public static String toString(Collection<? extends Object> collection) {
		return toString(collection, Collections.emptyList());
	}

	/**
     * Creates a multi line JSON like string representation from a Collection
     *
     * @param map The Map to create the string from
     * @param filterKeyList A list of objects that are ignored when encountered as keys
     * @return The String representation of the Map
     */
	public static String toString(Collection<? extends Object> collection, List<?> filterKeyList) {
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
	private static String toString(Map<? extends Object, ? extends Object> map, Stack<Object> parentStack, List<?> filterKeys) {
    	StringBuilder builder = new StringBuilder();
    	builder.append(CURLY_BRACKET_OPEN);

    	String indent = StringUtils.repeat(SPACE, parentStack.size()*SPACES);
    	String bracketIndent = StringUtils.repeat(SPACE, (parentStack.size()-1)*SPACES);

    	int i = 0;
    	int size = map.entrySet().size();
    	for(Entry<? extends Object, ? extends Object> entry : map.entrySet()) {
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



    private static String toString(Collection<? extends Object> collection, Stack<Object> parentStack, List<?> filterKeys) {
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
			builder.append(toString((Map<? extends Object, ? extends Object>) value, parentStack, filterKeys));
			parentStack.pop();
		} else if(value instanceof Collection<?>) {
			parentStack.push(current);
			builder.append(toString((Collection<? extends Object>) value, parentStack, filterKeys));
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
