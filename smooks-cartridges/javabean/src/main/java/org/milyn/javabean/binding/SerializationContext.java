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

package org.milyn.javabean.binding;

import org.milyn.commons.assertion.AssertArgument;
import org.milyn.javabean.binding.model.get.Getter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class SerializationContext {

    public static final String ROOT_OBJ = SerializationContext.class.getName() + "#ROOT_OBJ";

    private Object rootObject;
    private Map<String, Object> contextObjects = new LinkedHashMap<String, Object>();
    private int currentDepth;

    public SerializationContext(Object rootObject, String rootObjectBeanId) {
        AssertArgument.isNotNull(rootObject, "rootObject");
        this.rootObject = rootObject;
        addObject(rootObjectBeanId, rootObject);
    }

    public int getCurrentDepth() {
        return currentDepth;
    }

    public void incDepth() {
        currentDepth++;
    }

    public void decDepth() {
        currentDepth--;
    }

    public void addObject(String name, Object contextObject) {
        contextObjects.put(name, contextObject);
    }

    public Object removeObject(String name) {
        return contextObjects.remove(name);
    }

    public Object getValue(Getter getter) {
        return getter.get(rootObject);
    }

    public Object getValue(String contextObjectName, Getter getter) {
        if(ROOT_OBJ.equals(contextObjectName)) {
            return getter.get(rootObject);
        }

        Object contextObject = contextObjects.get(contextObjectName);

        if(contextObject == null) {
            throw new IllegalStateException("Unknown context object name '" + contextObjectName + "'.");
        }

        return getter.get(contextObject);
    }
}
