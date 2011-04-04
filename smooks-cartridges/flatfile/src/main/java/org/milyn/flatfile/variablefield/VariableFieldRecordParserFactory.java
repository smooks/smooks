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
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Abstract VariableFieldRecordParserFactory.
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public abstract class VariableFieldRecordParserFactory implements RecordParserFactory, VisitorAppender {

    @ConfigParam(defaultVal = AnnotationConstants.NULL_STRING)
    private String fields;
    private RecordMetaData recordMetaData; // Initialized if there's only one record type defined
    private Map<String, RecordMetaData> recordMetaDataMap; // Initialized if there's multiple record types defined

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

    public static final Pattern SINGLE_RECORD_PATTERN = Pattern.compile("^[\\w|[?$-_, ]]+$");
    public static final Pattern MULTI_RECORD_PATTERN = Pattern.compile("^([\\w|[?$-_*]]+)\\[([\\w|[?$-_, *]]+)\\]$");

    /**
     * Is this a parser factory for a multi-record type data stream.
     * @return True if this is a parser factory for a multi-record type data
     * stream, otherwise false.
     */
    public boolean isMultiTypeRecordSet() {
        return (recordMetaData == null && recordMetaDataMap != null);
    }

    /**
     * Get the record metadata for the variable field record parser.
     * @return The record metadata.
     * @see #isMultiTypeRecordSet()
     */
    public RecordMetaData getRecordMetaData() {
        if(isMultiTypeRecordSet()) {
            throw new IllegalStateException("Invalid call to getRecordMetaData().  This is a multi-type record set.  Must call getRecordMetaData(String recordTypeName).");
        }
        return recordMetaData;
    }

    /**
     * Get the record metadata for the variable field record parser.
     * @param recordTypeName The name of the record type.
     * @return The record metadata.
     * @see #isMultiTypeRecordSet()
     */
    public RecordMetaData getRecordMetaData(String recordTypeName) {
        AssertArgument.isNotNullAndNotEmpty(recordTypeName, "recordTypeName");
        if(!isMultiTypeRecordSet()) {
            throw new IllegalStateException("Invalid call to getRecordMetaData(String recordTypeName).  This is not a multi-type record set.  Must call getRecordMetaData().");
        }
        return recordMetaDataMap.get(recordTypeName);
    }

    /**
     * Get the record metadata for the record.
     * @param record The record.
     * @return The record metadata.
     * @see #isMultiTypeRecordSet()
     */
    public RecordMetaData getRecordMetaData(String[] record) {
        AssertArgument.isNotNullAndNotEmpty(record, "record");
        if(!isMultiTypeRecordSet()) {
            return recordMetaData;
        } else {
            return recordMetaDataMap.get(record[0].trim());
        }
    }

    /**
     * Get the record metadata for the record.
     * @param record The record.
     * @return The record metadata.
     * @see #isMultiTypeRecordSet()
     */
    public RecordMetaData getRecordMetaData(Collection<String> record) {
        AssertArgument.isNotNullAndNotEmpty(record, "record");
        if(!isMultiTypeRecordSet()) {
            return recordMetaData;
        } else {
            return recordMetaDataMap.get(record.iterator().next().trim());
        }
    }

    /**
     * Get the default record element name.
     * @return The default record element name.
     */
    public String getRecordElementName() {
        return recordElementName;
    }

    public void addVisitors(VisitorConfigMap visitorMap) {
        if(bindBeanId != null && bindBeanClass != null) {
            Bean bean;

            if(isMultiTypeRecordSet()) {
                throw new SmooksConfigurationException("Unsupported reader based bean binding config for a multi record type record set.  " +
                        "Only supported for single record type record sets.  Use <jb:bean> configs for multi binding record type record sets.");
            }

            if(bindingType == BindingType.LIST) {
                Bean listBean = new Bean(ArrayList.class, bindBeanId, SmooksResourceConfiguration.DOCUMENT_FRAGMENT_SELECTOR);

                bean = listBean.newBean(bindBeanClass, recordElementName);
                listBean.bindTo(bean);
                addFieldBindings(bean);

                listBean.addVisitors(visitorMap);
            } else if(bindingType == BindingType.MAP) {
                if(bindMapKeyField == null) {
                    throw new SmooksConfigurationException("'MAP' Binding must specify a 'keyField' property on the binding configuration.");
                }

                recordMetaData.assertValidFieldName(bindMapKeyField);

                Bean mapBean = new Bean(LinkedHashMap.class, bindBeanId, SmooksResourceConfiguration.DOCUMENT_FRAGMENT_SELECTOR);
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
    public final void buildRecordMetaData() {
        if(fields == null) {
            recordMetaData = new RecordMetaData(recordElementName, new ArrayList<FieldMetaData>(), true);
        } else{
            String[] recordDefs = fields.split("\\|");

            for(int i = 0; i < recordDefs.length; i++) {
                recordDefs[i] = recordDefs[i].trim();
            }

            if(recordDefs.length == 1) {
                recordDefs[0] = recordDefs[0].trim();
                if(SINGLE_RECORD_PATTERN.matcher(recordDefs[0]).matches()) {
                    recordMetaData = buildRecordMetaData(recordElementName, recordDefs[0].split(","));
                    return;
                }

                recordMetaData = buildMultiRecordMetaData(recordDefs[0]);
                if(recordMetaData == null) {
                    throw new SmooksConfigurationException("Unsupported fields definition '" + fields + "'.  Must match either the single ('" + SINGLE_RECORD_PATTERN.pattern() + "') or multi ('" + MULTI_RECORD_PATTERN.pattern() + "') record pattern.");
                }
            } else {
                for(String recordDef : recordDefs) {
                    recordDef = recordDef.trim();
                    RecordMetaData multiRecordMetaData = buildMultiRecordMetaData(recordDef);
                    if(multiRecordMetaData == null) {
                        throw new SmooksConfigurationException("Unsupported fields definition '" + recordDef + "'.  Must match the multi record pattern ('" + MULTI_RECORD_PATTERN.pattern() + "') .");
                    }
                    if(recordMetaDataMap == null) {
                        recordMetaDataMap = new HashMap<String, RecordMetaData>();
                    }
                    recordMetaDataMap.put(multiRecordMetaData.getName(), multiRecordMetaData);
                }
            }
        }
    }

    private RecordMetaData buildMultiRecordMetaData(String recordDef) {
        Matcher matcher = MULTI_RECORD_PATTERN.matcher(recordDef);

        if(matcher.matches()) {
            return buildRecordMetaData(matcher.group(1), matcher.group(2).split(","));
        }

        return null;
    }

    private RecordMetaData buildRecordMetaData(String recordName, String[] fieldNames) {
		// Parse input fields to extract names and lengths
        List<FieldMetaData> fieldsMetaData = new ArrayList<FieldMetaData>();

    	for(int i = 0; i < fieldNames.length; i++) {
            String fieldSpec = fieldNames[i].trim();

            if(fieldSpec.equals("*")) {
                return new RecordMetaData(recordName, fieldsMetaData, true);
            } else {
                FieldMetaData fieldMetaData;
                if(fieldSpec.indexOf('?') >= 0) {
                    String fieldName = fieldSpec.substring(0, fieldSpec.indexOf('?'));
                    String functionDefinition = fieldSpec.substring(fieldSpec.indexOf('?')+1);

                    fieldMetaData = new FieldMetaData(fieldName);
                    if(functionDefinition.length() != 0) {
                        fieldMetaData.setStringFunctionExecutor(StringFunctionExecutor.getInstance(functionDefinition));
                    }
                } else {
                    fieldMetaData = new FieldMetaData(fieldSpec);
                }

                fieldsMetaData.add(fieldMetaData);
                if(fieldMetaData.ignore() && fieldMetaData.getIgnoreCount() > 1 && fieldMetaData.getIgnoreCount() < Integer.MAX_VALUE) {
                    // pad out with an FieldMetaData instance for each additionally ignored
                    // field in the record...
                    for(int ii = 0; ii < fieldMetaData.getIgnoreCount() - 1; ii++) {
                        fieldsMetaData.add(new FieldMetaData(FieldMetaData.IGNORE_FIELD));
                    }
                }
            }
    	}

        return new RecordMetaData(recordName, fieldsMetaData);
	}

    private void addFieldBindings(Bean bean) {
        for(FieldMetaData fieldMetaData : recordMetaData.getFields()) {
            if(!fieldMetaData.ignore()) {
                bean.bindTo(fieldMetaData.getName(), recordElementName + "/" + fieldMetaData.getName());
            }
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
