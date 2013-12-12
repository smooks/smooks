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
package org.milyn.cartridge.javabean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.cdr.annotation.AnnotationConstants;
import org.milyn.cdr.annotation.AppContext;
import org.milyn.cdr.annotation.Config;
import org.milyn.cdr.annotation.ConfigParam;
import org.milyn.commons.SmooksException;
import org.milyn.commons.cdr.SmooksConfigurationException;
import org.milyn.commons.javabean.DataDecodeException;
import org.milyn.commons.javabean.DataDecoder;
import org.milyn.commons.javabean.decoders.StringDecoder;
import org.milyn.commons.util.ClassUtil;
import org.milyn.commons.util.CollectionsUtil;
import org.milyn.commons.xml.DomUtils;
import org.milyn.container.ApplicationContext;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.ContentDeliveryConfig;
import org.milyn.delivery.Fragment;
import org.milyn.delivery.annotation.Initialize;
import org.milyn.delivery.dom.DOMElementVisitor;
import org.milyn.delivery.ordering.Consumer;
import org.milyn.delivery.ordering.Producer;
import org.milyn.delivery.sax.SAXElement;
import org.milyn.delivery.sax.SAXUtil;
import org.milyn.delivery.sax.SAXVisitAfter;
import org.milyn.delivery.sax.SAXVisitBefore;
import org.milyn.event.report.annotation.VisitAfterReport;
import org.milyn.event.report.annotation.VisitBeforeReport;
import org.milyn.expression.MVELExpressionEvaluator;
import org.milyn.javabean.context.BeanContext;
import org.milyn.javabean.context.BeanIdStore;
import org.milyn.javabean.decoders.PreprocessDecoder;
import org.milyn.javabean.lifecycle.BeanContextLifecycleEvent;
import org.milyn.javabean.lifecycle.BeanLifecycle;
import org.milyn.cartridge.javabean.observers.BeanWiringObserver;
import org.milyn.cartridge.javabean.observers.ListToArrayChangeObserver;
import org.milyn.javabean.repository.BeanId;
import org.milyn.xml.NamespaceMappings;
import org.w3c.dom.Element;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Bean instance populator visitor class.
 * <p/>
 * Targeted via {@link BeanPopulator} expansion configuration.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 */
@VisitBeforeReport(condition = "parameters.containsKey('wireBeanId') || parameters.containsKey('valueAttributeName')",
        summary = "<#if resource.parameters.wireBeanId??>Create bean lifecycle observer for bean <b>${resource.parameters.wireBeanId}</b>." +
                "<#else>Populating <b>${resource.parameters.beanId}</b> with the value from the attribute <b>${resource.parameters.valueAttributeName}</b>.</#if>",
        detailTemplate = "reporting/BeanInstancePopulatorReport_Before.html")
@VisitAfterReport(condition = "!parameters.containsKey('wireBeanId') && !parameters.containsKey('valueAttributeName')",
        summary = "Populating <b>${resource.parameters.beanId}</b> with a value from this element.",
        detailTemplate = "reporting/BeanInstancePopulatorReport_After.html")
public class BeanInstancePopulator implements DOMElementVisitor, SAXVisitBefore, SAXVisitAfter, Producer, Consumer {

    private static final Log logger = LogFactory.getLog(BeanInstancePopulator.class);

    private static final String EXPRESSION_VALUE_VARIABLE_NAME = "_VALUE";

    public static final String VALUE_ATTRIBUTE_NAME = "valueAttributeName";
    public static final String VALUE_ATTRIBUTE_PREFIX = "valueAttributePrefix";

    public static final String NOTIFY_POPULATE = "org.milyn.javabean.notify.populate";

    private String id;

    @ConfigParam(name = "beanId")
    private String beanIdName;

    @ConfigParam(name = "wireBeanId", defaultVal = AnnotationConstants.NULL_STRING)
    private String wireBeanIdName;

    @ConfigParam(defaultVal = AnnotationConstants.NULL_STRING)
    private Class<?> wireBeanType;

    @ConfigParam(defaultVal = AnnotationConstants.NULL_STRING)
    private Class<? extends Annotation> wireBeanAnnotation;

    @ConfigParam(defaultVal = AnnotationConstants.NULL_STRING)
    private String expression;
    private MVELExpressionEvaluator expressionEvaluator;
    private boolean expressionHasDataVariable = false;

    @ConfigParam(defaultVal = AnnotationConstants.NULL_STRING)
    private String property;

