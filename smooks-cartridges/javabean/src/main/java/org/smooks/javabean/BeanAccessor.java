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

package org.smooks.javabean;

import org.smooks.assertion.AssertArgument;
import org.smooks.container.ExecutionContext;
import org.smooks.javabean.context.BeanContext;
import org.smooks.javabean.context.BeanIdStore;
import org.smooks.javabean.lifecycle.BeanLifecycle;
import org.smooks.javabean.repository.BeanId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Bean Accessor.
 * <p/>
 * This class provides support for saving and accessing Javabean instance.
 *
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 * @deprecated Use the {@link BeanContext} to manager the beans
 */
@Deprecated
public class BeanAccessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(BeanAccessor.class);

	private static boolean WARNED_USING_DEPRECATED_CLASS = false;

    /**
     * Public default constructor.
     */
    public BeanAccessor(ExecutionContext executionContext) {
    }

    /**
     * Public constructor.
     * <p/>
     * Creates an accessor based on the supplied result Map.
     *
     * @param resultMap The result Map.
     *
     */
    public BeanAccessor(ExecutionContext executionContext, Map<String, Object> resultMap) {
    }

    /**
     * Get the current bean, specified by the supplied beanId, from the supplied request.
     * <p/>
     * If the specified beanId refers to a bean instance list, this method returns the
     * last (current) bean from the list.
     * @param beanId Bean Identifier.
     * @param executionContext The request on which the bean instance is stored.
     * @return The bean instance, or null if no such bean instance exists on the supplied
     * request.
     *
     */
    public static Object getBean(String beanId, ExecutionContext executionContext) {
    	return getBean(executionContext, beanId);
    }

    /**
     * Get the current bean, specified by the supplied beanId, from the supplied request.
     * <p/>
     * If the specified beanId refers to a bean instance list, this method returns the
     * last (current) bean from the list.
     * @param beanId Bean Identifier.
     * @param executionContext The request on which the bean instance is stored.
     * @return The bean instance, or null if no such bean instance exists on the supplied
     * request.
     */
    public static Object getBean(ExecutionContext executionContext, String beanId) {
    	warnUsingDeprecatedMethod();

    	AssertArgument.isNotNull(executionContext, "executionContext");
    	AssertArgument.isNotNullAndNotEmpty(beanId, "beanId");

        return executionContext.getBeanContext().getBean(beanId);
    }

    /**
     * Get the bean map associated with the supplied request instance.
     * @param executionContext The execution context.
     * @return The bean map associated with the supplied request.
     *
     */
	public static HashMap<String, Object> getBeans(ExecutionContext executionContext) {
		warnUsingDeprecatedMethod();

		AssertArgument.isNotNull(executionContext, "executionContext");

		return (HashMap<String, Object>) getBeanMap(executionContext);
    }

    /**
     * Get the bean map associated with the supplied request instance.
     * @param executionContext The execution context.
     * @return The bean map associated with the supplied request.
     */
    public static Map<String, Object> getBeanMap(ExecutionContext executionContext) {
    	warnUsingDeprecatedMethod();

    	AssertArgument.isNotNull(executionContext, "executionContext");

    	return executionContext.getBeanContext().getBeanMap();
    }



    /**
     * Add a bean instance to the specified request under the specified beanId.
     *
     * @param executionContext The execution context within which the bean is created.
     * @param beanId The beanId under which the bean is to be stored.
     * @param bean The bean instance to be stored.
     */
    public static void addBean(ExecutionContext executionContext, String beanId, Object bean) {
		warnUsingDeprecatedMethod();

		AssertArgument.isNotNull(executionContext, "executionContext");
		AssertArgument.isNotNullAndNotEmpty(beanId, "beanId");
		AssertArgument.isNotNull(bean, "bean");

		BeanId beanIdObj = getBeanId(executionContext.getContext().getBeanIdStore(), beanId);

		executionContext.getBeanContext().addBean(beanIdObj, bean, null);

    }

    /**
     * Changes a bean object of the given beanId. The difference to addBean is that the
     * bean must exist, the associated beans aren't removed and the observers of the
     * {@link BeanLifecycle#CHANGE} event are notified.
     *
     * @param executionContext The execution context within which the bean is created.
     * @param beanId The beanId under which the bean is to be stored.
     * @param bean The bean instance to be stored.
     */
    public static void changeBean(ExecutionContext executionContext, String beanId, Object bean) {
    	warnUsingDeprecatedMethod();

    	AssertArgument.isNotNull(executionContext, "executionContext");
		AssertArgument.isNotNullAndNotEmpty(beanId, "beanId");
		AssertArgument.isNotNull(bean, "bean");

		BeanId beanIdObj = getBeanId(executionContext.getContext().getBeanIdStore(), beanId);

		executionContext.getBeanContext().changeBean(beanIdObj, bean, null);
    }

	/**
	 * @param beanIdStore
	 * @param beanId
	 */
	private static BeanId getBeanId(BeanIdStore beanIdStore, String beanId) {
		warnUsingDeprecatedMethod();

		BeanId beanIdObj = beanIdStore.getBeanId(beanId);

		if (beanIdObj == null) {
			beanIdObj = beanIdStore.register(beanId);
		}

		return beanIdObj;
	}

	private static void warnUsingDeprecatedMethod() {
		if(LOGGER.isWarnEnabled()) {
			if(!WARNED_USING_DEPRECATED_CLASS) {
				WARNED_USING_DEPRECATED_CLASS = true;

				LOGGER.warn("The deprecated class BeanAccessor is being used! It is strongly advised to switch to the new BeanRepository class. " +
						"The BeanAccessor is much slower then the BeanRepository. This class will be removed in a future release!");
			}
		}
	}

}
