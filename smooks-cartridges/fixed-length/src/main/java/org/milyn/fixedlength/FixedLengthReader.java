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

package org.milyn.fixedlength;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.milyn.SmooksException;
import org.milyn.cdr.SmooksConfigurationException;
import org.milyn.cdr.annotation.ConfigParam;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.VisitorAppender;
import org.milyn.delivery.VisitorConfigMap;
import org.milyn.delivery.annotation.Initialize;
import org.milyn.delivery.dom.DOMVisitAfter;
import org.milyn.delivery.ordering.Consumer;
import org.milyn.delivery.sax.SAXElement;
import org.milyn.delivery.sax.SAXVisitAfter;
import org.milyn.expression.MVELExpressionEvaluator;
import org.milyn.function.StringFunctionExecutor;
import org.milyn.javabean.Bean;
import org.milyn.javabean.context.BeanContext;
import org.milyn.javabean.repository.BeanRepository;
import org.milyn.javabean.repository.BeanRepositoryManager;
import org.milyn.xml.SmooksXMLReader;
import org.w3c.dom.Element;
import org.xml.sax.ContentHandler;
import org.xml.sax.DTDHandler;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.xml.sax.helpers.AttributesImpl;

import javax.xml.XMLConstants;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * Fixed Length Reader.
 * <p/>
 * This Fixed Length Reader can be plugged into the Smooks (for example) in order to convert a
 * Fixed Length record based message stream into a stream of SAX events to be consumed by the DOMBuilder.
 *
 * <h3>Configuration</h3>
 * To maintain a single binding instance in memory:
 * <pre>
 * &lt;?xml version="1.0"?&gt;
 * &lt;smooks-resource-list xmlns="http://www.milyn.org/xsd/smooks-1.1.xsd" xmlns:fl="http://www.milyn.org/xsd/smooks/fixed-length-1.3.xsd"&gt;
 *
 *     &lt;fl:reader fields="" separator="" quote="" skipLines="" lineNumber="" rootElementName="" recordElementName="" lineNumberAttributeName="" truncatedAttributeName=""&gt;
 *         &lt;fl:singleBinding beanId="" class="" /&gt;
 *     &lt;/fl:reader&gt;
 *
 * &lt;/smooks-resource-list&gt;</pre>
 *
 * <p/>
 * To maintain a {@link java.util.List} of binding instances in memory:
 * <pre>
 * &lt;?xml version="1.0"?&gt;
 * &lt;smooks-resource-list xmlns="http://www.milyn.org/xsd/smooks-1.1.xsd" xmlns:fl="http://www.milyn.org/xsd/smooks/fixed-length-1.3.xsd"&gt;
 *
 *     &lt;fl:reader fields="" separator="" quote="" skipLines="" lineNumber="" rootElementName="" recordElementName="" lineNumberAttributeName="" truncatedAttributeName=""&gt;
 *         &lt;fl:listBinding beanId="" class="" /&gt;
 *     &lt;/fl:reader&gt;
 *
 * &lt;/smooks-resource-list&gt;</pre>
 *
 * <p/>
 * To maintain a {@link Map} of binding instances in memory:
 * <pre>
 * &lt;?xml version="1.0"?&gt;
 * &lt;smooks-resource-list xmlns="http://www.milyn.org/xsd/smooks-1.1.xsd" xmlns:fl="http://www.milyn.org/xsd/smooks/fixed-length-1.3.xsd"&gt;
 *
 *     &lt;fl:reader fields="" separator="" quote="" skipLines="" lineNumber="" rootElementName="" recordElementName="" lineNumberAttributeName="" truncatedAttributeName=""&gt;
 *         &lt;fl:mapBinding beanId="" class="" keyField="" /&gt;
 *     &lt;/fl:reader&gt;
 *
 * &lt;/smooks-resource-list&gt;</pre>
 *
 * <h3>Field definition</h3>
 * The field definition is a comma separated list of fieldnames. After each fieldname the length is set between brackets. So a field
 * definition could look like this: firstname[10],lastname[10],gender[1]
 *
 * <h3>Strict parsing</h3>
 * You can choose if the data is read strictly or not. When strict is to true then the line which doesn't contain enough characters is skipped
 * else the fields that don't have enough characters are empty and those records and fields have the 'truncated' attribute set to true.
 * By default strict is set to false.
 *
 * <h3>Ignoring Fields</h3>
 * To ignore a field in a fixed length record set, just insert the string "<b>$ignore$[10]</b>" for that field in the fields attribute. You still
 * need to set the field length between the brackets
 *
 * <h3>String manipulation functions</h3>
 * String manipulation functions can be defined per field. These functions are executed before that the data is converted into SAX events.
 * The functions are defined after the field length definition and are optionally separated with a question mark.  So a field
 * definition with string functions could look like this: firstname[10]?trim,lastname[10]?right_trim,gender[1]?upper_case
 * Take a look in the Smooks manual for a list of all available functions.
 *
 * <h3>Simple Java Bindings</h3>
 * A simple java binding can be configured on the reader configuration.  This allows quick binding configuration where the
 * fixed length records map cleanly to the target bean.  For more complex bindings, use the Java Binging Framework.
 *
 * <h3>Example Usage</h3>
 * So the following configuration could be used to parse a fixed length stream into
 * a stream of SAX events:
 * <pre>
 * &lt;csv:reader fields="name[20]?trim,address[50]?trim,$ignore$[5],item[5],quantity[3].trim" /&gt;</pre>
 * <p/>
 * Within Smooks, the stream of SAX events generated by the "Acme-Order-List" message (and this parser) will generate
 * an event stream equivalent to the following:
 * <pre> &lt;set&gt;
 * 	&lt;record number="1"&gt;
 * 		&lt;name&gt;Tom Fennelly&lt;/name&gt;
 * 		&lt;address&gt;Ireland&lt;/address&gt;
 * 		&lt;item&gt;V1234&lt;/item&gt;
 * 		&lt;quantity&gt;3&lt;/quantity&gt;
 * 	&lt;record&gt;
 * 	&lt;record number="2"&gt;
 * 		&lt;name&gt;Joe Bloggs&lt;/name&gt;
 * 		&lt;address&gt;England&lt;/address&gt;
 * 		&lt;item&gt;D9123&lt;/item&gt;
 * 		&lt;quantity&gt;7&lt;/quantity&gt;
 * 	&lt;record&gt;
 * &lt;/set&gt;</pre>
 * <p/>
 * Other profile based transformations can then be used to transform the CSV records in accordance with the requirements
 * of the consuming entities.
 *
 * @author Cedric Rathgeb
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 */
public class FixedLengthReader implements SmooksXMLReader, VisitorAppender {

