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

package org.milyn.csv;

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
import org.milyn.xml.SmooksXMLReader;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/**
 * CSV Reader.
 * <p/>
 * This CSV Reader can be plugged into the Smooks (for example) in order to convert a
 * CSV based message stream into a stream of SAX events to be consumed by the DOMBuilder.
 *
 * <h3>Configuration</h3>
 * To maintain a single binding instance in memory:
 * <pre>
 * &lt;?xml version="1.0"?&gt;
 * &lt;smooks-resource-list xmlns="http://www.milyn.org/xsd/smooks-1.1.xsd" xmlns:csv="http://www.milyn.org/xsd/smooks/csv-1.2.xsd"&gt;
 *
 *     &lt;csv:reader fields="" separator="" quote="" skipLines="" rootElementName="" recordElementName=""&gt;
 *         &lt;csv:singleBinding beanId="" class="" /&gt;
 *     &lt;/csv:reader&gt;
 *
 * &lt;/smooks-resource-list&gt;</pre>
 *
 * <p/>
 * To maintain a {@link List} of binding instances in memory:
 * <pre>
 * &lt;?xml version="1.0"?&gt;
 * &lt;smooks-resource-list xmlns="http://www.milyn.org/xsd/smooks-1.1.xsd" xmlns:csv="http://www.milyn.org/xsd/smooks/csv-1.2.xsd"&gt;
 *
 *     &lt;csv:reader fields="" separator="" quote="" skipLines="" rootElementName="" recordElementName=""&gt;
 *         &lt;csv:listBinding beanId="" class="" /&gt;
 *     &lt;/csv:reader&gt;
 *
 * &lt;/smooks-resource-list&gt;</pre>
 *
 * <p/>
 * To maintain a {@link Map} of binding instances in memory:
 * <pre>
 * &lt;?xml version="1.0"?&gt;
 * &lt;smooks-resource-list xmlns="http://www.milyn.org/xsd/smooks-1.1.xsd" xmlns:csv="http://www.milyn.org/xsd/smooks/csv-1.2.xsd"&gt;
 *
 *     &lt;csv:reader fields="" separator="" quote="" skipLines="" rootElementName="" recordElementName=""&gt;
 *         &lt;csv:mapBinding beanId="" class="" keyField="" /&gt;
 *     &lt;/csv:reader&gt;
 *
 * &lt;/smooks-resource-list&gt;</pre>
 *
 * <h3>Strict parsing</h3>
 * Strict parsing was the only option until Smooks 1.2.x, whereby lines that would not comply with the provided tokens
 * (that it where the tokens present in the line is less than the number of tokens expected) would be garbled and a WARN log
 * statement was provided. Now, you can decide if you want those lines to be parsed too, this is accomplished by setting strict="false"
 * on the config.
 *
 * <h3>String manipulation functions</h3>
 * String manipulation functions can be defined per field. These functions are executed before that the data is converted into SAX events.
 * The functions are defined after the field name, separated with a question mark. So a field definition with string functions
 * could look like this: firstname?trim,lastname?right_trim,gender?upper_case
 * Take a look in the Smooks manual for a list of all available functions.
 *
 * <h3>Ignoring Fields</h3>
 * To ignore a field in a CSV record set, just insert the string "<b>$ignore$</b>" for that field in the fields attribute.
 *
 * <h3>Simple Java Bindings</h3>
 * A simple java binding can be configured on the reader configuration.  This allows quick binding configuration where the
 * CSV records map cleanly to the target bean.  For more complex bindings, use the Java Binging Framework.
 *
 * <h3>Example Usage</h3>
 * So the following configuration could be used to parse a CSV stream into
 * a stream of SAX events:
 * <pre>
 * &lt;csv:reader fields="name,address,$ignore$,item,quantity" /&gt;</pre>
 * <p/>
 * Within Smooks, the stream of SAX events generated by the "Acme-Order-List" message (and this parser) will generate
 * an event stream equivalent to the following:
 * <pre> &lt;csv-set&gt;
 * 	&lt;csv-record number="1"&gt;
 * 		&lt;name&gt;Tom Fennelly&lt;/name&gt;
 * 		&lt;address&gt;Ireland&lt;/address&gt;
 * 		&lt;item&gt;V1234&lt;/item&gt;
 * 		&lt;quantity&gt;3&lt;/quantity&gt;
 * 	&lt;csv-record&gt;
 * 	&lt;csv-record number="2"&gt;
 * 		&lt;name&gt;Joe Bloggs&lt;/name&gt;
 * 		&lt;address&gt;England&lt;/address&gt;
 * 		&lt;item&gt;D9123&lt;/item&gt;
 * 		&lt;quantity&gt;7&lt;/quantity&gt;
 * 	&lt;csv-record&gt;
 * &lt;/csv-set&gt;</pre>
 * <p/>
 * Other profile based transformations can then be used to transform the CSV records in accordance with the requirements
 * of the consuming entities.
 *
 * @author tfennelly
 * @deprecated Use the {@link org.milyn.flatfile.FlatFileReader} configured with the {@link CSVRecordParserFactory}.
 */
