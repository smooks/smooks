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

package org.smooks.javabean.binding.model;

import org.smooks.javabean.BeanInstancePopulator;

/**
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class WiredBinding extends Binding {

    private String wiredBeanId;
    private Bean wiredBean;

    public WiredBinding(BeanInstancePopulator populator) {
        super(populator);
        this.wiredBeanId = populator.getWireBeanId();
    }

    public String getWiredBeanId() {
        return wiredBeanId;
    }

    public void setWiredBean(Bean wiredBean) {
        this.wiredBean = wiredBean;
    }

    public Bean getWiredBean() {
        return wiredBean;
    }

    @Override
    public Object clone() {
        return new WiredBinding(getPopulator());
    }
}
