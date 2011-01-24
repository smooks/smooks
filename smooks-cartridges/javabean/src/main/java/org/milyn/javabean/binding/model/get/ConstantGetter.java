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

package org.milyn.javabean.binding.model.get;

import org.milyn.assertion.AssertArgument;

/**
 * Constant value getter.
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class ConstantGetter implements Getter {

    private String value;

    public ConstantGetter(String value) {
        AssertArgument.isNotNullAndNotEmpty(value, "value");
        this.value = value;
    }

    public Object get(final Object contextObject) {
        return value;
    }
}
