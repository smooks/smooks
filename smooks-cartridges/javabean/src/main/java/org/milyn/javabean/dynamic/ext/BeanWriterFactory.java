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

package org.milyn.javabean.dynamic.ext;

import org.milyn.cdr.SmooksConfigurationException;
import org.milyn.cdr.SmooksResourceConfiguration;
import org.milyn.cdr.annotation.AppContext;
import org.milyn.cdr.annotation.Config;
import org.milyn.cdr.annotation.ConfigParam;
import org.milyn.cdr.annotation.Configurator;
import org.milyn.container.ApplicationContext;
import org.milyn.delivery.ContentHandler;
import org.milyn.delivery.annotation.Initialize;
import org.milyn.javabean.dynamic.serialize.BeanWriter;
import org.milyn.javabean.ext.BeanConfigUtil;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * {@link BeanWriter} Factory.
 *
 * <a href="mailto:tom.fennelly@gmail.com">tom.fennelly@gmail.com</a>
 */
@SuppressWarnings({ "WeakerAccess", "unchecked" })
public class BeanWriterFactory implements ContentHandler {

    @ConfigParam
    private String beanId;
    @ConfigParam(name = "class")
    private Class<? extends BeanWriter> beanWriterClass;
    @ConfigParam(name = BeanConfigUtil.BEAN_CLASS_CONFIG)
    private Class<?> beanClass;
    @Config
    private SmooksResourceConfiguration config;
    @AppContext
    private ApplicationContext appContext;

    @Initialize
    public void createBeanWriter() {
        try {
            BeanWriter beanWriter = beanWriterClass.newInstance();

            Configurator.configure(beanWriter, config, appContext);
            getBeanWriters(beanClass, appContext).put(config.getSelectorNamespaceURI(), beanWriter);
        } catch (InstantiationException e) {
            throw new SmooksConfigurationException("Unable to create BeanWriter instance.", e);
        } catch (IllegalAccessException e) {
            throw new SmooksConfigurationException("Unable to create BeanWriter instance.", e);
        }
    }

    public static Map<String, BeanWriter> getBeanWriters(Class<?> beanClass, ApplicationContext appContext) {
        Map<Class<?>, Map<String, BeanWriter>> beanWriterMap = getBeanWriters(appContext);
        Map<String, BeanWriter> beanWriters = beanWriterMap.get(beanClass);

        if(beanWriters == null) {
            beanWriters = new LinkedHashMap<String, BeanWriter>();
            beanWriterMap.put(beanClass, beanWriters);
        }

        return beanWriters;
    }

    public static Map<Class<?>, Map<String, BeanWriter>> getBeanWriters(ApplicationContext appContext) {
        Map<Class<?>, Map<String, BeanWriter>> beanWriters = (Map<Class<?>, Map<String, BeanWriter>>) appContext.getAttribute(BeanWriter.class);

        if(beanWriters == null) {
            beanWriters = new HashMap<Class<?>, Map<String, BeanWriter>>();
            appContext.setAttribute(BeanWriter.class, beanWriters);
        }

        return beanWriters;
    }
}