	private static Log logger = LogFactory.getLog(FixedLengthReader.class);

    private static final AttributesImpl EMPTY_ATTRIBS = new AttributesImpl();
    private static final String IGNORE_FIELD = "$ignore$";
    private static final char FUNCTION_SEPARATOR = '?';

    private static char[] INDENT_LF = new char[] {'\n'};
    private static char[] INDENT_1  = new char[] {'\t'};
    private static char[] INDENT_2  = new char[] {'\t', '\t'};

    private ContentHandler contentHandler;
	private ExecutionContext execContext;

    @ConfigParam(name = "fields")
    private String[] flFields;
    private Field[] fields;
    private int totalFieldLenght;

    @ConfigParam(defaultVal = "false")
    private boolean lineNumber;

    @ConfigParam(defaultVal = "0")
    private int skipLines;

    @ConfigParam(defaultVal = "true")
    private boolean strict;

    @ConfigParam(defaultVal = "UTF-8")
    private Charset encoding;

    @ConfigParam(defaultVal="set")
    private String rootElementName;

    @ConfigParam(defaultVal="record")
    private String recordElementName;

    @ConfigParam(defaultVal="number")
    private String lineNumberAttributeName;

    @ConfigParam(defaultVal="truncated")
    private String truncatedAttributeName;

    @ConfigParam(defaultVal="false")
    private boolean indent;

    @ConfigParam(use = ConfigParam.Use.OPTIONAL)
    private String bindBeanId;

    @ConfigParam(use = ConfigParam.Use.OPTIONAL)
    private Class<?> bindBeanClass;

    @ConfigParam(use = ConfigParam.Use.OPTIONAL)
    private FixedLengthBindingType bindingType;

    @ConfigParam(use = ConfigParam.Use.OPTIONAL)
    private String bindMapKeyField;

    private static final String RECORD_BEAN = "flRecordBean";

    public boolean initialized = false;