    @ConfigParam(defaultVal = AnnotationConstants.NULL_STRING)
    private String setterMethod;

    @ConfigParam(defaultVal = AnnotationConstants.NULL_STRING)
    private String valueAttributeName;

    @ConfigParam(defaultVal = AnnotationConstants.NULL_STRING)
    private String valueAttributePrefix;
    private String valueAttributeNS;

    @ConfigParam(name = "type", defaultVal = AnnotationConstants.NULL_STRING)
    private String typeAlias;

    @ConfigParam(name = "default", defaultVal = AnnotationConstants.NULL_STRING)
    private String defaultVal;

    @ConfigParam(name = NOTIFY_POPULATE, defaultVal = "false")
    private boolean notifyPopulate;

    @Config
    private SmooksResourceConfiguration config;

    @AppContext
    private ApplicationContext appContext;

    private BeanIdStore beanIdStore;

    private BeanId beanId;

    private BeanId wireBeanId;

    private BeanRuntimeInfo beanRuntimeInfo;

    private BeanRuntimeInfo wiredBeanRuntimeInfo;
    private Method propertySetterMethod;
    private boolean checkedForSetterMethod;
    private boolean isAttribute = true;
    private DataDecoder decoder;

    private String mapKeyAttribute;

    private boolean isBeanWiring;
    private BeanWiringObserver wireByBeanIdObserver;
    private ListToArrayChangeObserver listToArrayChangeObserver;

    public SmooksResourceConfiguration getConfig() {
        return config;
    }

    public void setBeanId(String beanId) {
        this.beanIdName = beanId;
    }

    public String getBeanId() {
        return beanIdName;
    }

    public void setWireBeanId(String wireBeanId) {
        this.wireBeanIdName = wireBeanId;
    }

    public String getWireBeanId() {
        return wireBeanIdName;
    }

