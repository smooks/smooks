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

import org.milyn.Smooks;
import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.commons.assertion.AssertArgument;
import org.milyn.commons.javabean.DataDecoder;
import org.milyn.commons.util.ClassUtil;
import org.milyn.delivery.VisitorConfigMap;
import org.milyn.javabean.ext.SelectorPropertyResolver;
import org.milyn.javabean.factory.Factory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Programmatic Bean Configurator.
 * <p/>
 * This class can be used to programmatically configure a Smooks instance for performing
 * a Java Bindings on a specific class.  To populate a graph, you simply create a graph of
 * Bean instances by binding Beans onto Beans.
 * <p/>
 * This class uses a Fluent API (all methods return the Bean instance), making it easy to
 * string configurations together to build up a graph of Bean configuration.
 * <p/>
 * <h3>Example</h3>
 * Taking the "classic" Order message as an example and binding it into a corresponding
 * Java Object model.
 * <h4>The Message</h4>
 * <pre>
 * &lt;order xmlns="http://x"&gt;
 *     &lt;header&gt;
 *         &lt;y:date xmlns:y="http://y"&gt;Wed Nov 15 13:45:28 EST 2006&lt;/y:date&gt;
 *         &lt;customer number="123123"&gt;Joe&lt;/customer&gt;
 *         &lt;privatePerson&gt;&lt;/privatePerson&gt;
 *     &lt;/header&gt;
 *     &lt;order-items&gt;
 *         &lt;order-item&gt;
 *             &lt;product&gt;111&lt;/product&gt;
 *             &lt;quantity&gt;2&lt;/quantity&gt;
 *             &lt;price&gt;8.90&lt;/price&gt;
 *         &lt;/order-item&gt;
 *         &lt;order-item&gt;
 *             &lt;product&gt;222&lt;/product&gt;
 *             &lt;quantity&gt;7&lt;/quantity&gt;
 *             &lt;price&gt;5.20&lt;/price&gt;
 *         &lt;/order-item&gt;
 *     &lt;/order-items&gt;
 * &lt;/order&gt;
 * </pre>
 * <p/>
 * <h4>The Java Model</h4>
 * (Not including getters and setters):
 * <pre>
 * public class Order {
 *     private Header header;
 *     private List&lt;OrderItem&gt; orderItems;
 * }
 * public class Header {
 *     private Long customerNumber;
 *     private String customerName;
 * }
 * public class OrderItem {
 *     private long productId;
 *     private Integer quantity;
 *     private double price;
 * }
 * </pre>
 * <p/>
 * <h4>The Binding Configuration and Execution Code</h4>
 * The configuration code (Note: Smooks instance defined and instantiated globally):
 * <pre>
 * Smooks smooks = new Smooks();
 *
 * Bean orderBean = new Bean(Order.class, "order", "/order");
 * orderBean.bindTo("header",
 *     orderBean.newBean(Header.class, "/order")
 *         .bindTo("customerNumber", "header/customer/@number")
 *         .bindTo("customerName", "header/customer")
 *     ).bindTo("orderItems",
 *     orderBean.newBean(ArrayList.class, "/order")
 *         .bindTo(orderBean.newBean(OrderItem.class, "order-item")
 *             .bindTo("productId", "order-item/product")
 *             .bindTo("quantity", "order-item/quantity")
 *             .bindTo("price", "order-item/price"))
 *     );
 *
 * smooks.addVisitors(orderBean);
 * </pre>
 * <p/>
 * And the execution code:
 * <pre>
 * JavaResult result = new JavaResult();
 *
 * smooks.filterSource(new StreamSource(orderMessageStream), result);
 * Order order = (Order) result.getBean("order");
 * </pre>
 *
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 * @see Value
 */
public class Bean extends BindingAppender {

    BeanInstanceCreator beanInstanceCreator;
    private Class<?> beanClass;
    private String createOnElement;
    private String targetNamespace;
    private List<Binding> bindings = new ArrayList<Binding>();
    private List<Bean> wirings = new ArrayList<Bean>();
    private boolean processed = false;

    /**
     * Create a Bean binding configuration.
     * <p/>
     * The bean instance is created on the root/document fragment.
     *
     * @param beanClass The bean runtime class.
     * @param beanId    The bean ID.
     */
    @SuppressWarnings("unchecked")
    public Bean(Class<?> beanClass, String beanId) {
        this(beanClass, beanId, (Factory) null);
    }