    public void addVisitors(VisitorConfigMap visitorMap) {
    	initialize();
    	if(bindBeanId != null && bindBeanClass != null) {
            Bean bean;

            if(bindingType == FixedLengthBindingType.LIST) {
                Bean listBean = new Bean(ArrayList.class, bindBeanId, "$document");

                bean = listBean.newBean(bindBeanClass, recordElementName);
                listBean.bindTo(bean);
                addFieldBindings(bean);

                listBean.addVisitors(visitorMap);
            } else if(bindingType == FixedLengthBindingType.MAP) {
                if(bindMapKeyField == null) {
                    throw new SmooksConfigurationException("FixedLenght 'MAP' Binding must specify a 'keyField' property on the binding configuration.");
                }

                assertValidFieldName(bindMapKeyField);

                Bean mapBean = new Bean(LinkedHashMap.class, bindBeanId, "$document");
                Bean recordBean = new Bean(bindBeanClass, RECORD_BEAN, recordElementName);
                MapBindingWiringVisitor wiringVisitor = new MapBindingWiringVisitor(bindMapKeyField, bindBeanId);

                addFieldBindings(recordBean);

                mapBean.addVisitors(visitorMap);
                recordBean.addVisitors(visitorMap);
                visitorMap.addVisitor(wiringVisitor, recordElementName, null, false);
            } else {
                bean = new Bean(bindBeanClass, bindBeanId, recordElementName);
                addFieldBindings(bean);

                bean.addVisitors(visitorMap);
            }
        }
    }

    private void addFieldBindings(Bean bean) {
        for (Field field1 : fields) {
            String field = field1.getName();

            if (!field.equals(IGNORE_FIELD)) {
                bean.bindTo(field, recordElementName + "/" + field);
            }
        }
    }

	/* (non-Javadoc)
	 * @see org.milyn.xml.SmooksXMLReader#setExecutionContext(org.milyn.container.ExecutionContext)
	 */
	public void setExecutionContext(ExecutionContext request) {
		this.execContext = request;
	}

	@Initialize
	public void initialize() {
		buildFields();
	}


