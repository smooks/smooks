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

package org.milyn.javabean.binding.model;

import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.javabean.BeanInstancePopulator;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public abstract class Binding {

    private BeanInstancePopulator populator;
    private Bean parentBean;
    private String property;

    public Binding(BeanInstancePopulator populator) {
        this.populator = populator;
        this.property = populator.getProperty();
    }

    public SmooksResourceConfiguration getConfig() {
        return populator.getConfig();
    }

    public BeanInstancePopulator getPopulator() {
        return populator;
    }

    public String getProperty() {
        return property;
    }

    public Bean getParentBean() {
        return parentBean;
    }

    public Binding setParentBean(Bean parentBean) {
        this.parentBean = parentBean;
        return this;
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException("Unexpected cloning exception.");
        }
    }

    @Override
    public String toString() {
        return property;
    }
}
