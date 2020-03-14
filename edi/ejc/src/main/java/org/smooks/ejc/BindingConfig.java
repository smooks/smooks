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
package org.smooks.ejc;

import org.smooks.edisax.model.internal.MappingNode;
import org.smooks.javabean.pojogen.JClass;
import org.smooks.javabean.pojogen.JNamedType;

import java.util.ArrayList;
import java.util.List;

/**
 * BindingConfig
 *
 * @author bardl
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class BindingConfig {

    private String beanId;
    private String createOnElement;
    private JClass beanClass;
    private Class<?> runtimeClass;
    private MappingNode mappingNode;
    private BindingConfig parent;
    private JNamedType propertyOnParent;
    private List<ValueNodeInfo> valueBindings = new ArrayList<ValueNodeInfo>();
    private List<BindingConfig> wireBindings = new ArrayList<BindingConfig>();
    private WriteMethod writeMethod = null;

    public BindingConfig(String beanId, String createOnElement, JClass beanClass, BindingConfig parent, JNamedType propertyOnParent) {
        this.beanId = beanId;
        this.createOnElement = createOnElement;
        this.beanClass = beanClass;
        this.parent = parent;
        this.propertyOnParent = propertyOnParent;
    }

    public BindingConfig(String beanId, String createOnElement, Class<?> runtimeClass, BindingConfig parent, JNamedType propertyOnParent) {
        this.beanId = beanId;
        this.createOnElement = createOnElement;
        this.runtimeClass = runtimeClass;
        this.parent = parent;
        this.propertyOnParent = propertyOnParent;
    }

    public String getBeanId() {
        return beanId;
    }

    public String getCreateOnElement() {
        return createOnElement;
    }

    public JClass getBeanClass() {
        return beanClass;
    }

    public void setMappingNode(MappingNode mappingNode) {
        this.mappingNode = mappingNode;
    }

    public MappingNode getMappingNode() {
        return mappingNode;
    }

    public void setValueBindings(List<ValueNodeInfo> valueBindings) {
        this.valueBindings = valueBindings;
    }

    public void setBeanClass(JClass beanClass) {
        this.beanClass = beanClass;
    }

    public void setWireBindings(List<BindingConfig> wireBindings) {
        this.wireBindings = wireBindings;
    }

    public Class<?> getRuntimeClass() {
        if(beanClass != null) {
            return beanClass.getSkeletonClass();
        } else {
            return runtimeClass;
        }
    }

    public BindingConfig getParent() {
        return parent;
    }

    public JNamedType getPropertyOnParent() {
        return propertyOnParent;
    }

    public void setPropertyOnParent(JNamedType propertyOnParent) {
        this.propertyOnParent = propertyOnParent;
    }

    public List<ValueNodeInfo> getValueBindings() {
        return valueBindings;
    }

    public List<BindingConfig> getWireBindings() {
        return wireBindings;
    }

    public WriteMethod getWriteMethod() {
        return writeMethod;
    }

    public void setWriteMethod(WriteMethod writeMethod) {
        this.writeMethod = writeMethod;
    }
}