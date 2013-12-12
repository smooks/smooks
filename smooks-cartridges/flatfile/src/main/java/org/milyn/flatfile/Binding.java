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
package org.milyn.flatfile;

import org.milyn.commons.assertion.AssertArgument;

/**
 * Binding configuration.
 * <p/>
 * For more complex bindings, use the main java binding framework.
 * 
 * @author <a href="mailto:tom.fennelly@jboss.com">tom.fennelly@jboss.com</a>
 */
public class Binding {

    private String beanId;
    private Class beanClass;
    private BindingType bindingType;
    private String keyField;

    public Binding(String beanId, Class beanClass, BindingType bindingType) {
        AssertArgument.isNotNullAndNotEmpty(beanId, "beanId");
        AssertArgument.isNotNull(beanClass, "beanClass");
        this.beanId = beanId;
        this.beanClass = beanClass;
        this.bindingType = bindingType;
    }

    public String getBeanId() {
        return beanId;
    }

    public Class getBeanClass() {
        return beanClass;
    }

    public BindingType getBindingType() {
        return bindingType;
    }

    public String getKeyField() {
        return keyField;
    }

    public Binding setKeyField(String keyField) {
        this.keyField = keyField;
        return this;
    }
}
