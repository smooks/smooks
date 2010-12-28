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
import org.milyn.assertion.AssertArgument;
import org.milyn.cdr.SmooksConfigurationException;
import org.milyn.container.ExecutionContext;
import org.milyn.util.ClassUtil;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Bean utility methods.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public abstract class BeanUtils {

    private static Log logger = LogFactory.getLog(BeanUtils.class);

    /**
     * Create the bean setter method instance for this visitor.
     *
     * @param setterName The setter method name.
     * @param setterParamType
     * @return The bean setter method.
     */
    public static Method createSetterMethod(String setterName, Object bean, Class<?> setterParamType) {
        Method beanSetterMethod = ClassUtil.getSetterMethod(setterName, bean, setterParamType);

        // Try it as a list...
        if (beanSetterMethod == null && List.class.isAssignableFrom(setterParamType)) {
            String setterNamePlural = setterName + "s";

            // Try it as a List using the plural name...
            beanSetterMethod = ClassUtil.getSetterMethod(setterNamePlural, bean, setterParamType);
            if(beanSetterMethod == null) {
                // Try it as an array using the non-plural name...
            }
        }

        // Try it as a primitive...
        if(beanSetterMethod == null && Integer.class.isAssignableFrom(setterParamType)) {
            beanSetterMethod = ClassUtil.getSetterMethod(setterName, bean, Integer.TYPE);
        }
        if(beanSetterMethod == null && Long.class.isAssignableFrom(setterParamType)) {
            beanSetterMethod = ClassUtil.getSetterMethod(setterName, bean, Long.TYPE);
        }
        if(beanSetterMethod == null && Float.class.isAssignableFrom(setterParamType)) {
            beanSetterMethod = ClassUtil.getSetterMethod(setterName, bean, Float.TYPE);
        }
        if(beanSetterMethod == null && Double.class.isAssignableFrom(setterParamType)) {
            beanSetterMethod = ClassUtil.getSetterMethod(setterName, bean, Double.TYPE);
        }
        if(beanSetterMethod == null && Character.class.isAssignableFrom(setterParamType)) {
            beanSetterMethod = ClassUtil.getSetterMethod(setterName, bean, Character.TYPE);
        }
        if(beanSetterMethod == null && Short.class.isAssignableFrom(setterParamType)) {
            beanSetterMethod = ClassUtil.getSetterMethod(setterName, bean, Short.TYPE);
        }
        if(beanSetterMethod == null && Byte.class.isAssignableFrom(setterParamType)) {
            beanSetterMethod = ClassUtil.getSetterMethod(setterName, bean, Byte.TYPE);
        }
        if(beanSetterMethod == null && Boolean.class.isAssignableFrom(setterParamType)) {
            beanSetterMethod = ClassUtil.getSetterMethod(setterName, bean, Boolean.TYPE);
        }

        return beanSetterMethod;
    }

    /**
     * Get the bean instance on which this populator instance is to set data.
     *
     * @param execContext The execution context.
     * @return The bean instance.
     * @deprecated
     */
    @Deprecated
    public static Object getBean(String beanId, ExecutionContext execContext) {
        Object bean;


        // Get the bean instance from the request.  If there is non, it's a bad config!!
        bean =  execContext.getBeanContext().getBean(beanId);

        if (bean == null) {
            throw new SmooksConfigurationException("Bean instance [" + beanId + "] not available and bean runtime class not set on configuration.");
        }

        return bean;
    }

    /**
     * Convert the supplied List into an array of the specified array type.
     * @param list The List instance to be converted.
     * @param arrayClass The array type.
     * @return The array.
     */
    public static Object convertListToArray(List<?> list, Class<?> arrayClass) {
        AssertArgument.isNotNull(list, "list");
        AssertArgument.isNotNull(arrayClass, "arrayClass");

        int length = list.size();
        Object arrayObj = Array.newInstance(arrayClass, list.size());
        for(int i = 0; i < length; i++) {
            try {
                Array.set(arrayObj, i, list.get(i));
            } catch(ClassCastException e) {
                logger.error("Failed to cast type '" + list.get(i).getClass().getName() + "' to '" + arrayClass.getName() + "'.", e);
            }
        }

        return arrayObj;
    }
}