    public void setExpression(MVELExpressionEvaluator expression) {
        this.expressionEvaluator = expression;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getProperty() {
        return property;
    }

    public void setSetterMethod(String setterMethod) {
        this.setterMethod = setterMethod;
    }

    public void setValueAttributeName(String valueAttributeName) {
        this.valueAttributeName = valueAttributeName;
    }

    public void setValueAttributePrefix(String valueAttributePrefix) {
        this.valueAttributePrefix = valueAttributePrefix;
    }

    public void setTypeAlias(String typeAlias) {
        this.typeAlias = typeAlias;
    }

    public void setDecoder(DataDecoder decoder) {
        this.decoder = decoder;
    }

    public DataDecoder getDecoder() {
        return decoder;
    }

    public void setDefaultVal(String defaultVal) {
        this.defaultVal = defaultVal;
    }

    public boolean isBeanWiring() {
        return isBeanWiring;
    }

    /**
     * Set the resource configuration on the bean populator.
     *
     * @throws SmooksConfigurationException Incorrectly configured resource.
     */
    @Initialize
    public void initialize() throws SmooksConfigurationException {
        buildId();

        beanRuntimeInfo = BeanRuntimeInfo.getBeanRuntimeInfo(beanIdName, appContext);
        isBeanWiring = (wireBeanIdName != null || wireBeanType != null || wireBeanAnnotation != null);
        isAttribute = (valueAttributeName != null);

        if (valueAttributePrefix != null) {
            Properties namespaces = NamespaceMappings.getMappings(appContext);
            valueAttributeNS = namespaces.getProperty(valueAttributePrefix);
        }

        beanIdStore = appContext.getBeanIdStore();
        beanId = beanIdStore.getBeanId(beanIdName);

        if (setterMethod == null && property == null) {
            if (isBeanWiring && (beanRuntimeInfo.getClassification() == BeanRuntimeInfo.Classification.NON_COLLECTION || beanRuntimeInfo.getClassification() == BeanRuntimeInfo.Classification.MAP_COLLECTION)) {
                // Default the property name if it's a wiring...
                property = wireBeanIdName;
            } else if (beanRuntimeInfo.getClassification() == BeanRuntimeInfo.Classification.NON_COLLECTION) {
                throw new SmooksConfigurationException("Binding configuration for beanIdName='" + beanIdName + "' must contain " +
                        "either a 'property' or 'setterMethod' attribute definition, unless the target bean is a Collection/Array." +
                        "  Bean is type '" + beanRuntimeInfo.getPopulateType().getName() + "'.");
            }
        }

        if (beanRuntimeInfo.getClassification() == BeanRuntimeInfo.Classification.MAP_COLLECTION && property != null) {
            property = property.trim();
            if (property.length() > 1 && property.charAt(0) == '@') {
                mapKeyAttribute = property.substring(1);
            }
        }

        if (expression != null) {
            expression = expression.trim();

            expressionHasDataVariable = expression.contains(EXPRESSION_VALUE_VARIABLE_NAME);

            expression = expression.replace("this.", beanIdName + ".");
            if (expression.startsWith("+=")) {
                expression = beanIdName + "." + property + " +" + expression.substring(2);
            }
            if (expression.startsWith("-=")) {
                expression = beanIdName + "." + property + " -" + expression.substring(2);
            }

            expressionEvaluator = new MVELExpressionEvaluator();
            expressionEvaluator.setExpression(expression);

            // If we can determine the target binding type, tell MVEL.
            // If there's a decoder (a typeAlias), we define a String var instead and leave decoding
            // to the decoder...
            Class<?> bindingType = resolveBindTypeReflectively();
            if (bindingType != null) {
                if (typeAlias != null) {
                    bindingType = String.class;
                }
                expressionEvaluator.setToType(bindingType);
            }
        }

        if (wireBeanIdName != null) {
            wireBeanId = beanIdStore.getBeanId(wireBeanIdName);
            if (wireBeanId == null) {
                wireBeanId = beanIdStore.register(wireBeanIdName);
            }
        }

        if (isBeanWiring) {
            // These observers can be used concurrently across multiple execution contexts...
            wireByBeanIdObserver = new BeanWiringObserver(beanId, this).watchedBeanId(wireBeanId).watchedBeanType(wireBeanType).watchedBeanAnnotation(wireBeanAnnotation);
            if (wireBeanId != null) {
                // List to array change observer only makes sense if wiring by beanId.
                listToArrayChangeObserver = new ListToArrayChangeObserver(wireBeanId, property, this);
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Bean Instance Populator created for [" + beanIdName + "].  property=" + property);
        }
    }

    private void buildId() {
        StringBuilder idBuilder = new StringBuilder();
        idBuilder.append(BeanInstancePopulator.class.getName());
        idBuilder.append("#");
        idBuilder.append(beanIdName);

        if (property != null) {
            idBuilder.append("#")
                    .append(property);
        }
        if (setterMethod != null) {
            idBuilder.append("#")
                    .append(setterMethod)
                    .append("()");
        }
        if (wireBeanIdName != null) {
            idBuilder.append("#")
                    .append(wireBeanIdName);
        }

        id = idBuilder.toString();
    }

    public void visitBefore(Element element, ExecutionContext executionContext) throws SmooksException {
        if (!beanExists(executionContext)) {
            logger.debug("Cannot bind data onto bean '" + beanId + "' as bean does not exist in BeanContext.");
            return;
        }

        if (isBeanWiring) {
            bindBeanValue(executionContext, new Fragment(element));
        } else if (isAttribute) {
            // Bind attribute (i.e. selectors with '@' prefix) values on the visitBefore...
            bindDomDataValue(element, executionContext);
        }
    }

    public void visitAfter(Element element, ExecutionContext executionContext) throws SmooksException {
        if (!beanExists(executionContext)) {
            logger.debug("Cannot bind data onto bean '" + beanId + "' as bean does not exist in BeanContext.");
            return;
        }

        if (!isBeanWiring && !isAttribute) {
            bindDomDataValue(element, executionContext);
        }
    }

    public void visitBefore(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
        if (!beanExists(executionContext)) {
            logger.debug("Cannot bind data onto bean '" + beanId + "' as bean does not exist in BeanContext.");
            return;
        }

        if (isBeanWiring) {
            bindBeanValue(executionContext, new Fragment(element));
        } else if (isAttribute) {
            // Bind attribute (i.e. selectors with '@' prefix) values on the visitBefore...
            bindSaxDataValue(element, executionContext);
        } else if (expressionEvaluator == null || expressionHasDataVariable) {
            // It's not a wiring, attribute or expression binding => it's the element's text.
            // Turn on Text Accumulation...
            element.accumulateText();
        }
    }

    public void visitAfter(SAXElement element, ExecutionContext executionContext) throws SmooksException, IOException {
        if (!beanExists(executionContext)) {
            logger.debug("Cannot bind data onto bean '" + beanId + "' as bean does not exist in BeanContext.");
            return;
        }

        if (!isBeanWiring && !isAttribute) {
            bindSaxDataValue(element, executionContext);
        }
    }

    private boolean beanExists(ExecutionContext executionContext) {
        return (executionContext.getBeanContext().getBean(beanId) != null);
    }

    private void bindDomDataValue(Element element, ExecutionContext executionContext) {
        String dataString;

        if (isAttribute) {
            if (valueAttributeNS != null) {
                dataString = DomUtils.getAttributeValue(element, valueAttributeName, valueAttributeNS);
            } else {
                dataString = DomUtils.getAttributeValue(element, valueAttributeName);
            }
        } else {
            dataString = DomUtils.getAllText(element, false);
        }

        String propertyName;
        if (mapKeyAttribute != null) {
            propertyName = DomUtils.getAttributeValue(element, mapKeyAttribute);
            if (propertyName == null) {
                propertyName = DomUtils.getName(element);
            }
        } else if (property != null) {
            propertyName = property;
        } else {
            propertyName = DomUtils.getName(element);
        }

        if (expressionEvaluator != null) {
            bindExpressionValue(propertyName, dataString, executionContext, new Fragment(element));
        } else {
            decodeAndSetPropertyValue(propertyName, dataString, executionContext, new Fragment(element));
        }
    }

    private void bindSaxDataValue(SAXElement element, ExecutionContext executionContext) {
        String propertyName;

        if (mapKeyAttribute != null) {
            propertyName = SAXUtil.getAttribute(mapKeyAttribute, element.getAttributes(), null);
            if (propertyName == null) {
                propertyName = element.getName().getLocalPart();
            }
        } else if (property != null) {
            propertyName = property;
        } else {
            propertyName = element.getName().getLocalPart();
        }

        String dataString = null;
        if (expressionEvaluator == null || expressionHasDataVariable) {
            if (isAttribute) {
                if (valueAttributeNS != null) {
                    dataString = SAXUtil.getAttribute(valueAttributeNS, valueAttributeName, element.getAttributes(), null);
                } else {
                    dataString = SAXUtil.getAttribute(valueAttributeName, element.getAttributes(), null);
                }
            } else {
                dataString = element.getTextContent();
            }
        }

        if (expressionEvaluator != null) {
            bindExpressionValue(propertyName, dataString, executionContext, new Fragment(element));
        } else {
            decodeAndSetPropertyValue(propertyName, dataString, executionContext, new Fragment(element));
        }
    }

    private void bindBeanValue(final ExecutionContext executionContext, Fragment source) {
        final BeanContext beanContext = executionContext.getBeanContext();
        Object bean = null;

        if (wireBeanId != null) {
            bean = beanContext.getBean(wireBeanId);
        }

        if (bean != null) {
            if (!BeanWiringObserver.isMatchingBean(bean, wireBeanType, wireBeanAnnotation)) {
                bean = null;
            }
        }

        if (bean == null) {

            if (logger.isDebugEnabled()) {
                logger.debug("Registering bean ADD wiring observer for wiring bean '" + wireBeanId + "' onto target bean '" + beanId.getName() + "'.");
            }

            // Register the observer which looks for the creation of the selected bean via its beanIdName...
            beanContext.addObserver(wireByBeanIdObserver);
        } else {
            populateAndSetPropertyValue(bean, beanContext, wireBeanId, executionContext, source);
        }
    }

    public void populateAndSetPropertyValue(Object bean, BeanContext beanContext, BeanId targetBeanId, final ExecutionContext executionContext, Fragment source) {
        BeanRuntimeInfo wiredBeanRI = getWiredBeanRuntimeInfo();

        // When this observer is triggered then we look if we got something we can set immediately or that we got an array collection.
        // For an array collection, we need the array representation and not the list representation, so we register and observer that
        // listens for the change from the list to the array...
        if (wiredBeanRI != null && wiredBeanRI.getClassification() == BeanRuntimeInfo.Classification.ARRAY_COLLECTION) {

            if (logger.isDebugEnabled()) {
                logger.debug("Registering bean CHANGE wiring observer for wiring bean '" + targetBeanId + "' onto target bean '" + beanId.getName() + "' after it has been converted from a List to an array.");
            }
            // Register an observer which looks for the change that the mutable list of the selected bean gets converted to an array. We
            // can then set this array
            beanContext.addObserver(listToArrayChangeObserver);
        } else {
            setPropertyValue(property, bean, executionContext, source);
        }
    }

    private void bindExpressionValue(String mapPropertyName, String dataString, ExecutionContext executionContext, Fragment source) {
        Map<String, Object> beanMap = executionContext.getBeanContext().getBeanMap();

        Map<String, Object> variables = new HashMap<String, Object>();
        if (expressionHasDataVariable) {
            variables.put(EXPRESSION_VALUE_VARIABLE_NAME, dataString);
        }

        Object dataObject = expressionEvaluator.exec(beanMap, variables);
        decodeAndSetPropertyValue(mapPropertyName, dataObject, executionContext, source);
    }

    private void decodeAndSetPropertyValue(String mapPropertyName, Object dataObject, ExecutionContext executionContext, Fragment source) {
        if (dataObject instanceof String) {
            setPropertyValue(mapPropertyName, decodeDataString((String) dataObject, executionContext), executionContext, source);
        } else {
            setPropertyValue(mapPropertyName, dataObject, executionContext, source);
        }

    }

    @SuppressWarnings("unchecked")
    public void setPropertyValue(String mapPropertyName, Object dataObject, ExecutionContext executionContext, Fragment source) {
        if (dataObject == null) {
            return;
        }

        Object bean = executionContext.getBeanContext().getBean(beanId);

        BeanRuntimeInfo.Classification beanType = beanRuntimeInfo.getClassification();

        createPropertySetterMethod(bean, dataObject.getClass());

        if (logger.isDebugEnabled()) {
            logger.debug("Setting data object '" + wireBeanIdName + "' (" + dataObject.getClass().getName() + ") on target bean '" + beanId + "'.");
        }

        // Set the data on the bean...
        try {
            if (propertySetterMethod != null) {
                propertySetterMethod.invoke(bean, dataObject);
            } else if (beanType == BeanRuntimeInfo.Classification.MAP_COLLECTION) {
                ((Map) bean).put(mapPropertyName, dataObject);
            } else if (beanType == BeanRuntimeInfo.Classification.ARRAY_COLLECTION || beanType == BeanRuntimeInfo.Classification.COLLECTION_COLLECTION) {
                ((Collection) bean).add(dataObject);
            } else if (propertySetterMethod == null) {
                if (setterMethod != null) {
                    throw new SmooksConfigurationException("Bean [" + beanIdName + "] configuration invalid.  Bean setter method [" + setterMethod + "(" + dataObject.getClass().getName() + ")] not found on type [" + beanRuntimeInfo.getPopulateType().getName() + "].  You may need to set a 'decoder' on the binding config.");
                } else if (property != null) {
                    boolean throwException = true;

                    if (beanRuntimeInfo.isJAXBType() && getWiredBeanRuntimeInfo().getClassification() != BeanRuntimeInfo.Classification.NON_COLLECTION) {
                        // It's a JAXB collection type.  If the wired in bean is created by a factory then it's most
                        // probable that there's no need to set the collection because the JAXB type is creating it lazily
                        // in the getter method.  So... we're going to ignore this.
                        if (wireBeanId.getCreateResourceConfiguration().getParameter("beanFactory") != null) {
                            throwException = false;
                        }
                    }

                    if (throwException) {
                        throw new SmooksConfigurationException("Bean [" + beanIdName + "] configuration invalid.  Bean setter method [" + ClassUtil.toSetterName(property) + "(" + dataObject.getClass().getName() + ")] not found on type [" + beanRuntimeInfo.getPopulateType().getName() + "].  You may need to set a 'decoder' on the binding config.");
                    }
                }
            }

            if (notifyPopulate) {
                BeanContextLifecycleEvent event = new BeanContextLifecycleEvent(executionContext, source, BeanLifecycle.POPULATE, beanId, bean);
                executionContext.getBeanContext().notifyObservers(event);
            }
        } catch (IllegalAccessException e) {
            throw new SmooksConfigurationException("Error invoking bean setter method [" + ClassUtil.toSetterName(property) + "] on bean instance class type [" + bean.getClass() + "].", e);
        } catch (InvocationTargetException e) {
            throw new SmooksConfigurationException("Error invoking bean setter method [" + ClassUtil.toSetterName(property) + "] on bean instance class type [" + bean.getClass() + "].", e);
        }
    }

    private void createPropertySetterMethod(Object bean, Class<?> parameter) {

        if (!checkedForSetterMethod && propertySetterMethod == null) {
            String methodName = null;
            if (setterMethod != null && !setterMethod.trim().equals("")) {
                methodName = setterMethod;
            } else if (property != null && !property.trim().equals("")) {
                methodName = ClassUtil.toSetterName(property);
            }

            if (methodName != null) {
                propertySetterMethod = createPropertySetterMethod(bean, methodName, parameter);
            }

            checkedForSetterMethod = true;
        }
    }

    /**
     * Create the bean setter method instance for this visitor.
     *
     * @param setterName The setter method name.
     * @return The bean setter method.
     */
    private synchronized Method createPropertySetterMethod(Object bean, String setterName, Class<?> setterParamType) {
        if (propertySetterMethod == null) {
            propertySetterMethod = BeanUtils.createSetterMethod(setterName, bean, setterParamType);
        }

        return propertySetterMethod;
    }

    private Object decodeDataString(String dataString, ExecutionContext executionContext) throws DataDecodeException {
        if ((dataString == null || dataString.length() == 0) && defaultVal != null) {
            if (defaultVal.equals("null")) {
                return null;
            }
            dataString = defaultVal;
        }

        if (decoder == null) {
            decoder = getDecoder(executionContext);
        }

        try {
            return decoder.decode(dataString);
        } catch (DataDecodeException e) {
            throw new DataDecodeException("Failed to decode binding value '" + dataString + "' for property '" + property + "' on bean '" + beanId.getName() + "'.", e);
        }
    }


    private DataDecoder getDecoder(ExecutionContext executionContext) throws DataDecodeException {
        return getDecoder(executionContext.getDeliveryConfig());
    }

    public DataDecoder getDecoder(ContentDeliveryConfig deliveryConfig) {
        @SuppressWarnings("unchecked")
        List decoders = deliveryConfig.getObjects("decoder:" + typeAlias);

        if (decoders == null || decoders.isEmpty()) {
            if (typeAlias != null) {
                decoder = DataDecoder.Factory.create(typeAlias);
            } else {
                decoder = resolveDecoderReflectively();
            }
        } else if (!(decoders.get(0) instanceof DataDecoder)) {
            throw new DataDecodeException("Configured decoder '" + typeAlias + ":" + decoders.get(0).getClass().getName() + "' is not an instance of " + DataDecoder.class.getName());
        } else {
            decoder = (DataDecoder) decoders.get(0);
        }

        if (decoder instanceof PreprocessDecoder) {
            PreprocessDecoder preprocessDecoder = (PreprocessDecoder) decoder;
            if (preprocessDecoder.getBaseDecoder() == null) {
                preprocessDecoder.setBaseDecoder(resolveDecoderReflectively());
            }
        }

        return decoder;
    }

    private DataDecoder resolveDecoderReflectively() throws DataDecodeException {
        Class<?> bindType = resolveBindTypeReflectively();

        if (bindType != null) {
            DataDecoder resolvedDecoder = DataDecoder.Factory.create(bindType);

            if (resolvedDecoder != null) {
                return resolvedDecoder;
            }
        }

        return new StringDecoder();
    }

    private Class<?> resolveBindTypeReflectively() throws DataDecodeException {
        String bindingMember = (setterMethod != null ? setterMethod : property);

        if (bindingMember != null && beanRuntimeInfo.getClassification() == BeanRuntimeInfo.Classification.NON_COLLECTION) {
            Method bindingMethod = Bean.getBindingMethod(bindingMember, beanRuntimeInfo.getPopulateType());
            if (bindingMethod != null) {
                return bindingMethod.getParameterTypes()[0];
            }
        }

        return null;
    }

    private BeanRuntimeInfo getWiredBeanRuntimeInfo() {
        if (wiredBeanRuntimeInfo == null) {
            // Don't need to synchronize this.  Worse thing that can happen is we initialize it
            // more than once... no biggie...
            wiredBeanRuntimeInfo = BeanRuntimeInfo.getBeanRuntimeInfo(wireBeanIdName, appContext);
        }
        return wiredBeanRuntimeInfo;
    }

    private String getId() {
        return id;
    }

    public Set<? extends Object> getProducts() {
        return CollectionsUtil.toSet(beanIdName + "." + property, "]." + property);
    }

    public boolean consumes(Object object) {
        if (object.equals(beanIdName)) {
            return true;
        } else if (wireBeanIdName != null && object.equals(wireBeanIdName)) {
            return true;
        } else if (expressionEvaluator != null && expressionEvaluator.getExpression().indexOf(object.toString()) != -1) {
            return true;
        }

        return false;
    }

}