    /**
     * Create a Bean binding configuration.
     * <p/>
     * The bean instance is created on the root/document fragment.
     *
     * @param beanClass The bean runtime class.
     * @param beanId    The bean ID.
     * @param factory   The factory that will create the runtime object
     */
    public <T> Bean(Class<T> beanClass, String beanId, Factory<? extends T> factory) {
        this(beanClass, beanId, SmooksResourceConfiguration.DOCUMENT_FRAGMENT_SELECTOR, (String) null, factory);
    }

    /**
     * Create a Bean binding configuration.
     *
     * @param beanClass       The bean runtime class.
     * @param beanId          The bean ID.
     * @param createOnElement The element selector used to create the bean instance.
     */
    public Bean(Class<?> beanClass, String beanId, String createOnElement) {
        this(beanClass, beanId, createOnElement, (String) null);
    }

    /**
     * Create a Bean binding configuration.
     *
     * @param beanClass       The bean runtime class.
     * @param beanId          The bean ID.
     * @param createOnElement The element selector used to create the bean instance.
     * @param factory         The factory that will create the runtime object
     */
    public <T> Bean(Class<T> beanClass, String beanId, String createOnElement, Factory<? extends T> factory) {
        this(beanClass, beanId, createOnElement, (String) null, factory);
    }

    /**
     * Create a Bean binding configuration.
     *
     * @param beanClass         The bean runtime class.
     * @param beanId            The bean ID.
     * @param createOnElement   The element selector used to create the bean instance.
     * @param createOnElementNS The namespace for the element selector used to create the bean instance.
     */
    public Bean(Class<?> beanClass, String beanId, String createOnElement, String createOnElementNS) {
        this(beanClass, beanId, createOnElement, createOnElementNS, null);
    }

    /**
     * Create a Bean binding configuration.
     *
     * @param beanClass         The bean runtime class.
     * @param beanId            The bean ID.
     * @param createOnElement   The element selector used to create the bean instance.
     * @param createOnElementNS The namespace for the element selector used to create the bean instance.
     * @param factory           The factory that will create the runtime object
     */
    public <T> Bean(Class<T> beanClass, String beanId, String createOnElement, String createOnElementNS, Factory<? extends T> factory) {
        super(beanId);
        AssertArgument.isNotNull(beanClass, "beanClass");
        AssertArgument.isNotNull(createOnElement, "createOnElement");

        this.beanClass = beanClass;
        this.createOnElement = createOnElement;
        this.targetNamespace = createOnElementNS;

        beanInstanceCreator = new BeanInstanceCreator(beanId, beanClass, factory);
    }

    /**
     * Create a Bean binding configuration.
     *
     * @param beanClass         The bean runtime class.
     * @param beanId            The bean ID.
     * @param createOnElement   The element selector used to create the bean instance.
     * @param createOnElementNS The namespace for the element selector used to create the bean instance.
     */
    public static Bean newBean(Class<?> beanClass, String beanId, String createOnElement, String createOnElementNS) {
        return new Bean(beanClass, beanId, createOnElement, createOnElementNS);
    }


    /**
     * Create a Bean binding configuration.
     *
     * @param beanClass         The bean runtime class.
     * @param beanId            The bean ID.
     * @param createOnElement   The element selector used to create the bean instance.
     * @param createOnElementNS The namespace for the element selector used to create the bean instance.
     * @param factory           The factory that will create the runtime object
     */
    public static <T> Bean newBean(Class<T> beanClass, String beanId, String createOnElement, String createOnElementNS, Factory<T> factory) {
        return new Bean(beanClass, beanId, createOnElement, createOnElementNS, factory);
    }

    /**
     * Create a Bean binding configuration.
     * <p/>
     * This method binds the configuration to the same {@link Smooks} instance
     * supplied in the constructor.  The beanId is generated.
     *
     * @param beanClass       The bean runtime class.
     * @param createOnElement The element selector used to create the bean instance.
     * @return <code>this</code> Bean configuration instance.
     */
    public Bean newBean(Class<?> beanClass, String createOnElement) {
        String randomBeanId = UUID.randomUUID().toString();
        return new Bean(beanClass, randomBeanId, createOnElement);
    }