	/* (non-Javadoc)
	 * @see org.xml.sax.XMLReader#parse(org.xml.sax.InputSource)
	 */
	public void parse(InputSource flInputSource) throws IOException, SAXException {
        if(contentHandler == null) {
            throw new IllegalStateException("'contentHandler' not set.  Cannot parse Fixed Length stream.");
        }
        if(execContext == null) {
            throw new IllegalStateException("'execContext' not set.  Cannot parse Fixed Length stream.");
        }

        try {
	        Reader flStreamReader;
			BufferedReader flLineReader;
	        String flRecord;
	        int lineNumber = 0;
	
			// Get a reader for the Fixed Length source...
	        flStreamReader = flInputSource.getCharacterStream();
	        if(flStreamReader == null) {
	            flStreamReader = new InputStreamReader(flInputSource.getByteStream(), encoding);
	        }
	
	        // Create the Fixed Length line reader...
	        flLineReader = new BufferedReader(flStreamReader);
	
	        // Start the document and add the root element...
	        contentHandler.startDocument();
	        contentHandler.startElement(XMLConstants.NULL_NS_URI, rootElementName, StringUtils.EMPTY, EMPTY_ATTRIBS);
	
	        // Output each of the Fixed Length line entries...
	        while ((flRecord = flLineReader.readLine()) != null) {
	        	lineNumber++; // First line is line "1"
	
	        	if (lineNumber <= this.skipLines) {
	        		continue;
	        	}
	        	boolean invalidLength = flRecord.length() < totalFieldLenght;
	        	if(invalidLength && strict) {
	        		if(logger.isWarnEnabled()) {
	            		logger.warn("[WARNING-FIXEDLENGTH] Fixed Length line #" + lineNumber + " is invalid.  The line doesn't contain enough characters to fill all the fields. This line is skipped.");
	            	}
	        		continue;
	        	}
	
	        	char[] recordChars = flRecord.toCharArray();
	
	        	if(indent) {
	                contentHandler.characters(INDENT_LF, 0, 1);
	                contentHandler.characters(INDENT_1, 0, 1);
	            }
	
	            AttributesImpl attrs = EMPTY_ATTRIBS;
	            // Add a lineNumber ID
	            if (this.lineNumber || invalidLength) {
	            	attrs = new AttributesImpl();
	            	if(this.lineNumber) {
	            		attrs.addAttribute(XMLConstants.NULL_NS_URI, lineNumberAttributeName, lineNumberAttributeName, "xs:int", Integer.toString(lineNumber));
	            	}
	            	if(invalidLength) {
	            		attrs.addAttribute(XMLConstants.NULL_NS_URI, truncatedAttributeName, truncatedAttributeName, "xs:boolean", Boolean.TRUE.toString());
	            	}
	            }
	
	            contentHandler.startElement(XMLConstants.NULL_NS_URI, recordElementName, StringUtils.EMPTY, attrs);
	
	            // Loops through fields
	            int fieldLengthTotal = 0;
	        	for(int i = 0; i < flFields.length; i++) {
	           		// Field name local to the loop
	                String fieldName = fields[i].getName();
	                // Field length local to the loop
	                int fieldLength = fields[i].getLength();
	
	                StringFunctionExecutor stringFunctionExecutor = fields[i].getStringFunctionExecutor();
	
	                if(!fields[i].ignore()) {
	                	if(indent) {
	                        contentHandler.characters(INDENT_LF, 0, 1);
	                        contentHandler.characters(INDENT_2, 0, 2);
	                    }
	
	                	// Check that there are enough characters in the string
	                	boolean truncated = fieldLengthTotal + fieldLength > flRecord.length();
	
	                	AttributesImpl recordAttrs = EMPTY_ATTRIBS;
	
	                	//If truncated then set the truncated attribute
	                	if(truncated) {
	                		recordAttrs = new AttributesImpl();
	                        recordAttrs.addAttribute(XMLConstants.NULL_NS_URI, truncatedAttributeName, truncatedAttributeName, "xs:boolean", Boolean.TRUE.toString());
	                	}
	
	                    contentHandler.startElement(XMLConstants.NULL_NS_URI, fieldName, StringUtils.EMPTY, recordAttrs);
	
	                    // If not truncated then set the element data
	                    if(!truncated) {
	                    	if(stringFunctionExecutor == null) {
	                    		contentHandler.characters(recordChars, fieldLengthTotal, fieldLength);
	                    	} else {
	                    		String value = flRecord.substring(fieldLengthTotal, fieldLengthTotal + fieldLength);
	
	                    		value = stringFunctionExecutor.execute(value);
	
	                    		contentHandler.characters(value.toCharArray(), 0, value.length());
	                    	}
	            		}
	
	                    contentHandler.endElement(XMLConstants.NULL_NS_URI, fieldName, StringUtils.EMPTY);
	        		}
	
	                fieldLengthTotal += fieldLength;
	        	}
	
	        	if(indent) {
	                contentHandler.characters(INDENT_LF, 0, 1);
	                contentHandler.characters(INDENT_1, 0, 1);
	            }
	
	            contentHandler.endElement(null, recordElementName, StringUtils.EMPTY);
	
	
	        }
	
	        if(indent) {
	            contentHandler.characters(INDENT_LF, 0, 1);
	        }
	
	        // Close out the "fixedlength-set" root element and end the document..
	        contentHandler.endElement(XMLConstants.NULL_NS_URI, rootElementName, StringUtils.EMPTY);
	        contentHandler.endDocument();
        } finally {
        	// These properties need to be reset for every execution (e.g. when reader is pooled).
        	contentHandler = null;
        	execContext = null;
        }
	}

    public void setContentHandler(ContentHandler contentHandler) {
        this.contentHandler = contentHandler;
    }

    public ContentHandler getContentHandler() {
        return contentHandler;
    }

    private void assertValidFieldName(String fieldName) {
        for(Field field : fields) {
            if(field.getName().equals(fieldName)) {
                return;
            }
        }

        String fieldNames = "";
        for(Field field : fields) {
        	if(!field.ignore()) {
                if(fieldNames.length() > 0) {
                    fieldNames += ", ";
                }
                fieldNames += field.getName();
            }
        }

        throw new SmooksConfigurationException("Invalid field name '" + fieldName + "'.  Valid names: [" + fieldNames + "].");
    }

