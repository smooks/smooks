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

package org.milyn.flatfile.variablefield;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.milyn.commons.SmooksException;
import org.milyn.commons.cdr.SmooksConfigurationException;
import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.cdr.annotation.AnnotationConstants;
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
import org.milyn.flatfile.BindingType;
import org.milyn.flatfile.FieldMetaData;
import org.milyn.flatfile.RecordMetaData;
import org.milyn.flatfile.RecordParserFactory;
import org.milyn.cartridge.javabean.Bean;
import org.milyn.javabean.context.BeanContext;
import org.milyn.commons.xml.XmlUtil;
import org.w3c.dom.Element;

/**
 * Abstract VariableFieldRecordParserFactory.
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public abstract class VariableFieldRecordParserFactory implements RecordParserFactory, VisitorAppender {

    @ConfigParam(defaultVal = AnnotationConstants.NULL_STRING)
    private String fields;

    private VariableFieldRecordMetaData vfRecordMetaData;

    @ConfigParam(use = ConfigParam.Use.OPTIONAL)
    private String recordDelimiter;
    private Pattern recordDelimiterPattern;

    @ConfigParam(defaultVal = "false")
    private boolean keepDelimiter;

    @ConfigParam(defaultVal = "record")
    private String recordElementName;

    @ConfigParam(use = ConfigParam.Use.OPTIONAL)
    private String bindBeanId;

    @ConfigParam(use = ConfigParam.Use.OPTIONAL)
    private Class<?> bindBeanClass;

    @ConfigParam(use = ConfigParam.Use.OPTIONAL)
    private BindingType bindingType;
    @ConfigParam(use = ConfigParam.Use.OPTIONAL)
    private String bindMapKeyField;
    private static final String RECORD_BEAN = "recordBean";

    @ConfigParam(name = "skip-line-count", defaultVal = "0")
    private int skipLines;

    @ConfigParam(name = "fields-in-message", defaultVal = "0")
    private boolean fieldsInMessage;

    @ConfigParam(defaultVal = "false")
    private boolean validateHeader;

    @ConfigParam(defaultVal = "false")
    private boolean strict;

    private String overFlowFromLastRecord = "";

    public int getSkipLines() {
        if (skipLines < 0) {
            return 0;
        } else {
            return skipLines;
        }
    }

    public boolean fieldsInMessage() {
        return fieldsInMessage;
    }

    public boolean validateHeader() {
        return validateHeader;
    }

    /**
     * Get the default record element name.
     * 
     * @return The default record element name.
     */
    public String getRecordElementName() {
        return recordElementName;
    }

    public RecordMetaData getRecordMetaData() {
        return vfRecordMetaData.getRecordMetaData();
    }

    /**
     * Get the {@link RecordMetaData} instance for the specified fields.
     * @param fieldValues The fields.
     * @return The RecordMetaData instance.
     */
    public RecordMetaData getRecordMetaData(List<String> fieldValues) {
        return vfRecordMetaData.getRecordMetaData(fieldValues);
    }

    /**
     * Is the parser configured to parse multiple record types.
     * @return True if the parser configured to parse multiple record types, otherwise false.
     */
    public boolean isMultiTypeRecordSet() {
        return vfRecordMetaData.isMultiTypeRecordSet();
    }

    /**
     * Is this parser instance strict.
     * 
     * @return True if the parser is strict, otherwise false.
     */
    public boolean strict() {
        return strict;
    }

    public void addVisitors(VisitorConfigMap visitorMap) {
        if (bindBeanId != null && bindBeanClass != null) {
            Bean bean;

            if (fieldsInMessage) {
                throw new SmooksConfigurationException("Unsupported reader based bean binding config.  Not supported when fields are defined in message.  See 'fieldsInMessage' attribute.");
            }

            if (vfRecordMetaData.isMultiTypeRecordSet()) {
                throw new SmooksConfigurationException(
                        "Unsupported reader based bean binding config for a multi record type record set.  "
                                + "Only supported for single record type record sets.  Use <jb:bean> configs for multi binding record type record sets.");
            }

            if (bindingType == BindingType.LIST) {
                Bean listBean = new Bean(ArrayList.class, bindBeanId,
                        SmooksResourceConfiguration.DOCUMENT_FRAGMENT_SELECTOR);

                bean = listBean.newBean(bindBeanClass, recordElementName);
                listBean.bindTo(bean);
                addFieldBindings(bean);

                listBean.addVisitors(visitorMap);
            } else if (bindingType == BindingType.MAP) {
                if (bindMapKeyField == null) {
                    throw new SmooksConfigurationException(
                            "'MAP' Binding must specify a 'keyField' property on the binding configuration.");
                }

                vfRecordMetaData.getRecordMetaData().assertValidFieldName(bindMapKeyField);

                Bean mapBean = new Bean(LinkedHashMap.class, bindBeanId,
                        SmooksResourceConfiguration.DOCUMENT_FRAGMENT_SELECTOR);
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

    @Initialize
    public final void fixupRecordDelimiter() {
        if (recordDelimiter == null) {
            return;
        }

        // Fixup the record delimiter...
        if (recordDelimiter.startsWith("regex:")) {
            recordDelimiterPattern = Pattern.compile(recordDelimiter.substring("regex:".length()),
                    (Pattern.MULTILINE | Pattern.DOTALL));
        } else {
            recordDelimiter = removeSpecialCharEncodeString(recordDelimiter, "\\n", '\n');
            recordDelimiter = removeSpecialCharEncodeString(recordDelimiter, "\\r", '\r');
            recordDelimiter = removeSpecialCharEncodeString(recordDelimiter, "\\t", '\t');
            recordDelimiter = XmlUtil.removeEntities(recordDelimiter);
        }
    }

    @Initialize
    public final void buildRecordMetaData() {
        vfRecordMetaData = new VariableFieldRecordMetaData(recordElementName, fields);
    }

    /**
     * Read a record from the specified reader (up to the next recordDelimiter).
     * 
     * @param recordReader The record {@link Reader}.
     * @param recordBuffer The record buffer into which the record is read.
     * @throws IOException Error reading record.
     */
    public void readRecord(Reader recordReader, StringBuilder recordBuffer, int recordNumber) throws IOException {
        recordBuffer.setLength(0);
        recordBuffer.append(overFlowFromLastRecord);

        RecordBoundaryLocator boundaryLocator;
        if (recordDelimiterPattern != null) {
            boundaryLocator = new RegexRecordBoundaryLocator(recordBuffer, recordNumber);
        } else {
            boundaryLocator = new SimpleRecordBoundaryLocator(recordBuffer, recordNumber);
        }

        int c;
        while ((c = recordReader.read()) != -1) {
            if (recordBuffer.length() == 0) {
                if (c == '\n' || c == '\r') {
                    // A leading CR or LF... ignore...
                    continue;
                }
            }

            recordBuffer.append((char) c);
            if (boundaryLocator.atEndOfRecord()) {
                break;
            }
        }

        overFlowFromLastRecord = boundaryLocator.getOverflowCharacters();
    }

    private void addFieldBindings(Bean bean) {
        for (FieldMetaData fieldMetaData : vfRecordMetaData.getRecordMetaData().getFields()) {
            if (!fieldMetaData.ignore()) {
                bean.bindTo(fieldMetaData.getName(), recordElementName + "/" + fieldMetaData.getName());
            }
        }
    }

    private static String removeSpecialCharEncodeString(String string, String encodedString, char replaceChar) {
        return string.replace(encodedString, new String(new char[] { replaceChar }));
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

        public void visitAfter(SAXElement element, ExecutionContext executionContext) throws SmooksException,
                IOException {
            wireObject(executionContext);
        }

        private void wireObject(ExecutionContext executionContext) {
            BeanContext beanContext = executionContext.getBeanContext();
            Map<String, Object> beanMap = beanContext.getBeanMap();
            Object key = keyExtractor.getValue(beanMap);

            @SuppressWarnings("unchecked")
            // TODO: Optimize to use the BeanId object
            Map<Object, Object> map = (Map<Object, Object>) beanContext.getBean(mapBindingKey);
            Object record = beanContext.getBean(RECORD_BEAN);

            map.put(key, record);
        }

        public boolean consumes(Object object) {
            if (keyExtractor.getExpression().indexOf(object.toString()) != -1) {
                return true;
            }

            return false;
        }
    }

    private class SimpleRecordBoundaryLocator extends RecordBoundaryLocator {

        private SimpleRecordBoundaryLocator(StringBuilder recordBuffer, int recordNumber) {
            super(recordBuffer, recordNumber);
        }

        @Override
        boolean atEndOfRecord() {
            int builderLen = recordBuffer.length();
            char lastChar = recordBuffer.charAt(builderLen - 1);

            if (recordDelimiter != null) {
                int stringLen = recordDelimiter.length();

                if (builderLen < stringLen) {
                    return false;
                }

                int stringIndx = 0;
                for (int i = (builderLen - stringLen); i < builderLen; i++) {
                    if (recordBuffer.charAt(i) != recordDelimiter.charAt(stringIndx)) {
                        return false;
                    }
                    stringIndx++;
                }

                if (!keepDelimiter) {
                    // Strip off the delimiter from the end before returning...
                    recordBuffer.setLength(builderLen - stringLen);
                }

                return true;
            } else if (lastChar == '\r' || lastChar == '\n') {
                if (!keepDelimiter) {
                    // Strip off the delimiter from the end before returning...
                    recordBuffer.setLength(builderLen - 1);
                }
                return true;
            }

            return false;
        }

        @Override
        String getOverflowCharacters() {
            return "";
        }
    }

    private class RegexRecordBoundaryLocator extends RecordBoundaryLocator {

        private int startFindIndex;
        private int endRecordIndex;
        private String overFlow = "";

        protected RegexRecordBoundaryLocator(StringBuilder recordBuffer, int recordNumber) {
            super(recordBuffer, recordNumber);
            startFindIndex = recordBuffer.length();
        }

        @Override
        boolean atEndOfRecord() {
            Matcher matcher = recordDelimiterPattern.matcher(recordBuffer);

            if (matcher.find(startFindIndex)) {
                if (recordNumber == 1 && startFindIndex == 0) {
                    // Need to find the second instance of the pattern in the
                    // first record buffer
                    // The second instance marks the start of the second record,
                    // which will be captured
                    // as overflow from this record read and will be auto added
                    // to the read of the next record.
                    startFindIndex = matcher.end();
                    return false;
                } else {
                    // For records following the first record, we already have
                    // the start so we just need to find
                    // the first instance of the pattern, which marks the start
                    // of the next record, which again
                    // will be captured as overflow from this record read and
                    // will be auto added to the read of
                    // the next record.
                    endRecordIndex = matcher.start();
                    overFlow = recordBuffer.substring(endRecordIndex);
                    recordBuffer.setLength(endRecordIndex);
                    return true;
                }
            }

            return false;
        }

        @Override
        String getOverflowCharacters() {
            return overFlow;
        }
    }

    private abstract class RecordBoundaryLocator {

        protected StringBuilder recordBuffer;
        protected int recordNumber;

        protected RecordBoundaryLocator(StringBuilder recordBuffer, int recordNumber) {
            this.recordBuffer = recordBuffer;
            this.recordNumber = recordNumber;
        }

        abstract boolean atEndOfRecord();

        abstract String getOverflowCharacters();
    }
}