    /**
     * Create a Bean binding configuration.
     * <p/>
     * This method binds the configuration to the same {@link Smooks} instance
     * supplied in the constructor.  The beanId is generated.
     *
     * @param beanClass       The bean runtime class.
     * @param createOnElement The element selector used to create the bean instance.
     * @param factory         The factory that will create the runtime object
     * @return <code>this</code> Bean configuration instance.
     */
    public <T> Bean newBean(Class<T> beanClass, String createOnElement, Factory<T> factory) {
        String randomBeanId = UUID.randomUUID().toString();
        return new Bean(beanClass, randomBeanId, createOnElement, factory);
    }

    /**
     * Create a Bean binding configuration.
     * <p/>
     * This method binds the configuration to the same {@link Smooks} instance
     * supplied in the constructor.
     *
     * @param beanClass       The bean runtime class.
     * @param beanId          The beanId.
     * @param createOnElement The element selector used to create the bean instance.
     * @return <code>this</code> Bean configuration instance.
     */
    public Bean newBean(Class<?> beanClass, String beanId, String createOnElement) {
        return new Bean(beanClass, beanId, createOnElement);
    }

    /**
     * Create a Bean binding configuration.
     * <p/>
     * This method binds the configuration to the same {@link Smooks} instance
     * supplied in the constructor.
     *
     * @param beanClass       The bean runtime class.
     * @param beanId          The beanId.
     * @param createOnElement The element selector used to create the bean instance.
     * @param factory         The factory that will create the runtime object
     * @return <code>this</code> Bean configuration instance.
     */
    public <T> Bean newBean(Class<T> beanClass, String beanId, String createOnElement, Factory<T> factory) {
        return new Bean(beanClass, beanId, createOnElement, factory);
    }


    /**
     * Create a binding configuration to bind the data, selected from the message by the
     * dataSelector, to the specified bindingMember (field/method).
     * <p/>
     * Discovers the {@link org.milyn.commons.javabean.DataDecoder} through the specified
     * bindingMember.
     *
     * @param bindingMember The name of the binding member.  This is a bean property (field)
     *                      or method name.
     * @param dataSelector  The data selector for the data value to be bound.
     * @return The Bean configuration instance.
     */
    public Bean bindTo(String bindingMember, String dataSelector) {
        return bindTo(bindingMember, dataSelector, null);
    }

    /**
     * Create a binding configuration to bind the data, selected from the message by the
     * dataSelector, to the target Bean member specified by the bindingMember param.
     *
     * @param bindingMember The name of the binding member.  This is a bean property (field)
     *                      or method name.
     * @param dataSelector  The data selector for the data value to be bound.
     * @param dataDecoder   The {@link org.milyn.commons.javabean.DataDecoder} to be used for decoding
     *                      the data value.
     * @return <code>this</code> Bean configuration instance.
     */
    public Bean bindTo(String bindingMember, String dataSelector, DataDecoder dataDecoder) {
        assertNotProcessed();
        AssertArgument.isNotNull(bindingMember, "bindingMember");
        AssertArgument.isNotNull(dataSelector, "dataSelector");
        // dataDecoder can be null

        BeanInstancePopulator beanInstancePopulator = new BeanInstancePopulator();
        SmooksResourceConfiguration populatorConfig = new SmooksResourceConfiguration(dataSelector);

        SelectorPropertyResolver.resolveSelectorTokens(populatorConfig);

        // Configure the populator visitor...
        beanInstancePopulator.setBeanId(getBeanId());
        beanInstancePopulator.setValueAttributeName(populatorConfig.getStringParameter(BeanInstancePopulator.VALUE_ATTRIBUTE_NAME));
        beanInstancePopulator.setValueAttributePrefix(populatorConfig.getStringParameter(BeanInstancePopulator.VALUE_ATTRIBUTE_PREFIX));

        Method bindingMethod = getBindingMethod(bindingMember, beanClass);
        if (bindingMethod != null) {
            if (dataDecoder == null) {
                Class<?> dataType = bindingMethod.getParameterTypes()[0];
                dataDecoder = DataDecoder.Factory.create(dataType);
            }

            if (bindingMethod.getName().equals(bindingMember)) {
                beanInstancePopulator.setSetterMethod(bindingMethod.getName());
            } else {
                beanInstancePopulator.setProperty(bindingMember);
            }
        } else {
            beanInstancePopulator.setProperty(bindingMember);
        }
        beanInstancePopulator.setDecoder(dataDecoder);

        bindings.add(new Binding(populatorConfig.getSelector(), beanInstancePopulator, false));

        return this;
    }