public class CSVReader implements SmooksXMLReader, VisitorAppender {

	private static Log logger = LogFactory.getLog(CSVReader.class);
    private static Attributes EMPTY_ATTRIBS = new AttributesImpl();
    private static final String IGNORE_FIELD = "$ignore$";

    private static char[] INDENT_LF = new char[] {'\n'};
    private static char[] INDENT_1  = new char[] {'\t'};
    private static char[] INDENT_2  = new char[] {'\t', '\t'};
    private static String RECORD_NUMBER_ATTR = "number";
    private static String RECORD_TRUNCATED_ATTR = "truncated";

    private ContentHandler contentHandler;
	private ExecutionContext execContext;

    @ConfigParam(name = "fields")
    private String[] csvFields;
    private Field[] fields;

    @ConfigParam(defaultVal = ",")
    private char separator;

    @ConfigParam(name = "quote-char", defaultVal = "\"")
    private char quoteChar;
	
	@ConfigParam(name = "escape-char", defaultVal = "\\")
    private char escapeChar;

    @ConfigParam(name = "skip-line-count", defaultVal = "0")
    private int skipLines;

    @ConfigParam(defaultVal = "UTF-8")
    private Charset encoding;

    @ConfigParam(defaultVal="csv-set")
    private String rootElementName;

    @ConfigParam(defaultVal="csv-record")
    private String recordElementName;

    @ConfigParam(defaultVal="false")
    private boolean indent;

    @ConfigParam(defaultVal="true")
    private boolean strict;

	@ConfigParam(defaultVal = "false")
	private boolean validateHeader;

    @ConfigParam(use = ConfigParam.Use.OPTIONAL)
    private String bindBeanId;

    @ConfigParam(use = ConfigParam.Use.OPTIONAL)
    private Class<?> bindBeanClass;

    @ConfigParam(use = ConfigParam.Use.OPTIONAL)
    private CSVBindingType bindingType;

    @ConfigParam(use = ConfigParam.Use.OPTIONAL)
    private String bindMapKeyField;
    private static final String RECORD_BEAN = "csvRecordBean";

	@Initialize
	public void initialize() {
		buildFields();
	}

