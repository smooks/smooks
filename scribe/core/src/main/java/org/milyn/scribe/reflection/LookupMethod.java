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

import org.milyn.commons.annotation.AnnotatedMethod;
import org.milyn.commons.annotation.AnnotationManager;
import org.milyn.commons.assertion.AssertArgument;
import org.milyn.scribe.annotation.Param;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author maurice
 *         <p/>
 *         TODO: implement type checking for primative types...
 */
public class LookupMethod {

    final Method method;

    Map<String, Integer> parameterPositions;

    private boolean namedParameters = false;

    /**
     *
     */
    public LookupMethod(final Method method) {
        AssertArgument.isNotNull(method, "method");

        this.method = method;

        analyzeParameters();
    }

    /**
     *
     */
    private void analyzeParameters() {

        final AnnotatedMethod aMethod = AnnotationManager.getAnnotatedClass(method.getDeclaringClass()).getAnnotatedMethod(method);

        final Annotation[][] parameterAnnotations = aMethod.getParameterAnnotations();

        int parameterSize = aMethod.getMethod().getParameterTypes().length;

        for (int i = 0; i < parameterAnnotations.length; i++) {


            for (final Annotation annotation : parameterAnnotations[i]) {

                if (Param.class.equals(annotation.annotationType())) {
                    namedParameters = true;

                    final Param param = (Param) annotation;

                    final String name = param.value().trim();

                    if (name.length() == 0) {
                        throw new RuntimeException("Illegal empty parameter value encounterd on parameter " + i
                                + " of method '" + method + "' from class '" + method.getDeclaringClass().getName() + "'.");
                    }

                    if (parameterPositions == null) {
                        parameterPositions = new HashMap<String, Integer>();
                    }

                    parameterPositions.put(param.value(), i);

                    break;
                }

            }

        }
        if (namedParameters && parameterPositions.size() != parameterSize) {
            throw new RuntimeException("Not all the parameters of the method '" + method.getDeclaringClass().getName() + "." + method + "' are annotated with the '" + Param.class.getName() + "' annotation."
                    + " All the parameters of the method need to be have the '" + Param.class.getName() + "' annotation when using the annotation on a method.");
        }

    }


    /* (non-Javadoc)
     * @see org.milyn.scribe.method.DAOMethod#invoke()
     *
     *
     */
    public Object invoke(final Object obj, final Map<String, ?> parameters) {

        if (!namedParameters) {
            throw new IllegalStateException("This Lookup Method doesn't have name parameters and there for can't be invoked with a parameter Map.");
        }

        Object[] args = new Object[parameterPositions.size()];

        //TODO: evaluate a faster way to map the arguments but which is equally safe
        for (final Entry<String, ?> parameterEntry : parameters.entrySet()) {
            String parameterName = parameterEntry.getKey();

            final Integer position = parameterPositions.get(parameterName);

            if (position == null) {
                throw new RuntimeException("Parameter with the name " + parameterName + " isn't found on the method '" + method + "' of the class '" + method.getDeclaringClass().getName() + "'");
            }

            args[position] = parameterEntry.getValue();
        }

        return invoke(obj, args);

    }

    /**
     * @param obj
     * @param args
     * @return
     */
    public Object invoke(final Object obj, Object... args) {
        try {
            return method.invoke(obj, args);
        } catch (final IllegalArgumentException e) {
            throw new RuntimeException("The method '" + method + "' of the class '" + method.getDeclaringClass().getName() + "' threw an exception, while invoking it with the object '" + obj + "'.", e);
        } catch (final IllegalAccessException e) {
            throw new RuntimeException("The method '" + method + "' of the class '" + method.getDeclaringClass().getName() + "' threw an exception, while invoking it with the object '" + obj + "'.", e);
        } catch (final InvocationTargetException e) {
            throw new RuntimeException("The method '" + method + "' of the class '" + method.getDeclaringClass().getName() + "' threw an exception, while invoking it with the object '" + obj + "'.", e);
        }
    }
}
