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
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.milyn.SmooksException;
import org.milyn.assertion.AssertArgument;
import org.milyn.cdr.SmooksConfigurationException;
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
import org.milyn.function.StringFunctionExecutor;
import org.milyn.javabean.Bean;
import org.milyn.javabean.context.BeanContext;
import org.milyn.xml.XmlUtil;
import org.w3c.dom.Element;

/**
 * Abstract VariableFieldRecordParserFactory.
 * 
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public abstract class VariableFieldRecordParserFactory implements RecordParserFactory, VisitorAppender {

    @ConfigParam(defaultVal = AnnotationConstants.NULL_STRING)
    private String fields;
    private RecordMetaData recordMetaData; // Initialized if there's only one
                                           // record type defined
    private Map<String, RecordMetaData> recordMetaDataMap; // Initialized if
                                                           // there's multiple
                                                           // record types
                                                           // defined

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

    @ConfigParam(defaultVal = "true")
    private boolean strict;

    private String overFlowFromLastRecord = "";

    public static final Pattern SINGLE_RECORD_PATTERN = Pattern.compile("^[\\w|[?$-_, ]]+$");

    public static final Pattern MULTI_RECORD_PATTERN = Pattern.compile("^([\\w|[?$-_*]]+)\\[([\\w|[?$-_, *]]+)\\]$");
    protected static RecordMetaData unknownVRecordType;

    static {
        unknownVRecordType = new RecordMetaData("UNMATCHED", new ArrayList<FieldMetaData>());
        unknownVRecordType.getFields().add(new FieldMetaData("value"));
    }

    /**
     * Is this a parser factory for a multi-record type data stream.
     * 
     * @return True if this is a parser factory for a multi-record type data
     *         stream, otherwise false.
     */
    public boolean isMultiTypeRecordSet() {
        return (recordMetaData == null && recordMetaDataMap != null);
    }

    /**
     * Get the record metadata for the variable field record parser.
     * 
     * @return The record metadata.
     * @see #isMultiTypeRecordSet()
     */
    public RecordMetaData getRecordMetaData() {
        if (isMultiTypeRecordSet()) {
            throw new IllegalStateException(
                    "Invalid call to getRecordMetaData().  This is a multi-type record set.  Must call getRecordMetaData(String recordTypeName).");
        }
        return recordMetaData;
    }

    /**
     * Get the record metadata for the variable field record parser.
     * 
     * @param recordTypeName The name of the record type.
     * @return The record metadata.
     * @see #isMultiTypeRecordSet()
     */
    public RecordMetaData getRecordMetaData(String recordTypeName) {
        AssertArgument.isNotNullAndNotEmpty(recordTypeName, "recordTypeName");
        if (!isMultiTypeRecordSet()) {
            throw new IllegalStateException(
                    "Invalid call to getRecordMetaData(String recordTypeName).  This is not a multi-type record set.  Must call getRecordMetaData().");
        }
        return recordMetaDataMap.get(recordTypeName);
    }

    /**
     * Get the record metadata for the record.
     * 
     * @param record The record.
     * @return The record metadata.
     * @see #isMultiTypeRecordSet()
     */
    public RecordMetaData getRecordMetaData(String[] record) {
        AssertArgument.isNotNullAndNotEmpty(record, "record");
        if (!isMultiTypeRecordSet()) {
            return recordMetaData;
        } else {
            return recordMetaDataMap.get(record[0].trim());
        }
    }

    /**
     * Get the record metadata for the record.
     * 
     * @param record The record.
     * @return The record metadata.
     * @see #isMultiTypeRecordSet()
     */
    public RecordMetaData getRecordMetaData(Collection<String> record) {
        AssertArgument.isNotNullAndNotEmpty(record, "record");
        if (!isMultiTypeRecordSet()) {
            return recordMetaData;
        } else {
            RecordMetaData vrecordMetaData = recordMetaDataMap.get(record.iterator().next().trim());
            if (vrecordMetaData == null) {
                vrecordMetaData = unknownVRecordType;
            }
            return vrecordMetaData;
        }
    }

    /**
     * Get the default record element name.
     * 
     * @return The default record element name.
     */
    public String getRecordElementName() {
        return recordElementName;
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

            if (isMultiTypeRecordSet()) {
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

                recordMetaData.assertValidFieldName(bindMapKeyField);

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
        if (fields == null) {
            recordMetaData = new RecordMetaData(recordElementName, new ArrayList<FieldMetaData>(), true);
        } else {
            String[] recordDefs = fields.split("\\|");

            for (int i = 0; i < recordDefs.length; i++) {
                recordDefs[i] = recordDefs[i].trim();
            }

            if (recordDefs.length == 1) {
                recordDefs[0] = recordDefs[0].trim();
                if (SINGLE_RECORD_PATTERN.matcher(recordDefs[0]).matches()) {
                    recordMetaData = buildRecordMetaData(recordElementName, recordDefs[0].split(","));
                    return;
                }

                recordMetaData = buildMultiRecordMetaData(recordDefs[0]);
                if (recordMetaData == null) {
                    throw new SmooksConfigurationException("Unsupported fields definition '" + fields
                            + "'.  Must match either the single ('" + SINGLE_RECORD_PATTERN.pattern()
                            + "') or multi ('" + MULTI_RECORD_PATTERN.pattern() + "') record pattern.");
                }
            } else {
                for (String recordDef : recordDefs) {
                    recordDef = recordDef.trim();
                    RecordMetaData multiRecordMetaData = buildMultiRecordMetaData(recordDef);
                    if (multiRecordMetaData == null) {
                        throw new SmooksConfigurationException("Unsupported fields definition '" + recordDef
                                + "'.  Must match the multi record pattern ('" + MULTI_RECORD_PATTERN.pattern()
                                + "') .");
                    }
                    if (recordMetaDataMap == null) {
                        recordMetaDataMap = new HashMap<String, RecordMetaData>();
                    }
                    recordMetaDataMap.put(multiRecordMetaData.getName(), multiRecordMetaData);
                }
            }
        }
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

    private RecordMetaData buildMultiRecordMetaData(String recordDef) {
        Matcher matcher = MULTI_RECORD_PATTERN.matcher(recordDef);

        if (matcher.matches()) {
            return buildRecordMetaData(matcher.group(1), matcher.group(2).split(","));
        }

        return null;
    }

    private RecordMetaData buildRecordMetaData(String recordName, String[] fieldNames) {
        // Parse input fields to extract names and lengths
        List<FieldMetaData> fieldsMetaData = new ArrayList<FieldMetaData>();

        for (int i = 0; i < fieldNames.length; i++) {
            String fieldSpec = fieldNames[i].trim();

            if (fieldSpec.equals("*")) {
                return new RecordMetaData(recordName, fieldsMetaData, true);
            } else {
                FieldMetaData fieldMetaData;
                if (fieldSpec.indexOf('?') >= 0) {
                    String fieldName = fieldSpec.substring(0, fieldSpec.indexOf('?'));
                    String functionDefinition = fieldSpec.substring(fieldSpec.indexOf('?') + 1);

                    fieldMetaData = new FieldMetaData(fieldName);
                    if (functionDefinition.length() != 0) {
                        fieldMetaData.setStringFunctionExecutor(StringFunctionExecutor.getInstance(functionDefinition));
                    }
                } else {
                    fieldMetaData = new FieldMetaData(fieldSpec);
                }

                fieldsMetaData.add(fieldMetaData);
                if (fieldMetaData.ignore() && fieldMetaData.getIgnoreCount() > 1
                        && fieldMetaData.getIgnoreCount() < Integer.MAX_VALUE) {
                    // pad out with an FieldMetaData instance for each
                    // additionally ignored
                    // field in the record...
                    for (int ii = 0; ii < fieldMetaData.getIgnoreCount() - 1; ii++) {
                        fieldsMetaData.add(new FieldMetaData(FieldMetaData.IGNORE_FIELD));
                    }
                }
                if (fieldMetaData.useHeader() && fieldMetaData.getUseHeaderCount() > 1
                        && fieldMetaData.getUseHeaderCount() < Integer.MAX_VALUE) {
                    // pad out with an FieldMetaData instance for each
                    // additionally ignored
                    // field in the record...
                    for (int ii = 0; ii < fieldMetaData.getUseHeaderCount() - 1; ii++) {
                        fieldsMetaData.add(new FieldMetaData(FieldMetaData.USE_HEADER));
                    }
                }
            }
        }

        return new RecordMetaData(recordName, fieldsMetaData);
    }

    private void addFieldBindings(Bean bean) {
        for (FieldMetaData fieldMetaData : recordMetaData.getFields()) {
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
