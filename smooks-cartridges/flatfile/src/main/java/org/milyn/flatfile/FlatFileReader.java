/*
 * Milyn - Copyright (C) 2006 - 2010
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License (version 2.1) as published by the Free Software
 * Foundation.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU Lesser General Public License for more details:
 * http://www.gnu.org/licenses/lgpl.txt
 */

package org.milyn.flatfile;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.cdr.annotation.AppContext;
import org.milyn.cdr.annotation.Config;
import org.milyn.cdr.annotation.ConfigParam;
import org.milyn.cdr.annotation.Configurator;
import org.milyn.container.ApplicationContext;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.VisitorAppender;
import org.milyn.delivery.VisitorConfigMap;
import org.milyn.delivery.annotation.Initialize;
import org.milyn.xml.SmooksXMLReader;
import org.milyn.xml.XmlUtil;
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
import java.util.List;

/**
 * Flat file reader.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class FlatFileReader implements SmooksXMLReader, VisitorAppender {

	private static Log logger = LogFactory.getLog(FlatFileReader.class);
    private static Attributes EMPTY_ATTRIBS = new AttributesImpl();

    private static char[] INDENT_LF = new char[] {'\n'};
    private static char[] INDENTCHARS = new char[] {'\t', '\t'};
    private static String RECORD_NUMBER_ATTR = "number";
    private static String RECORD_TRUNCATED_ATTR = "truncated";

    private ContentHandler contentHandler;
	private ExecutionContext execContext;

    @Config
    private SmooksResourceConfiguration config;

    @AppContext
    private ApplicationContext appContext;

    @ConfigParam(name = "parserFactory")
    private Class<? extends RecordParserFactory> parserFactoryClass;
    private RecordParserFactory parserFactory;

    @ConfigParam(defaultVal = "\n")
    private String recordDelimiter;

    @ConfigParam(defaultVal="record-set")
    private String rootElementName;

    @ConfigParam(defaultVal="false")
    private boolean indent;

    @ConfigParam(defaultVal="true")
    private boolean strict;

	@Initialize
	public void initialize() throws IllegalAccessException, InstantiationException {
        parserFactory = parserFactoryClass.newInstance();
        Configurator.configure(parserFactory, config, appContext);

        // Fixup the record delimiter...
        recordDelimiter = removeSpecialCharEncodeString(recordDelimiter, "\\n", '\n');
        recordDelimiter = removeSpecialCharEncodeString(recordDelimiter, "\\r", '\r');
        recordDelimiter = removeSpecialCharEncodeString(recordDelimiter, "\\t", '\t');
        recordDelimiter = XmlUtil.removeEntities(recordDelimiter);
	}

    public void addVisitors(VisitorConfigMap visitorMap) {
        if(parserFactory instanceof VisitorAppender) {
            ((VisitorAppender) parserFactory).addVisitors(visitorMap);
        }
    }

    private static String removeSpecialCharEncodeString(String string, String encodedString, char replaceChar) {
        return string.replaceAll(encodedString, new String(new char[] {replaceChar}));
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
            throw new IllegalStateException("'contentHandler' not set.  Cannot parse Record stream.");
        }
        if(execContext == null) {
            throw new IllegalStateException("'execContext' not set.  Cannot parse Record stream.");
        }

        try {
			Reader recordReader;

			// Get a reader for the CSV source...
	        recordReader = csvInputSource.getCharacterStream();
	        if(recordReader == null) {
	            recordReader = new InputStreamReader(csvInputSource.getByteStream(), execContext.getContentEncoding());
	        }

            // Create the record parser....
            RecordParser recordParser = parserFactory.newRecordParser();
            recordParser.setRecordParserFactory(parserFactory);
            recordParser.setReader(recordReader);

	        // Start the document and add the root "record-set" element...
	        contentHandler.startDocument();
	        contentHandler.startElement(XMLConstants.NULL_NS_URI, rootElementName, StringUtils.EMPTY, EMPTY_ATTRIBS);
	
	        // Output each of the CVS line entries...
	        int lineNumber = 0;

            Record record = recordParser.nextRecord();
            while (record != null) {
                lineNumber++; // First line is line "1"

                if(record != null) {
                    List<Field> recordFields = record.getFields();

                    if(indent) {
                        contentHandler.characters(INDENT_LF, 0, 1);
                        contentHandler.characters(INDENTCHARS, 0, 1);
                    }

                    AttributesImpl attrs = new AttributesImpl();
                    attrs.addAttribute(XMLConstants.NULL_NS_URI, RECORD_NUMBER_ATTR, RECORD_NUMBER_ATTR, "xs:int", Integer.toString(lineNumber));

                    RecordMetaData recordMetaData = record.getRecordMetaData();
                    if(recordFields.size() < recordMetaData.getUnignoredFieldCount()) {
                        attrs.addAttribute(XMLConstants.NULL_NS_URI, RECORD_TRUNCATED_ATTR, RECORD_TRUNCATED_ATTR, "xs:boolean", Boolean.TRUE.toString());
                    }

                    contentHandler.startElement(XMLConstants.NULL_NS_URI, record.getName(), StringUtils.EMPTY, attrs);
                    for(Field recordField : recordFields) {
                        String fieldName = recordField.getName();

                        if(indent) {
                            contentHandler.characters(INDENT_LF, 0, 1);
                            contentHandler.characters(INDENTCHARS, 0, 2);
                        }

                        contentHandler.startElement(XMLConstants.NULL_NS_URI, fieldName, StringUtils.EMPTY, EMPTY_ATTRIBS);

                        String value = recordField.getValue();
                        contentHandler.characters(value.toCharArray(), 0, value.length());
                        contentHandler.endElement(XMLConstants.NULL_NS_URI, fieldName, StringUtils.EMPTY);
                    }

                    if(indent) {
                        contentHandler.characters(INDENT_LF, 0, 1);
                        contentHandler.characters(INDENTCHARS, 0, 1);
                    }

                    contentHandler.endElement(XMLConstants.NULL_NS_URI, record.getName(), StringUtils.EMPTY);
                }

                record = recordParser.nextRecord();
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

    private void readRecord(Reader recordReader, StringBuilder recordBuffer) throws IOException {
        recordBuffer.setLength(0);

        int c;
        boolean removeCRLF = true;
        while((c = recordReader.read()) != -1) {
            if(removeCRLF) {
                if(c == '\n' || c == '\r') {
                    // A leading CR or LF... ignore...
                    continue;
                } else {
                    // All leading CR and LF chars are skipped...
                    removeCRLF = false;
                }
            }

            recordBuffer.append((char)c);
            if(builderEndsWith(recordBuffer, recordDelimiter)) {
                // Strip off the delimiter from the end before returning...
                recordBuffer.setLength(recordBuffer.length() - recordDelimiter.length());
                break;
            }
        }
    }

    private static boolean builderEndsWith(StringBuilder stringBuilder, String string) {
        if(string == null) {
            return false;
        }

        int builderLen = stringBuilder.length();
        int stringLen = string.length();

        if(builderLen < stringLen) {
            return false;
        }

        int stringIndx = 0;
        for(int i = (builderLen - stringLen); i < builderLen; i++) {
            if(stringBuilder.charAt(i) != string.charAt(stringIndx)) {
                return false;
            }
            stringIndx++;
        }

        return true;
    }

    public void setContentHandler(ContentHandler contentHandler) {
        this.contentHandler = contentHandler;
    }

    public ContentHandler getContentHandler() {
        return contentHandler;
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

    public Object getProperty(String name) throws SAXNotRecognizedException, SAXNotSupportedException {
        return null;
    }

    public void setProperty(String name, Object value)
            throws SAXNotRecognizedException, SAXNotSupportedException {
    }
}
