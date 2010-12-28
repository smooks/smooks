/*
 * Milyn - Copyright (C) 2006 - 2010
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License (version 2.1) as published by the Free Software
 *  Foundation.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 *  See the GNU Lesser General Public License for more details:
 *  http://www.gnu.org/licenses/lgpl.txt
 */

package org.smooks.model.javabean;

import org.milyn.javabean.dynamic.serialize.DefaultNamespace;

import java.util.List;

/**
 * Bean configuration.
 * <p/>
 * Corresponds to the top level &lt;jb:bean&gt; element. 
 *
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
@DefaultNamespace(uri = "http://www.milyn.org/xsd/smooks/javabean-1.3.xsd", prefix = "jb13")
public class Bean {

    private String beanId;
    private String beanClass;
    private String createOnElement;
    private String createOnElementNS;
    private List<Value> valueBindings;
    private List<Wiring> wireBindings;
    private List<Expression> expressionBindings;

    public String getBeanId() {
        return beanId;
    }

    public void setBeanId(String beanId) {
        this.beanId = beanId;
    }

    public String getBeanClass() {
        return beanClass;
    }

    public void setBeanClass(String beanClass) {
        this.beanClass = beanClass;
    }

    public String getCreateOnElement() {
        return createOnElement;
    }

    public void setCreateOnElement(String createOnElement) {
        this.createOnElement = createOnElement;
    }

    public String getCreateOnElementNS() {
        return createOnElementNS;
    }

    public void setCreateOnElementNS(String createOnElementNS) {
        this.createOnElementNS = createOnElementNS;
    }

    public List<Value> getValueBindings() {
        return valueBindings;
    }

    public void setValueBindings(List<Value> valueBindings) {
        this.valueBindings = valueBindings;
    }

    public List<Wiring> getWireBindings() {
        return wireBindings;
    }

    public void setWireBindings(List<Wiring> wireBindings) {
        this.wireBindings = wireBindings;
    }

    public List<Expression> getExpressionBindings() {
        return expressionBindings;
    }

    public void setExpressionBindings(List<Expression> expressionBindings) {
        this.expressionBindings = expressionBindings;
    }
}