    /**
     * Add a bean binding configuration for the specified bindingMember (field/method) to
     * this bean binding config.
     * <p/>
     * This method is used to build a binding configuration graph, which in turn configures
     * Smooks to build a Java Object Graph (ala &lt;jb:wiring&gt; configurations).
     *
     * @param bindingMember The name of the binding member.  This is a bean property (field)
     *                      or method name.  The bean runtime class should match the
     * @param bean          The Bean instance to be bound
     * @return <code>this</code> Bean configuration instance.
     */
    public Bean bindTo(String bindingMember, Bean bean) {
        assertNotProcessed();
        AssertArgument.isNotNull(bindingMember, "bindingMember");
        AssertArgument.isNotNull(bean, "bean");

        BeanInstancePopulator beanInstancePopulator = new BeanInstancePopulator();

        // Configure the populator visitor...
        beanInstancePopulator.setBeanId(getBeanId());
        beanInstancePopulator.setWireBeanId(bean.getBeanId());
        Method bindingMethod = getBindingMethod(bindingMember, beanClass);

        if (bindingMethod != null) {
            if (bindingMethod.getName().equals(bindingMember)) {
                beanInstancePopulator.setSetterMethod(bindingMethod.getName());
            } else {
                beanInstancePopulator.setProperty(bindingMember);
            }
        } else {
            beanInstancePopulator.setProperty(bindingMember);
        }

        bindings.add(new Binding(createOnElement, beanInstancePopulator, false));
        wirings.add(bean);

        return this;
    }

    /**
     * Add a bean binding configuration to this Collection/array bean binding config.
     * <p/>
     * This method checks that this bean's beanClass is a Collection/array, generating an
     * {@link IllegalArgumentException} if the check fails.
     *
     * @param bean The Bean instance to be bound
     * @return <code>this</code> Bean configuration instance.
     * @throws IllegalArgumentException <u><code>this</code></u> Bean's beanClass (not the supplied bean!) is
     *                                  not a Collection/array.  You cannot call this method on Bean configurations whose beanClass is not a
     *                                  Collection/array.  For non Collection/array types, you must use one of the bindTo meths that specify a
     *                                  'bindingMember'.
     */
    public Bean bindTo(Bean bean) throws IllegalArgumentException {
        assertNotProcessed();
        AssertArgument.isNotNull(bean, "bean");

        BeanInstancePopulator beanInstancePopulator = new BeanInstancePopulator();

        // Configure the populator visitor...
        beanInstancePopulator.setBeanId(getBeanId());
        beanInstancePopulator.setWireBeanId(bean.getBeanId());

        bindings.add(new Binding(createOnElement, beanInstancePopulator, true));
        wirings.add(bean);

        return this;
    }

    /**
     * Create a binding configuration to bind the data, selected from the message by the
     * dataSelector, to the target Collection/array Bean beanclass instance.
     *
     * @param dataSelector The data selector for the data value to be bound.
     * @return <code>this</code> Bean configuration instance.
     */
    public Bean bindTo(String dataSelector) {
        return bindTo(dataSelector, (DataDecoder) null);
    }


    /**
     * Create a binding configuration to bind the data, selected from the message by the
     * dataSelector, to the target Collection/array Bean beanclass instance.
     *
     * @param dataSelector The data selector for the data value to be bound.
     * @param dataDecoder  The {@link org.milyn.commons.javabean.DataDecoder} to be used for decoding
     *                     the data value.
     * @return <code>this</code> Bean configuration instance.
     */
    public Bean bindTo(String dataSelector, DataDecoder dataDecoder) {
        assertNotProcessed();
        AssertArgument.isNotNull(dataSelector, "dataSelector");
        // dataDecoder can be null

        BeanInstancePopulator beanInstancePopulator = new BeanInstancePopulator();
        SmooksResourceConfiguration populatorConfig = new SmooksResourceConfiguration(dataSelector);

        SelectorPropertyResolver.resolveSelectorTokens(populatorConfig);

        // Configure the populator visitor...
        beanInstancePopulator.setBeanId(getBeanId());
        beanInstancePopulator.setValueAttributeName(populatorConfig.getStringParameter(BeanInstancePopulator.VALUE_ATTRIBUTE_NAME));
        beanInstancePopulator.setValueAttributePrefix(populatorConfig.getStringParameter(BeanInstancePopulator.VALUE_ATTRIBUTE_PREFIX));
        beanInstancePopulator.setDecoder(dataDecoder);

        bindings.add(new Binding(populatorConfig.getSelector(), beanInstancePopulator, true));

        return this;
    }

