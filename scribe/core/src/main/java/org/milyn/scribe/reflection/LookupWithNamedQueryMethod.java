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
import java.util.Collection;
import java.util.Map;

/**
 * @author maurice
 */
public class LookupWithNamedQueryMethod {

    final Method method;

    final int queryIndex;
    final int parameterIndex;

    /**
     *
     */
    public LookupWithNamedQueryMethod(final Method method, final int queryIndex, final int parameterIndex) {
        AssertArgument.isNotNull(method, "method");

        if (queryIndex < 0) {
            throw new IllegalArgumentException("queryIndex can't be smaller then zero");
        }
        if (queryIndex > 1) {
            throw new IllegalArgumentException("queryIndex can't be bigger then one");
        }
        if (parameterIndex < 0) {
            throw new IllegalArgumentException("queryIndex can't be smaller then zero");
        }
        if (parameterIndex > 1) {
            throw new IllegalArgumentException("queryIndex can't be bigger then one");
        }
        if (queryIndex == parameterIndex) {
            throw new IllegalArgumentException("queryIndex and parameterIndex can't be the same");
        }

        this.method = method;
        this.queryIndex = queryIndex;
        this.parameterIndex = parameterIndex;
    }

    /* (non-Javadoc)
     * @see org.milyn.scribe.method.DAOMethod#invoke()
     */
    public Collection<?> invoke(final Object obj, final String query, final Map<String, ?> parameters) {
        final Object[] args = new Object[2];
        args[queryIndex] = query;
        args[parameterIndex] = parameters;


        try {
            return (Collection<?>) method.invoke(obj, args);
        } catch (final IllegalArgumentException e) {
            throw new RuntimeException("The method [" + method + "] of the class [" + method.getDeclaringClass().getName() + "] threw an exception, while invoking it with the object [" + obj + "].", e);
        } catch (final IllegalAccessException e) {
            throw new RuntimeException("The method [" + method + "] of the class [" + method.getDeclaringClass().getName() + "] threw an exception, while invoking it with the object [" + obj + "].", e);
        } catch (final InvocationTargetException e) {
            throw new RuntimeException("The method [" + method + "] of the class [" + method.getDeclaringClass().getName() + "] threw an exception, while invoking it with the object [" + obj + "].", e);
        }
    }

}
