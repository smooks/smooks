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

package org.smooks.javabean.binding.model.get;

import org.smooks.assertion.AssertArgument;
import org.smooks.javabean.binding.BeanSerializationException;
import org.smooks.javabean.binding.SerializationContext;
import org.smooks.javabean.binding.model.Bean;
import org.smooks.javabean.binding.model.Binding;
import org.smooks.javabean.binding.model.DataBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Getter Graph.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
@SuppressWarnings("unchecked")
public class GetterGraph<T> implements Getter<T> {

    private String contextObjectName = SerializationContext.ROOT_OBJ;
    private List<Getter> graph = new ArrayList<Getter>();

    public Object get(final T contextObject) throws BeanSerializationException {
        AssertArgument.isNotNull(contextObject, "contextObject");

        Object value = contextObject;

        for (final Getter getter : graph)
        {
            value = getter.get(value);
            if (value == null)
            {
                return null;
            }
        }

        return value;
    }

    private GetterGraph add(Getter getter) {
        // Insert the getter at the start of the graph list...
        graph.add(0, getter);
        return this;
    }

    public void add(DataBinding binding) {
        add(toGetter(binding.getParentBean(), binding));
    }

    public GetterGraph add(Bean bean, String property) {
        AssertArgument.isNotNull(bean, "bean");
        AssertArgument.isNotNullAndNotEmpty(property, "property");

        Getter getter = null;
        for(Binding binding : bean.getBindings()) {
            if(property.equals(binding.getProperty())) {
                getter = toGetter(bean, binding);
                break;
            }
        }

        if(getter == null) {
            throw new IllegalStateException("Failed to create Getter instance for property '" + property + "' on bean type '" + bean.getBeanClass().getName() + "'.");
        }
        add(getter);

        return this;
    }

    private Getter toGetter(Bean bean, Binding binding) {
        if(Map.class.isAssignableFrom(bean.getBeanClass())) {
            return new MapGetter(binding.getProperty());
        } else {
            return new BeanGetter(bean.getBeanClass(), binding.getProperty());
        }
    }

    public String getContextObjectName() {
        return contextObjectName;
    }

    public void setContextObjectName(String contextObjectName) {
        this.contextObjectName = contextObjectName;
    }
}