    /**
     * Add the visitors, associated with this Bean instance, to the visitor map.
     *
     * @param visitorMap The visitor Map.
     */
    public void addVisitors(VisitorConfigMap visitorMap) {

        // Need to protect against multiple calls.  This can happen where e.g. beans are
        // wired together in 2-way relationships, or the creating code doesn't use the
        // fluent interface and calls Smooks.addVisitor to each bean instance.
        if (processed) {
            return;
        }
        processed = true;

        // Add the create bean visitor...
        SmooksResourceConfiguration creatorConfig = visitorMap.addVisitor(beanInstanceCreator, createOnElement, targetNamespace, true);
        creatorConfig.setParameter("beanId", getBeanId());
        creatorConfig.setParameter("beanClass", beanClass.getName());

        // Recurse down the wired beans...
        for (Bean bean : wirings) {
            bean.addVisitors(visitorMap);
        }

        // Add the populate bean visitors...
        for (Binding binding : bindings) {
            SmooksResourceConfiguration populatorConfig = visitorMap.addVisitor(binding.beanInstancePopulator, binding.selector, targetNamespace, true);
            populatorConfig.setParameter("beanId", getBeanId());
            if (binding.assertTargetIsCollection) {
                assertBeanClassIsCollection();
            }
        }
    }

    /**
     * Get the bean binding class Member (field/method).
     *
     * @param bindingMember Binding member name.
     * @return The binding member, or null if not found.
     */
    public static Method getBindingMethod(String bindingMember, Class<?> beanClass) {
        Method[] methods = beanClass.getMethods();

        // Check is the bindingMember an actual fully qualified method name...
        for (Method method : methods) {
            if (method.getName().equals(bindingMember) && method.getParameterTypes().length == 1) {
                return method;
            }
        }

        // Check is the bindingMember defined by a property name.  If so, there should be a
        // bean setter method for that property...
        String asPropertySetterMethod = ClassUtil.toSetterName(bindingMember);
        for (Method method : methods) {
            if (method.getName().equals(asPropertySetterMethod) && method.getParameterTypes().length == 1) {
                return method;
            }
        }

        // Can't resolve it...
        return null;
    }

    /**
     * Assert that the beanClass associated with this configuration is an array or Collection.
     */
    private void assertBeanClassIsCollection() {
        BeanRuntimeInfo beanRuntimeInfo = beanInstanceCreator.getBeanRuntimeInfo();

        if (beanRuntimeInfo.getClassification() != BeanRuntimeInfo.Classification.COLLECTION_COLLECTION && beanRuntimeInfo.getClassification() != BeanRuntimeInfo.Classification.ARRAY_COLLECTION) {
            throw new IllegalArgumentException("Invalid call to a Collection/array Bean.bindTo method for a non Collection/Array target.  Binding target type '" + beanRuntimeInfo.getPopulateType().getName() + "' (beanId '" + getBeanId() + "').  Use one of the Bean.bindTo methods that specify a 'bindingMember' argument.");
        }
    }

    private void assertNotProcessed() {
        if (processed) {
            throw new IllegalStateException("Unexpected attempt to bindTo Bean instance after the Bean instance has been added to a Smooks instance.");
        }
    }

    private static class Binding {
        private String selector;
        private BeanInstancePopulator beanInstancePopulator;
        private boolean assertTargetIsCollection;

        private Binding(String selector, BeanInstancePopulator beanInstancePopulator, boolean assertTargetIsCollection) {
            this.selector = selector;
            this.beanInstancePopulator = beanInstancePopulator;
            this.assertTargetIsCollection = assertTargetIsCollection;
        }
    }
}
