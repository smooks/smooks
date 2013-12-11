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
package org.milyn.javabean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.milyn.cdr.annotation.AnnotationConstants;
import org.milyn.cdr.annotation.AppContext;
import org.milyn.cdr.annotation.ConfigParam;
import org.milyn.commons.SmooksException;
import org.milyn.commons.cdr.SmooksConfigurationException;
import org.milyn.commons.javabean.DataDecodeException;
import org.milyn.commons.javabean.DataDecoder;
import org.milyn.commons.util.CollectionsUtil;
import org.milyn.commons.xml.DomUtils;
import org.milyn.container.ApplicationContext;
import org.milyn.container.ExecutionContext;
import org.milyn.delivery.Fragment;
import org.milyn.delivery.annotation.Initialize;
import org.milyn.delivery.dom.DOMElementVisitor;
import org.milyn.delivery.ordering.Producer;
import org.milyn.delivery.sax.SAXElement;
import org.milyn.delivery.sax.SAXUtil;
import org.milyn.delivery.sax.SAXVisitAfter;
import org.milyn.delivery.sax.SAXVisitBefore;
import org.milyn.event.report.annotation.VisitAfterReport;
import org.milyn.event.report.annotation.VisitBeforeReport;
import org.milyn.javabean.context.BeanContext;
import org.milyn.javabean.repository.BeanId;
import org.w3c.dom.Element;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Value Binder.
 * <p/>
 * This class can be used to configure a Smooks instance for creating value
 * objects using the Smooks DataDecoders.
 * <h3>XML Schema & Namespace</h3>
 * The Value Binder XML configuration schema is in the following XML Schema Namespace:
 * <p/>
 * <a href="http://www.milyn.org/xsd/smooks/javabean-1.3.xsd"><b>http://www.milyn.org/xsd/smooks/javabean-1.3.xsd</b></a>
 * <p/>
 * The value binder element is '&lt;value&gt;'. Take a look in the schema for all
 * the configuration attributes.
 * <p/>
 * <h3>Programmatic configuration</h3>
 * The value binder can be programmatic configured using the {@link Value} Object.
 * <p/>
 * <h3>Example</h3>
 * Taking the "classic" Order message as an example and getting the order number and
 * name as Value Objects in the form of an Integer and String.
 * <h4>The Message</h4>
 * <pre>
 * &lt;order xmlns="http://x"&gt;
 *     &lt;header&gt;
 *         &lt;y:date xmlns:y="http://y"&gt;Wed Nov 15 13:45:28 EST 2006&lt;/y:date&gt;
 *         &lt;customer number="123123"&gt;Joe&lt;/customer&gt;
 *         &lt;privatePerson&gt;&lt;/privatePerson&gt;
 *     &lt;/header&gt;
 *     &lt;order-items&gt;
 *         &lt;!-- .... --&gt;
 *     &lt;/order-items&gt;
 * &lt;/order&gt;
 * </pre>
 * <p/>
 * <h4>The Binding Configuration</h4>
 * <pre>
 * &lt;?xml version=&quot;1.0&quot;?&gt;
 * &lt;smooks-resource-list xmlns=&quot;http://www.milyn.org/xsd/smooks-1.1.xsd&quot; xmlns:jb=&quot;http://www.milyn.org/xsd/smooks/javabean-1.3.xsd&quot;&gt;
 *
 *    &lt;jb:value
 *       beanId=&quot;customerName&quot;
 *       data=&quot;customer&quot;
 *       default=&quot;unknown&quot;
 *    /&gt;
 *
 *    &lt;jb:value
 *       beanId=&quot;customerNumber&quot;
 *       data=&quot;customer/@number&quot;
 * 	     decoder=&quot;Integer&quot;
 *    /&gt;
 *
 * &lt;/smooks-resource-list&gt;
 * </pre>
 *
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 */
@VisitBeforeReport(condition = "parameters.containsKey('valueAttributeName')",
        summary = "Creating object under bean id <b>${resource.parameters.beanId}</b> with a value from the attribute <b>${resource.parameters.valueAttributeName}</b>.",
        detailTemplate = "reporting/ValueBinderReport_Before.html")
@VisitAfterReport(condition = "!parameters.containsKey('valueAttributeName')",
        summary = "Creating object <b>${resource.parameters.beanId}</b> with a value from this element.",
        detailTemplate = "reporting/ValueBinderReport_After.html")
public class ValueBinder implements DOMElementVisitor, SAXVisitBefore, SAXVisitAfter, Producer {

    private static final Log logger = LogFactory.getLog(ValueBinder.class);

    @ConfigParam(name = "beanId")
    private String beanIdName;

    @ConfigParam(defaultVal = AnnotationConstants.NULL_STRING)
    private String valueAttributeName;

    @ConfigParam(name = "default", defaultVal = AnnotationConstants.NULL_STRING)
    private String defaultValue;

