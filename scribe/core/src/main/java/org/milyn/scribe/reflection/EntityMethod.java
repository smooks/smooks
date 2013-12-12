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
package org.milyn.scribe.reflection;

import org.milyn.commons.assertion.AssertArgument;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 */
public class EntityMethod {

    private final Method method;

    private final boolean returnsEntity;

    /**
     *
     */
    public EntityMethod(final Method method, final boolean returnsEntity) {
        AssertArgument.isNotNull(method, "method");

        this.method = method;
        this.returnsEntity = returnsEntity;
    }


    /* (non-Javadoc)
     * @see org.milyn.scribe.method.DAOMethod#invoke()
     */
    public Object invoke(final Object obj, final Object entity) {
        try {

            Object result = method.invoke(obj, entity);

            if (returnsEntity) {
                return result;
            } else {
                return null;
            }

        } catch (final IllegalArgumentException e) {
            throw new RuntimeException("The method [" + method + "] of the class [" + method.getDeclaringClass().getName() + "] threw an exception, while invoking it with the object [" + obj + "].", e);
        } catch (final IllegalAccessException e) {
            throw new RuntimeException("The method [" + method + "] of the class [" + method.getDeclaringClass().getName() + "] threw an exception, while invoking it with the object [" + obj + "].", e);
        } catch (final InvocationTargetException e) {
            throw new RuntimeException("The method [" + method + "] of the class [" + method.getDeclaringClass().getName() + "] threw an exception, while invoking it with the object [" + obj + "].", e);
        }
    }

    /* (non-Javadoc)
     * @see org.milyn.scribe.method.DAOMethod#invoke()
     */
    public Object invoke(final Object obj, final String id, final Object entity) {
        try {

            Object result = method.invoke(obj, id, entity);

            if (returnsEntity) {
                return result;
            } else {
                return null;
            }

        } catch (final IllegalArgumentException e) {
            throw new RuntimeException("The method [" + method + "] of the class [" + method.getDeclaringClass().getName() + "] threw an exception, while invoking it with the object [" + obj + "] and using the id '" + id + "'.", e);
        } catch (final IllegalAccessException e) {
            throw new RuntimeException("The method [" + method + "] of the class [" + method.getDeclaringClass().getName() + "] threw an exception, while invoking it with the object [" + obj + "] and using the id '" + id + "'.", e);
        } catch (final InvocationTargetException e) {
            throw new RuntimeException("The method [" + method + "] of the class [" + method.getDeclaringClass().getName() + "] threw an exception, while invoking it with the object [" + obj + "] and using the id '" + id + "'.", e);
        }
    }
}
