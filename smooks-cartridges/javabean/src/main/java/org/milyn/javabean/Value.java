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

import org.milyn.assertion.AssertArgument;
import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.delivery.VisitorConfigMap;
import org.milyn.javabean.ext.SelectorPropertyResolver;

/**
 * Programmatic Value Configurator.
 * <p/>
 * This class can be used to programmatically configure a Smooks instance for creating value
 * objects using the Smooks DataDecoders.
 * <p/>
 * This class uses a Fluent API (all methods return the Bean instance), making it easy to
 * string configurations together.
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
 *         &lt;!-- .... --!&gt;
 *     &lt;/order-items&gt;
 * &lt;/order&gt;
 * </pre>
 * <p/>
 * <h4>The Binding Configuration and Execution Code</h4>
 * The configuration code (Note: Smooks instance defined and instantiated globally):
 * <pre>
 * Smooks smooks = new Smooks();
 *
 * Value customerNumberValue = new Value( "customerNumber", "customer/@number")
 *                                   .setDecoder("Integer");
 * Value customerNameValue = new Value( "customerName", "customer")
 *                                   .setDefault("Unknown");
 *
 * smooks.addVisitors(customerNumberValue);
 * smooks.addVisitors(customerNameValue);
 * </pre>
 * <p/>
 * And the execution code:
 * <pre>
 * JavaResult result = new JavaResult();
 *
 * smooks.filterSource(new StreamSource(orderMessageStream), result);
 * Integer customerNumber = (Integer) result.getBean("customerNumber");
 * String customerName = (String) result.getBean("customerName");
 * </pre>
 *
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 * @see Bean
 */
public class Value extends BindingAppender {

	private String dataSelector;

	private String targetNamespace;

	private String defaultValue;

	private DataDecoder decoder;

	/**
     * Create a Value binding configuration.
     *
	 * @param beanId The bean id under which the value will be stored.
	 * @param data The data selector for the data value to be bound.
	 */
	public Value(String beanId, String data) {
		super(beanId);
		AssertArgument.isNotNullAndNotEmpty(beanId, "beanId");
		AssertArgument.isNotNullAndNotEmpty(data, "dataSelector");

		this.dataSelector = data;
	}

	/**
     * Create a Value binding configuration.
     *
	 * @param beanId The bean id under which the value will be stored.
	 * @param data The data selector for the data value to be bound.
	 * @param type Data type.
	 */
	public Value(String beanId, String data, Class<?> type) {
		this(beanId, data);
		AssertArgument.isNotNull(type, "type");

		this.decoder = DataDecoder.Factory.create(type);
	}

	/**
	 * The namespace for the data selector for the data value to be bound.
	 *
	 * @param targetNamespace The namespace
	 * @return <code>this</code> Value configuration instance.
	 */
	public Value setTargetNamespace(String targetNamespace) {
		this.targetNamespace = targetNamespace;

		return this;
	}

	/**
	 * The default value for if the data is null or empty
	 *
	 * @param targetNamespace The default value
	 * @return <code>this</code> Value configuration instance.
	 */
	public Value setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;

		return this;
	}

	/**
	 * Set the binding value data type.
	 * @param type The data type.
	 * 
	 * @return <code>this</code> Value configuration instance.
	 */
	public Value setType(Class<?> type) {
		this.decoder = DataDecoder.Factory.create(type);

		return this;
	}

	/**
	 * The {@link org.milyn.javabean.DataDecoder} to be used for decoding
     * the data value.
	 *
	 * @param targetNamespace The {@link org.milyn.javabean.DataDecoder}
	 * @return <code>this</code> Value configuration instance.
	 */
	public Value setDecoder(DataDecoder dataDecoder) {
		this.decoder = dataDecoder;

		return this;
	}

	/**
	 * Used by Smooks to retrieve the visitor configuration of this Value Configuration
	 */
	public void addVisitors(VisitorConfigMap visitorMap) {

		ValueBinder binder = new ValueBinder(getBeanId());
		SmooksResourceConfiguration populatorConfig = new SmooksResourceConfiguration(dataSelector);

		SelectorPropertyResolver.resolveSelectorTokens(populatorConfig);

		binder.setDecoder(decoder);
		binder.setDefaultValue(defaultValue);
		binder.setValueAttributeName(populatorConfig.getStringParameter(BeanInstancePopulator.VALUE_ATTRIBUTE_NAME));

		visitorMap.addVisitor(binder, populatorConfig.getSelector(), targetNamespace, true);
	}

}
