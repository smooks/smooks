/*
 * Milyn - Copyright (C) 2006 - 2011
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

package org.milyn.cartridge.javabean.binding.model.get;

import org.milyn.cartridge.javabean.binding.BeanSerializationException;
import org.milyn.commons.util.ClassUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Bean getter method.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class BeanGetter<T extends Object> implements Getter<T> {

    private Method getterMethod;

    public BeanGetter(Class<?> beanClass, String property) {
        getterMethod = ClassUtil.getGetterMethodByProperty(property, beanClass, null);
        if(getterMethod == null) {
            throw new IllegalStateException("Failed to get getter method for property '" + property + "' on bean type '" + beanClass.getName() + "'.");
        }
    }

    public Object get(final T contextObject) throws BeanSerializationException {
        try {
            return getterMethod.invoke(contextObject);
        } catch (IllegalArgumentException e) {
            throw new BeanSerializationException("Error invoking bean getter method '" + getterMethod.getName() + "' on bean type '" + contextObject.getClass().getName() + "'.", e);
        } catch (IllegalAccessException e) {
            throw new BeanSerializationException("Error invoking bean getter method '" + getterMethod.getName() + "' on bean type '" + contextObject.getClass().getName() + "'.", e);
        } catch (InvocationTargetException e) {
            throw new BeanSerializationException("Error invoking bean getter method '" + getterMethod.getName() + "' on bean type '" + contextObject.getClass().getName() + "'.", e.getCause());
        }
    }
}
