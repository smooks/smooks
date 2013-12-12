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
package org.milyn.fixedlength;

import org.milyn.commons.assertion.AssertArgument;

/**
 * Fixed Lenght Binding configuration.
 * <p/>
 * For more complex bindings, use the main java binding framwework.
 *
 * @author <a href="mailto:maurice.zeijen@smies.com">maurice.zeijen@smies.com</a>
 */
public class FixedLengthBinding {

    private String beanId;
    private Class<?> beanClass;
    private FixedLengthBindingType bindingType;
    private String keyField;

    public FixedLengthBinding(String beanId, Class<?> beanClass, FixedLengthBindingType bindingType) {
        AssertArgument.isNotNullAndNotEmpty(beanId, "beanId");
        AssertArgument.isNotNull(beanClass, "beanClass");
        this.beanId = beanId;
        this.beanClass = beanClass;
        this.bindingType = bindingType;
    }

    public String getBeanId() {
        return beanId;
    }

    public Class<?> getBeanClass() {
        return beanClass;
    }

    public FixedLengthBindingType getBindingType() {
        return bindingType;
    }

    public String getKeyField() {
        return keyField;
    }

    public FixedLengthBinding setKeyField(String keyField) {
        this.keyField = keyField;
        return this;
    }
}