	private void buildFields() {
		// Parse input fields to extract names and lengths
        Field[] fields = new Field[this.flFields.length];
        int totalFieldLenght = 0;
    	for(int i = 0; i < this.flFields.length; i++) {
    		// Extract informations about the field
            String fieldInfos = this.flFields[i].trim();
            // Extract name of the field (before bracket)
            String fieldName = fieldInfos.substring(0, fieldInfos.lastIndexOf('['));
            // Extract length of the field (between brackets)
            int fieldLength = Integer.parseInt(fieldInfos.substring(fieldInfos.lastIndexOf('[') + 1, fieldInfos.lastIndexOf(']')));

            String functionDefinition = fieldInfos.substring(fieldInfos.lastIndexOf(']')+1);

            if(functionDefinition.length() != 0 && functionDefinition.charAt(0) == FUNCTION_SEPARATOR) {
                functionDefinition = functionDefinition.substring(1);
            }

            StringFunctionExecutor stringFunctionExecutor = null;
            if(functionDefinition.length() != 0) {
            	stringFunctionExecutor = StringFunctionExecutor.getInstance(functionDefinition);
            }

            fields[i] = new Field(fieldName, fieldLength, stringFunctionExecutor);

            totalFieldLenght += fieldLength;
    	}

    	this.fields = fields;
    	this.totalFieldLenght = totalFieldLenght;
	}
    /****************************************************************************
     *
     * The following methods are currently unimplemented...
     *
     ****************************************************************************/

    public void parse(String systemId) throws IOException, SAXException {
        throw new UnsupportedOperationException("Operation not supported by this reader.");
    }

    public boolean getFeature(String name) throws SAXNotRecognizedException,
            SAXNotSupportedException {
        return false;
    }

    public void setFeature(String name, boolean value)
            throws SAXNotRecognizedException, SAXNotSupportedException {
    }

    public DTDHandler getDTDHandler() {
        return null;
    }

    public void setDTDHandler(DTDHandler arg0) {
    }

    public EntityResolver getEntityResolver() {
        return null;
    }

    public void setEntityResolver(EntityResolver arg0) {
    }

    public ErrorHandler getErrorHandler() {
        return null;
    }

    public void setErrorHandler(ErrorHandler arg0) {
    }

    public Object getProperty(String name) throws SAXNotRecognizedException,
            SAXNotSupportedException {
        return null;
    }

    public void setProperty(String name, Object value)
            throws SAXNotRecognizedException, SAXNotSupportedException {
    }

    private class Field {

    	private final String name;

    	private final int length;

    	private final boolean ignore;

    	private final StringFunctionExecutor stringFunctionExecutor;

        public Field(String name, int length, StringFunctionExecutor stringFunctionExecutor) {
			this.name = name;
			this.length = length;
			this.stringFunctionExecutor = stringFunctionExecutor;

			ignore = IGNORE_FIELD.equals(name);
		}

		public String getName() {
			return name;
		}

		public int getLength() {
			return length;
		}

		public boolean ignore() {
			return ignore;
		}

		public StringFunctionExecutor getStringFunctionExecutor() {
			return stringFunctionExecutor;
		}

		@Override
		public String toString() {
			ToStringBuilder builder = new ToStringBuilder(this);
			builder.append("name", name)
				   .append("length", length)
                   .append("stringFunctionExecutor", stringFunctionExecutor);
			return builder.toString();
		}
    }

    private class MapBindingWiringVisitor implements DOMVisitAfter, SAXVisitAfter, Consumer {

        private MVELExpressionEvaluator keyExtractor = new MVELExpressionEvaluator();
        private String mapBindingKey;

        private MapBindingWiringVisitor(String bindKeyField, String mapBindingKey) {
            keyExtractor.setExpression(RECORD_BEAN + "." + bindKeyField);
            this.mapBindingKey = mapBindingKey;
        }

        public void visitAfter(Element element, ExecutionContext executionContext) throws SmooksException {
            wireObject(executionContext);
        }

        public void visitAfter(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
            wireObject(executionContext);
        }


		private void wireObject(ExecutionContext executionContext) {
			BeanContext beanContext = executionContext.getBeanContext();
            Map<String, Object> beanMap = beanContext.getBeanMap();
            Object key = keyExtractor.getValue(beanMap);

            @SuppressWarnings("unchecked") //TODO: Optimize to use the BeanId object
            Map<Object, Object> map =  (Map<Object, Object>) beanContext.getBean(mapBindingKey);
            Object record = beanContext.getBean(RECORD_BEAN);

            map.put(key, record);
        }

        public boolean consumes(Object object) {
            return keyExtractor.getExpression().indexOf(object.toString()) != -1;
        }
    }
}
