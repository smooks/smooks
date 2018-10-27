/*
 * Milyn - Copyright (C) 2006 - 2010
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

package org.milyn.edi.test;

import org.milyn.edisax.model.internal.Delimiters;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * This utility class manufactures test messages by reflectively constructing the messages.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
@SuppressWarnings("unchecked")
public class MessageBuilder {

    private String packageScope;
    private String delimiterForEscaping;
    private Delimiters delimiters;

    public MessageBuilder(String packageScope, String delimiterForEscaping, Delimiters delimiters) {
        this.packageScope = packageScope;
        this.delimiterForEscaping = delimiterForEscaping;
        this.delimiters = delimiters;
    }

    public Delimiters getDelimiters() {
        return delimiters;
    }

    public <T> T buildMessage(Class<T> messageType) {
        try {
            return buildObject(messageType, null);
        } catch (Exception e) {
            throw new IllegalArgumentException("Unable to construct an instance of '" + messageType.getName() + "'", e);
        }
    }

    private <T> T buildObject(Class<T> objectType, String name) throws IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {

        if (objectType == Delimiters.class) {
            return objectType.cast(delimiters);
        }

        //
        // Assumptions...
        // 1. Either a Number, String or a type inside the packageScope of the constructing
        //    packageScope package
        // 2. If a primitive Number... it's an int
        //

        if (String.class.isAssignableFrom(objectType)) {
            // Return the object name as the
            // string value... with a delimiter in it to test
            // escaping of delims...
            return objectType.cast(name + delimiterForEscaping + name);
        } else if (Number.class.isAssignableFrom(objectType)) {
            return objectType.getConstructor(String.class).newInstance("1.1");
        } else if (int.class.isAssignableFrom(objectType)) {
            return (T) new Integer(1);
        } else if (objectType == Object.class) {
            // don't construct raw Object types... leave them and just return null...
            return null;
        }

        // Make sure the object is within the package packageScope...
        if (!objectType.getPackage().getName().startsWith(packageScope)) {
            throw new InstantiationException("Cannot create instance of type '" + objectType.getName() + "'.  Not inside the scope of package '" + packageScope + "'");
        }

        T messageInstance = objectType.newInstance();

        // populate all the fields...
        Method[] methods = objectType.getMethods();
        for (Method method : methods) {
            if (method.getName().startsWith("set") && method.getParameterTypes().length == 1) {
                Class<?> propertyType = method.getParameterTypes()[0];
                Object propertyInstance = null;

                if (List.class.isAssignableFrom(propertyType)) {
                    Type genericType = method.getGenericParameterTypes()[0];

                    if (genericType instanceof ParameterizedType) {
                        List list = new ArrayList();
                        ParameterizedType genericTypeClass = (ParameterizedType) genericType;

                        list.add(buildObject((Class<Object>) genericTypeClass.getActualTypeArguments()[0], method.getName().substring(3)));
                        propertyInstance = list;
                    }
                } else {
                    propertyInstance = buildObject(propertyType, method.getName().substring(3));
                }

                if (propertyInstance != null) {
                    method.invoke(messageInstance, propertyInstance);
                }
            }
        }

        return messageInstance;
    }
}