    public void addVisitors(VisitorConfigMap visitorMap) {
        if(bindBeanId != null && bindBeanClass != null) {
            Bean bean;

            if(bindingType == CSVBindingType.LIST) {
                Bean listBean = new Bean(ArrayList.class, bindBeanId, "$document");

                bean = listBean.newBean(bindBeanClass, recordElementName);
                listBean.bindTo(bean);
                addFieldBindings(bean);

                listBean.addVisitors(visitorMap);
            } else if(bindingType == CSVBindingType.MAP) {
                if(bindMapKeyField == null) {
                    throw new SmooksConfigurationException("CSV 'MAP' Binding must specify a 'keyField' property on the binding configuration.");
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
        for(Field field : fields) {
            if(!field.ignore()) {
                bean.bindTo(field.getName(), recordElementName + "/" + field.getName());
            }
        }
    }

	private void buildFields() {
		// Parse input fields to extract names and lengths
        Field[] fields = new Field[this.csvFields.length];
    	for(int i = 0; i < this.csvFields.length; i++) {
    		// Extract informations about the field
            String fieldInfos = this.csvFields[i].trim();
            String fieldName = fieldInfos;
            StringFunctionExecutor stringFunctionExecutor = null;

            if(fieldInfos.indexOf('?') >= 0) {
                fieldName = fieldInfos.substring(0, fieldInfos.indexOf('?'));
                String functionDefinition = fieldInfos.substring(fieldInfos.indexOf('?')+1);

                if(functionDefinition.length() != 0) {
                    stringFunctionExecutor = StringFunctionExecutor.getInstance(functionDefinition);
                }
            }
            fields[i] = new Field(fieldName, stringFunctionExecutor);
    	}

    	this.fields = fields;
	}

    /* (non-Javadoc)
	 * @see org.milyn.xml.SmooksXMLReader#setExecutionContext(org.milyn.container.ExecutionContext)
	 */
	public void setExecutionContext(ExecutionContext request) {
		this.execContext = request;
	}

	/* (non-Javadoc)
	 * @see org.xml.sax.XMLReader#parse(org.xml.sax.InputSource)
	 */
	public void parse(InputSource csvInputSource) throws IOException, SAXException {
        if(contentHandler == null) {
            throw new IllegalStateException("'contentHandler' not set.  Cannot parse CSV stream.");
        }
        if(execContext == null) {
            throw new IllegalStateException("'execContext' not set.  Cannot parse CSV stream.");
        }

        try {
			Reader csvStreamReader;
			au.com.bytecode.opencsv.CSVReader csvLineReader;
	        String[] csvRecord;
	
			// Get a reader for the CSV source...
	        csvStreamReader = csvInputSource.getCharacterStream();
	        if(csvStreamReader == null) {
	            csvStreamReader = new InputStreamReader(csvInputSource.getByteStream(), encoding);
	        }
	
	        // Create the CSV line reader...
	        csvLineReader = new au.com.bytecode.opencsv.CSVReader(csvStreamReader, separator, quoteChar, escapeChar, skipLines);

			if (validateHeader) {
				validateHeader(csvLineReader);
			}

	        // Start the document and add the root "csv-set" element...
	        contentHandler.startDocument();
	        contentHandler.startElement(XMLConstants.NULL_NS_URI, rootElementName, StringUtils.EMPTY, EMPTY_ATTRIBS);
	
	        // Output each of the CVS line entries...
	        int lineNumber = 0;
	        int expectedCount = getExpectedColumnsCount();
	
	        while ((csvRecord = csvLineReader.readNext()) != null) {
	        	lineNumber++; // First line is line "1"
	
	        	if(csvRecord.length < expectedCount && strict) {
	        		logger.debug("[CORRUPT-CSV] CSV line #" + lineNumber + " invalid [" + Arrays.asList(csvRecord) + "].  The line should contain number of items at least as in CSV config file " + csvFields.length + " fields [" + csvFields + "], but contains " + csvRecord.length + " fields.  Ignoring!!");
	        		continue;
	        	}
	
	            if(indent) {
	                contentHandler.characters(INDENT_LF, 0, 1);
	                contentHandler.characters(INDENT_1, 0, 1);
	            }
	
	            AttributesImpl attrs = new AttributesImpl();
	            // If we reached here it means that this line has to be in the sax stream
	            // hence we first add the record number attribute on the csv-record element
	            attrs.addAttribute(XMLConstants.NULL_NS_URI, RECORD_NUMBER_ATTR, RECORD_NUMBER_ATTR, "xs:int", Integer.toString(lineNumber));
	            // if this line is truncated, we add the truncated attribute onto the csv-record element
	            if (csvRecord.length < expectedCount)
	            	attrs.addAttribute(XMLConstants.NULL_NS_URI, RECORD_TRUNCATED_ATTR, RECORD_TRUNCATED_ATTR, "xs:boolean", Boolean.TRUE.toString());
	            contentHandler.startElement(XMLConstants.NULL_NS_URI, recordElementName, StringUtils.EMPTY, attrs);
	        	int recordIt = 0;
	            for(Field field : fields) {
	                String fieldName = field.getName();
	
	                if(field.ignore()) {
	                	int toSkip = parseIgnoreFieldDirective(fieldName);
	                	if(toSkip == Integer.MAX_VALUE){
	                		break;
	                	}
	                	recordIt += toSkip;
	                	continue;
	                }
	
	                if(indent) {
	                    contentHandler.characters(INDENT_LF, 0, 1);
	                    contentHandler.characters(INDENT_2, 0, 2);
	                }
	
	                // Don't insert the element if the csv record does not contain it!!
	                if (recordIt < csvRecord.length) {
	                	String value = csvRecord[recordIt];
	
	                    contentHandler.startElement(XMLConstants.NULL_NS_URI, fieldName, StringUtils.EMPTY, EMPTY_ATTRIBS);
	
	                    StringFunctionExecutor stringFunctionExecutor = field.getStringFunctionExecutor();
	                    if(stringFunctionExecutor != null) {
	                    	value = stringFunctionExecutor.execute(value);
	                    }
	
	                    contentHandler.characters(value.toCharArray(), 0, value.length());
	                    contentHandler.endElement(XMLConstants.NULL_NS_URI, fieldName, StringUtils.EMPTY);
	                }
	
	                if(indent) {
	                }
	
	                recordIt++;
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
	
	        // Close out the "csv-set" root element and end the document..
	        contentHandler.endElement(XMLConstants.NULL_NS_URI, rootElementName, StringUtils.EMPTY);
	        contentHandler.endDocument();
        } finally {
        	// These properties need to be reset for every execution (e.g. when reader is pooled).
        	contentHandler = null;
        	execContext = null;
        }
	}

	private void validateHeader(final au.com.bytecode.opencsv.CSVReader reader) throws IOException {
		String[] headers = reader.readNext();
		if (headers == null) {
			throw new CSVHeaderValidationException(Arrays.asList(getFieldNames(fields)));
		}

		if (validateHeader(fields, headers)) {
			return;
		}

		throw new CSVHeaderValidationException(Arrays.asList(getFieldNames(fields)), Arrays.asList(headers));
	}

	private String[] getFieldNames(final Field[] fields) {
		if (fields == null) {
			return new String[] {};
		}

		String[] names = new String[fields.length];

		int n = 0;
		for (Field field : fields) {
			if (!field.ignore()) {
				names[n] = field.getName();
			}
			n++;
		}

		return names;
	}

	private boolean validateHeader(final Field[] fields, final String[] headers) {
		if (fields.length != headers.length) {
			return false;
		}

		int n = 0;
		for (Field field : fields) {
			if (!field.ignore()) {
				if (headers.length <= n) {
					return false;
				}

				String header = headers[n];
				if (header == null) {
					header = "";
				}

				String name = field.getName();
				if (name == null) {
					name = "";
				}

				if (!name.equals(header)) {
					return false;
				}
			}
			n++;
		}

		return true;
	}

	private int parseIgnoreFieldDirective(String field) {
        String op = field.substring(IGNORE_FIELD.length());
        int toSkip = 0;
        if (op.length() == 0) {
            toSkip = 1;
        } else if ("+".equals(op)) {
            toSkip = Integer.MAX_VALUE;
        } else {
            toSkip = Integer.parseInt(op);
        }
        return toSkip;

    }

    private int getExpectedColumnsCount() {
        int count = 0;
        for (Field field : fields) {
            if (!field.ignore()) {
                count++;
            }
        }
        return count;
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

    /****************************************************************************
     *
     * The following methods are currently unimplemnted...
     *
     ****************************************************************************/

    public void parse(String systemId) throws IOException, SAXException {
        throw new UnsupportedOperationException("Operation not supports by this reader.");
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

    	private final boolean ignore;

    	private final StringFunctionExecutor stringFunctionExecutor;

        public Field(String name, StringFunctionExecutor stringFunctionExecutor) {
			this.name = name;
			this.stringFunctionExecutor = stringFunctionExecutor;

			ignore = name.startsWith(IGNORE_FIELD);
		}

		public String getName() {
			return name;
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
            if(keyExtractor.getExpression().indexOf(object.toString()) != -1) {
                return true;
            }

            return false;
        }
    }
}