    @ConfigParam(name = "type", defaultVal = "String")
    private String typeAlias;

    private BeanId beanId;

    @AppContext
    private ApplicationContext appContext;

    private boolean isAttribute;

    private DataDecoder decoder;

    /**
     *
     */
    public ValueBinder() {
    }

    /**
     * @param beanId
     */
    public ValueBinder(String beanId) {
        this.beanIdName = beanId;
    }

    /**
     * @return the beanIdName
     */
    public String getBeanIdName() {
        return beanIdName;
    }

    /**
     * @param beanIdName the beanIdName to set
     */
    public void setBeanIdName(String beanIdName) {
        this.beanIdName = beanIdName;
    }

    /**
     * @return the valueAttributeName
     */
    public String getValueAttributeName() {
        return valueAttributeName;
    }

    /**
     * @param valueAttributeName the valueAttributeName to set
     */
    public void setValueAttributeName(String valueAttributeName) {
        this.valueAttributeName = valueAttributeName;
    }

    /**
     * @return the defaultValue
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * @param defaultValue the defaultValue to set
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * @return the typeAlias
     */
    public String getTypeAlias() {
        return typeAlias;
    }

    /**
     * @param typeAlias the typeAlias to set
     */
    public void setTypeAlias(String typeAlias) {
        this.typeAlias = typeAlias;
    }

    /**
     * @return the decoder
     */
    public DataDecoder getDecoder() {
        return decoder;
    }

    /**
     * @param decoder the decoder to set
     */
    public void setDecoder(DataDecoder decoder) {
        this.decoder = decoder;
    }

    /**
     * Set the resource configuration on the bean populator.
     *
     * @throws SmooksConfigurationException Incorrectly configured resource.
     */
    @Initialize
    public void initialize() throws SmooksConfigurationException {
        isAttribute = (valueAttributeName != null);

        beanId = appContext.getBeanIdStore().register(beanIdName);

        if (logger.isDebugEnabled()) {
            logger.debug("Value Binder created for [" + beanIdName + "].");
        }
    }

    public void visitBefore(Element element, ExecutionContext executionContext)
            throws SmooksException {
        if (isAttribute) {
            bindValue(DomUtils.getAttributeValue(element, valueAttributeName), executionContext, new Fragment(element));
        }
    }

    public void visitAfter(Element element, ExecutionContext executionContext)
            throws SmooksException {
        if (!isAttribute) {
            bindValue(DomUtils.getAllText(element, false), executionContext, new Fragment(element));
        }
    }

    public void visitBefore(SAXElement element,
                            ExecutionContext executionContext) throws SmooksException,
            IOException {
        if (isAttribute) {
            bindValue(SAXUtil.getAttribute(valueAttributeName, element.getAttributes()), executionContext, new Fragment(element));
        } else {
            // Turn on Text Accumulation...
            element.accumulateText();
        }
    }

    public void visitAfter(SAXElement element, ExecutionContext executionContext)
            throws SmooksException, IOException {
        if (!isAttribute) {
            bindValue(element.getTextContent(), executionContext, new Fragment(element));
        }
    }

    private void bindValue(String dataString, ExecutionContext executionContext, Fragment source) {
        Object valueObj = decodeDataString(dataString, executionContext);

        BeanContext beanContext = executionContext.getBeanContext();

        if (valueObj == null) {
            beanContext.removeBean(beanId, source);
        } else {
            beanContext.addBean(beanId, valueObj, source);
        }
    }

    public Set<? extends Object> getProducts() {
        return CollectionsUtil.toSet(beanIdName);
    }

    private Object decodeDataString(String dataString, ExecutionContext executionContext) throws DataDecodeException {
        if ((dataString == null || dataString.length() == 0) && defaultValue != null) {
            if (defaultValue.equals("null")) {
                return null;
            }
            dataString = defaultValue;
        }

        try {
            return getDecoder(executionContext).decode(dataString);
        } catch (DataDecodeException e) {
            throw new DataDecodeException("Failed to decode the value '" + dataString + "' for the bean id '" + beanIdName + "'.", e);
        }
    }

    private DataDecoder getDecoder(ExecutionContext executionContext) throws DataDecodeException {
        if (decoder == null) {
            @SuppressWarnings("unchecked")
            List decoders = executionContext.getDeliveryConfig().getObjects("decoder:" + typeAlias);

            if (decoders == null || decoders.isEmpty()) {
                decoder = DataDecoder.Factory.create(typeAlias);
            } else if (!(decoders.get(0) instanceof DataDecoder)) {
                throw new DataDecodeException("Configured decoder '" + typeAlias + ":" + decoders.get(0).getClass().getName() + "' is not an instance of " + DataDecoder.class.getName());
            } else {
                decoder = (DataDecoder) decoders.get(0);
            }
        }
        return decoder;
    }

}
