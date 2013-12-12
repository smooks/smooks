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

package org.milyn.cartridge.javabean.binding.model;

import org.milyn.cartridge.javabean.BeanInstanceCreator;
import org.milyn.cartridge.javabean.BeanRuntimeInfo;
import org.milyn.cdr.SmooksResourceConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Bean.
 * @author <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
public class Bean {

    private BeanInstanceCreator creator;
    private String beanId;
    private boolean cloneable = false;
    private Bean wiredInto;
    private List<Binding> bindings = new ArrayList<Binding>();

    public Bean(BeanInstanceCreator creator) {
        this.creator = creator;
        this.beanId = creator.getBeanId();
    }

    public SmooksResourceConfiguration getConfig() {
        return creator.getConfig();
    }

    public BeanInstanceCreator getCreator() {
        return creator;
    }

    public String getBeanId() {
        return beanId;
    }

    public Bean setCloneable(boolean cloneable) {
        this.cloneable = cloneable;
        return this;
    }

    public Bean getWiredInto() {
        return wiredInto;
    }

    public Bean wiredInto(Bean wiredInto) {
        if(cloneable) {
            throw new IllegalStateException("Illegal wiring of a bean that is cloneable.  Only non cloneable beans (e.g. non base beans) can be wired together.");
        }

        this.wiredInto = wiredInto;
        return this;
    }

    public List<Binding> getBindings() {
        return bindings;
    }

    public WiredBinding getWiredBinding(Bean wiredBean) {
        for(Binding binding : bindings) {
            if(binding instanceof WiredBinding) {
                WiredBinding wiredBinding = (WiredBinding) binding;
                if(wiredBinding.getWiredBean() == wiredBean) {
                    return wiredBinding;
                }
            }
        }

        return null;
    }

    protected Bean clone(Map<String, Bean> baseBeans, Bean parentBean) {
        if(!cloneable) {
            throw new IllegalStateException("Illegal call to clone a Bean instance that is not cloneable.");
        }

        Bean beanClone = new Bean(creator).wiredInto(parentBean);

        for(Binding binding : bindings) {
            Binding bindingClone = (Binding) binding.clone();

            bindingClone.setParentBean(beanClone);
            if(bindingClone instanceof WiredBinding) {
                WiredBinding wiredBinding = (WiredBinding) bindingClone;
                String wiredBeanId = wiredBinding.getWiredBeanId();
                Bean beanToBeWired = baseBeans.get(wiredBeanId);

                if(beanToBeWired != null) {
                    if(parentBean == null || (!parentBean.getBeanId().equals(wiredBeanId) && parentBean.getParentBean(wiredBeanId) == null)) {
                        wiredBinding.setWiredBean(beanToBeWired.clone(baseBeans, beanClone));
                        beanClone.bindings.add(wiredBinding);
                    }
                }
            } else {
                beanClone.bindings.add(bindingClone);
            }
        }

        return beanClone;
    }

    public Bean getParentBean(String beanId) {
        if(wiredInto != null) {
            if(wiredInto.getBeanId().equals(beanId)) {
                return wiredInto;
            } else {
                return wiredInto.getParentBean(beanId);
            }
        }
        return null;
    }

    public Class<?> getBeanClass() {
        return creator.getBeanRuntimeInfo().getPopulateType();
    }

    public boolean isCollection() {
        BeanRuntimeInfo.Classification classification = creator.getBeanRuntimeInfo().getClassification();
        return (classification == BeanRuntimeInfo.Classification.COLLECTION_COLLECTION || classification == BeanRuntimeInfo.Classification.ARRAY_COLLECTION);
    }

    @Override
    public String toString() {
        return beanId;
    }
}
