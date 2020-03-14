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
package org.smooks.javabean.gen.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class ClassConfig {

    private Class beanClass;
    private String beanId;
    private List<BindingConfig> bindings = new ArrayList<BindingConfig>();
    private boolean isArray;

    public ClassConfig(Class beanClass) {
        this.beanClass = beanClass;
    }

    public ClassConfig(Class beanClass, String beanId) {
        this.beanClass = beanClass;
        this.beanId = beanId;
    }

    public Class getBeanClass() {
        return beanClass;
    }

    public List<BindingConfig> getBindings() {
        return bindings;
    }

    public void setBindings(List<BindingConfig> bindings) {
        this.bindings = bindings;
    }

    public boolean isArray() {
        return isArray;
    }

    public void setArray(boolean array) {
        isArray = array;
    }

    public String getBeanId() {
        if(beanId != null) {
            return beanId;
        } else {
            StringBuilder beanId = new StringBuilder(beanClass.getSimpleName());
            beanId.setCharAt(0, Character.toLowerCase(beanId.charAt(0)));
            return beanId.toString();
        }
    }
}
